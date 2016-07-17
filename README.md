# AndroidMvc Framework
[![Build Status](https://travis-ci.org/kejunxia/AndroidMvc.svg?branch=ci-travis)](https://travis-ci.org/kejunxia/AndroidMvc)
[![Coverage Status](https://coveralls.io/repos/kejunxia/AndroidMvc/badge.svg)](https://coveralls.io/r/kejunxia/AndroidMvc)
[![jCenter](https://api.bintray.com/packages/kejunxia/maven/android-mvc/images/download.svg)](https://bintray.com/kejunxia/maven/android-mvc/_latestVersion)

## Features
---

  - Easy to implement MVC/MVP/MVVM pattern for Android development
  - Wrap all business logic in controllers **including Android lifecycles**
  - Easy unit test on JVM since controllers don't depend on any Android APIs
  - Easy navigation between pages. Navigation is done in controllers instead of views so navigation can be unit tested on JVM
  - All framgents lifecyles are mapped into controllers thus the lifecycles are testable on JVM
  - Built in event bus. Event bus automatically gurantees post event view events on the UI thread
  - Automatically save and restore instance state. You don't have to touch onSaveInstance and onCreate(savedInstanceState) with countless key-value pairs, it's all managed by the framework.
  - [Dependency injection with Poke to make mock easy](https://github.com/kejunxia/AndroidMvc/tree/master/library/poke)


## Code quick glance
---
Let's take a quick glance how to use the framework to **navigate** between screens first, more details will be discussed later.

The sample code can be found in [Here](https://github.com/kejunxia/AndroidMvc/tree/master/samples/simple-mvc). It is a simple counter app that has a master page and detail page. This 2 pages are represented by two fragments

- CounterMasterScreen paired with CounterMasterController
- CounterDetailScreen paired with CounterDetailController

#### Controller
In CounterMasterController, to navigate simply call
```java
    public void goToDetailView(Object sender) {
        //Navigate to CounterDetailController which is paired by CounterDetailScreen
        navigationManager.navigate(sender).to(CounterDetailController.class);
    }
```

#### View
In CounterMasterScreen call the navigation method wrapped by the controller
```java
buttonGoToDetailScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Use counterController to manage navigation to make navigation testable
                controller.goToDetailView(v);
            }
        });
```

In CounterDetailScreen
```java
@Override
public void update() {
    /**
     * Controller will call update() whenever the controller thinks the state of the screen
     * changes. So just bind the state of the controller to this screen then the screen is always
     * reflecting the latest state/model of the controller
     */
    display.setText(controller.getCount());
}
```

## Unit test
---
```java
//Act: navigate to MasterScreen
navigationManager.navigate(this).to(CounterMasterController.class);

//Verify: location should be changed to MasterScreen
Assert.assertEquals(CounterMasterController.class.getName(),
        navigationManager.getModel().getCurrentLocation().getLocationId());

//Act: navigate to DetailScreen
controller.goToDetailScreen(this);

//Verify: Current location should be at the view paired with CounterDetailController
Assert.assertEquals(CounterDetailController.class.getName(),
        navigationManager.getModel().getCurrentLocation().getLocationId());
```


## Division between Views and Controllers
---
- **View**: Mainly fragments that bind user interactions to controllers such as tap, long press and etc. Views reflect the model managed by their controllers.
- **Controller**: Controllers expose methods to its view peer to caputure user inputs. Once the state of the controller changes, the controller needs to notify its view the update. Usually by calling view.update() or post an event to its view.
- **Model**: Represents the state of view and managed by the controller. It can be accessed by controller.getModel(). But only read it to bind the model to views but don't modify the model from views. Modification of model should only be done by controller.
- **Manager**: What about controllers have shared logic? Break shared code out into managers. If managers need to access data. Inject services into managers. Managers can be thought as partial controllers serve multile views through the controllers depending on them.
- **Service**: Services are below controller used to access data such as SharedPreferences, database, cloud API, files and etc. It provides abstraction for controllers or managers that can be eaisly mocked in unit tests for controllers. They the data access layer can be replaced quickly. For example, when some resources are removed from local data to remote data, just simply replace the services implementation to access web api instead of database or sharedPreferences.

See the illustration below

![AndroidMvc Layers](http://i.imgur.com/dfW8TLM.png)

## How to use
---
To enfore you don't write Android dependent functions into controllers to make unit tests harder, you can separate your Android project into 2 modules:
- **app**: View layer - a lean module depending on Android API only bind model to Android UI and pass user interactions to core module. This module should include lib **"android-mvc"** explained in download section below. This module includes:
  - Activties, Fragments, Views, Android Services and anything as views depending on Android API
  - Implementations of abstract contract defined in core module that depending on Android API. For example, a SharedPreferenceImpl that depends on Android context object.
- **core**: Controller layer - also includes model, managers and services. It's a module doesn't have any Android dependency so can be tested straight away on JVM. This module should include lib **"android-mvc-core"** explained in download section below. This module includes:
  - Controllers
  - Models
  - Managers - shared by controllers
  - Data services. When a service needs Android API it can be defined as an interface and implemented in app module. For example, define an interface SharedPreference to save and get data from Android preference. So in core module, the interface can be easily to be mocked for controllers or managers to provide mocked shared preference in unit tests.

However, separating the android project into two modules as above is not necessary. They for sure can be merged into one module and just depend on lib **"android-mvc"**, which has already included **"android-mvc-core"**. But in this way, you may accidentally write android dependent functions into controller to make mocking harder in controller unit tests.

See the chart below as an example of how to seperate the modules. Also check out the **[Sample Code](https://github.com/kejunxia/AndroidMvc/tree/SampleWithInterimFragment/samples/simple)**
![Project structure](http://i.imgur.com/Nx1vtyz.png)

## Download
---
Here is the the latest version number in jCenter

[![Download](https://api.bintray.com/packages/kejunxia/maven/android-mvc/images/download.svg)](https://bintray.com/kejunxia/maven/android-mvc/_latestVersion)

**Maven:**
- lib **android-mvc**
    ```xml
    <dependency>
        <groupId>com.shipdream</groupId>
        <artifactId>android-mvc</artifactId>
        <version>[LatestVersion]</version>
    </dependency>
    ```
- lib **android-mvc-core**
    ```xml
    <dependency>
        <groupId>com.shipdream</groupId>
        <artifactId>android-mvc-core</artifactId>
        <version>[LatestVersion]</version>
    </dependency>
    ```

**Gradle:**
- lib **android-mvc**
    ```groovy
    compile "com.shipdream:android-mvc:[LatestVersion]"
    ```
- lib **android-mvc-core**
    ```groovy
    compile "com.shipdream:android-mvc-core:[LatestVersion]"
    ```
