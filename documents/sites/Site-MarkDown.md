# AndroidMvc Framework
Android Mvc framework helps Android developers implement Android projects simpler and cleaner with MVC/MVP/MVVM patterns and make them testable.

## Features
  - [Easy to implement MVC/MVP/MVVM pattern](#-Implement-MVC/MVP/MVVM-pattern) for Android development
  - [Enhanced Android life cycles](#Life-cycles) - e.g. when view needs to refresh when being brought back to foreground but not on rotation, onResume() is not specific to differentiate the two scenarios. Android mvc framework provides more granular life cycles
  - [All fragment life cycles are mapped into FragmentController](#FragmentController-Life-cycles) thus more business logic can be moved into controllers including the ones in life cycles. Apps are more testable on JVM!
  - [Easy and clean navigation](#Navigation). Navigation is done in controllers instead of views. Thus navigation can be unit tested on JVM
  - [Run async tasks in controllers](#Run-AsyncTask-in-controller) and easy mocking of http requests
  - Easy unit test on JVM since controllers don't depend on any Android APIs
  - Built in event bus. Event bus also automatically guarantees post event view events on the UI thread
  - Automatically save and restore instance state. You don't have to touch onSaveInstance and onCreate(savedInstanceState) with countless key-value pairs, it's all managed by the framework.
  - Dependency injection with Poke to make mock easy
  - Well tested - non-Android components are tested as the test coverage status [![Coverage Status](https://coveralls.io/repos/kejunxia/AndroidMvc/badge.svg)](https://coveralls.io/r/kejunxia/AndroidMvc). For Android dependent module "android-mvc", it's tested by real emulator with [this UI test module](https://github.com/kejunxia/AndroidMvc/tree/master/library/android-mvc-test). **It's also tested with "Don't Keep Activities" turned on in dev options** to guarantee your app doesn't crash due to loss of instance state after it's killed by OS in the background!

## Implement MVC/MVP/MVVM pattern
As we know MVP and MVVM patterns are just simply derivatives of MVC pattern. All of them are targeting the same goal - Separation. 

Separating Views and Controllers allows moving more business logic away from views into controllers. This makes code cleaner and, more importantly, it makes code more testable since most logical components are not depending on specific views. In the sense of Android, it means more fuctions can be written without depending on Android API therefore not have to be tested on emulators.

Since MVC, MVP and MVVM are similar, in this framework we call both **Prestener** in MVP and **ViewModel** in MVVM just as traditionally **Controller**. 

- **Controller**: A controller is a delegate for business logic of a view. Thus controllers and views have **one-to-one** relationship. A controller provides methods to be invoked by the view to receive user's interactions such as click and long press. Once user's input is processed in controller, the state of view would be changed. And the controller needs to notify the view the change. When the controller needs to update view, it can be done differently in MVP and MVVM pattern
  - In **MVP**: controller calls the method view.update() of the view it holds to update the entire view. If the needed, the view can define more granular methods to update just a part of the entire view. For example, view.showProgressBar() or view.hideProgressBar().
  - In **MVVM**: controller post an event to view. View uses methods onEvent([EventClassType] event) to mointore the posted event. In the mothods the view update the UI accordingly.
  
- **View**: A view is an Android component that can be either Fragments, Services, Notification, Activity and etc. Every view has a controller to manage its business logic. As metioned, controllers and views have **one-to-one** relationship. Views should not process business logic but delegate all business processes to their corespondingcontrollers.
  - Note that, AndroidMvc is use a single Activity to host multiple fragments. Navgation is at fragment level and in the same activity.
- **Model**: A model represents the state of view and managed by controller. It can be accessed by controller.getModel(). The model should be reflected to Android UI in views and modified by controllers. **The model will be automatically searialized and restored in onSaveInstanceState by the framework**. So you don't need to use messy key-value pairs to save and restore view state.
- **Managers**: Managers are not necessary in MVC/MVP/MVVM model. However as metioned above, views and controllers are one-to-one mapped, when multiple views as well as their controllers share same data or logic managers are a good fit. Shared logic and data of controllers can be broken out into a manager. For example, managing logged in user is common feature of a lot apps. To have a UserManager is a perfect solution that can modify and read current users. The the manager can be used by LoginController and other controllers after login screen.
- **Services**: We are not taling about [Android Services](https://developer.android.com/guide/components/services.html). Services here are providing a layer between controller controllers and external data such as http apis, database, files, sharedPreferences and etc. Services can be injected into controllers as well as managers since managers can be thought as partial controllers.
  Services abstract out data access logic so that they can be replaced by different implementations. e.g. Use database to replace sharedPreference when the data structure become complex and query is required. In addition, the controllers can use mocked data provided by mocked services for **unit tests**.

See the diagram illustrating the relation between components above

![AndroidMvc Layers](http://i.imgur.com/dfW8TLM.png)

#### Sample code to implement MVP
- This sample shosw how a simple view just simply binds the model to the view
    ```java
    //Base view interface defined in AndroidMvc framework
    public interface UiView {
    	void update();
    }
    
    //Base controller defined in AndroidMvc framework
    public abstract class Controller<MODEL, VIEW extends UiView> extends Bean<MODEL> {
    	protected VIEW view;
        ....
    }
    
    //A concrete controller extending Controller
    public class SomeController extends Controller<SomeController.Model, UiView> {
        @Override
        public Class modelType() {
            return SomeController.class;
        }
    
    	//The model of the controller that represents the state of the view
        public static class Model {
            String title;
            public String getTitle() {
                return title;
            }
        }
    
        public void updateTitle(String text) {
            //Model is updated
            getModel().title = text;
            //Notify the view. The implementation of method update() in 
            //concrete view, the view reads the model of the controller 
            //and reflect the model to UI
            view.update();
        }
    }
    
    //View paired with the controller 
    public class SomeView implements UiView {
        private TextView title;
    
        @Inject
        private SomeController someController;
    
        @Override
        public void update() {
        	//Read the controller's model and bind it to text
            title.setText(someController.getModel().getTitle());
        }
    }
    ```
- This sample shosw how a more complicated view defines extract methods to update view partially. However, this can also be done only with binding model appoach. For example, define a flag in the model, when the flag changes the controller call view.update() which show loading UI according to the flag.
    ```java
    //Extend UiView to define granular methods to update view partially instead of binding
    //entire model to the view
    public interface AsyncView extends UiView {
        void showLoadingStatus();
        void hideLoadingStatus();
    }

    //A screen view that extends MvcFragment
    public static class LoginScreen extends MvcFragment<LoginController> implements AsyncView{
        private EditText username;
        private EditText password;
        private Button button;

        //Specify the class type of the paired controller
        @Override
        protected Class<LoginController> getControllerClass() {
            return LoginController.class;
        }

        @Override
        protected int getLayoutResId() {
            return R.id.screen_login;
        }

        //Called when the view is ready. Similar to onViewCreated but this
        //callback guaranteed all injectable instances depended by this view are ready
        @Override
        public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
            super.onViewReady(view, savedInstanceState, reason);

            //assign view by findViewById
            //...
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    controller.login(username.getText().toString(),
                            password.getText().toString());
                }
            });
        }

        @Override
        public void showLoadingStatus() {
            //Show progress dialog or progress bar
        }

        @Override
        public void hideLoadingStatus() {
            //Hide progress dialog or progress bar
        }

        @Override
        public void update() {
            //Bind model here
        }

    }

    public class LoginController extends FragmentController<Void, AsyncView> {
        @Override
        public Class<Void> modelType() {
            return null;
        }

        public void login(String username, String password) {
            runTask(new Task<Void>() {
                @Override
                public Void execute(Monitor<Void> monitor) throws Exception {
                    //Task.execute methods runs on non-UI thread, so we need to
                    //post the view update logic back to UI thread
                    uiThreadRunner.post(new Runnable() {
                        @Override
                        public void run() {
                            view.showLoadingStatus();
                        }
                    });

                    //Send a http request to login
                    //...
                    //Request returns successfully

                    //Task.execute methods runs on non-UI thread, so we need to
                    //post the view update logic back to UI thread
                    uiThreadRunner.post(new Runnable() {
                        @Override
                        public void run() {
                            view.hideLoadingStatus();
                        }
                    });
                    return null;
                }
            }, new Task.Callback<Void>() {
                @Override
                public void onException(Exception e) throws Exception {
                    //Call back is guaranteed to run on UI thread by the framework
                    //No need to use uiThreadRunner to post action
                    view.hideLoadingStatus();
                }
            });
        }
    }
    ```
#### Sample code to implement MVVM
```java
//A concrete controller as a ViewModel
    public static class SomeController extends Controller<SomeController.Model, UiView> {
        interface Event {
            class OnModelUpdated {
            }

            class OnTitleChanged {
                private final String title;

                public OnTitleChanged(String title) {
                    this.title = title;
                }

                public String getTitle() {
                    return title;
                }
            }
        }

        @Override
        public Class modelType() {
            return SomeController.class;
        }

        //The model of the controller that represents the state of the view
        public class Model {
            private String title;

            //Update model properties will fire events
            public void setTitle(String title) {
                this.title = title;
                
                //Post an event
                //Note postEvent method guarantees the event will be posted to UI thread!!!
                postEvent(new Event.OnTitleChanged(getModel().getTitle()));
            }

            public String getTitle() {
                return title;
            }
        }

        //Expose to view to update title
        public void updateTitle(String text) {
            //Model is updated
            getModel().setTitle(text);
        }
        
        public void rebindModel() {
            //...
            //code to update model
            //
            
            postEvent(new Event.OnModelUpdated());
        }
    }

    //View paired with the controller
    public class SomeView extends View {
        private TextView title;

        @Inject
        private SomeController controller;

        @Inject
        @EventBusV //Event bus for subscribers as views
        private EventBus eventBus;

        public SomeView(Context context) {
            super(context);

            //Register this view to the event bus for views
            //Since here, when an even in type of SomeController.Event.OnTitleChanged
            //is posted, method onEvent(SomeController.Event.OnTitleChanged event)
            //will be called
            eventBus.register(this);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();

            //Unregister the view from the event bus.
            //This is working but not ideal because the event bus will be unregistered until
            //the view is removed from the window not it's parent
            //You can consider viewGroup.setOnHierarchyChangeListener
            
            //but make sub view in fragments extending MvcFragment is recommended since 
            //MvcFragment register and unregister event bus in onCreate and onDestroy 
            //lifecycle call backs. 
            // 
            //This is just an example of using eventBus manually.
            eventBus.unregister(this);
        }

        //Monitor event SomeController.Event.OnTitleChanged
        //All event subscriber methods should be called onEvent with one argument of the
        //event's class type
        private void onEvent(SomeController.Event.OnTitleChanged event) {
            title.setText(event.getTitle());
        }

        //Monitor event SomeController.Event.OnModelUpdated
        private void onEvent(SomeController.Event.OnModelUpdated event) {
            title.setText(controller.getModel().getTitle());
            
            //other views to bind to the controller/ViewModel's model
            //...
        }
    }
```

## Fragment Life cycles
Since AndroidMvc framwork is designed to implement apps with a single Activity in most cases. Fragments are playing an important role. In the framework, fragments can be used as a screen which is the what an activity does traditionally. Also it can be used as sub view as well.

Below are life cycle callback of MvcFragment provided by AndroidMvc framework
- **onCreateView** is final and **SEALED**, use onViewReady described below
- **onViewCreated** is final and **SEALED**, use onViewReady described below
- **onViewReady**(View, Bundle, **Reason**) is called when the fragement is ready to bind Android widgets by findViewById and **use controllers**. The argument "Reason" indicates why the view is created or recreated. For example, view is created
  - first time
  - on restoration
  - rotation.
- **onReturnForeground** called when the app is brought to the front after being pushed to background. It complements onResume since onResume doesn't differentiate foregrounding app, rotation, creation and etc.
- **onPushToBackStack** called when the fragment is about to be pushed to back stack typically on navigation. It complements onPause since onPause doesn't differentiate pushing to back stack, removing fragment, rotation and etc.
- **onPoppedOutToFront** called when the fragment is popping out from the fragment backstack to present as the top most fragment.
- **onPopAway** called when the fragment was the top most presenting fragment but will be popped away and replaced by the fragment will pop out under it.
- **onPreNavigationTransaction(FragmentTransaction transaction, MvcFragment nextFragment)** called before the fragment transaction is about to commit that will replace current fragment by the next. It's main providing the transaction being committed to configure transaction animation. e.g adding SharedElements
- **onOrientationChanged** called when orientation changed.

## FragmentController Life cycles
All fragment life cycles are mapped into [FragmentController](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc-core/src/main/java/com/shipdream/lib/android/mvc/FragmentController.java). So fragments are further liberated from handling business logic. For example, if you need to do some stuff in Framgent.onViewReady, you can do it in FragmentController.onViewReady.

Here are the life cycles
- **onCreate** when the controller is injected into the corresponding fragment
- **onViewReady(Reason)** is called when the fragement is ready to show. The argument "Reason" indicates why the view is created or recreated. For example, view is created
  - first time
  - on restoration
  - rotation.
- **onResume** callced when the fragment is calling its own onResume
- **onPasue** called when the fragment is calling its own onPuase
- **onDestroy** called when the controller is released from being used by the corresponding fragment and not referenced by anything it was injected to.
- **onBackButtonPressed()** called when the phycical back button is pressed
- **onReturnForeground** samed as the corresponding life cycle callback in fragment
- **onPushToBackStack** samed as the corresponding life cycle callback in fragment
- **onPoppedOutToFront** samed as the corresponding life cycle callback in fragment
- **onPopAway** samed as the corresponding life cycle callback in fragment
- **onOrientationChanged** called when orientation changed.

## Navigation
As metioned earlier, AndroidMvc framework uses single activity to create Android apps. Therefore navigation in AndroidMvc is to swap full screen fragments. Though fragment transactions involve complexity primarily because they may be committed asynchronously, AndroidMvc aims to wrap all the tricks up. This is another reason why onCreateView and onViewCreated life cycle call back are sealed and replaced by onViewReady() metioned in [FragmentController Life cycles](#FragmentController-Life-cycles) section. 

The navigation functions are tested in instrumentation test cases. If you are interested you can check out the in [instrumentation test project](https://github.com/kejunxia/AndroidMvc/tree/master/library/android-mvc-test).

#### Mapping between [MvcFragment](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc/src/main/java/com/shipdream/lib/android/mvc/MvcFragment.java) and [FragmentController](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc-core/src/main/java/com/shipdream/lib/android/mvc/FragmentController.java)

- [MvcFragment](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc/src/main/java/com/shipdream/lib/android/mvc/MvcFragment.java) should be extended by fragments in AndroidMvc. It can represents a screen or just a sub view. 
  - It can represent a full screen
  - It also can be used for just a sub view
  - Class extending [MvcFragment](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc/src/main/java/com/shipdream/lib/android/mvc/MvcFragment.java) needs to abstract method **MvcFragment#getResouceId()** to provide the layout resouce id that will be automatically inflated as the root view of the fragment.
  - Class extending [MvcFragment](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc/src/main/java/com/shipdream/lib/android/mvc/MvcFragment.java) needs to implement the abstract method **MvcFragment#getControllerClass()** to provide the class type of a concrete [FragmentController](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc-core/src/main/java/com/shipdream/lib/android/mvc/FragmentController.java). The MvcFragment will automatically create inject the instance of the controller. You don't need to create it, just use MvcFragment.controller straight away. 
    - Note that, null is allow to returned if the fragment doesn't need a controller or you want to inject your own controller by @Inject manually for a reason. However, beware of that in this case MvcFragment.controller will be **NULL**.
- [FragmentController](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc-core/src/main/java/com/shipdream/lib/android/mvc/FragmentController.java) should be extended by a concrete controller for a [MvcFragment](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc/src/main/java/com/shipdream/lib/android/mvc/MvcFragment.java). 
  - It can be used by [NavigationManager](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc-core/src/main/java/com/shipdream/lib/android/mvc/NavigationManager.java) to navigate to its corresponding MvcFragment. In this case, the corresponding MvcFragment will be treated as a full screen page. See the code snippet in [sample code](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple-mvp/core/src/main/java/com/shipdream/lib/android/mvc/samples/simple/mvp/controller/CounterMasterController.java) as below
    ```java
    public void goToDetailScreen(Object sender) {
        navigationManager.navigate(sender).to(CounterDetailController.class);
    }
    ```

#### Routing
Routing rules can be defined in you main activity extending [MvcActivity](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc/src/main/java/com/shipdream/lib/android/mvc/MvcActivity.java). Implement method **MvcActivity#mapControllerFragment()** to map which fragment will be launched as a full screen page for the corresponding controller class type. 

A typical routing rule is as the code below. 
```java
@Override
protected Class<? extends MvcFragment> mapControllerFragment(
        Class<? extends Controller> controllerClass) {
    if (controllerClass == CounterMasterController.class) {
        return CounterMasterScreen.class;
    } else if (controllerClass == CounterDetailController.class) {
        return CounterDetailScreen.class;
    } else {
        return null;
    }
}
```

If you want more automation, you can choose your own package structure file name pattern to apply a generic routing rule to locate concrete MvcFragment classes like below. See the code in the [sample project](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple-mvp/app/src/main/java/com/shipdream/lib/android/mvc/samples/simple/mvp/MainActivity.java)
```java
@Override
protected Class<? extends MvcFragment> mapControllerFragment(
        Class<? extends FragmentController> controllerClass) {
    String controllerPackage = controllerClass.getPackage().getName();
    
    //Find the classes of fragment under package .view and named in form of xxxScreen
    //For example
    
    //a.b.c.CounterMasterController -> a.b.c.view.CounterMasterScreen
    
    String viewPkgName = controllerPackage.substring(0, controllerPackage.lastIndexOf(".")) + ".view";
    String fragmentClassName = viewPkgName + "."
            + controllerClass.getSimpleName().replace("Controller", "Screen");
    
    try {
        return (Class<? extends MvcFragment>) Class.forName(fragmentClassName);
    } catch (ClassNotFoundException e) {
        String msg = String.format("Fragment class(%s) for controller(%s) can not be found",
                fragmentClassName, controllerClass.getName());
        throw new RuntimeException(msg, e);
    }
}
```

#### Continuity between screens
AndroidMvc has 3 different ways to ensure continuity between two consequent screens on navigation transition
1. Shared injectable instances will be retained through the navigation transition. For example, when 2 controllers have the same type of manager injected the same instance of the manager from the first screen's controller will be retained for the second screen's controller. You can check out the [sample code](https://github.com/kejunxia/AndroidMvc/tree/master/samples/simple-mvp), in the [CounterMasterController](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple-mvp/core/src/main/java/com/shipdream/lib/android/mvc/samples/simple/mvp/controller/CounterMasterController.java) there is an injected field called counterManager which is injected into [CounterDetailController](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple-mvp/core/src/main/java/com/shipdream/lib/android/mvc/samples/simple/mvp/controller/CounterDetailController.java) as well. So when master controller navigate to detail controller, the state of the manager retains.
2. Just prepare the controller of the next screen just before navigation is taking place. In this case, the controller prepared will be injected into the next screen framgment.
    ```java
    navigationManager.navigate(this).with(CounterDetailController.class, 
                new Preparer<CounterDetailController>() {
            @Override
            public void prepare(CounterDetailController detailController) {
                //Set the initial state for the controller of the next screen
                detailController.setCount(123);
            }
        }).to(CounterDetailController.class);
    ```
3. Hold an injected instance of the manager depending on the next screen in the controller held by the delegateFragment. DelegateFragment is a long life fragment in the single activity containing all other fragments, so its controller will referenced during the entire lifespan of the app UI comopnent. So the inject managers held by the controller remain during the whole app session. For example, AndroidMvc has already had an internal controller for the delegateFragment holding NavigationManager, so the navigationManager is singleton globally and live through the entire app life span. Another example is, you can have an AccountManager held by delegateFragment's controller so accountManager will span the entire app session to manage the logged in user.

#### Navigation tool bar
The screen fragment doesn't have to take the entire screen. For example, all screens can share the same toolbar just like the tranditional ActionBar.

More details can be found in the [Sample Code](https://github.com/kejunxia/AndroidMvc/tree/master/samples/simple-mvp)

## Run AsyncTask in controller
When a http request need to be sent or other long running actions need to be performed, they need to be run off the UI thread. To run an asyncTask in controllers, simply call 
```java
//In any controller method you can use below code to run async task

Task.Monitor<Void> monitor = runTask(new Task<Void>() {
    @Override
    public Void execute(Monitor<Void> monitor) throws Exception {
        //Execute on Non-UI thread
        //When the view need to be updated here, you need use
        //uiThreadRunner to post it back to UI thread

        return null;
    }
}, new Task.Callback<Void>() {
    @Override
    public void onException(Exception e) throws Exception {
        //Handle exception
        //All callback methods are executed on UI thread
    }
});

//If you need to cancel unscheduled or executing task, call
//cancel against its monitor
boolean canInterrupt = true;
monitor.cancel(canInterrupt);
```

As you see, runTask will give you a monitor which can be used to query the state of the task or cancel it if it has not started or interupt the currently executing task for example a downloading task.

**Tips**:
- Only run async task in controllers to avoid blocking UI thread. All methods in managers or services should simply be synchrounous. Since managers and services are supposed to be consumed by controllers, when they are in use, controllers can choose invoke methods of managers and services on non-UI thread or not.
- In the scope the Task.execute() every line is running in sequence. The callback.onScucess will be called until all lines in Task.execute() execute successfully otherwise, callback.onException is called.
- You can run multiple long running methods in the same Task.execute() method. It's useful if you need to send some consequent http requests each is depending the previous response. So any one of the requests fails, it fails the entire task. See example:
    ```java
    Task.Monitor<Void> monitor = runTask(new Task<Void>() {
        @Override
        public Void execute(Monitor<Void> monitor) throws Exception {
            //Send login http request
            Login loginResponse = loginHttpService.login(username, password);
    
            //use the token contained in loginResponse to send another http
            //request to register device to push notification services
            Status status = pushNotificationHttpService.register(deviceId, loginResponse.token());
            return null;
        }
    });
    ```

---
## Below are old documents for AndroidMvc below 2.3.0. They will be updated.
---

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
