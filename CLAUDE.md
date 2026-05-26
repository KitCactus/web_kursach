# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Fullstack cafe management system. Staff and admins manage the menu and orders via a web app. Customers order via a Telegram bot (separate repo at `D:\Уник\Практика\cafe-backend-bot`).

## Build & Run

### Full build (Angular + Spring Boot → single JAR)
```
cd cafe/cafe
mvn clean package -DskipTests
java -jar target/cafe-backend-0.0.1-SNAPSHOT.jar
```

### Backend only (skip Angular build)
```
cd cafe/cafe
mvn clean package -DskipTests -Pbackend-only
```

### Frontend dev server (separate from Spring)
```
cd cafe-client
npm install
npm start        # ng serve on http://localhost:4200
npm run build    # production build → dist/cafe-client/browser/
```

### Run tests
```
cd cafe/cafe
mvn test                                              # all tests
mvn test -Dtest=MenuServiceTest                       # single test class
mvn test -Dtest=MenuServiceTest#methodName            # single test method
```
Tests use H2 in-memory DB — no PostgreSQL required.

### Telegram bot
```
cd D:\Уник\Практика\cafe-backend-bot
pip install -r requirements.txt
python bot_new.py
```

## Architecture

### Spring Boot backend (`cafe/cafe/src/main/java/ru/ssau/cafe/`)

- **Auth**: HTTP Basic auth (no JWT). Credentials sent as `Basic base64(user:pass)` — stored in browser `sessionStorage`. Roles: `ROLE_ADMIN`, `ROLE_USER`.
- **Security**: `SecurityConfig` — `/api/bot/**`, `/api/images/**`, `/api/menu/bot/**`, and public menu endpoints are permitAll. Bot endpoints additionally validated by `X-Bot-Api-Key` header inside `BotController`.
- **Image storage**: Photos are stored in Telegram (not on disk). `TelegramStorageService` uploads via `sendPhoto` Bot API → stores `file_id` in DB as `photo_file_id`. `ImageProxyController` at `/api/images/{fileId}` downloads and proxies bytes to browser with 24h cache.
- **Bot integration**: `BotController` exposes `/api/bot/clients/**`, `/api/bot/cart/**`, `/api/bot/orders` for the Python Telegram bot to call instead of direct DB access.
- **Static frontend**: Angular build is copied into `classpath:/static/` by Maven during package. SPA fallback routes configured in `WebConfig`.

### Angular frontend (`cafe-client/src/app/`)

- Standalone components (no NgModules). All routing in `app.routes.ts`.
- Auth via `AuthService` + `auth.interceptor.ts` (injects Basic auth header). Guards: `authGuard`, `adminGuard`, `loginRedirectGuard`.
- Photo upload flow: file selected → stored locally as `pendingPhotoFile` (blob preview via `URL.createObjectURL`) → uploaded to Telegram only when "Create"/"Save" button is pressed.
- `environment.apiUrl` = `http://localhost:8080/api`.

### Python Telegram bot (`D:\Уник\Практика\cafe-backend-bot/`)

- `bot_new.py` — all handlers, uses `api_client.py` for data.
- `api_client.py` — replaces direct DB access; calls Spring REST API via `httpx`. Maps Spring's English JSON keys to Russian keys expected by bot handlers.
- `config.py` — loads `BOT_TOKEN`, `BACKEND_URL`, `BOT_API_KEY` from `.env`.
- Runs through SOCKS5 proxy `127.0.0.1:10808` (V2Ray) — configured in `main()` via `HTTPXRequest`.

## Key Configuration

### `cafe/cafe/src/main/resources/application.properties`
- `spring.datasource.url` — PostgreSQL `cafedb` on port 5432
- `telegram.storage.bot-token` / `telegram.storage.chat-id` — storage bot for menu photos
- `telegram.bot.api-key` — shared secret for `X-Bot-Api-Key` header (must match `BOT_API_KEY` in bot's `.env`)

### Test properties (`src/test/resources/application.properties`)
Uses H2 in-memory. Requires `telegram.storage.bot-token`, `telegram.storage.chat-id`, and `telegram.bot.api-key` stubs if `TelegramStorageService` is loaded in test context.

## Data Model Notes

- `MenuItem.photoFileId` stores a Telegram **photo** `file_id` (not a filename). Old items may have stale document file_ids — re-upload photo to fix.
- `Client` = Telegram bot user (identified by `telegramId`). Separate from `User` (web app staff accounts).
- `CartItem` links `Client` → `MenuItem` directly; price is always read from `MenuItem.price` at query time.
- Orders created by the bot via `POST /api/bot/orders` automatically clear the cart and increment `Client.totalOrders`.
