# Orbital_Project

MINDMESH is a studying-efficiency improvement app being developed for NUS Orbital.

## Prerequisites

Make sure the following are installed:

- Java 21
- Node.js and npm

## Local Development Setup

Clone the repository and move into the project root:

```bash
git clone <your-repo-url>
cd Orbital_Project
```

### Option 1: Start manually with two terminals

Terminal 1: start the backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend runs on `http://localhost:8080`.

Terminal 2: start the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:5173` by default.

Open the frontend URL shown by Vite in your browser.

## Environment Notes

The current local development setup uses:

- frontend API base URL: `http://localhost:8080`
- backend datasource: local H2 file database

Relevant files:

- [frontend/.env.development](/home/tauzihkhan/MyMainSHARED/Dev/Orbital_Project/frontend/.env.development)
- [backend/src/main/resources/application.properties](/home/tauzihkhan/MyMainSHARED/Dev/Orbital_Project/backend/src/main/resources/application.properties)
### Frontend build check

From the `frontend` directory:

```bash
npm install
npm run build
```

### Frontend lint check

From the `frontend` directory:

```bash
npm run lint
```

### Backend test suite

From the `backend` directory:

```bash
./mvnw test
