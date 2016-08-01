# AndroidMvc Framework
Android Mvc framework helps Android developers implement Android projects simpler and cleaner with MVC/MVP/MVVM patterns and make them testable.

## Features
  - [Easy to implement MVC/MVP/MVVM pattern](#-Implement-MVC/MVP/MVVM-pattern) for Android development
  - [Enhanced Android life cycles](#Life-cycles) - e.g. when view needs to refresh when being brought back to foreground but not on rotation, onResume() is not specific to differentiate the two scenarios. Android mvc framework provides more granular life cycles
  - [All fragment life cycles are mapped into FragmentController](#FragmentController-Life-cycles) thus more business logic can be moved into controllers including the ones in life cycles. Apps are more testable on JVM!
  - [Easy and clean navigation](#Navigation). Navigation is done in controllers instead of views. Thus navigation can be unit tested on JVM
  - [Run async tasks in controllers](#Run-AsyncTask-in-controller) and easy mocking of http requests
  - [Easy unit test on JVM](#Easy-Unit-Test) since controllers don't depend on any Android APIs
  - [Built in Event Bus](#Event-Bus). Event bus also automatically guarantees post event view events on the UI thread
  - [Automatically save and restore instance state](#Instance-State-Management). You don't have to touch onSaveInstance and onCreate(savedInstanceState) with countless key-value pairs, it's all managed by the framework.
  - [Built in simple dependency injection](#Dependency-injection-(Poke))
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
protected Class<? extends MvcFragment> mapFragmentRouting(
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
protected Class<? extends MvcFragment> mapFragmentRouting(
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
    
## Easy Unit Test
So far, you should have already got some ideas how much business logic can be written in controllers with AndroidMvc. [See sample unit tests here](https://github.com/kejunxia/AndroidMvc/tree/master/samples/simple-mvp/core/src/test/java/com/shipdream/lib/android/mvc/samples/simple/mvp/controller/internal)

#### Mock dependencies
As most codes are wrapped in controllers without Android API dependencies. You can just simply test everything on JVM. Because in app, there are dependencies implemented with Android API which are lacking in the sole controller module, those dependencies' implementations need to be replaced by mocked instances. For example, every controller has an injected field - ExecutorService to run a aysncTask by 
controller.runTask(Task task). In unit test, this ExecutorService can be mocked and run task immediately on the same thread to mimic a http response with mocked data.

To override providers to replace injectable classes, 
1. create a MvcComponent say overridingComponent
2. register your providers to provider mocking objects
3. attach the overridingComponent to Mvc.graph().getRootComponent() with the 

See the code sample below
```java
//Mock executor service so that all async tasks run on non-UI thread in app will
//run on the testing thread (main thread for testing) to avoid multithreading headache
executorService = mock(ExecutorService.class);

doAnswer(new Answer() {
    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Callable runnable = (Callable) invocation.getArguments()[0];
        runnable.call();
        Future future = mock(Future.class);
        when(future.isDone()).thenReturn(true); //by default execute immediately succeed.
        when(future.isCancelled()).thenReturn(false);
        return future;
    }
}).when(executorService).submit(any(Callable.class));

overridingComponent = new MvcComponent("TestOverridingComponent");
overridingComponent.register(new Object(){
    @Provides
    public ExecutorService createExecutorService() {
        return executorService;
    }
});

//For base test class, allow sub test cases to register overriding providers
prepareGraph(overridingComponent);

Component rootComponent = Mvc.graph().getRootComponent();

//overriding indicates providers of this component attached to the root component will override 
//existing providers managing to provide instances with the same type and qualifier.
boolean overriding = true;
rootComponent.attach(overridingComponent, overriding);
```

#### Mock http response
Below are some code snippets of mocking http traffic. More code can be found in the  [sample](https://github.com/kejunxia/AndroidMvc/tree/master/samples/simple-mvp) in the project. The sample is using Retrofit for http resources.

- **Mock Successful http response**

    ```java
    @Test
    public void should_update_view_with_correct_ip_and_show_and_dismiss_progress_bar() throws Exception {
        //Prepare
        //Prepare a good http response
        final String fakeIpResult = "abc.123.456.xyz";
    
        IpPayload payload = mock(IpPayload.class);
        when(payload.getIp()).thenReturn(fakeIpResult);
        when(ipServiceCallMock.execute()).thenReturn(Response.success(payload));
    
        //Action
        controller.refreshIp();
    
        //Verify
        //Showed loading progress
        verify(view).showProgress();
        //Dismissed loading progress
        verify(view).hideProgress();
        //Updated view's text view by the given fake ip result
        verify(view).updateIpValue(fakeIpResult);
        //Should not show error message
        verify(view, times(0)).showHttpError(anyInt(), anyString());
        //Should not show network error message
        verify(view, times(0)).showNetworkError(any(IOException.class));
    }
    ```
- **Mock erred http response**

    ```java
    @Test
    public void should_show_error_message_on_HttpError_and_show_and_dismiss_progress_bar() throws Exception {
        //Prepare
        //Return 401 in the http response
        int errorStatusCode = 401;
        ResponseBody responseBody = mock(ResponseBody.class);
        when(ipServiceCallMock.execute()).thenReturn(
                Response.<IpPayload>error(errorStatusCode, responseBody));
    
        //Action
        controller.refreshIp();
    
        //Verify
        //Showed loading progress
        verify(view).showProgress();
        //Dismissed loading progress
        verify(view).hideProgress();
        //View's ip address text view should not be updated
        verify(view, times(0)).updateIpValue(anyString());
        //Should show http error message with given mocking data
        verify(view, times(1)).showHttpError(errorStatusCode, null);
        //Should not show network error message
        verify(view, times(0)).showNetworkError(any(IOException.class));
    }
    ```
- **Mock network error**

    ```java
    @Test
    public void should_show_error_message_on_NetworkError_and_show_and_dismiss_progress_bar() throws Exception {
        //Prepare
        //Throw an IOException to simulate an network error
        IOException ioExceptionMock = mock(IOException.class);
        when(ipServiceCallMock.execute()).thenThrow(ioExceptionMock);
    
        //Action
        controller.refreshIp();
    
        //Verify
        //Showed loading progress
        verify(view).showProgress();
        //Dismissed loading progress
        verify(view).hideProgress();
        //View's ip address text view should not be updated
        verify(view, times(0)).updateIpValue(anyString());
        //Should not show http error message
        verify(view, times(0)).showHttpError(anyInt(), anyString());
        //Should show network error message with the given mocking exception
        verify(view, times(1)).showNetworkError(ioExceptionMock);
    }
    ```

## Event Bus
There are event buses built in the framework can be used straight away. 
- **Event bus object can be injected by @Inject as a field of a class.**
- **There are two event buses in the framework. Both event buses are singleton app wide.**
  - **EventBusC**: Routes events to non-view objects. Events on this bus usually come from non-Android module(see [core module in the the sample](https://github.com/kejunxia/AndroidMvc/tree/master/samples/simple-mvp)). Events on the bus will be **observed on the same thread** that the invoker is running. 
  - **EventBusV**: Routes events to view/android objects such as activity, fragment, services and etc. Events on the bus will be guaranteed to be **observed on the UI thread** automatically by the framework.  
- **Event buses above can be injected with qualifiers.**
    ```java
    @Inject
    @EventBusC
    private EventBus eventBusC;
    
    @Inject
    @EventBusV
    private EventBus eventBusV;
    ```
- **Events are defined by a class type.** It's recommended to define it as enclosed class if it's closely related to the class. As a result, the observer knows what events are for what. For example, 
    ```java
    public class UserManager {
        interface Event {
            class OnUserLoggedIn{
            }
        }
        
        //...
        public void func() {
            eventBus.post(new Event.OnUserLoggedIn());
        }
    }
    ```
- **Events can have their own arguments.** For example,
    ```java
    class OnUserLoggedIn {
        private final User user;
        public OnUserLoggedIn(User user) {
            this.user = user;
        }
        public User getUser() {
            return user;
        }
    }
    ```
- **Sender as an argument in an event is a good practice.** To define a sender argument in an event is useful to distiguish who initiate the request results in this event. For example, when refreshing a list view, you may need different logic to handle the requests caused by 
  - User interaction. e.g. pull to refresh
  - Some internal state change. e.g. a polling every 3 seconds
  
  In this case, define a sender argument in the event class. Then when observers receive the event they know who initially request the refresh and handle differently.
- **Events are observed by methods with naming convention.** To subscribe an event by declaring a method called **onEvent** with **one argument** in the class type of the event. The name of the event doesn't matter. For example
    ```java
    public class OneView {
        //Observe event OnListViewRefreshed.
        private void onEvent(OnListViewRefreshed event) {
            //handle event
        }
    }
    ```
- **Event bus needs to be registered to observe event** by calling EventBus.register(Object observer). However, these pre-defined Mvc objects register by themselves so you don't need to worry about to registering by using them. Just define your onEvent methods to subscribe. These objects are:
  - **MvcActivity** registers to **EventBusV** when created
  - **MvcFragment** registers to **EventBusV** when created
  - **MvcService** registers to **EventBusV** when created
  - **Controller** registers to **EventBusC** when injected for the first time
  - **Manager** registers to **EventBusC** when injected for the first time

  If you have your own objects need to observe a event bus, just call EventBus.register(Object observer) when they are created. Also it's better to unregister them when the obsever is not used any more. This can be done in onDestroy life cycle.

Below is a code snippet. Note that the code is not complete to run but just for demostrating how to use event bus
```java
public class OneView {
    @Inject
    private OneController onController;
    
    private Button refreshButton;
    
    public void onCreated() {
        eventBusV.register(this);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View refreshButton) {
                onController.refresh(refreshButton);
            }
        });
    }

    public void onDestroy() {
        eventBusV.unregister(this);
    }

    //Observe event OnListViewRefreshed from OneController
    //It runs on UI thread automatically
    private void onEvent(OneController.Event.OnListViewRefreshed event) {
        if (event.getSender() == refreshButton) {
            //refreshed by user interaction of pressing the refresh button
            //so if erred, it's better to show error message
        } else {
            //should by something else, e.g. controller wants to refresh for some 
            //reason
            //In this case, error message may not have to be shown
        }
        
        //refresh the list view
    }
}

public class OneController extends Controller{
    interface Event {
        public class OnListViewRefreshed {
            private final Object sender;
            public OnListViewRefreshed(Object sender) {
                this.sender = sender;
            }

            public Object getSender() {
                return sender;
            }
        }
    }
    
    @Inject
    private NavigationManager navigateManager;
    
    @Inject
    @EventBusV
    private EventBus eventBusV; //Framework guarantees the event will be posted to UI thread
    
    public void refresh(final Object sender) {
        runTask(new Task() {
            @Override
            public Object execute(Monitor monitor) throws Exception {
                //Pull data from http server
                //...
                //
                
                //successful and post event
                //Though execute method is run on non-Ui thread,
                //eventBusV will guarantee the observer will receive the event onto
                //UI thread
                eventBusV.post(new Event.OnListViewRefreshed(sender));
                return null;
            }
        });
    }
    
    //Observe logged on event from UserManager
    private void onEvent(UserManager.Event.OnLoggedOut event) {
        //...
        //User logged out

        navigateManager.navigate(this).to(LoginController.class, new Forwarder().clearAll());
    }
}
    
public class UserManager {
    interface Event {
        class OnLoggedOut{
            private final Object sender;
            public OnLoggedOut(Object sender) {
                this.sender = sender;
            }

            public Object getSender() {
                return sender;
            }
        }
    }
    
    @Inject
    @EventBusC
    private EventBus eventBusC; 
    
    public void setCurrentUser(Object sender, User user) {
        //... 

        //EventBusC post event to the thread that the invoker calling setCurrentUser 
        //is running on
        eventBusC.post(new Event.OnLoggedOut(sender));
    }
}
```

## Instance State Management
Instance state management in standard Android SDK is painful. It requires defining countless key-value pairs. No mention to create Paracelable needs to write a lot of boilerplate code and it's error prone.

AndroidMvc framework manages the state automatically. Since a controller represents a view in abstraction so it has a property called model to contain the state of its view. AndroidMvc just getModel() and serialise it when the app is pushed to background and deserialise the model and bind it to the controller and view automatically. This is why you just need to bind the view in UiView.update() method and then no matter the view is newly created or restored, it's always reflecting the latest state of the model managed by the view's controller. Review [Implement MVC/MVP/MVVM pattern](#-Implement-MVC/MVP/MVVM-pattern) to check the patterns.

Also not just controllers manage their state automatically, managers, services and any injected objects extending Bean(#https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc-core/src/main/java/com/shipdream/lib/android/mvc/Bean.java) and returns non-null class type in its method modelType() will be automatically managed. See more details about Bean.java and injection in section [Dependency injection](#Dependency-injection-(Poke))

## Dependency injection (Poke)
The framework has a built in dependency injection called **Poke**. 
Why reinvent the wheel?
* The main reason is to incorporate with [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) framework, it needs to do **reference count**. Since [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) automatically saves and restores state of controllers, when a controller is not used its state won't need to be managed any more. So [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) needs to know when a controller is not referenced.
* Another reason: In Dagger, a lot of boiler plate code still needs to be done. This definitely minimizes run time overhead for injection but there are just two much codes. And you need to remember to declare you injection. It's almost like writting a setter or inject method for each injectable class and then we need to manually set or inject objects.

To find the balance between simpler code and runtime performance, **Poke** can use naming convention to automatically locate implementations. So we don't need to repeatedly declare implementations by writing in **real** application with. However, **Poke** also allows registering implementation manually. This is helpful either for dynamical replacement of implementations or mocking injectable dependencies in unit tests.


#### Bean
Classes and interfaces can be injected with the their instances. There are some classes can be injected with some special behaviours. These classes are called beans. A [Bean](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc-core/src/main/java/com/shipdream/lib/android/mvc/Bean.java) is an object 
1. has life cycles. When a bean is created by the first injection, its method **onCreated** will be called. When a bean is released by the last object it was injected into, it method **onDestroy** will be called. 
2. has a model. A model contains the state of the bean that can be serialised and deserialised. So the bean's state can be saved and restored during the Android's activity and fragment's life cycles.

In AndroidMvc framework there are a couple of pre-defined beans
- [Controllers](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc-core/src/main/java/com/shipdream/lib/android/mvc/Controller.java)
- [Managers](https://github.com/kejunxia/AndroidMvc/blob/master/library/android-mvc-core/src/main/java/com/shipdream/lib/android/mvc/Manager.java)
So controllers and managers(can be thought as partial controller that are shared by controllers) will have their life cycle and be automatically saved and restored. And you can define you own beans without any restrictions. Just simply make the class extend the Bean class. 

If you want to use an interface as a bean, just extend bean class in its implementation.

#### Architecture
Below are the main parts to inject objects
- **Providers** provide instances. Providers can be registered to component.
- **Components** contain providers. Components can also the attached to other components to form a component tree. 
  - **Scope** A component has a cache to cache instances provided by providers held by the component. So providers are **singleton** in the scope of a component. You can set the cache of a component to be null. Then the providers in this component will always create new instances. 
  - By default, a component tree can only have unique provider providing instances with a specific class type and qualifier. Registering a provider to a component tree with duplicate to provide instance of type and qualifier already existing will throw exceptions.
  - But you can explicitly declare you want to register a provider overriding the existing providers with the same class type and qualifier in the component tree. Last overriding provider registered wins.
    
      ```java
      boolean overriding = true;
      rootComponent.attach(overridingComponent, overriding);
      ```
- **Graph** A graph is used to inject instances into target objects. A graph has a root component with providers or its child components providers to provide instances to be injected. Usually an application should have only one graph. So by default, providers registered to the root component of the graph is Singleton globally. To have different scope, simply manage your own components and attach/detach them to/from the root components of the graph. Providers of these components will be singleton until the components are detached from the graph.

#### How to inject
With poke and AndroidMvc, to inject an instance is easy. It doesn't need to declare  what needs to don't need to  Below we will explore how to inject in different scenarios.
- Inject an instance of a concrete class
  
  AndroidMvc automatically inject concrete classes that have an empty default constructors.
  
    ```java
    public class Break {
        public void slow(Car car) {
            car.setSpeed(car.getSpeed() - 1);
        }
    }
    public class Car {
        @Inject
        private Break aBreak;
        private float speed;
        public float getSpeed() {
            return speed;
        }
        public void setSpeed(float speed) {
            this.speed = speed;
        }
        public void decelerate() {
            aBreak.slow(this);
        }
    }
    @Test
    public void run_car_with_default_components() {
        Car car = new Car();
        Mvc.graph().inject(car);
        car.decelerate();
    }
    ```
- Inject an instance of an interface or abstract class
  
  By default, AndroidMvc will look for implementation class of interface or abstract class in the sub package **"internal"** at the same level of the package of the interface or abstract class. The implementation class should have the same name of the interface or abstract class but with a suffix **"impl"**. And the rest steps to inject an instance of an interface or an abstract class is exactly the same as injecting a concrete class as above.
  For example, when we have an interface called Engine under package com.xyz. To let AndroidMvc find its implementation automatically, the concrete class should call Engine**Impl** and resides under com.xyz.**internal***. See the file structure below
    ```
    --com
      --xyz
        Engine.java
        --internal
          EngineImpl.java
    ```
- Customise providers

  You can also register a provider to a component attached to the graph. Or register an object with methods annotated by @Provides to provide instance.
    ```java
    MvcComponent testComponent = new MvcComponent("TestComponent");
    
    //Register new providers with better v8 engine and racing break
    testComponent.register(new Object(){
        @Provides
        public Engine v8Engine() {
            return new Engine() {
                @Override
                public void push(Car car) {
                    car.setSpeed(car.getSpeed() + 3);
                }
            };
        }
    
        @Provides
        public Break racingBreak() {
            return new Break() {
                @Override
                public void slow(Car car) {
                    car.setSpeed(car.getSpeed() - 2.5f);
                }
            };
        }
    });
    ```
- Override existing providers

  Registering providers providing classes for same signature of class (same class type and qualifier) will result in throwing out exceptions since AndroidMvc won't be able to figure out which provider should be used. But the you can implicitly inform AndroidMvc you want to override providers with code like below
    ```java
    //Attach the component to the graph's root component to override default providers
    boolean overrideExistingProviders = true;
    Mvc.graph().getRootComponent().attach(testComponent, overrideExistingProviders);
    ```
