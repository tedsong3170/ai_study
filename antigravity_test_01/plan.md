# TDD Test Plan for Firebase Authentication API

This plan follows the TDD methodology described in `tdd.md`. Each test should be implemented one at a time following the Red → Green → Refactor cycle.

## Domain Layer Tests
- [x] Should create User domain model with all required fields
- [x] Should update FCM token in User domain model
- [x] Should update last login time in User domain model

## Persistence Layer Tests
- [x] Should convert UserEntity to domain User
- [x] Should convert domain User to UserEntity
- [x] Should save and find user by Firebase UID using repository

## Setup Tests
- [x] Should load Firebase configuration successfully
- [x] Should initialize Firebase Admin SDK

## Firebase Login API Tests
- [x] Should return 400 when Firebase token is missing
- [x] Should return 401 when Firebase token is invalid
- [x] Should return 200 with user info when Firebase token is valid
- [x] Should create new user in database if not exists
- [x] Should update existing user's last login time

## FCM Token Registration API Tests
- [x] Should return 400 when FCM token is missing
- [x] Should return 401 when user is not authenticated
- [x] Should return 200 when FCM token is successfully registered
- [x] Should update FCM token if user already has one
- [x] Should associate FCM token with authenticated user

## Integration Tests
- [x] Should complete full login flow with valid Firebase token
- [x] Should register FCM token after successful login

---

**Instructions:**
- Mark tests with `[x]` only after they pass
- Each test should follow: Write test (Red) → Implement code (Green) → Refactor → Commit
- Separate structural changes from behavioral changes
- Run all tests after each change
