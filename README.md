# AndroidMvc Framework

[![Build Status](https://travis-ci.org/kejunxia/AndroidMvc.svg?branch=ci-travis)](https://travis-ci.org/kejunxia/AndroidMvc)
[![Coverage Status](https://coveralls.io/repos/kejunxia/AndroidMvc/badge.svg)](https://coveralls.io/r/kejunxia/AndroidMvc)
[![jCenter](https://api.bintray.com/packages/kejunxia/maven/android-mvc/images/download.svg)](https://bintray.com/kejunxia/maven/android-mvc/_latestVersion)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-AndroidMvc-green.svg?style=true)](https://android-arsenal.com/details/1/4098)

## Features

  - Easy to implement MVC/MVP/MVVM pattern for Android development
  - Enhanced Android life cycles - e.g. a view needs to refresh when being brought back to foreground but not on rotation, onResume() is not specific enough to differentiate the two scenarios. Android mvc framework provides more granular life cycles
  - All fragment life cycles are mapped into controllers thus logic in life cycles are testable on JVM
  - Easy navigation between pages. Navigation is done in controllers instead of views so navigation can be unit tested on JVM
  - Easy unit test on JVM since controllers don't depend on any Android APIs
  - Built in event bus. Event bus also automatically guarantees post event view events on the UI thread
  - Automatically save and restore instance state. You don't have to touch onSaveInstance and onCreate(savedInstanceState) with countless key-value pairs, it's all managed by the framework.
  - [Dependency injection with Poke to make mock easy](https://github.com/kejunxia/AndroidMvc/tree/master/library/poke)
  - Well tested - non-Android components are tested as the test coverage shown above (over 90%). For Android dependent module "android-mvc", it's tested by real emulator with [this UI test module](https://github.com/kejunxia/AndroidMvc/tree/master/library/android-mvc-test), even with  "Don't Keep Activities" turned on in dev options to guarantee your app doesn't crash due to loss of instance state after it's killed by OS in the background!

## More details on 

[Website](http://kejunxia.github.io/AndroidMvc)

## Code quick glance

Let's take a quick glance how to use the framework to **navigate** between screens first, more details will be discussed later.

The sample code can be found in [Here](https://github.com/kejunxia/AndroidMvc/tree/master/samples/simple-mvc). 
It is a simple counter app that has a master page and detail page. This 2 pages are represented by two fragments

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
buttonGoToDetailScreen.setOnClickListener(
    new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Use counterController to manage navigation to make navigation testable
            controller.goToDetailView(v);
        }
    });
    
```

If you use Butterknife, the code can be shorten as below. Also you can use Android Data Binding library to shorten the code similarly
```java
@OnClick(R.id.fragment_master_buttonShowDetailScreen)
void goToDetailPage(View v) {
    controller.goToDetailScreen(v);
}
```

In CounterDetailScreen
```java
@Override
public void update() {
    /**
     * Controller will call update() whenever the controller thinks the state of the screen
     * changes. So just bind the state of the controller to this screen then the screen is always
     * reflecting the latest state/model of the controller. This is a simple solution but works for most cases.
     * This solution can be thought as refreshing the whole web page in a browser. If you want more granular 
     * control like ajax to update partial page, define more callbacks in View for MVP pattern and events for MVVM 
     * pattern and call them in the controller when needed.
     */
    display.setText(controller.getCount());
}
```

#### Unit test

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

- **View**: Mainly fragments that bind user interactions to controllers such as tap, long press and etc. 
Views reflect the model managed by their controllers.
- **Controller**: Controllers expose methods to its view peer to capture user inputs. Once the state 
of the controller changes, the controller needs to notify its view the update. Usually by calling 
view.update() or post an event to its view.
- **Model**: Represents the state of view and managed by the controller. It can be accessed by 
controller.getModel(). But only read it to bind the model to views but don't modify the model from 
views. Modification of model should only be done by controller.
- **Manager**: What about controllers have shared logic? Break shared code out into managers. 
If managers need to access data. Inject services into managers. Managers can be thought as partial 
controllers serve multiple views through the controllers depending on them.
- **Service**: Services are below controller used to access data such as SharedPreferences, 
database, cloud API, files and etc. It provides abstraction for controllers or managers that can be 
easily mocked in unit tests for controllers. They the data access layer can be replaced quickly. 
For example, when some resources are removed from local data to remote data, just simply replace 
the services implementation to access web api instead of database or sharedPreferences.

See the illustration below

![AndroidMvc Layers](http://i.imgur.com/dfW8TLM.png)

## Download
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

## More details on 

[Website](http://kejunxia.github.io/AndroidMvc)
