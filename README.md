# Job_Hunter (Learning Project)

Job_Hunter is a two-part application containing a backend (BE) and a frontend (FE). The repository uses Gradle for building the backend and a JavaScript toolchain (npm/yarn) for the frontend.

This README explains how to run the project locally, how to build artifacts, and provides troubleshooting tips.

## Table of contents
- About
- Repository layout
- Prerequisites
- Run (development)
  - Backend
  - Frontend
  - Running both together
- Build (production)
- Tests
- Environment / config
- Common troubleshooting
- Contributing
- License

## About
This repository contains the backend (API) and frontend (UI) for the Job_Hunter project. The backend is built with a JVM-based Gradle project (see Gradle wrapper in repository root). The frontend is a JavaScript SPA (located in the `FE` directory).

## Repository layout
- `BE/` — Backend source code (Gradle module)
- `FE/` — Frontend source code (npm/yarn project)
- `gradlew`, `gradlew.bat` — Gradle wrapper (use to run Gradle without installing it)
- `settings.gradle.kts` — Gradle multi-module settings

## Prerequisites
- JDK 17+ (or JDK required by the project; check BE/build.gradle or the backend module if unsure)
- Node.js 16+ and npm (or yarn) for the frontend
- Git (to clone)
- Optional: Docker if you want to containerize services or run databases locally

Note: The Gradle wrapper included in the repository (`./gradlew`) will download an appropriate Gradle distribution automatically; you still need a compatible JDK installed.

## Run (development)

General approach: run the backend API and frontend dev server concurrently in separate terminals.

### 1) Backend (development)

Option A — run from the project root (multi-module invocation)
- Start the backend with the Gradle wrapper:
  - macOS / Linux:
    - ./gradlew :BE:bootRun
  - Windows (PowerShell/CMD):
    - gradlew.bat :BE:bootRun

Option B — cd into the `BE` directory and run Gradle there
- cd BE
- ./gradlew bootRun

What these commands do:
- `bootRun` (common Spring Boot task) starts the backend on its default port (often 8080). If the project doesn't use Spring Boot, use the run or application task defined in the backend's build file.

To run the packaged JAR:
- ./gradlew :BE:build
- java -jar BE/build/libs/<backend-artifact>.jar

### 2) Frontend (development)
- cd FE
- Install dependencies:
  - npm install
  - or yarn install
- Start the dev server:
  - npm start
  - or yarn start

By default many front-end dev servers run on port 3000 (or a configured port). It will proxy API calls to the backend if configured in the frontend's dev configuration (check `FE/package.json` or dev config files).

### 3) Running both together
- Open two terminal tabs:
  - Terminal 1: start the backend (`./gradlew :BE:bootRun`)
  - Terminal 2: start the frontend (`cd FE && npm start`)
- Visit the frontend URL (e.g., http://localhost:3000). The UI should make requests to the backend API (e.g., http://localhost:8080). If CORS or proxy settings are required, add them to the backend or frontend dev config.

## Build (production)
- Backend:
  - ./gradlew :BE:build
  - The backend artifact will be in `BE/build/libs/`.
  - Run with `java -jar BE/build/libs/<artifact>.jar`

- Frontend:
  - cd FE
  - npm run build
  - or yarn build
  - The static assets will be in `FE/build/` or `FE/dist/` (check the frontend build output). Serve them with a static file server or configure the backend to serve the frontend build folder.

## Tests
- Run backend tests:
  - ./gradlew :BE:test
- Run frontend tests:
  - cd FE
  - npm test
  - or yarn test

## Environment / configuration
The project may require environment variables for database connections, API keys, secrets, etc. Typical variables to set (replace with real keys used by your project):
- SPRING_DATASOURCE_URL — JDBC URL for the database
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- SPRING_PROFILES_ACTIVE — development / production profile
- FRONTEND_API_BASE_URL — if the frontend needs an explicit API base URL

You can supply environment variables in your shell, using an `.env` file (if the project uses it), or via your IDE run configuration.

## Common troubleshooting
- "Gradle/bootRun not found" — ensure you are using the Gradle wrapper (`./gradlew`) and are in the repository root or module directory.
- "Port already in use" — change the backend port (e.g., `server.port` property for Spring Boot) or the frontend dev server port.
- CORS errors — if the frontend dev server does not proxy API requests to the backend, configure CORS on the backend or configure a proxy in the frontend dev server.
- Missing Node modules — run `npm install` or `yarn install` in `FE/`.
- Database connection errors — ensure your database is running and env vars are set properly.

## Contributing
- Clone the repo: git clone https://github.com/Noreo24/Job_Hunter.git
- Create a feature branch:
  - git checkout -b feat/your-feature
- Implement changes, add tests, run `./gradlew build` and `cd FE && npm test`.
- Open a pull request with a clear description of the change.

## Contact / Author
- Repo owner: Noreo24 - Bui Anh Tuan (https://github.com/Noreo24)

## License
- This project is licensed under the MIT License.
- See the LICENSE file for full details.
