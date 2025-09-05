# android-codelab
Android project that serves as a base for code challenges implemented by applicants.
The base is written in kotlin. 

(!)If you have the NDK plugin installed, please disable it for the project, as errors may occur.

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

## Issues found:
- Package structure: data related classes was in repository, that should contain business logic, repository interface, models. 
Also the structure did not gave advantage of reusability, if we decide to separate this project into several modules.
I prefer feature based structuring, with separate core package, that each of feature can use. So divided into create, detail, home, core packages.
- Versions catalog, according to new best practices libs.versions.toml should be used.
- Repository class was coupled with DB creation, that is hard to test. Also it was global object (singleton) that hides lifecycle concerns (closing DB, multiple processes). 
DI: implemented with Koin (for small project would be enough) Also possible to make with HILT. HILT would not gave big gain in speed, but give additional complexity, that i did not wanted.
- @WorkerThread  inside Repository does not enforce threading at runtime, should be replaced with suspend call. Room database operations (insert, update, delete, query) are blocking by default, that can lead to ANR's.
- ScopeProvider.application makes sense only for background work that must outlive the ViewModel/Activity.
When you use it from viewmodel can lead to inconsistent state, when user launch operation and then before it completed want to leave the screen. Replaced with viewmodel scope.
- Dispatcher Default should be used for CPU related work not IO (saving in DB).
- Memo was renamed to MemoEntity, and separate model Memo was created in Domain.
- Update Memo was overriding previous fields, added default values.
