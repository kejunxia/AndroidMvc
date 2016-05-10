# AndroidMvc Framework
## Introduction
First of all, let's look at some problems of the Android development below:
- **No enforced design pattern**. That's why there are many questions online asking about how to implement MVC, MVVM, MVP and etc patterns to Android.
- **Hard to do unit testing** for Android since the tight coupling of Android components. For example most Android components are heavily depending on "android.Content.Context". Mocking Android components would lead to "Error java.lang.RuntimeException: Stub!".

  Though there is a great Android test tool - Robolectric makes it much easier, it only shadows core parts of Android framework because there are too many components there. Another issue sometimes Android frameworks got some bugs and even worse only on specific versions of Android SDK or support library. In this case, pass of test with Roboletric doesn't necessarily guarantee the end to end behavior is correct. For an instance, this bug [Nested Fragment doesn't retain instance state as expected since support library v4.rev 20](https://code.google.com/p/android/issues/detail?id=74222) is a serious issue Android team has not targeted for ages. With this bug, if we got a view in a nested fragment, Roboletric may think we can see this view with specific logic. But this view may not show up on real devices.

  Furthermore, if the purpose is to test controller or business logic we don't have to use real or even shadowed Android functions. For example, if we are developing a calculator and we can wrap core math functions in a calculator controller. To test the calculator controller, should the controller care about if the view is Android, a mock, HTML or even iOS? No, the controller itself doesn't need to have anything related to Android. We just want to test if we give 1+1 to the controller as input does it return 2. See the samples below or in the github code to see how AndroidMvc abstract Android components out from controllers.


- **Flawed lifecycle of Activity/Fragment**. Take a news app as an example. Think about this scenario, when the app resumes from background and needs to call services to get latest content to refresh the page. Which lifecycle callback should the refresh logic sit in? onResume? OK, the page would be refreshed on each rotation as well which is definitely NOT what we want. This is just one example of many, you might have seen more conflicting scenarios with the lifecycle that we have to write dodgy code to work around.
- **Tedious to manage app instance state**. App is likely to crash when relaunch an activity that has been killed by OS. This doesn't have to happen if all state the activity is referencing is carefully saved and restored. But it is painful as it requires a lot of boilerplate code in onSaveInstanceState and onCreate and still easy to break if anything is missing.
- **Not easy to share state during navigation.** When navigate from one activity to another, if the app needs to share data the data has to be put into Bundle. If the data is not primitive, it needs to be serialised or parceled. Furthermore, this is not fun and mistake prone because it's all key-value pair based which loses the compile time strong type check.
- **Large memory consumption with deep fragments back stack** Android doesn't call onDestroy of fragments if they are pushed into back stack and will hold the memory they used. If we have a deep fragment back stack, it will be a huge waste of memory and the worst to cause out of memory crash! So when a fragment is pushed into back stack, the instances of its holding members could be released as long as it's saved by onSaveInstanceState and restored properly when the fragment is resuming after popped out from the back stack.

**AndroidMvc framework comes to tackle the problems above and provides more**

##### AndroidMvc Features
  - Easy to apply MVC/MVVM pattern for Android development
  - Easy testing for controllers on JVM without Android dependency
  - Automatically save restore instance state
  - Improved Fragment lifecycle
    - __onViewReady(View view, Bundle savedInstanceState, Reason reason):__ Where reason differentiates the cause of creation of view: 1. Reason.isNewInstance(), 2. Reason.isFirstTime(), 3. Reason.isRestored(), 4 Reason.isRotated()
    - __onReturnForeground():__ When app resume from background
    - __onOrientationChanged(int lastOrientation, int currentOrientation):__ When app rotated
    - __onPushingToBackStack():__ When current page is pushed into back stack and navigate to next page
    - __onPoppedOutToFront():__ When last page is becomes the top page on backwards navigation
  - Manage navigation by NavigationManager which is also testable
  - Event driven views
  - [Dependency injection to make mock easy](https://github.com/kejunxia/AndroidMvc/tree/master/library/poke)
  - Optimized memory consumption. Since most data are abstracted out to models, fragments are much leaner. When fragments are pushed to back stack, AndroidMvc will release controllers the fragments hold. Therefore most of the memory used by the models of the controllers will be freed. When the fragments are popped out of the back stack, AndroidMvc will resume the models of their controllers automatically.
  - Well tested by jUnit and instrument test with Espresso.

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

## Samples
 - **[Counter](https://docs.google.com/uc?authuser=0&id=0BwcZml9gnwoZRS1pYURMMVRzdHM&export=download)** - A simple sample demonstrates how to use the framework including dependency injection, event bus, unit testing, navigation and etc.
         
   See [**Source code** here](https://github.com/kejunxia/AndroidMvc/tree/master/samples/simple) and download [**Sample APK** here](https://docs.google.com/uc?authuser=0&id=0BwcZml9gnwoZRS1pYURMMVRzdHM&export=download)
   
   
 - **[Note](https://docs.google.com/uc?authuser=0&id=0BwcZml9gnwoZOHcxZFI3Z0ZGUUk&export=download)** - A more complex sample to make notes and query weathers with slide menu and also demonstrates how consume network resources ([public weather API](http://openweathermap.org/api)) and test the async task without depending on Android SDK on pure JVM.

   See [**Source code** here](https://github.com/kejunxia/AndroidMvc/tree/master/samples/note) and download [**Sample APK** here](https://docs.google.com/uc?authuser=0&id=0BwcZml9gnwoZOHcxZFI3Z0ZGUUk&export=download)

## Overview
![AndroidMvc Layers](http://i.imgur.com/dfW8TLM.png)

#### View
All views should be **as lean as possible** because their responsibilities are only to capture user interactions and display data. Then as long as controllers are unit tested properly it's less likely to make mistake on view layer. Therefore, business logic can be maximally abstracted away from views into controllers. As a result, more business logic can be unit tested against controllers directly.

At a high level, all components of Android framework could be considered as views including activities, fragments, widgets and even services and etc, because as mentioned above, responsibilities of all Android components are just to capture user interactions and present data to users.

An analogy is that we can think Android as a browser. HTML (<!doctype html>) is like an activity, iFrame or a Ajax driven div is like a fragment and a javascript timer running as a polling loop is like a Android service. So as we can see, like what a service oriented web app does, all business logic should not be put on the front end (html/css/javascript) but in controllers on backend such as servlet, php, nodejs, asp.net and etc.

#### Controller
Controllers manage business logic including how to retrieve, calculate, format and wrap the data into event sending back to views. Controllers are defined in Java interfaces and injected into views via annotation @Inject. In this way, the controllers would be easy to mocked against the interface definition. Views subscribe to events defined by those controller interfaces. When views receive user interactions, they invoke methods against the injected controller interfaces. The underlining controller implementations will process the request by required business logic and send processed data back to views by events through **EventBusC2V** that views have subscribed on.

Note that, in this MVC design, all controllers are **SINGLETON** application wide so that the state of controllers are guaranteed from the same source of truth.

#### Model
Models in AndroidMVC design encapsulate and represent the state of views. So each controller has only one model object to represent the state of the specific business logic. When the controllers are requested to process data, they will manage the model and box and format part of or entire model into an event subscribed by view and notify the views to update themselves by the data conveyed by the event. Alternatively, the views can also directly read the model from the controllers injected into them as long as their is no much formatting requirement. But make sure, views should NOT change the value of models directly which should be only done by controllers.

In addition, to reduce boiler plate code, AndroidMvc framework will automatically save and restore the instance state of the controller models. So we don't need to always manually write code manually to use saveInstanceState and restore them in onCreate.

#### Events
With the builtin EventBus, events are defined as Java classes. It's also recommended to define them in controller interfaces to namespace them, so that we know what do the events do with more context. In addition, in a complex application with thousands of different events, the events won't be scattered everywhere. Events defined as Java classes instead of strings like Android messages has many benefits such as 1. extra data can be self-contained in them, 2. they are strong typed which avoids typo that can make debugging like a disaster, 3. strong typed events are also easier to track through inside IDEs (Android Studio, Eclipse and etc).

Once a event is defined, it can be broadcast to multiple views who subscribe to them. When events contain data, they can be thought as a partial **ViewModel** that will drive subscribed views to update themselves. So to some extent AndroidMvc could be thought as a variant of **MVVM** pattern as well.

To use it as a traditional **MVVM** or an Ajax like MVVM is totally depending on how the events are designed. For example, we can define only one event for a controller called EventC2V.OnModelUpdate and whenever the controller updates the model it raise this event. In this way, it's exactly the same as the traditional **MVVM** pattern. Also we can divide the update of model into more granular events, then it's like Ajax in web app and the earlier approach is like to refresh the whole page whenever there is a model update.

AndroidMvc is event driven. To isolate events between different layers, there are 3 event buses pre-setup in the framework:

- **EventBusC2V** (Controllers to Views): One way event bus routing events from controllers to views. Events sent to views will be guaranteed to be run on Android UI thread by the framework.
- **EventBusC2C** (Controllers to Controllers): Routes events among controllers. Events will be received on the same thread who send them.
- **EventBusV2V** (Views to views): Routes events among views. Events will be received on the same thread who send them.
- 
## Using AndroidMvc

Let's take a simple app counting number as an example. The counter app has two navigation locations:
1. LocationA: presented by FragmentA
   * One text view to display the current count in number. Updated by event OnCounterUpdated
   * Two buttons which increment and decrement count **on click**.
   * An nested fragment with a TextView to display count in English. Updated by event OnCounterUpdated too
   * An button to show advance view which results in navigating to LocationB
   * Shares the result of counting updated by LocationB
2. LocationB: presented by FragmentB
   * One text view to display the current count in number. Updated by event OnCounterUpdated
   * Two buttons which increment and decrement count **continuously on hold**.
   * Shares the result of counting updated by LocationB

Below is how to use AndroidMvc framework to implement and test the app including navigation. Note the code below doesn't show all code. To see more details check the sample project in the app - Simple under samples subfolder.

##### 1. Extend MvcActivity for the single Activity
````java
/**
 * Single activity for the app
 */
public class MainActivity extends MvcActivity {
    /**
     * Define how to map navigation location id to full screen fragments
     * @param locationId The location id in string
     * @return The class of the fragment representing the navigation locations
     */
    @Override
    protected Class<? extends MvcFragment> mapNavigationFragment(String locationId) {
        switch (locationId) {
            case "LocationA":
                return FragmentA.class;
            case "LocationB":
                return FragmentB.class;
            default:
                return null;
        }
    }

    /**
     * Define the delegate fragment for the activity
     * @return
     */
    @Override
    protected Class<? extends DelegateFragment> getDelegateFragmentClass() {
        return ContainerFragment.class;
    }

    /**
     * Container fragment extends DelegateFragment would be the root container fragments to swap
     * full screen fragments inside it on navigation.
     */
    public static class ContainerFragment extends DelegateFragment {
        /**
         * What to do when app starts for the first time
         */
        @Override
        protected void onStartUp() {
            //startApp method of appController will navigate the app to the landing page
            appController.startApp(this);
        }
    }
}
````
##### 2. Create FragmentA to present "LocationA"
````java
public class FragmentA extends MvcFragment {
    /**
     * @return Layout id used to inflate the view of this MvcFragment.
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_a;
    }
}
````
##### 3. Create a controller contract and the model it's managing
````java
package com.shipdream.lib.android.mvc.samples.simple.controller;

/**
 * Define controller contract and its events. And specify which model it manages by binding the
 * model type.
 */
public interface CounterController extends BaseController<CounterModel> {
    /**
     * Increment count and will raise {@link EventC2V.OnCounterUpdated}
     * @param sender Who requests this action
     */
    void increment(Object sender);

    /**
     * Decrement count and will raise {@link EventC2V.OnCounterUpdated}
     * @param sender Who requests this action
     */
    void decrement(Object sender);

    /**
     * Method to convert number to english
     * @param number
     * @return
     */
    String convertNumberToEnglish(int number);

    /**
     * Namespace the events for this controller by nested interface so that all its events would
     * be referenced as CounterController.EventC2V.BlaBlaEvent
     */
    interface EventC2V {
        /**
         * Event to notify views counter has been updated
         */
        class OnCounterUpdated extends BaseEventC2V {
            private final int count;
            private final String countInEnglish;
            public OnCounterUpdated(Object sender, int count, String countInEnglish) {
                super(sender);
                this.count = count;
                this.countInEnglish = countInEnglish;
            }

            public int getCount() {
                return count;
            }

            public String getCountInEnglish() {
                return countInEnglish;
            }
        }
    }
}
````
##### 3. Implement the controller
**Note that, to allow AndroidMvc to find the default implementation of injectable object, the implementation class must be under the sub-package "internal" which resides in the same parent package as the interface and the name must be [InterfaceName]Impl.** For this example, say CounterController is under package samples.simple.controller the  implementation must be named as CounterControllerImpl and placed under package samples.simple.controller.internal
````java
/**
 * Note the structure of the package name. It is in a subpackage(internal) sharing the same parent
 * package as the controller interface CounterController
 */
package com.shipdream.lib.android.mvc.samples.simple.controller.internal;

/**
 * Note the class name is [CounterController]Impl.
 */
public class CounterControllerImpl extends BaseControllerImpl<CounterModel> implements CounterController{
    /**
     * Just return the class type of the model managed by this controller
     * @return
     */
    @Override
    protected Class<CounterModel> getModelClassType() {
        return CounterModel.class;
    }

    @Override
    public void increment(Object sender) {
        int count = getModel().getCount();
        getModel().setCount(++count);
        //Post controller to view event to views
        postC2VEvent(new EventC2V.OnCounterUpdated(sender, count, convertNumberToEnglish(count)));
    }

    @Override
    public void decrement(Object sender) {
        int count = getModel().getCount();
        getModel().setCount(--count);
        //Post controller to view event to views
        postC2VEvent(new EventC2V.OnCounterUpdated(sender, count, convertNumberToEnglish(count)));
    }

    @Override
    public String convertNumberToEnglish(int number) {
        if (number < -3) {
            return "Less than negative three";
        } else  if (number == -3) {
            return "Negative three";
        } else  if (number == -2) {
            return "Negative two";
        } else  if (number == -1) {
            return "Negative one";
        } else if (number == 0) {
            return "Zero";
        } else if (number == 1) {
            return "One";
        } else if (number == 2) {
            return "Two";
        } else if (number == 3) {
            return "Three";
        } else {
            return "Greater than three";
        }
    }
}
````

##### 4. Inject Controller into Views, setup views and handle C2V events from controllers
````java
public class FragmentA extends MvcFragment {
	@Inject
    private CounterController counterController;

    private TextView display;
    private Button increment;
    private Button decrement;

    /**
     * @return Layout id used to inflate the view of this MvcFragment.
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_a;
    }

    /**
     * Lifecycle similar to onViewCreated by with more granular control with an extra argument to
     * indicate why this view is created: 1. first time created, or 2. rotated or 3. restored
     * @param view The root view of the fragment
     * @param savedInstanceState The savedInstanceState when the fragment is being recreated after
     *                           its enclosing activity is killed by OS, otherwise null including on
     *                           rotation
     * @param reason Indicates the {@link Reason} why the onViewReady is called.
     */
    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        display = (TextView) view.findViewById(R.id.fragment_a_counterDisplay);
        increment = (Button) view.findViewById(R.id.fragment_a_buttonIncrement);
        decrement = (Button) view.findViewById(R.id.fragment_a_buttonDecrement);

        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counterController.increment(v);
            }
        });

        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counterController.decrement(v);
            }
        });

        updateCountDisplay(counterController.getModel().getCount());
    }

    /**
     * Callback when the fragment is popped out by back navigation
     */
    @Override
    protected void onPoppedOutToFront() {
        super.onPoppedOutToFront();
        updateCountDisplay(counterController.getModel().getCount());
    }

    //Define event handler by method named as onEvent with single parameter of the event type
    //to respond event CounterController.EventC2V.OnCounterUpdated
    private void onEvent(CounterController.EventC2V.OnCounterUpdated event) {
        updateCountDisplay(event.getCount());
    }

    /**
     * Update the text view of count number
     * @param count The number of count
     */
    private void updateCountDisplay(int count) {
        display.setText(String.valueOf(count));
    }
}
````
##### 5. Unit tests on controllers
As discussed before, business logic should be decoupled from view(Android components) and abstracted to controllers, then we can pretty much test most logic just on controllers without dependencies of any Android components. Views just need to make sure data carried back from controllers are displayed correctly. Whether or not the data is processed correctly is completely controllers' responsibilities that is what is being tested here.

````java
public class TestCounterController {
	...other dependencies are omitted here

    private CounterController counterController;

    @Before
    public void setUp() throws Exception {
    	...other dependencies are omitted here

        //create instance of CounterController
        counterController = new CounterControllerImpl();
        counterController.init();
    }

    @Test
    public void increment_should_post_counter_update_event_with_incremented_value() {
        //1. Prepare
        //prepare event monitor
        class Monitor {
            void onEvent(CounterController.EventC2V.OnCounterUpdated event) {
            }
        }
        Monitor monitor = mock(Monitor.class);
        eventBusC2V.register(monitor);

        //mock controller model for count value
        int value = new Random().nextInt();
        CounterModel counterModel = mock(CounterModel.class);
        when(counterModel.getCount()).thenReturn(value);
        //Bind the mock model to the controller
        counterController.bindModel(this, counterModel);

        //2. Act
        counterController.increment(this);

        //3. Verify
        ArgumentCaptor<CounterController.EventC2V.OnCounterUpdated> updateEvent
                = ArgumentCaptor.forClass(CounterController.EventC2V.OnCounterUpdated.class);
        //event should be fired once
        verify(monitor, times(1)).onEvent(updateEvent.capture());
        //event should carry incremented value
        Assert.assertEquals(value + 1, updateEvent.getValue().getCount());
    }
}
````
##### 5. Navigation
Instead creating, replacing or popping full screen fragments by FragmentManager of Android Activity, AndroidMvc provides NavigationManager to manage navigation. Therefore, navigation logic can be abstracted out from View layer. To make navigation easier to be tested, we can inject NavigationManager to CounterController and then test the model of NavigationManager to verify if navigation location is changed as expected.
##### 5.1. Add two methods to CounterController to wrap navigation logic
````java
public interface CounterController extends BaseController<CounterModel> {
	... other methods

    /**
     * Navigate to LocationB by {@link NavigationManager}to show advance view that can update
     * count continuously by holding buttons.
     * @param sender
     */
    void goToAdvancedView(Object sender);

    /**
     * Navigate back to LocationA by {@link NavigationManager}to show basic view from LocationB
     * @param sender
     */
    void goBackToBasicView(Object sender);

	... other methods
}
````
##### 5.2. Inject NavigationManager to CounterControllerImpl and implement navigation methods
````java
public class CounterControllerImpl extends BaseControllerImpl<CounterModel> implements CounterController{
	... other methods

    @Inject
    NavigationManager navigationManager;

    @Override
    public void goToAdvancedView(Object sender) {
        navigationManager.navigate(sender).to("LocationB");
    }

    @Override
    public void goBackToBasicView(Object sender) {
        navigationManager.navigate(sender).back();
    }

    ... other methods
}
````
##### 5.3. Invoke CounterController methods wrapping navigation in views
````java
public class FragmentA extends MvcFragment {
	...

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        ...

        buttonShowAdvancedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Use counterController to manage navigation to make navigation testable
                counterController.goToAdvancedView(v);
                //Or we can use navigationManager directly though it's harder to unit test on
                //controller level.
                //example:
                //navigationManager.navigateTo(v, "LocationB");
            }
        });

        ...
    }

    ...
}

