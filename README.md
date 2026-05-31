# MindMesh

MindMesh is an AI-assisted learning system built for the NUS Orbital programme. It helps students turn scattered practice-based learning into durable revision material by generating structured Coursework Flashcards (CFCs) from rough notes, question text, and screenshots.

The current implementation for milestone 1 includes authenticated user ownership, module/topic management, AI-assisted CFC generation, saved CFC retrieval, backend validation, and protection for the heavy generation endpoint through rate limiting.

## Documentation

For the full project writeup, system flow diagrams, architecture, software engineering principles, and testing approach, see the [MindMesh Wiki](https://github.com/SleepySharkz/Orbital_Project/wiki/MindMesh-Wiki#1-product-overview).

## Local Environment

- Backend local env lives in [backend/.env](backend/.env) and is loaded through [`.envrc`](.envrc) when using `direnv`.
- The backend uses the high-level `local` profile for normal development and `production` for deployed runs.
- Frontend build/runtime API configuration is provided through `VITE_API_BASE_URL`; see [frontend/.env.example](frontend/.env.example).

## Deployment

- Frontend can be deployed on Vercel. Set `VITE_API_BASE_URL` to the public Railway backend URL in the Vercel project environment variables, then redeploy.
- Backend can be deployed on Railway. In the backend service Variables tab, configure:
  - `SPRING_PROFILES_ACTIVE=production`
  - `JWT_SECRET=...`
  - `JWT_EXPIRATION_MS=3600000`
  - `FRONTEND_URL=https://your-vercel-domain`
  - `MINDMESH_AI_API_KEY=...`
  - `MINDMESH_AI_BASE_URL=...`
  - `MINDMESH_AI_MODEL=...`
  - `MINDMESH_AI_TIMEOUT_MS=30000`
- Because the backend currently uses SQLite, Railway must also provide persistent disk storage for the database file:
  1. Open the backend service in Railway.
  2. Create or attach a **Volume** to that service.
  3. Set the Volume mount path to `/data`.
  4. In the backend service Variables tab, set:
     - `SPRING_DATASOURCE_URL=jdbc:sqlite:/data/mindmesh.db`
  5. Redeploy the backend service.
- This stores the SQLite database at `/data/mindmesh.db` on the Railway Volume, so the data survives redeploys and restarts.
- After updating Railway variables, review the staged changes and deploy the service again so the new values take effect.
