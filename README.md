# 📚 ChapterCircle

**ChapterCircle** is a social network for book lovers, built to foster community, discussions, and recommendations around books and reading.

---

## 🚀 Features

- 🧰 **Spring Boot** backend
- ⚡ **Next.js** frontend
- 📦 **MongoDB** as the primary datastore for books, reviews, and chats
- 💬 **Real-time chat** between users and inside book clubs
- 🧠 **Sentiment-based recommendations** derived from user reviews
- 🔍 **Automated book metadata ingestion** via the [Google Books API](https://developers.google.com/books)
- 📊 **Observability and metrics** to track lifts in:
  - Match-rate
  - Engagement
- 🛡️ **Production hardening**:
  - Rate limiting
  - De-duplication across sources
  - Retry logic for ingestion

---

## ⚙️ Quick Start (Backend)

### ✅ Prerequisites

- [Java 17+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [Docker](https://www.docker.com/) (optional, for infrastructure setup)

---

### 🏗️ Infrastructure (Optional)

Start PostgreSQL and MailDev via Docker:

```bash
docker compose up -d
````

For MongoDB profile, run:

```bash
docker run -d --name mongo -p 27017:27017 mongo:6
```

Ensure MongoDB is accessible at:

```text
mongodb://localhost:27017/chaptercircle
```

---

### 📦 Build the Project

```bash
./mvnw -q -DskipTests package
```

---

### ▶️ Run Profiles

#### Default (`dev`, PostgreSQL):

```bash
./mvnw spring-boot:run
```

#### MongoDB + REST API profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mongo
```

Or with environment variable:

```bash
SPRING_PROFILES_ACTIVE=mongo ./mvnw spring-boot:run
```

---

## 🔌 API Overview

> **Base path**: `/api/v1`

### 📚 Books API (`mongo` profile)

* `GET /api/v1/books?q=term` — Search by title
* `GET /api/v1/books/{id}` — Fetch a single book
* `POST /api/v1/books` — Create a new book (JSON body)
* `GET /api/v1/books/search/google?q=term&limit=10` — Search via Google Books (no persistence)
* `POST /api/v1/books/ingest/google?query=subject:fiction&max=100` — Ingest books into MongoDB

### 📝 Reviews API

* `GET /api/v1/reviews?bookId={bookId}` — List all reviews for a book
* `GET /api/v1/reviews/{id}` — Get a single review
* `POST /api/v1/reviews` — Create a review

    * Includes **sentiment analysis** if `text` is provided

### 📖 Recommendations API

* `GET /api/v1/recommendations/sentiment?userId={userId}&limit=20` —
  Sentiment-based book recommendations

---

## ⚙️ Configuration Highlights

### MongoDB Profile (`mongo`)

| Property                          | Description                                                                 |
| --------------------------------- | --------------------------------------------------------------------------- |
| `spring.data.mongodb.uri`         | MongoDB connection URI (default: `mongodb://localhost:27017/chaptercircle`) |
| `googlebooks.apiKey`              | API Key from env `GOOGLE_BOOKS_API_KEY`                                     |
| `googlebooks.baseUrl`             | Google Books API base URL                                                   |
| `googlebooks.scheduler.enabled`   | `false` by default (set to `true` to enable scheduled ingestion)            |
| `googlebooks.scheduler.cron`      | Ingestion cron expression (default: hourly)                                 |
| `googlebooks.scheduler.query`     | Ingestion query (default: `subject:fiction`)                                |
| `googlebooks.scheduler.batchSize` | Batch size for ingestion (default: 500)                                     |
| `sentiment.engine`                | `simple` or `nltk`                                                          |
| `sentiment.nltk.url`              | If using `nltk`, default is `http://localhost:5001/sentiment`               |

---

### 📁 Active Profiles

* **Default**: `dev`
* 🔧 Configurations:

    * PostgreSQL (dev): `src/main/resources/application-dev.yml`
    * MongoDB (mongo): `src/main/resources/application-mongo.yml`

---

## 📄 License

This project is licensed under the terms specified in the [LICENSE](./LICENSE) file.

---

> ✨ *Join readers, share reviews, chat in real time, and get personalized book recommendations with ChapterCircle!*
---