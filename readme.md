# MVSR Connect

A campus social platform exclusively for **MVSR Engineering College** students. Built with Spring Boot and PostgreSQL, it lets students post, comment, vote, join clubs, and moderate content — all behind Google OAuth restricted to `@mvsrec.edu.in` accounts.

---

## Features

- **Google OAuth login** — restricted to `@mvsrec.edu.in` accounts only
- **Feed** — post text, images, and videos with hot/top/recent sorting
- **Clubs** — join clubs, post within them, apply to become a moderator
- **Voting** — upvote/downvote posts (Reddit-style hot ranking)
- **Comments** — threaded replies with toxicity filtering
- **Tags** — tag posts and filter feed by tag
- **Search** — global search across posts, users, clubs, and tags
- **Media uploads** — image and video uploads via Cloudinary
- **Content moderation** — AI toxicity detection via an external Flask service, image/video safety checks
- **Dashboard** — personal stats, post/comment history, club memberships, liked posts, profile editor
- **Mod Panel** — moderators can manage members, approve/reject join requests, delete posts and comments
- **Admin Panel** — admins can resolve reports, approve/reject moderator appeals, and manage all clubs

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4 (Java 17) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Auth | Spring Security + Google OAuth2 |
| Media | Cloudinary |
| Moderation | External Flask microservice |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Frontend | Vanilla HTML/CSS/JS (served as static files) |
| Container | Docker |

---

## Project Structure