public class FragmentB extends MvcFragment {
	...

    @Override
    public boolean onBackButtonPressed() {
        //Use counterController to manage navigation back make navigation testable
        counterController.goBackToBasicView(this);
        //Return true to not pass the back button pressed event to upper level handler.
        return true;
        //Or we can let the fragment manage back navigation back automatically where we don't
        //override this method which will call NavigationManager.navigate(this).back()
        //automatically
    }

    ...
}
````
##### 5.4. Able to test navigation on CounterController
````java
    @Test
    public void should_navigate_to_locationB_when_go_to_advance_view_and_back_to_locationA_after_go_to_basic_view() {
        //Prepare
        NavigationManager navigationManager = ((CounterControllerImpl) counterController).navigationManager;
        NavigationManager.Model navModel = navigationManager.getModel();
        //App has not navigated to anywhere, current location should be null
        Assert.assertNull(navModel.getCurrentLocation());
        //Simulate navigating to location A
        navigationManager.navigateTo(this, "LocationA");
        //Verify: location should be changed to LocationA
        Assert.assertEquals(navModel.getCurrentLocation().getLocationId(), "LocationA");

        //Act: CounterController now goes to advanced view underlining logic is navigating to locationB
        counterController.goToAdvancedView(this);

        //Verify: Current location should be LocationB
        Assert.assertEquals(navModel.getCurrentLocation().getLocationId(), "LocationB");

        //Act: CounterController now goes back to basic view underlining logic is navigating back to locationA
        counterController.goBackToBasicView(this);

        //Verify: Current location should be back to LocationA
        Assert.assertEquals(navModel.getCurrentLocation().getLocationId(), "LocationA");
    }
