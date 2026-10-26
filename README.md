# MediFind

An Android app that helps patients find a clinic and remember their appointments. Search for a place on the map, see the route from your current location, and set reminders that notify you before an appointment. Accounts and appointments are stored in Firebase.

Built as a group project by a team of four students; the team is listed on the app's About Us page.

## Features

- **Sign up and sign in** with email and password (Firebase Authentication)
- **Home:** greeting by name and a list of your upcoming appointments
- **Maps:** search an address or place name, see it marked on a Google Map, and get a route drawn from your current location
- **Appointment reminders:** enter a clinic name, date and time, and the app saves the appointment and schedules a notification
- **Profile:** store your name, age, gender and phone number
- **About Us** page

## Tech stack

| Area | Tools |
|---|---|
| Language | Java |
| Platform | Android, `minSdk` 24, `targetSdk` 35 |
| Auth and data | Firebase Authentication, Firebase Realtime Database |
| Maps and location | Google Maps SDK, Fused Location Provider, Google Directions API, Maps Utils (polyline decoding) |
| Reminders | `AlarmManager` exact alarms and a broadcast receiver that posts the notification |
| UI | Material Components, bottom navigation, fragments |
| Build | Gradle (Kotlin DSL) |

## App structure

The app uses a bottom navigation bar with five tabs.

| Tab | Class | What it does |
|---|---|---|
| Home | `HomeFragment` | Greets the user and lists their appointments from Firebase |
| Maps | `MapsFragment` | Place search, location and route drawing |
| Appointment | `ReminderFragment` | Saves an appointment and schedules the alarm |
| Profile | `ProfileFragment` / `ProfileActivity` | Edits the user profile |
| About Us | `AboutUsFragment` | Team information |

Other classes: `MainActivity` (launcher), `LoginActivity`, `RegisterActivity`, `HomeActivity` (hosts the tabs), `ReminderBroadcast` (shows the notification), and `DataHelper` (a local SQLite helper with `userProfile` and `appointments` tables).

Firebase Realtime Database layout:

```
Users/{uid}            name, age, gender, phone
Appointments/{id}      clinicName, year, month, day, hour, minute, ...
```

## Getting started

### Requirements
- Android Studio (recent version) with an Android SDK for API 35
- A Google account for Firebase and Google Cloud
- A device or emulator running Android 7.0 (API 24) or newer; use Android 13+ to see the notification permission prompt

### 1. Firebase

1. Create a project in the [Firebase console](https://console.firebase.google.com/) and add an Android app with the package name `com.example.medifind`.
2. Enable **Authentication** with the Email/Password provider.
3. Create a **Realtime Database** and note its URL.
4. Download `google-services.json` and place it in `app/`, replacing the existing file.
5. The Realtime Database URL is written directly in the code (`HomeFragment`, `ReminderFragment`, `ProfileFragment`, `ProfileActivity`, `LoginActivity`). Change it to your own database URL.

### 2. Google Maps

1. In Google Cloud, enable **Maps SDK for Android** and **Directions API** for your project and create an API key.
2. Put the key in `app/src/main/AndroidManifest.xml` under the `com.google.android.geo.API_KEY` meta-data.
3. The route request in `MapsFragment` (`drawRoute`) also contains a key in its URL. Replace it with your own key.

Restrict your key to your app's package name and SHA-1 fingerprint, and to the APIs listed above, so it cannot be abused if the code is shared.

### 3. Build and run

Open the project in Android Studio, let Gradle sync, and run the `app` configuration on a device or emulator. From the command line:

```bash
./gradlew assembleDebug
```

### Permissions

| Permission | Why |
|---|---|
| `INTERNET` | Firebase and Google APIs |
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Show your position and start the route from it |
| `POST_NOTIFICATIONS` | Appointment reminder notifications (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Fire the reminder at the exact time |

## Known limitations

- API keys and the database URL are hardcoded in the source and should be moved to `local.properties` or the Secrets Gradle plugin before publishing the app.
- The reminder is currently set to fire 10 seconds before the appointment time, which is handy for demos. Change the offset in `ReminderFragment` for real use.
- Reminders use exact alarms, which can be cancelled by the system after a device restart, because there is no boot receiver.
- A local SQLite helper exists, but the app currently reads and writes its data through Firebase.

## Author

Muhammad Syazwan Syahmi Bin Mat Zaidi. [GitHub](https://github.com/syzwanzaidi) | [Portfolio](https://syzwanzaidi-portfolio.vercel.app/)
