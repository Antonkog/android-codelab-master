# android-codelab
Android project that serves as a base for code challenges implemented by applicants.
The base is written in kotlin. 

# WARNING
You need to set your Google Maps API key for the app to work.  
Set the environment variable `MAPS_API_KEY` to the key you received via email, because the project uses the Google Secret plugin.

# Versions

1. **XML-only version**
    - Implemented first.
    - Available on the `xml_view` branch.

2. **XML + Compose hybrid version**
    - Combines XML views and Compose.
    - Available on the `xml_and_compose` branch.
    - To switch between implementations, replace `ComposeActivity` with `MainActivity` in the manifest.

3. **Compose-only version**
    - Fully rewritten using Jetpack Compose.
    - Available on the `compose` branch.

## Implementation Notes

1. **Initial approach: Geofence API**
    - Used the Android **Geofence API** to minimize battery usage and allow notifications even when the app process is not running.
    - Tested on several devices:
        - Worked correctly on Google Pixel (API 34).
        - On some devices, the `BroadcastReceiver` was never triggered.
    - According to [Google’s documentation](https://developer.android.com/training/location/geofencing), geofence notifications are **not guaranteed to be delivered in time**.
        - Example: when moving quickly in a car, the notification may arrive late (after the user has already passed the location).

2. **Transition to Foreground Service**
    - To improve reliability, introduced a **foreground service** that uses the **Fused Location Provider** for continuous location updates.
    - This ensures more timely notifications at the cost of slightly higher battery usage.

3. **Hybrid Approach**
    - The `GeofenceBroadcastReceiver` now checks whether the `LocationService` is running:
        - If the service **is running** → the broadcast is ignored (avoids duplicate notifications).
        - If the service **is not running** → the broadcast triggers the notification directly.

### Components
- **Broadcast Receiver** → [`GeofenceBroadcastReceiver.kt`](app/src/main/java/com/sap/codelab/main/GeofenceBroadcastReceiver.kt)
- **Location Service** → [`LocationService.kt`](app/src/main/java/com/sap/codelab/main/LocationService.kt)
- **Geofence Manager** → [`GeoFenceManager.kt`](app/src/main/java/com/sap/codelab/utils/geofence/GeoFenceManager.kt)

---

## Issues Found

- **Package structure**
    - Data-related classes were placed inside the repository, which should instead contain only business logic, repository interfaces, and models.
    - The original structure did not support reusability if the project were to be split into multiple modules.
    - Refactored into a **feature-based structure**: `create`, `detail`, `home`, and `core` packages.

- **Dependency management**
    - According to current best practices, a **versions catalog** (`libs.versions.toml`) should be used.

- **Repository design**
    - The repository class was coupled with database creation, which makes testing difficult.
    - It was implemented as a global singleton, hiding lifecycle concerns (e.g., closing the DB, handling multiple processes).
    - Refactored with **Dependency Injection**:
        - Implemented using **Koin** (sufficient for a small project).
        - **Hilt** was considered but not adopted to avoid additional complexity.

- **Threading**
    - `@WorkerThread` annotation in the repository does not enforce threading at runtime.
    - Replaced with **suspend functions**.
    - Room database operations (insert, update, delete, query) are blocking by default, which can cause ANRs if run on the main thread.

- **Scopes**
    - `ScopeProvider.application` makes sense only for background work that must outlive the `ViewModel` or `Activity`.
    - Using it directly in a `ViewModel` can lead to inconsistent state if a user leaves the screen before the operation completes.
    - Replaced with **ViewModel scope**.

- **Dispatchers**
    - `Dispatchers.Default` was incorrectly used for I/O operations (DB access).
    - Replaced with `Dispatchers.IO`.

- **Data models**
    - `Memo` entity was renamed to `MemoEntity`.
    - Introduced a separate domain model `Memo` and added **mappers** (`MemoMappers`).

- **Update behavior**
    - Updating a `Memo` was overriding previous fields.
    - Added **default values** to prevent data loss.

- **Database types**
    - Latitude and longitude were stored as `Long` in the Room entity.
    - Replaced with `Double` (without migration).

- **UI fixes**
    - App bar layout was overlapping the status bar on some devices.
    - Fixed layout constraints.

- **RecyclerView optimization**
    - `MemoAdapter` improved with:
        - `DiffUtil.ItemCallback`
        - Stable IDs
        - Simplified listener interface.

- **Navigation**
    - Activities were replaced with **Fragments**.
    - Navigation implemented with **Safe Args**.
    - Navigation implemented with **Safe Args**.

---

## Unit Tests

- [RepositoryRobolectricTest.kt](app/src/test/java/com/sap/codelab/core/data/RepositoryRobolectricTest.kt)
  Verifies that the Room database correctly saves and retrieves memos.

- [MemoMappersTest.kt](app/src/test/java/com/sap/codelab/core/domain/MemoMappersTest.kt)
  Ensures that mapping between the domain model (`Memo`) and the persistence model (`MemoEntity`) works as expected.

- [GeofenceBroadcastReceiverRobolectricTest.kt](app/src/test/java/com/sap/codelab/core/GeofenceBroadcastReceiverRobolectricTest.kt)
  Verifies that the `GeofenceBroadcastReceiver` can handle incoming broadcasts and trigger the fallback notification path without crashing.

---

# Android Coding Challenges
Coding challenges are useful when the applicant does not provide a github repository or any work samples. Even if a github repository has been provided it is generally a good idea to give the applicant a task to solve and have him present his solution in a separate session. 

## General Instructions
The following instructions/conditions are valid independently of the actual coding challenge

- The code base has been tested with Android Studio Narwhal Feature Drop which is the recommended version, however feel free to try a higher version and adjust the configuration as needed
- The task should be implemented in kotlin
- Approach this task as if it was a real-world implementation - i.e. exactly how you would approach the task if you were working for a company
- 3rd party libraries may be used
- The base project for this task will be provided by us
- Once completed, please send us your solution and presents it to us, followed by a discussion about the implementation and design decisions made
- The solution can be sent as a zip file or as a publicly accessible github/gitlub etc project link
- The solution sent to us must be complete, i.e. can be opened directly via Android Studio without additional configuration

## Location Based Notifications
In this challenge the applicant has to implement location-based notifications/reminders, the following conditions are given:

- When creating a new memo, the user provides a location by selecting a point on a map (for instance: google maps or open street maps)
- The memo is then saved
- Once the user physically reaches that location, a notification should be displayed in the phone's status bar, that contains the title and the first 140 characters of the note text
- "Reaching the location" is defined as follows: The user is within 200 meters of the location he initially selected during the memo creation
- The notification should also contain an icon (the icon choice is up to you)
- The feature must also work, when the app is running in the background (or possibly not running at all)
