# Android Chat App using Java and Firebase 

Welcome to the Android Chat App! This Android application leverages Firebase for real-time communication, providing users with a seamless and responsive chat experience.

## Table of Contents

1. [Introduction](#introduction)
2. [Features](#features)
3. [Technologies Used](#technologies-used)
4. [Getting Started](#getting-started)
5. [Firebase Configuration](#firebase-configuration)
6. [Contributing](#contributing)

## Introduction

This Android chat app is built with Java and Firebase to enable users to chat in real-time. Whether you're looking to integrate chat functionality into your existing app or learn about Firebase integration, this project serves as a robust starting point.

## Features

- **Real-time Messaging**: Instant messaging with real-time updates.
- **User Authentication**: Securely authenticate users using Firebase Authentication.
- **Image and Text Messages**: Support for sending both text and image messages.
- **User Presence**: Display online/offline status for users.
- **Firebase Cloud Messaging (FCM)**: Push notifications for new messages.

## Technologies Used

- **Android (Java)**: The core programming language for Android app development.
- **Firebase Realtime Database**: Stores and syncs data in real-time.
- **Firebase Authentication**: Manages user authentication.
- **Firebase Cloud Messaging (FCM)**: Handles push notifications.
- **Firebase Storage**: Stores and retrieves images.

## Getting Started

1. Clone the repository to your local machine.
2. Open the project in Android Studio.
3. Connect the app to your Firebase project.
4. Run the app on an emulator or a physical device.

## Firebase Configuration

1. Create a new project on the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app to your Firebase project.
3. Download the `google-services.json` file and place it in the `app` directory of your Android project.
4. Enable Firebase Authentication and Firebase Realtime Database.
5. Update the Firebase configurations in the `app/build.gradle` file.
<!--
<span style="color: #555555;">
// app/build.gradle

apply plugin: 'com.google.gms.google-services'

android {
    // ...
}

dependencies {
    // ...
    implementation 'com.google.firebase:firebase-auth:22.0.0'
    implementation 'com.google.firebase:firebase-database:22.0.0'
    implementation 'com.google.firebase:firebase-storage:22.0.0'
    // ...
}
</span>
-->
 6.Sync your project with the changes.


## Contributing

Contributions are welcome! If you'd like to enhance the app, fix issues, or add new features, please follow these steps:
1.Fork the repository.
2.Create a new branch for your changes.
3.Make your modifications.Submit a pull request.
