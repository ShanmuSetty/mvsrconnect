from flask import Flask, request, jsonify
from detoxify import Detoxify
import opennsfw2
import requests
import cv2
import os
import re
from rapidfuzz import fuzz
from uuid import uuid4
import unicodedata

app = Flask(__name__)

# ---------------- MODELS ---------------- #

text_model = Detoxify("multilingual")
nsfw_model = opennsfw2.make_open_nsfw_model()

# ---------------- CONFIG ---------------- #

TOXIC_THRESHOLD = 0.6
NSFW_THRESHOLD = 0.6

# ---------------- BAD WORDS ---------------- #

BAD_WORDS = [
    "madarchod", "bhenchod", "pandhi", "lanja", "lanjadhana",
    "kothi guddhoda", "mental pukoda", "mental lanja", "mentaldhana",
    "louda pukoda", "louda", "loveda", "love da", "lambdikodaka",
    "ne erripukulo sulli", "boku munda", "peethi guddha", "gudda musko",
    "guddha musukoni kucho", "lathkor lanja", "modda guduvu", "madda guduv",
    "lamja", "errihook", "bot guddhoda", "baddha", "lingam", "ne puku",
    "madichi guddhalo petko", "ne pellani denga", "ne notlo na sulli",
    "mukkulo sulli", "ne kallalo na sulli", "ne akka ni denga",
    "ne akka puku lo na sulli",
]

SINGLE_BAD_WORDS = [w for w in BAD_WORDS if " " not in w]
PHRASE_BAD_WORDS  = [w for w in BAD_WORDS if " " in w]

BAD_EMOJIS = {"🖕", "💩", "🤡", "🍌", "🍆", "🍑", "🍒", "💦"}

# ---------------- TEXT UTILITIES ---------------- #

LEET_MAP = {
    "0": "o", "1": "i", "3": "e", "4": "a",
    "@": "a", "$": "s", "5": "s", "7": "t", "!": "i",
}

VOWELS = set("aeiou")

def normalize_unicode(text):
    return unicodedata.normalize("NFKD", text)

def normalize_leetspeak(text):
    for k, v in LEET_MAP.items():
        text = text.replace(k, v)
    return text

def collapse_repeated_chars(text):
    return re.sub(r'(.)\1{2,}', r'\1', text)

def remove_spaced_chars(text):
    words = text.split()
    if len(words) > 2 and all(len(w) == 1 for w in words):
        return "".join(words)
    return text

def compress_word(word):
    return "".join(c for c in word if c not in VOWELS)

def normalize_text(text):
    text = normalize_unicode(text)
    text = text.lower()
    text = remove_spaced_chars(text)
    text = normalize_leetspeak(text)
    text = re.sub(r'[^a-z0-9\s]', '', text)
    text = collapse_repeated_chars(text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text

_COMPRESSED_SINGLE = [(w, compress_word(w)) for w in SINGLE_BAD_WORDS]
MIN_COMPRESSED_LEN = 3

def _tokens(text):
    return text.split()

def contains_bad_word(text):
    tokens = _tokens(text)
    compressed_tokens = [compress_word(t) for t in tokens]

    for ct in compressed_tokens:
        if len(ct) < MIN_COMPRESSED_LEN:
            continue
        for _orig, cb in _COMPRESSED_SINGLE:
            if ct == cb:
                return True

    for phrase in PHRASE_BAD_WORDS:
        phrase_tokens = phrase.split()
        n = len(phrase_tokens)
        for i in range(len(tokens) - n + 1):
            window = " ".join(tokens[i:i + n])
            if window == phrase:
                return True
            compressed_window = " ".join(compressed_tokens[i:i + n])
            compressed_phrase  = " ".join(compress_word(pt) for pt in phrase_tokens)
            if (
                len(compressed_phrase) >= MIN_COMPRESSED_LEN
                and compressed_window == compressed_phrase
            ):
                return True

    return False

def fuzzy_bad_word(text):
    tokens = _tokens(text)
    for token in tokens:
        if len(token) < 5:
            continue
        for bad in SINGLE_BAD_WORDS:
            if len(bad) < 5:
                continue
            if fuzz.ratio(bad, token) > 85:
                return True

    for phrase in PHRASE_BAD_WORDS:
        if len(phrase) < 8:
            continue
        if fuzz.partial_ratio(phrase, text) > 88:
            return True

    return False

def emoji_check(text):
    return any(e in text for e in BAD_EMOJIS)

# ---------------- FILE UTILITIES ---------------- #

def download_temp_file(url, ext):
    response = requests.get(url, timeout=30)
    response.raise_for_status()
    filename = f"temp_{uuid4()}.{ext}"
    with open(filename, "wb") as f:
        f.write(response.content)
    return filename

# ---------------- TEXT MODERATION ---------------- #

@app.route("/check_text", methods=["POST"])
def check_text():
    data = request.json
    text   = data.get("text", "")
    parent = data.get("context", "")

    raw_combined = (parent + " " + text).strip()
    combined     = normalize_text(raw_combined)

    model_input = combined[:500] if len(combined) > 500 else combined
    score = float(text_model.predict(model_input)["toxicity"])

    dictionary_flag = contains_bad_word(combined)
    fuzzy_flag      = fuzzy_bad_word(combined)
    emoji_flag      = emoji_check(raw_combined)

    toxic = score > TOXIC_THRESHOLD or dictionary_flag or fuzzy_flag or emoji_flag

    return jsonify({
        "toxic":            bool(toxic),
        "score":            score,
        "dictionary_match": dictionary_flag,
        "fuzzy_match":      fuzzy_flag,
        "emoji_match":      emoji_flag,
    })

# ---------------- IMAGE MODERATION ---------------- #

@app.route("/check_image", methods=["POST"])
def check_image():
    url      = request.json["url"]
    filename = download_temp_file(url, "jpg")
    try:
        score = float(opennsfw2.predict_image(filename))
    finally:
        os.remove(filename)

    return jsonify({"unsafe": score > NSFW_THRESHOLD, "score": score})

# ---------------- VIDEO MODERATION ---------------- #

@app.route("/check_video", methods=["POST"])
def check_video():
    url      = request.json["url"]
    filename = download_temp_file(url, "mp4")
    unsafe   = False
    max_score = 0.0
    frame_file = f"frame_{uuid4()}.jpg"

    try:
        cap         = cv2.VideoCapture(filename)
        frame_index = 0

        while True:
            ret, frame = cap.read()
            if not ret:
                break
            frame_index += 1
            if frame_index % 20 != 0:
                continue
            cv2.imwrite(frame_file, frame)
            score     = float(opennsfw2.predict_image(frame_file))
            max_score = max(max_score, score)
            if os.path.exists(frame_file):
                os.remove(frame_file)
            if score > NSFW_THRESHOLD:
                unsafe = True
                break

        cap.release()
    finally:
        if os.path.exists(filename):
            os.remove(filename)
        if os.path.exists(frame_file):
            os.remove(frame_file)

    return jsonify({"unsafe": unsafe, "score": max_score})

# ---------------- SERVER ---------------- #

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)