````

## Other features
#### 1. Dependency Injection

##### @Inject

The framework currently only support field injection. To inject an object, use @Inject to annotate fields and then inject the object with those fields by
````java
AndroidMvc.graph().inject(ObjectToBeInjected)
````
All injected objects will be reference counted. This is because
the graph will automatically save and restore injected objects implementing StateManaged. For performance reasons the injected but not anymore referenced objects don't need to be saved and restored. So don't forget to call
````java
AndroidMvc.graph().release(ObjectBeenInjected)
````
to dereference them. Fortunately, all MvcFragment will do the injection and releasing in their
Android lifecycle - onCreate and onDestroy. So we don't need to do this manually for fragments.

**But why do we release? Isn't Java managing garbage collection automatically?

Yes java does it. But since all controllers of AndroidMvc are singleton to assure the single source of truth, if there are used by multiple consumers, the shared instance of the controller will be held by a cache. And the model of the controller will be automatically saved and restored on demand of recreation and destroy of fragments. So if a controller is not used anymore, we can dereference the controller. Then AndroidMvc will stop managing its model.

In addition, instances of fragments in their back stack will be held by OS until they are killed. This holds up a lot memory if the back stack is deep. Since those fragments are not visible, there is no point to hold the data they are referencing any longer. To release reference of controllers will help AndroidMvc be aware which controllers are not used anymore, thereafter AndroidMvc can free up the memory holding their models. What about if those fragments want to resume by popping out from the back stack? As mentioned before, AndroidMvc will restore the state/model of the controllers the fragments reference which are saved when the fragments are pushed into the back stack.**

