<div align="center">

<br/>

```
███╗   ███╗██╗   ██╗███████╗██████╗      ██████╗ ██████╗ ███╗   ██╗███╗   ██╗███████╗ ██████╗████████╗
████╗ ████║██║   ██║██╔════╝██╔══██╗    ██╔════╝██╔═══██╗████╗  ██║████╗  ██║██╔════╝██╔════╝╚══██╔══╝
██╔████╔██║██║   ██║███████╗██████╔╝    ██║     ██║   ██║██╔██╗ ██║██╔██╗ ██║█████╗  ██║        ██║   
██║╚██╔╝██║╚██╗ ██╔╝╚════██║██╔══██╗    ██║     ██║   ██║██║╚██╗██║██║╚██╗██║██╔══╝  ██║        ██║   
██║ ╚═╝ ██║ ╚████╔╝ ███████║██║  ██║    ╚██████╗╚██████╔╝██║ ╚████║██║ ╚████║███████╗╚██████╗   ██║   
╚═╝     ╚═╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝     ╚═════╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═══╝╚══════╝ ╚═════╝   ╚═╝   
```

**The exclusive campus social platform for MVSR Engineering College students.**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-336791?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![Live](https://img.shields.io/badge/Live-mvsrconnect.onrender.com-brightgreen?style=flat-square&logo=render&logoColor=white)](https://mvsrconnect.onrender.com/)
[![License](https://img.shields.io/badge/License-Educational-lightgrey?style=flat-square)](#license)

<br/>

*Post · Comment · Vote · Join Clubs · Moderate · Lost & Found*  
*Exclusively for `@mvsrec.edu.in` accounts*

</div>

---

## 📌 What is MVSR Connect?

MVSR Connect is a **Reddit-style campus forum** built exclusively for students of MVSR Engineering College. It's a single platform where you can share posts, join clubs, vote on content, have threaded discussions, report inappropriate material, and find lost items — all locked behind Google OAuth so only real `@mvsrec.edu.in` accounts can get in.

Think of it as a private, AI-moderated version of a discussion forum, but built specifically for your campus.

---

## ✨ Features

### Core
- 🔐 **Google OAuth Login** — Restricted to `@mvsrec.edu.in` emails only. No external signups.
- 📰 **Feed** — Create posts with text, images, or videos. Sort by **Hot**, **Top**, or **Recent**.
- 🗳️ **Reddit-style Voting** — Upvote and downvote posts. Hot ranking uses a time-decay algorithm.
- 💬 **Threaded Comments** — Nested replies with full toxicity filtering before publishing.
- 🏷️ **Tags** — Tag posts and filter the feed by tag (Announcement, Event, Question, etc.).
- 🔍 **Global Search** — Search across posts, users, clubs, and tags simultaneously.
- 📎 **Media Uploads** — Images and videos via Cloudinary. Images are auto-compressed client-side before upload.

### Clubs
- 🎭 Join clubs (Drama, Music, Photo/Videography, Book, Dance, and more)
- 📬 Submit join requests; moderators approve or reject
- 📋 Post within clubs — only members can post inside a club
- 🛡️ Apply to become a club moderator via the appeal system

### Lost & Found
- 🔎 **Report Lost Items** — Post what you lost with location, date, category, and a photo
- 🎉 **Report Found Items** — Post what you found so the owner can claim it
- 🙋 **Claim System** — Finders respond to lost posts; owners respond to found posts with proof of ownership
- 🔒 **Private Responses** — Only the original poster can see who responded, protecting user privacy
- ✓ **Resolve Cases** — Mark your post resolved once the item is returned
- 🔍 **Search** — Search across item names, descriptions, locations, and categories
- 🤖 **Moderation** — All text and images run through the same AI toxicity and safety pipeline

### Events
- 📅 **Create Events** — Moderators/admins create events with title, venue, date, capacity, and optional banner
- 💰 **Free & Paid Events** — Free events issue QR tickets instantly; paid events use a UPI payment + UTR verification flow
- 🎟️ **QR Tickets** — Confirmed enrollments get a unique QR code ticket generated server-side
- 📷 **Live QR Scanner** — Camera-based scanner page for moderators to scan and check in attendees at the venue
- 🔒 **UPI Privacy** — UPI IDs are stored server-side only and never exposed in any API response
- 📊 **Enrollment Manager** — Organizers can view all enrollments, approve/reject payment submissions, and track check-ins
- 🏷️ **Club-linked Events** — Events can optionally be tied to a specific club

### Moderation & Admin
- 🤖 **AI Toxicity Detection** — Every comment and post is checked by an external Flask microservice before going live
- 🖼️ **Image & Video Safety** — Unsafe media is rejected and automatically deleted from Cloudinary
- 📢 **Report System** — Users can report posts; admins resolve them
- 🧑‍⚖️ **Mod Panel** — Moderators manage their club's members, posts, comments, and join requests
- 👑 **Admin Panel** — Admins approve/reject moderator appeals and resolve all reports

### Notifications
- **In-App Notifications** — Real-time notification bell with unread badge and dropdown history
- 🌐 **Web Push Notifications** — Native device notifications (even when tab is closed)
- ⚡ **Event-Based Alerts**:
    - New comments on your posts
    - Replies to your comments
    - Club join request approvals/rejections
    - Moderator appeal updates
    - Lost & Found responses
    - Event enrollment confirmations
    - New posts in your clubs
- 📦 **Persistent History** — All notifications stored in DB and visible in dropdown
- 🔕 **Smart Permission Handling** — Shows “Enable notifications” prompt only when needed

### Personal
- 📊 **Dashboard** — View your posts, comments, clubs, upvotes received, and liked posts
- ✏️ **Profile Editor** — Update your display name, bio, and avatar

---

## 🛠️ Tech Stack

| Layer          | Technology |
|----------------|---|
| **Backend**    | Spring Boot 4.0.3 (Java 17) |
| **Database**   | PostgreSQL |
| **ORM**        | Spring Data JPA / Hibernate |
| **Auth**       | Spring Security + Google OAuth2 |
| **Media**      | Cloudinary (images + videos) |
| **Moderation** | External Flask microservice |
| **API Docs**   | SpringDoc OpenAPI (Swagger UI) |
| **Frontend**   | Vanilla HTML / CSS / JavaScript (+ jsQR for scanning, qrcode.js for ticket generation)|
| **Container**  | Docker |
| **Email**      | Spring Mail + Gmail SMTP (async notifications) |
| **Notifications** | Web Push API (VAPID) + Service Workers |

---

## 🗂️ Project Structure
```
src/
├── main/
│   ├── java/com/mvsr/mvsrconnect/
│   │   ├── config/              # SecurityConfig, OAuth2LoginSuccessHandler, CloudinaryConfig
│   │   ├── controller/          # REST controllers (Posts, Comments, Clubs, Votes, Search, LostFound, Events…)
│   │   ├── dto/                 # Data transfer objects (LostFoundItemDTO…)
│   │   ├── model/               # JPA entities (User, Post, Club, Comment, Vote, Report, LostFoundItem, Event, EventEnrollment…)
│   │   ├── repository/          # Spring Data repositories
│   │   └── service/             # Business logic (ModerationService, ClubService, ReportService, EventService…)
│   └── resources/
│       ├── application.properties
│       └── static/              # Frontend pages
│           ├── index.html       # Main feed
│           ├── events.html      # Events board — browse, enroll, pay, manage
│           ├── scanner.html     # QR check-in scanner (mod/admin only)
│           ├── lostandfound.html # Lost & Found board
│           ├── dashboard.html   # Personal dashboard
│           ├── mod.html         # Moderator panel
│           ├── admin.html       # Admin panel
│           ├── search.html      # Global search
│           ├── post.html        # Specific Post View
│           └── sw.js            # Service Worker for push notifications
└── test/
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17
- PostgreSQL (running locally or remotely)
- Google Cloud project with OAuth2 credentials
- Cloudinary account
- *(Optional)* Flask moderation microservice running on port `5001`

### Environment Variables

Set these in your deployment platform (e.g., Render):

```bash
# Database
DB_URL=your_postgresql_connection_url
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret

# Moderation (Flask service)
MODERATION_URL=your_moderation_service_url
MODERATION_ENABLED=true

# HuggingFace (used by moderation service if applicable)
HUGGINGFACE_API_KEY=your_huggingface_api_key

# Email (Gmail SMTP)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password

# Web Push Notifications (VAPID)
VAPID_PUBLIC_KEY=your_vapid_public_key
VAPID_PRIVATE_KEY=your_vapid_private_key
VAPID_SUBJECT=mailto:your_email@gmail.com
```
```md id="env-notes"
> ⚠️ Notes:
> - `MAIL_PASSWORD` must be a Gmail **App Password**, not your actual password
> - `VAPID_SUBJECT` is just an identifier (use any valid email with `mailto:` prefix)
> - All variables must be set in your hosting platform (e.g., Render) — do not hardcode secrets
```
### Run Locally
```bash
./mvnw spring-boot:run
```


---
App starts at → `http://localhost:8080`

### Run with Docker (Local Deployment Only)
```bash
# Build the image
docker build -t mvsrconnect .

# Run the container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/your_db \
  -e DB_USERNAME=your_db_user \
  -e DB_PASSWORD=your_db_password \
  -e GOOGLE_CLIENT_ID=your_google_client_id \
  -e GOOGLE_CLIENT_SECRET=your_google_client_secret \
  -e CLOUDINARY_CLOUD_NAME=your_cloud_name \
  -e CLOUDINARY_API_KEY=your_cloudinary_api_key \
  -e CLOUDINARY_API_SECRET=your_cloudinary_api_secret \
  -e MODERATION_URL=http://localhost:5001 \
  -e MODERATION_ENABLED=true \
  -e HUGGINGFACE_API_KEY=your_huggingface_api_key \
  -e MAIL_USERNAME=your_email@gmail.com \
  -e MAIL_PASSWORD=your_gmail_app_password \
  -e VAPID_PUBLIC_KEY=your_vapid_public_key \
  -e VAPID_PRIVATE_KEY=your_vapid_private_key \
  -e VAPID_SUBJECT=mailto:your_email@gmail.com \
  mvsrconnect
```

---

## 🔌 API Overview

> 📘 Full interactive docs at `/swagger-ui.html` when running locally.

### Auth & Users

| Method | Endpoint | Description |
|---|---|---|
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/user` | Get the currently logged-in user |

### Posts

| Method | Endpoint | Description |
|---|---|---|
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/posts` | All posts (newest first) |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/posts` | Create a new post |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/posts/hot` | Hot-ranked posts (time-decay algorithm) |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/posts/top` | Top-voted posts |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/posts/{id}` | Get a single post |
| ![DELETE](https://img.shields.io/badge/DELETE-F93E3E?style=flat-square) | `/posts/{id}` | Delete a post (author / mod / admin) |

### Comments

| Method | Endpoint | Description |
|---|---|---|
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/posts/{id}/comment` | Add a comment (supports threaded replies) |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/posts/{id}/comments` | Get all comments for a post |
| ![DELETE](https://img.shields.io/badge/DELETE-F93E3E?style=flat-square) | `/posts/comments/{id}` | Delete a comment |

### Votes

| Method | Endpoint | Description |
|---|---|---|
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/votes/{postId}?value=1` | Upvote or downvote a post |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/votes/up/{postId}` | Get upvote count |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/votes/down/{postId}` | Get downvote count |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/votes/user/{postId}` | Get the current user's vote |

### Clubs

| Method | Endpoint | Description |
|---|---|---|
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/clubs` | List all clubs |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/clubs/{id}/posts` | Posts within a club |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/clubs/{id}/join-request` | Request to join a club |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/clubs/requests` | All pending join requests (admin) |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/clubs/requests/{id}/approve` | Approve a join request |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/clubs/requests/{id}/reject` | Reject a join request |

### Events

| Method | Endpoint | Description |
|---|---|---|
| ![GET](…) | `/events` | All active events (sorted by date) |
| ![POST](…) | `/events` | Create an event (mod/admin) |
| ![GET](…) | `/events/{id}` | Get a single event (public view, UPI stripped) |
| ![POST](…) | `/events/{id}/enroll` | Enroll in an event |
| ![POST](…) | `/events/{id}/pay` | Submit UTR after UPI payment |
| ![GET](…) | `/events/{id}/my-enrollment` | Get current user's enrollment for an event |
| ![GET](…) | `/events/{id}/enrollments` | List all enrollments (organizer/admin) |
| ![POST](…) | `/events/enrollments/{id}/approve` | Approve payment → issue QR |
| ![POST](…) | `/events/enrollments/{id}/reject` | Reject payment |
| ![GET](…) | `/events/verify/{token}` | Verify a QR token (scanner) |
| ![POST](…) | `/events/checkin/{token}` | Mark attendee as checked in |
| ![GET](…) | `/events/my-tickets` | Current user's enrolled tickets |
| ![GET](…) | `/events/my-events` | Events created by current user |

### Lost & Found

| Method | Endpoint | Description |
|---|---|---|
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/lost-found` | All items (newest first) |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/lost-found` | Report a lost or found item |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/lost-found/search?q=` | Search items by keyword |
| ![DELETE](https://img.shields.io/badge/DELETE-F93E3E?style=flat-square) | `/lost-found/{id}` | Delete an item (author / admin) |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/lost-found/{id}/resolve` | Mark item as resolved |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/lost-found/{id}/responses` | View responses (author / admin only) |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/lost-found/{id}/respond` | Submit a finder / ownership claim |

### Moderator Panel

| Method | Endpoint | Description |
|---|---|---|
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/mod/my-clubs` | Clubs where I'm a moderator |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/mod/club/{id}` | Club dashboard (members, posts, requests) |
| ![DELETE](https://img.shields.io/badge/DELETE-F93E3E?style=flat-square) | `/mod/club/{id}/posts/{postId}` | Delete a post as moderator |
| ![DELETE](https://img.shields.io/badge/DELETE-F93E3E?style=flat-square) | `/mod/club/{id}/comments/{commentId}` | Delete a comment as moderator |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/mod/club/{id}/requests/{reqId}/approve` | Approve join request |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/mod/club/{id}/requests/{reqId}/reject` | Reject join request |
| ![DELETE](https://img.shields.io/badge/DELETE-F93E3E?style=flat-square) | `/mod/club/{id}/members/{memberId}` | Remove a member |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/mod/appeal` | Submit appeal to become a moderator |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/mod/appeals` | List pending appeals (admin) |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/mod/appeals/{id}/approve` | Approve appeal → user becomes moderator |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/mod/appeals/{id}/reject` | Reject appeal |

### Reports

| Method | Endpoint | Description |
|---|---|---|
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/reports` | Report a post |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/reports` | List open reports (admin) |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/reports/{id}/resolve` | Resolve a report (admin) |

### Search & Tags

| Method | Endpoint | Description |
|---|---|---|
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/search?q=` | Global search (posts, users, clubs, tags) |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/search/tag/{tagId}` | Filter posts by tag |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/tags` | List all tags |

### Dashboard & Media

| Method | Endpoint | Description |
|---|---|---|
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/dashboard` | Personal stats, posts, comments, clubs |
| ![PATCH](https://img.shields.io/badge/PATCH-FCA130?style=flat-square) | `/dashboard/profile` | Update name, bio, avatar |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/api/media/upload` | Upload image or video to Cloudinary |

### 🔔 Notifications

| Method | Endpoint | Description |
|---|---|---|
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/notifications` | Get latest 20 notifications |
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/notifications/unread-count` | Get unread notification count |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/notifications/read-all` | Mark all notifications as read |

### 🔔 Push Notifications

| Method | Endpoint | Description |
|---|---|---|
| ![GET](https://img.shields.io/badge/GET-61AFFE?style=flat-square) | `/push/vapid-key` | Get public VAPID key |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/push/subscribe` | Save browser push subscription |
| ![POST](https://img.shields.io/badge/POST-49CC90?style=flat-square) | `/push/unsubscribe` | Remove push subscription |

---

## 🖥️ Pages

| Page             | URL                  | What it does                                                     |
|------------------|----------------------|------------------------------------------------------------------|
| **Feed**         | `/`                  | Main feed with post creation, clubs sidebar, tag filters, voting |
| **Lost & Found** | `/lostandfound.html` | Report lost/found items, search, claim, resolve                  |
| **Dashboard**    | `/dashboard.html`    | Your posts, comments, clubs, liked posts, profile editor         |
| **Mod Panel**    | `/mod.html`          | Club moderation — members, posts, join requests, appeals         |
| **Admin Panel**  | `/admin.html`        | Platform-wide reports, club requests, moderator appeals          |
| **Search**       | `/search.html`       | Global search with tag filter bar                                |
| **Events**       | `/events.html`       | Browse events, enroll, pay, view QR tickets, manage enrollments  |
| **QR Scanner**   | `/scanner.html`      | Camera scanner for check-in at event entry (mod/admin only)      |
| **Post**         | `/post.html`         | Shows the selected post using postId                             |

---

## 🔒 Roles & Permissions

| Role | What they can do |
|---|---|
| `USER` | Post, comment, vote, join clubs, report posts, use Lost & Found |
| `MODERATOR` | Everything above + manage club posts, comments, members, and join requests |
| `ADMIN` | Everything above + approve/reject moderator appeals, resolve all reports, manage all clubs, delete any Lost & Found item |

> Roles are stored in the `users` table. The first admin must be set manually in the database.

---

## 🤖 Content Moderation

All user-generated content is automatically screened before it goes live:

- **Text** — Posts, comments, and Lost & Found descriptions are checked for toxicity via the Flask microservice at `/check_text`.
- **Images** — Image URLs are sent to `/check_image`. If flagged, the image is deleted from Cloudinary and the post is rejected.
- **Videos** — Same flow via `/check_video`.

To disable in development:
```properties
MODERATION_ENABLED=false
```

If the Flask service is unavailable, moderation **fails open** (content is allowed through) so the app stays functional.

---

## 🗄️ Database Schema (Key Tables)
```
users                → id, name, email, google_id, picture, role, bio
posts                → id, title, content, author_id, author_name, media_url, media_type, club_id, created_at
comments             → id, content, post_id, user_id, parent_comment_id, created_at
votes                → id, post_id, user_id, value (+1 / -1)
clubs                → id, name, description, created_at
club_members         → id, club_id, user_id, role (MEMBER/MODERATOR/PRESIDENT), joined_at
club_join_requests   → id, club_id, user_id, status (PENDING/APPROVED/REJECTED), created_at
moderator_appeals    → id, club_id, user_id, reason, status, created_at
reports              → id, post_id, user_id, reason, status (OPEN/RESOLVED), created_at
tags                 → id, name
post_tags            → post_id, tag_id
lost_found_items     → id, type (LOST/FOUND), title, description, location, category, date, author_id, author_name, media_url, media_type, media_public_id, resolved, created_at
lost_found_responses → id, item_id, author_id, author_name, message, contact, mode (found_it/its_mine), created_at
events               → id, title, description, venue, event_date, fee_in_paise, capacity, upi_id, upi_name, organizer_id, organizer_name, club_id, banner_url, banner_public_id, active, created_at
event_enrollments    → id, event_id, user_id, user_name, user_email, user_picture, status (PENDING_PAYMENT/PENDING_APPROVAL/CONFIRMED/REJECTED/CHECKED_IN), utr_number, qr_token, enrolled_at, checked_in_at
notifications        → id, user_id, title, body, url, read, created_at
push_subscriptions   → id, user_id, endpoint, p256dh, auth, created_at
```

---

## 🧠 Hot Ranking Algorithm

Posts in the **Hot** feed are ranked using a Reddit-inspired time-decay formula:
```sql
SELECT p.*
FROM posts p
LEFT JOIN votes v ON p.id = v.post_id
GROUP BY p.id
ORDER BY
  COALESCE(SUM(v.value), 0) /
  POWER(EXTRACT(EPOCH FROM (NOW() - p.created_at)) / 3600 + 2, 1.5)
DESC
```

Where the numerator is the net vote score and the denominator grows over time. The `+2` prevents brand-new posts from dividing by zero, and the `1.5` exponent controls how aggressively older posts decay out of the feed.

---

## 📄 License

This project is built for **educational and internal use** at MVSR Engineering College.  
Not intended for commercial deployment.

---

<div align="center">

Built with ☕ and late nights by **Shanmu Setty**  
*MVSR Engineering College · 2026*

</div>