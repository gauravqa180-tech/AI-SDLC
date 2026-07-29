# AI-SDLC

AI SDLC service.

## Prerequisites

- Node.js 18+ (Node 20+ recommended)
- npm 9+ / yarn / pnpm
- Git

## Setup

1. Clone the repository:
   - `git clone <repo-url>`
   - `cd <repo-folder>`

2. Install dependencies:
   - `npm install`

3. Configure environment variables:
   - Copy `.env.example` to `.env` (if `.env.example` exists), otherwise create a `.env` file in the project root.

4. Start the service:
   - Development (watch mode): `npm run dev`
   - Production build: `npm run build`
   - Run production build: `npm start`

## Configuration

Set the following environment variables in `.env`:

- `NODE_ENV` — `development` | `test` | `production`
- `PORT` — HTTP port to bind (default commonly `3000`)
- `HOST` — Bind host (optional, e.g., `0.0.0.0`)
- `BASE_URL` — Public base URL (optional, used for docs/links)

If your project uses a database or external services, add the relevant variables (e.g., `DATABASE_URL`, API keys) as required by the codebase.

## Running Locally

- Start dev server:
  - `npm run dev`

- Health check (once running):
  - `GET http://localhost:<PORT>/health`

## API Documentation (Swagger)

Swagger UI:
- `http://localhost:<PORT>/docs`

OpenAPI JSON:
- `http://localhost:<PORT>/docs-json`

## Endpoints

Base URL: `http://localhost:<PORT>`

- `GET /health`  
  Returns service health status.

- `GET /`  
  Returns a basic service welcome/status response (if implemented).

- Swagger:
  - `GET /docs` (UI)
  - `GET /docs-json` (OpenAPI spec)

## Troubleshooting

- If the port is already in use, change `PORT` in `.env` or stop the process using that port.
- If dependencies fail to install, ensure you’re on Node 18+ and delete `node_modules` and lockfile before reinstalling:
  - `rm -rf node_modules package-lock.json && npm install`

## License

Proprietary / Internal (update as appropriate).