#### [More details about dependency injection with Poke, see its documentation here](https://github.com/kejunxia/AndroidMvc/tree/master/library/poke)


### 2. Unit testing on asynchronous actions, e.g. Http requests
Below is an example to consume a public weather API from [OpenWeatherMap](http://openweathermap.org/api). To be able to test controller without real http communication, the http request can be abstracted into a service interface. The service interface is injected into controllers. Then in real implementation of the service interface we send http request by http client while in controller testings we mock the service to provide mock data.

See more details in the sample project - Node

**Note that, BaseMvcControllerImpl provides protected methods to run actions asynchronously. The ExecutorService is injected into controllers. By default, AndroidMvc framework automatically injects with an implementation running tasks on non-main thread. Whereas in unit tests we can override the injection with an implementation runs the task on the same thread as the caller's so that the asynchronous actions can be tested easier.**
````java
/**
 * Run async task on the default ExecutorService injected as a field of this class. Exceptions
 * occur during running the task will be handled by the given {@link AsyncExceptionHandler}.
 *
 * @param sender                who initiated this task
 * @param asyncTask             task to execute
 * @param asyncExceptionHandler error handler for the exception during running the task
 * @return the reference of {@link AsyncTask} that can be used to query its state and cancel it.
 */
protected AsyncTask runAsyncTask(Object sender, AsyncTask asyncTask, AsyncExceptionHandler asyncExceptionHandler)
````

##### 1. Define http service interface
````java
package com.shipdream.lib.android.mvc.samples.note.service.http;

public interface WeatherService {
    /**
     * Get weathers of the cities with the given ids
     * @param ids Ids of the cities
     * @return The response
     */
    WeatherListResponse getWeathers(List<Integer>ids) throws IOException;
}
````
##### 2. Send and consume real http service in implementation
````java
/**
 * Note the package structure which is under internal subpackage sharing the same parent package as
 * WeatherService as above
 */
package com.shipdream.lib.android.mvc.samples.note.service.http.internal;

public class WeatherServiceImpl implements WeatherService{
    private HttpClient httpClient;
    private Gson gson;

    public WeatherServiceImpl() {
        httpClient = new DefaultHttpClient();
        gson = new Gson();
    }

    @Override
    public WeatherListResponse getWeathers(List<Integer> ids) throws IOException {
        String idsStr = "";
        for (Integer id : ids) {
            if (!idsStr.isEmpty()) {
                idsStr += ", ";
            }
            idsStr += String.valueOf(id);
        }
        String url = String.format("http://api.openweathermap.org/data/2.5/group?id=%s&units=metric",
                URLEncoder.encode(idsStr, "UTF-8"));
        HttpGet get = new HttpGet(url);
        HttpResponse resp = httpClient.execute(get);
        String responseStr = EntityUtils.toString(resp.getEntity());
        return gson.fromJson(responseStr, WeatherListResponse.class);
    }
}
````
##### 3. Inject the http service into WeatherControllerImpl
````java
public class WeatherControllerImpl extends BaseControllerImpl <WeatherModel> implements
        WeatherController{
	....

    @Inject
    private WeatherService weatherService;

    //consume the service and fetch weathers
    //...
}
````
##### 4. In controller unit test, override injection of ExecutorService with implementation running actions on the same thread as the caller's
Code below is partial implementation, see sample Note in the project for more details.
````java
public class TestWeatherController extends TestControllerBase<WeatherController> {
@Override
protected void registerDependencies(MvcGraph mvcGraph) {
	...

	//Setup mock executor service mock that runs task on the same thread.
	executorService = mock(ExecutorService.class);
	doAnswer(new Answer() {
		@Override
		public Object answer(InvocationOnMock invocation) throws Throwable {
			Runnable runnable = (Runnable) invocation.getArguments()[0];
			runnable.run();
			return null;
		}
	}).when(executorService).submit(any(Runnable.class));

    //Register the injecting component to mvcGraph to override the implementation being injected
    //to controllers
	TestComp testComp = new TestComp();
	testComp.testNoteController = this;
	mvcGraph.register(testComp);
}
}
````

##### 5. Test if WeatherController sends successful event with good http response
What to mock
1. Http Service to provide good response
2. Event monitor to subscribe to the successful event
So when WeatherController#updateAllCities(Object) is called, we can verify whether the mocked monitor receives the successful event.

````java
@Test
public void shouldRaiseSuccessEventForGoodUpdateWeathers() throws IOException {
	//---Arrange---
	//Define a subscriber class
	class Monitor {
		public void onEvent(WeatherController.EventC2V.OnWeathersUpdated event) {
		}
		public void onEvent(WeatherController.EventC2V.OnWeathersUpdateFailed event) {
		}
	}
	Monitor monitor = mock(Monitor.class);
    //Subscribe to eventBus
	eventBusC2V.register(monitor);

    //Weather service mock prepares a good response
	WeatherListResponse responseMock = mock(WeatherListResponse.class);
	when(weatherServiceMock.getWeathers(any(List.class))).thenReturn(responseMock);

	//---Action---
	controllerToTest.updateAllCities(this);

	//---Verify---
	//Success event should be raised
	ArgumentCaptor<WeatherController.EventC2V.OnWeathersUpdated> eventSuccess
			= ArgumentCaptor.forClass(WeatherController.EventC2V.OnWeathersUpdated.class);
	verify(monitor, times(1)).onEvent(eventSuccess.capture());
	//Failed event should not be raised
	ArgumentCaptor<WeatherController.EventC2V.OnWeathersUpdateFailed> eventFailure
			= ArgumentCaptor.forClass(WeatherController.EventC2V.OnWeathersUpdateFailed.class);
	verify(monitor, times(0)).onEvent(eventFailure.capture());
}
````

##### 6. **Test if WeatherController sends failed event with bad http response**
What to mock
1. Http Service to provide bad response
2. Event monitor to subscribe to the failed event
So when WeatherController#updateAllCities(Object) is called, we can verify whether the mocked monitor receives the failed event.
````java
@Test
public void shouldRaiseFailEventForNetworkErrorToUpdateWeathers() throws IOException {
	//---Arrange---
	//Define a subscriber class
	class Monitor {
		public void onEvent(WeatherController.EventC2V.OnWeathersUpdated event) {
		}
		public void onEvent(WeatherController.EventC2V.OnWeathersUpdateFailed event) {
		}
	}
	Monitor monitor = mock(Monitor.class);
    //Subscribe to eventBus
	eventBusC2V.register(monitor);

	//Weather service mock prepares a bad response
    //by throwing an exception when getting the weather data
	when(weatherServiceMock.getWeathers(any(List.class))).thenThrow(new IOException());

	//---Action---
	controllerToTest.updateAllCities(this);

	//---Verify---
	//Success event should not be raised
	ArgumentCaptor<WeatherController.EventC2V.OnWeathersUpdated> eventSuccess
			= ArgumentCaptor.forClass(WeatherController.EventC2V.OnWeathersUpdated.class);
	verify(monitor, times(0)).onEvent(eventSuccess.capture());
	//Failed event must be raised
	ArgumentCaptor<WeatherController.EventC2V.OnWeathersUpdateFailed> eventFailure
			= ArgumentCaptor.forClass(WeatherController.EventC2V.OnWeathersUpdateFailed.class);
	verify(monitor, times(1)).onEvent(eventFailure.capture());
}
````

### 3. Custom mechanism to automatically save/restore models of controllers
By default, AndroidMvc uses GSON to serialize and deserialize models of controllers automatically. In general uses the performance is acceptable. For example, on rotation, as long as the models are not very large, the frozen time of the rotation would be between 200ms and 300ms.

If we need to provide more optimized mechanism to do so in case there are large models taking long to be serialized and deserialized by GSON, custom StateKeeper can be set to provide alternative save/restore implementation. For Android, Parcelable is the best performed mechanism to save/restore state but it is not fun and error prone. Fortunately, there a handy library [Parceler](https://github.com/johncarl81/parceler) from another developer does this automatically. In the example below, we tried this library to implement custom StateKeeper to save/restore state by Parcelables automatically. The best place to set the custom StateKeeper is the Application#onCreate().

Check out more details in the sample code - Note

````java
public class NoteApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AndroidMvc.setCustomStateKeeper(new AndroidStateKeeper() {
            @SuppressWarnings("unchecked")
            @Override
            public Parcelable saveState(Object state, Class type) {
                /**
                 * Use parcelable to save all states.
                 */
                return Parcels.wrap(state);
                //type of the state can be used as a filter to handle some state specially
                //if (type == BlaBlaType) {
                //    special logic to save state
                //}
            }

            @SuppressWarnings("unchecked")
            @Override
            public Object getState(Parcelable parceledState, Class type) {
                /**
                 * Use parcelable to restore all states.
                 */
                return Parcels.unwrap(parceledState);

                //type of the state can be used as a filter to handle some state specially
                //if (type == BlaBlaType) {
                //    special logic to restore state
                //}
            }
        });
    }
}
````
