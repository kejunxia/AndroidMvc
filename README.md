# AndroidMvc Framework
[![Build Status](https://travis-ci.org/kejunxia/AndroidMvc.svg)](https://travis-ci.org/kejunxia/AndroidMvc)
[![Coverage Status](https://coveralls.io/repos/kejunxia/AndroidMvc/badge.svg)](https://coveralls.io/r/kejunxia/AndroidMvc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.shipdream/android-mvc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.shipdream/android-mvc)

#Website
[http://kejunxia.github.io/AndroidMvc](http://kejunxia.github.io/AndroidMvc)

## Features
  - Easy to apply MVC/MVVM pattern for Android development
  - Event driven
  - Easy testing for controllers on JVM without Android dependency
  - Dependency injection to make mock easy
  - Manage navigation by NavigationController which is also testable
  - Improved Fragment life cycles - e.g. Differentiate why view is created: 1. __NewlyCreated__, 2. __Rotated__ or 3. __StateRestored__
  - Automatically save restore instance state

## Samples APKs
 - **[Counter](https://docs.google.com/uc?authuser=0&id=0BwcZml9gnwoZRS1pYURMMVRzdHM&export=download)** - A simple sample demonstrates how to use the framework including dependency injection, event bus, unit testing, navigation and etc.
 - **[Note](https://docs.google.com/uc?authuser=0&id=0BwcZml9gnwoZOHcxZFI3Z0ZGUUk&export=download)** - A more complex sample to make notes and query weathers with slide menu and also demonstrates how consume network resources ([public weather API](http://openweathermap.org/api)) and test the async task without depending on Android SDK on pure JVM.
## Download
The library is currently release to jCenter and MavenCentral

**Maven:**
```xml
<dependency>
    <groupId>com.shipdream</groupId>
    <artifactId>android-mvc</artifactId>
    <version>1.0</version>
</dependency>
```

**Gradle:**
```groovy
compile "com.shipdream:android-mvc:1.0"
```
