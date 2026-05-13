# Auth Feature Checklist

## Goal

Build a full-stack authentication slice for MINDMESH that proves:

- the React frontend can handle authentication UI and auth state
- the Spring Boot backend can authenticate users securely
- the database can persist users
- protected access works correctly
- logout removes access

---

## Scope

In scope:

- sign up with email and password
- log in with email and password
- log out
- fetch the current logged-in user
- one protected frontend page

Out of scope:

- OAuth
- password reset
- email verification
- roles and permissions
- friends, marketplace, flashcards
- profile editing

---

## Architecture Choices

Use:

- React frontend
- Spring Boot backend
- Spring Security
- SQLite for the checkpoint
- JWT authentication

Reason for JWT:

- clean separation between frontend and backend
- suitable for SPA plus API architecture
- easy to test and extend later

If JWT becomes a blocker, session-based auth is an acceptable fallback for the checkpoint.

---

## Core Data Model

Create a `users` table with these fields:

- `id`
- `email`
- `password_hash`
- `created_at`

Requirements:

- `email` must be unique
- `email` must not be null
- `password_hash` must not be null
- passwords must be hashed with `BCrypt`
- plaintext passwords must never be stored

---

## Backend Endpoints

### `POST /auth/signup`

Request:

```json
{
  "email": "user@example.com",
  "password": "strongPassword123"
}
```

Success response:

```json
{
  "message": "User created successfully"
}
```

Failure cases:

- `400` invalid input
- `409` email already exists

### `POST /auth/login`

Request:

```json
{
  "email": "user@example.com",
  "password": "strongPassword123"
}
```

Success response:

```json
{
  "token": "jwt-token-here",
  "user": {
    "id": "1",
    "email": "user@example.com"
  }
}
```

Failure cases:

- `400` invalid input
- `401` wrong credentials

### `GET /auth/me`

Header:

```http
Authorization: Bearer <token>
```

Success response:

```json
{
  "id": "1",
  "email": "user@example.com"
}
```

Failure cases:

- `401` missing token
- `401` invalid token

### `POST /auth/logout`

Checkpoint behavior:

- frontend removes stored JWT
- backend may simply return success

Success response:

```json
{
  "message": "Logged out successfully"
}
```

---

## Frontend Pages

### `/signup`

- email input
- password input
- submit button
- error message area
- link to login page

### `/login`

- email input
- password input
- submit button
- error message area
- link to sign up page

### `/dashboard` or `/home`

- protected page
- displays logged-in user email
- logout button

---

## Frontend Behavior

### Sign up flow

- validate basic input
- call `POST /auth/signup`
- on success, redirect to login or auto-login

### Login flow

- call `POST /auth/login`
- store JWT
- update auth state
- redirect to protected page

### Protected page flow

- call `GET /auth/me` on load
- if unauthorized, redirect to login

### Logout flow

- clear JWT from storage
- clear auth state
- redirect to login

---

## Validation Rules

Minimum requirements:

- email must have valid format
- password must not be empty
- password should have minimum length `8`

Validation should exist in:

- frontend for user experience
- backend for correctness and security

---

## Security Requirements

Must have:

- `BCrypt` password hashing
- unique email constraint
- protected endpoint requiring valid token
- no password returned in any response
- proper `401` for unauthorized access

Nice to have later:

- stronger password policy
- refresh tokens
- rate limiting
- secure cookie strategy

---

## Definition of Done

The feature is complete only if:

1. A new user can sign up.
2. An existing user can log in with correct credentials.
3. Wrong credentials are rejected.
4. A logged-in user can access the protected page.
5. A logged-out user cannot access the protected page.
6. Passwords are hashed in the database.
7. Frontend and backend are integrated end to end.

---

## Implementation Order

1. Freeze endpoint payloads and responses.
2. Create the `users` schema.
3. Implement backend signup, login, and `me` endpoints.
4. Add JWT security configuration and request filtering.
5. Build frontend sign up and login pages.
6. Build the protected page and logout flow.
7. Test both success and failure cases.

---

## Notes

- Keep this checkpoint focused on proving full-stack capability.
- Do not expand into flashcards or marketplace features yet.
- If implementation complexity becomes too high, reduce polish, not architecture correctness.
