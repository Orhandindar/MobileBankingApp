# MobileBankingApp

This project is a secure mobile banking application featuring face recognition authentication to enhance user experience and security.

## Features
- User registration with personal details.
- Secure login with password and face verification.
- Face verification compares the registered face image with a newly captured one.
- Uses Android CameraX API for capturing face images.
- Simple pixel-based image similarity for face verification.

## How it works
1. During registration, user captures their face photo saved as `TC.jpg`.
2. On login, after entering credentials, user captures a new face photo saved as `TC_user.jpg`.
3. The app compares the two images for similarity.
4. If similarity is above the threshold (0.9), user is authenticated and taken to the home screen.

## Technologies
- Android Kotlin
- CameraX


---

