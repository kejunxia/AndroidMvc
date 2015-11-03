# AndroidMvc Framework
[![Build Status](https://travis-ci.org/kejunxia/AndroidMvc.svg?branch=ci-travis)](https://travis-ci.org/kejunxia/AndroidMvc)
[![Coverage Status](https://coveralls.io/repos/kejunxia/AndroidMvc/badge.svg)](https://coveralls.io/r/kejunxia/AndroidMvc)
[![Download](https://api.bintray.com/packages/kejunxia/maven/android-mvc/images/download.svg)](https://bintray.com/kejunxia/maven/android-mvc/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.shipdream/android-mvc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.shipdream/android-mvc)

# Website
[http://kejunxia.github.io/AndroidMvc](http://kejunxia.github.io/AndroidMvc)

## Features
  - Easy to apply MVC/MVVM pattern for Android development
  - Event driven
  - Easy testing for controllers on JVM without Android dependency
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