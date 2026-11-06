# Frontend API contract

The Android client uses Java and ViewBinding. Existing user APIs remain unchanged.

## Administration

Administration includes listing users, viewing details, locking and unlocking. It does not include search.

| Method | Endpoint | Response data |
| --- | --- | --- |
| GET | `/api/v1/admin/users?page=0&size=20` | Page with `content`, `last`, `totalPages` |
| GET | `/api/v1/admin/users/{id}` | User details |
| PATCH | `/api/v1/admin/users/{id}/lock` | Updated user with `userstatus=LOCKED` |
| PATCH | `/api/v1/admin/users/{id}/unlock` | Updated user with `userstatus=ACTIVE` |

Responses use the existing `BaseResponse` envelope. A user has `id`, `firstName`, `lastName`, `email`, `phone`, `university`, `majorName`, `role`, and `userstatus`. The client also accepts `status` or `userStatus` for the status field. Login authorities contain `ADMIN` or `ROLE_ADMIN`. The server must enforce authorization and prevent self-locking independently of the UI.

## Push and reminders

Place a Firebase Android client configuration for `com.example.myapplication` at `app/google-services.json`. This file is excluded from Git. Without it the app builds, but remote push registration needs configuration.

FCM data messages use `recipientId`, `type`, `targetType`, `targetId`, `title`, and `body`. Booking messages use `targetType=BOOKING` and a booking ID in `targetId` or `bookingId`. Data messages allow the client to apply category preferences while in the background. Notification payloads may be displayed directly by Android when the app is in the background.

The client schedules a device reminder 30 minutes before a confirmed booking. It cancels reminders when a loaded booking is rejected, cancelled or completed, and restores cached reminders after reboot. Exact timing depends on device permission; inexact alarms are the fallback. Remote cancellations require an FCM event or a subsequent server refresh to update the device cache.

## WebSocket and location

WebSocket authentication uses the Authorization header and STOMP CONNECT header. It does not place tokens in URL parameters.

Radar coordinate transmission remains pending explicit authorization. The frontend does not collect or send device coordinates in this version.

## Messaging scope

The frontend supports direct text messages, images, recall, and local deletion. Conversation list search remains available. Replies, forwarding, reactions, message search within a conversation, group management, Google sign-in, arbitrary file sending, and voice messages are excluded from the frontend scope.
