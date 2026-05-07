# Changes_Timeline.md

# Contribution Timeline

Purpose:

Track concise daily contributions by Tauzih and Dhruv so milestone contribution documents can be generated later.

Guidelines:

- Use date format: DD/MM/YYYY.
- Group entries by date.
- Keep each person's entry concise, usually 1 to 3 bullets.
- Summarise meaningful frontend, backend, database, AI, design, documentation, testing, review, or debugging work.
- Do not include overly detailed implementation notes, code snippets.
- If a person has no recorded work for a date, write "No entry recorded yet" only if needed for clarity.

---

## Entry Template

## DD/MM/YYYY

Dhruv:
- Summary of Dhruv's work.

Tauzih:
- Summary of Tauzih's work.

---

## 05/05/2026

Dhruv:
- Set up the contribution timeline system for tracking milestone work summaries.

Tauzih:
- Planned the first authentication feature and defined its implementation checklist: email/password auth, Spring Security with JWT, SQLite `users` table, core auth endpoints (`signup`, `login`, `logout`, `me`), and frontend sign up/login/protected page flow.

## 07/05/2026

Tauzih:
- Scaffolded the Spring Boot backend auth flow and configured security, password hashing, and H2-based persistence for the authentication checkpoint.
- Built the user entity, repository, request/response DTOs, and the signup/login service and controller flow.
- Tested the backend auth flow end to end, including validation behavior, duplicate email handling, login checks, and persistent local database storage.
