# AndroidMvc Framework
See details on website - [http://kejunxia.github.io/AndroidMvc](http://kejunxia.github.io/AndroidMvc)

## Features
  - Easy to apply MVC/MVVM pattern for Android development
  - Event driven
  - Easy testing for controllers on JVM without Android dependency
  - Dependency injection to make mock easy
  - Manage navigation by NavigationController which is also testable
  - Improved Fragment life cycles - e.g. Differentiate why view is created: 1. __NewlyCreated__, 2. __Rotated__ or 3. __StateRestored__
  - Automatically save restore instance state


## Download
The library is currently release to jCenter and MavenCentral

Gradle dependency is 
```groovy
compile "com.shipdream:android-mvc:1.0"
```

## Samples APKs
 - [Counter](hhttps://github.com/kejunxia/AndroidMvc/blob/master/documents/apks/samples/simple-counter.apk) - A simple sample demonstrates how to use the framework including dependency injection, event bus, unit testing, navigation and etc.
 - [Note](https://github.com/kejunxia/AndroidMvc/blob/master/documents/apks/samples/notes.apk) - Another more complex sample also demonstrates how to use async tasks to get network resources and test the async task without Android SDK on pure JVM.