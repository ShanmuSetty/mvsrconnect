FROM ubuntu:latest
LABEL authors="shanmu"

ENTRYPOINT ["top", "-b"]