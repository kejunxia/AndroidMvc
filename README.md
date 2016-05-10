# AndroidMvc Framework
[![Build Status](https://travis-ci.org/kejunxia/AndroidMvc.svg?branch=ci-travis)](https://travis-ci.org/kejunxia/AndroidMvc)
[![Coverage Status](https://coveralls.io/repos/kejunxia/AndroidMvc/badge.svg)](https://coveralls.io/r/kejunxia/AndroidMvc)
[![Download](https://api.bintray.com/packages/kejunxia/maven/android-mvc/images/download.svg)](https://bintray.com/kejunxia/maven/android-mvc/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.shipdream/android-mvc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.shipdream/android-mvc)

## Overview
- **View**: Fragments that to caputre user interaction and reflect view model managed by controller. More broadyly it can be anything included in Android SDK including notification, activity, services etc that users can see and touch.
- **Controller**: The abstraction of a view/fragment. Define methods to be consumered by the view and then fire events back to the view to notify it update its UI. So views and controllers are one to one relation. Every view has and only has one controller.
- **Model**: Represents the state of view. Can be thought as ViewModel in MVVM pattern. Each controller has a model. So model-controller is one to one relation so does model-controller-view.
- **Service**: Data access layer below controller. Provides interface for controllers/managers to access data from database/clound api/sharedPreferences etc. It hides details of data tansportation so it can be easiy swapped by different data source as well as easily mocked in unit tests.
- **Manager**: What about controllers have shared logic? Break shared code out into managers. If managers need to access data. Inject services into managers.

See the illustration below

![AndroidMvc Layers](http://i.imgur.com/PsA2VG0.png)

## Features
  - Easy to apply MVC/MVVM pattern for Android development
  - Event driven
  - Easy testing for controllers running directly on JVM without Android dependency
  - [Dependency injection with Poke to make mock easy](https://github.com/kejunxia/AndroidMvc/tree/master/library/poke)
  - Manage navigation by NavigationController which is also testable
  - Improved Fragment life cycles - e.g. Differentiate why view is created: 1. Reason.isNewInstance(), 2. Reason.isFirstTime(), 3. Reason.isRestored(), 4 Reason.isRotated()
  - Automatically save restore instance state

## Samples
 - **[Counter](https://docs.google.com/uc?authuser=0&id=0BwcZml9gnwoZRS1pYURMMVRzdHM&export=download)** - A simple sample demonstrates how to use the framework including dependency injection, event bus, unit testing, navigation and etc.
         
   See [**Source code** here](https://github.com/kejunxia/AndroidMvc/tree/master/samples/simple) and download [**Sample APK** here](https://docs.google.com/uc?authuser=0&id=0BwcZml9gnwoZRS1pYURMMVRzdHM&export=download)
   
   
 - **[Note](https://docs.google.com/uc?authuser=0&id=0BwcZml9gnwoZOHcxZFI3Z0ZGUUk&export=download)** - A more complex sample to make notes and query weathers with slide menu and also demonstrates how consume network resources ([public weather API](http://openweathermap.org/api)) and test the async task without depending on Android SDK on pure JVM.

   See [**Source code** here](https://github.com/kejunxia/AndroidMvc/tree/master/samples/note) and download [**Sample APK** here](https://docs.google.com/uc?authuser=0&id=0BwcZml9gnwoZOHcxZFI3Z0ZGUUk&export=download)

## Download
The library is currently released to both
* jCenter [![Download](https://api.bintray.com/packages/kejunxia/maven/android-mvc/images/download.svg)](https://bintray.com/kejunxia/maven/android-mvc/_latestVersion)
* Maven Central [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.shipdream/android-mvc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.shipdream/android-mvc)

**Maven:**
```xml
<dependency>
    <groupId>com.shipdream</groupId>
    <artifactId>android-mvc</artifactId>
    <version>[LatestVersion]</version>
</dependency>
```

**Gradle:**
```groovy
compile "com.shipdream:android-mvc:[LatestVersion]"
```

## Dependency injection with reference count
[See the documentation of Poke](https://github.com/kejunxia/AndroidMvc/tree/master/library/poke)
