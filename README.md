Echo
====

Time travelling recorder for Android.
It is free/libre and gratis software.

Download
---

* [F-Droid](https://f-droid.org/repository/browse/?fdid=eu.mrogalski.saidit)

Building
---

1. Install gradle-1.10 (version is important)
1. Install SDK platform API 21 and 21.0.2 build-tools, with either [sdkmanager](https://developer.android.com/studio/command-line/sdkmanager) or [Android Studio](https://developer.android.com/studio)
1. Create a Key Store - [Instructions](http://stackoverflow.com/questions/3997748/how-can-i-create-a-keystore)
1. Fill Key Store details in `SaidIt/build.gradle`
1. From this directory run `gradle installDebug` - to install it on a phone or `gradle assembleRelease` - to generate signed APK

If you had any issues and fixed them, please correct these instructions in your fork!
