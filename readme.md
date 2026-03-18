<div align="center">

<br/>

```
███╗   ███╗██╗   ██╗███████╗██████╗      ██████╗ ██████╗ ███╗   ██╗███╗   ██╗███████╗ ██████╗████████╗
████╗ ████║██║   ██║██╔════╝██╔══██╗    ██╔════╝██╔═══██╗████╗  ██║████╗  ██║██╔════╝██╔════╝╚══██╔══╝
██╔████╔██║██║   ██║███████╗██████╔╝    ██║     ██║   ██║██╔██╗ ██║██╔██╗ ██║█████╗  ██║        ██║   
██║╚██╔╝██║╚██╗ ██╔╝╚════██║██╔══██╗    ██║     ██║   ██║██║╚██╗██║██║╚██╗██║██╔══╝  ██║        ██║   
██║ ╚═╝ ██║ ╚████╔╝ ███████║██║  ██║    ╚██████╗╚██████╔╝██║ ╚████║██║ ╚████║███████╗╚██████╗   ██║   
╚═╝     ╚═╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝    ╚═════╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═══╝╚══════╝ ╚═════╝   ╚═╝   
```

**The exclusive campus social platform for MVSR Engineering College students.**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-336791?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-Educational-lightgrey?style=flat-square)](#license)

<br/>

*Post · Comment · Vote · Join Clubs · Moderate*  
*Exclusively for `@mvsrec.edu.in` accounts*

</div>

---

## 📌 What is MVSR Connect?

MVSR Connect is a **Reddit-style campus forum** built exclusively for students of MVSR Engineering College. It's a single platform where you can share posts, join clubs, vote on content, have threaded discussions, and report inappropriate material — all locked behind Google OAuth so only real `@mvsrec.edu.in` accounts can get in.

Think of it as a private, AI-moderated version of Reddit, but built specifically for your campus.

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

### Moderation & Admin
- 🤖 **AI Toxicity Detection** — Every comment and post is checked by an external Flask microservice before going live
- 🖼️ **Image & Video Safety** — Unsafe media is rejected and automatically deleted from Cloudinary
- 📢 **Report System** — Users can report posts; admins resolve them
- 🧑‍⚖️ **Mod Panel** — Moderators manage their club's members, posts, comments, and join requests
- 👑 **Admin Panel** — Admins approve/reject moderator appeals and resolve all reports

### Personal
- 📊 **Dashboard** — View your posts, comments, clubs, upvotes received, and liked posts
- ✏️ **Profile Editor** — Update your display name, bio, and avatar

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Spring Boot 4.0.3 (Java 17) |
| **Database** | PostgreSQL |
| **ORM** | Spring Data JPA / Hibernate |
| **Auth** | Spring Security + Google OAuth2 |
| **Media** | Cloudinary (images + videos) |
| **Moderation** | External Flask microservice |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Frontend** | Vanilla HTML / CSS / JavaScript |
| **Container** | Docker |

---

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/com/mvsr/mvsrconnect/
│   │   ├── config/              # SecurityConfig, OAuth2LoginSuccessHandler, CloudinaryConfig
│   │   ├── controller/          # REST controllers (Posts, Comments, Clubs, Votes, Search…)
│   │   ├── dto/                 # Data transfer objects
│   │   ├── model/               # JPA entities (User, Post, Club, Comment, Vote, Report…)
│   │   ├── repository/          # Spring Data repositories
│   │   └── service/             # Business logic (ModerationService, ClubService, ReportService…)
│   └── resources/
│       ├── application.properties
│       └── static/              # Frontend pages
│           ├── index.html       # Main feed
│           ├── dashboard.html   # Personal dashboard
│           ├── mod.html         # Moderator panel
│           ├── admin.html       # Admin panel
│           └── search.html      # Global search
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

Set these before running the app:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/your_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Moderation
MODERATION_URL=http://localhost:5001   # Flask service URL
MODERATION_ENABLED=true                # Set to false in dev to skip checks
```

### Run Locally

```bash
./mvnw spring-boot:run
```

App starts at → `http://localhost:8080`

### Run with Docker

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
  -e CLOUDINARY_API_KEY=your_api_key \
  -e CLOUDINARY_API_SECRET=your_api_secret \
  mvsrconnect
```

### Restore the Database

```bash
psql -U your_db_user -d your_db < backup.sql
```

---

## 🔌 API Overview

### Auth & Users
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/user` | Get the currently logged-in user |

### Posts
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/posts` | All posts (newest first) |
| `POST` | `/posts` | Create a new post |
| `GET` | `/posts/hot` | Hot-ranked posts (time-decay algorithm) |
| `GET` | `/posts/top` | Top-voted posts |
| `GET` | `/posts/{id}` | Get a single post |
| `DELETE` | `/posts/{id}` | Delete a post (author / mod / admin) |

### Comments
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/posts/{id}/comment` | Add a comment (supports threaded replies) |
| `GET` | `/posts/{id}/comments` | Get all comments for a post |
| `DELETE` | `/posts/comments/{id}` | Delete a comment |

### Votes
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/votes/{postId}?value=1` | Upvote or downvote a post |
| `GET` | `/votes/up/{postId}` | Get upvote count |
| `GET` | `/votes/down/{postId}` | Get downvote count |
| `GET` | `/votes/user/{postId}` | Get the current user's vote |

### Clubs
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/clubs` | List all clubs |
| `GET` | `/clubs/{id}/posts` | Posts within a club |
| `POST` | `/clubs/{id}/join-request` | Request to join a club |
| `GET` | `/clubs/requests` | All pending join requests (admin) |
| `POST` | `/clubs/requests/{id}/approve` | Approve a join request |
| `POST` | `/clubs/requests/{id}/reject` | Reject a join request |

### Moderator Panel
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/mod/my-clubs` | Clubs where I'm a moderator |
| `GET` | `/mod/club/{id}` | Club dashboard (members, posts, requests) |
| `DELETE` | `/mod/club/{id}/posts/{postId}` | Delete a post as moderator |
| `DELETE` | `/mod/club/{id}/comments/{commentId}` | Delete a comment as moderator |
| `POST` | `/mod/club/{id}/requests/{reqId}/approve` | Approve join request |
| `POST` | `/mod/club/{id}/requests/{reqId}/reject` | Reject join request |
| `DELETE` | `/mod/club/{id}/members/{memberId}` | Remove a member |
| `POST` | `/mod/appeal` | Submit appeal to become a moderator |
| `GET` | `/mod/appeals` | List pending appeals (admin) |
| `POST` | `/mod/appeals/{id}/approve` | Approve appeal → user becomes moderator |
| `POST` | `/mod/appeals/{id}/reject` | Reject appeal |

### Reports
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/reports` | Report a post |
| `GET` | `/reports` | List open reports (admin) |
| `POST` | `/reports/{id}/resolve` | Resolve a report (admin) |

### Search & Tags
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/search?q=` | Global search (posts, users, clubs, tags) |
| `GET` | `/search/tag/{tagId}` | Filter posts by tag |
| `GET` | `/tags` | List all tags |

### Dashboard & Media
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/dashboard` | Personal stats, posts, comments, clubs |
| `PATCH` | `/dashboard/profile` | Update name, bio, avatar |
| `POST` | `/api/media/upload` | Upload image or video to Cloudinary |

> 📘 Full interactive docs available at `/swagger-ui.html` when the app is running.

---

## 🖥️ Pages

| Page | URL | What it does |
|---|---|---|
| **Feed** | `/` | Main feed with post creation, clubs sidebar, tag filters, voting |
| **Dashboard** | `/dashboard.html` | Your posts, comments, clubs, liked posts, profile editor |
| **Mod Panel** | `/mod.html` | Club moderation — members, posts, join requests, appeals |
| **Admin Panel** | `/admin.html` | Platform-wide reports, club requests, moderator appeals |
| **Search** | `/search.html` | Global search with tag filter bar |

---

## 🔒 Roles & Permissions

| Role | What they can do |
|---|---|
| `USER` | Post, comment, vote, join clubs, report posts |
| `MODERATOR` | Everything above + manage club posts, comments, members, and join requests |
| `ADMIN` | Everything above + approve/reject moderator appeals, resolve all reports, manage all clubs |

> Roles are stored in the `users` table. The first admin must be set manually in the database.

---

## 🤖 Content Moderation

All user-generated content is automatically screened before it goes live:

- **Text** — Posts and comments are checked for toxicity via the Flask microservice at `/check_text`. Context-aware (checks replies in the context of their parent comment).
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
users           → id, name, email, google_id, picture, role, bio
posts           → id, title, content, author_id, author_name, media_url, media_type, club_id, created_at
comments        → id, content, post_id, user_id, parent_comment_id, created_at
votes           → id, post_id, user_id, value (+1 / -1)
clubs           → id, name, description, created_at
club_members    → id, club_id, user_id, role (MEMBER/MODERATOR/PRESIDENT), joined_at
club_join_requests → id, club_id, user_id, status (PENDING/APPROVED/REJECTED), created_at
moderator_appeals  → id, club_id, user_id, reason, status, created_at
reports         → id, post_id, user_id, reason, status (OPEN/RESOLVED), created_at
tags            → id, name
post_tags       → post_id, tag_id
```

---

## 🧠 Hot Ranking Algorithm

Posts in the **Hot** feed are ranked using a Reddit-inspired time-decay formula:

```sql
score / POWER(age_in_hours + 2, 1.5)
```

Where `score` is the net vote total and `age_in_hours` is how old the post is. Fresh posts with good engagement rise quickly; older posts decay out of the feed naturally.

---

## 📄 License

This project is built for **educational and internal use** at MVSR Engineering College.  
Not intended for commercial deployment.

---

<div align="center">

Built with ☕ and late nights by **Shanmu Setty**  
*MVSR Engineering College · 2026*

</div>