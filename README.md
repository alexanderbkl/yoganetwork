<div align="center">

<img src="Images/logo-built_white.png" alt="SoulFriends logo" width="120"/>

# 🧘 SoulFriends (YogaNetwork)

**A social & community Android app for yogis and spiritual seekers to connect, chat, and share their journey.**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Gradle](https://img.shields.io/badge/Gradle-8.0-02303A?logo=gradle&logoColor=white)](https://gradle.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![Min SDK](https://img.shields.io/badge/minSdk-23-blue)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/targetSdk-34-blue)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Unlicensed-lightgrey)](#-license)

</div>

---

## 📖 About

**SoulFriends** (internally known as *YogaNetwork*) is a native Android application written in **Kotlin** and **Java** that connects people interested in yoga, spirituality, and mindful living. It combines a **social feed**, a **Tinder-style match/swipe experience**, **real-time chat (1:1 and groups)**, and **location-based discovery**, all backed by **Firebase**.

## ✨ Features

### 🪪 Authentication & Onboarding
- Email/password sign-up and login powered by **Firebase Authentication**.
- Multi-step post-registration profile setup (`PostRegistrationActivity`) to collect bio, photos, and preferences.
- User profile editing and settings management (`SettingsActivity`).

### 🃏 Discover & Match (Card Stack)
- Swipeable card-stack UI (`CardStackActivity`) inspired by dating apps, letting users like/pass on other members to find like-minded souls nearby.
- Location-aware discovery using **Google Maps**, **GeoFire**, and the **Google Play Services Maps** SDK to surface nearby users and events.

### 📰 Social Feed & Posts
- Create, view, like, and comment on posts (`AddPostActivity`, `PostDetailActivity`).
- See who liked a post or a comment (`PostLikedByActivity`, `CommentLikedByActivity`).
- Rich media support for posts, including images and video playback (`VideoPlayerActivity`) via **ExoPlayer**.
- Embedded **YouTube** video playback (`YouTubePlayerActivity`).

### 💬 Real-Time Messaging
- One-to-one private chat (`ChattingActivity`) backed by **Cloud Firestore** / **Realtime Database** for instant delivery.
- Group chat support with dedicated flows for:
  - Creating a group (`GroupCreateActivity`)
  - Editing group details (`GroupEditActivity`)
  - Viewing group info & participants (`GroupInfoActivity`)
  - Adding/removing participants (`GroupParticipantAddActivity`, `AddToGroupActivity`)
  - Sharing images inside group chats (`GroupChatImageActivity`)
- Custom chat views and adapters for a smooth, WhatsApp-like messaging experience.

### 👥 Community & Profiles
- Browse other members' profiles (`ThereProfileActivity`, `UsersActivity`).
- Follow/connect with other users and manage your personal dashboard (`DashboardActivity`).

### 🔔 Notifications
- Push notifications via **Firebase Cloud Messaging (FCM)** to keep users engaged with likes, comments, messages, and group activity.

### 🖼️ Media Handling
- Image selection, capture, and cropping powered by a built-in crop module (`crop/`) and **Glide** for efficient image loading/caching.
- Camera and microphone integration for capturing photos, videos, and voice content.

### ☁️ Backend & Infrastructure
- **Firebase** suite: Authentication, Firestore, Realtime Database, Storage, Cloud Messaging, Analytics.
- **Google Maps Platform** for geolocation features.
- Modular Gradle project structure with separate `debug` and `release` source sets and secure key management (`key.jks`, encrypted keystore for Google Play enrollment).

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin, Java |
| Platform | Android (min SDK 23, target SDK 34) |
| Build System | Gradle 8.0 |
| Backend / BaaS | Firebase (Auth, Firestore, Realtime Database, Storage, FCM, Analytics) |
| Maps & Location | Google Maps SDK, Google Play Services, GeoFire |
| Media | Glide, ExoPlayer, YouTube Android Player, custom image cropper |
| Architecture | Activities + Fragments, Adapters, ViewBinding |

## 📂 Project Structure

```
yoganetwork/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/amit/yoganet/
│       │   │   ├── adapters/       # RecyclerView / list adapters
│       │   │   ├── cardstack/      # Swipeable card-stack (match) logic
│       │   │   ├── ChatView/       # Chat UI components
│       │   │   ├── constants/      # App-wide constants
│       │   │   ├── crop/           # Image cropping utility
│       │   │   ├── fragments/      # UI fragments
│       │   │   ├── models/         # Data models
│       │   │   ├── notifications/  # FCM notification handling
│       │   │   ├── utils/          # Helper/utility classes
│       │   │   └── *Activity.java  # Screens (Login, Feed, Chat, Groups, etc.)
│       │   └── res/                # Layouts, drawables, strings, themes
│       ├── debug/                  # Debug-specific resources
│       ├── release/                # Release-specific resources
│       └── androidTest/ & test/    # Instrumented & unit tests
├── build.gradle                    # Top-level Gradle config
├── app/build.gradle                # App module Gradle config
├── gradle.properties
└── settings.gradle
```

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Giraffe or newer recommended)
- **JDK 17**
- **Gradle 8.0** (bundled via the included Gradle Wrapper — no manual install needed)
- A **Firebase** project with `google-services.json` configured for the app
- A **Google Maps API key** configured in `google_maps_key` string resource

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/alexanderbkl/yoganetwork.git
   cd yoganetwork
   ```

2. **Add your Firebase configuration**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com/).
   - Download the `google-services.json` file and place it inside `app/`.

3. **Configure your Google Maps API key**
   - Add your API key to `google_maps_key` in `app/src/main/res/values/google_maps_api.xml`.

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run on a device or emulator**
   ```bash
   ./gradlew installDebug
   ```
   Or simply open the project in **Android Studio** and press ▶️ **Run**.

### Running Tests

```bash
./gradlew test              # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests on a connected device/emulator
```

## 🔐 Signing & Release

The repository includes keystore references (`key.jks`, `mykeystore.keystore`) used for signing release builds and Google Play enrollment. Treat these and any associated credentials as sensitive — do **not** commit new secrets, and use environment-specific signing configs for your own builds.

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/my-feature`.
3. Commit your changes: `git commit -m "Add my feature"`.
4. Push to the branch: `git push origin feature/my-feature`.
5. Open a Pull Request.

## 📄 License

No license has been specified for this project yet. All rights are reserved by the author unless otherwise stated.

---

<div align="center">
Made with 🧘 for the yoga & wellness community
</div>
