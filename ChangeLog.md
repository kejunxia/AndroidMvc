Version: 3.3.0
* Update to latest android build tools, support library.

Version: 3.2.0
* Change Task#onStarted to have a parameter "monitor", so that the invoker can use the monitor to cancel the task before the task runs or when it's running

Version: 3.1.1
* Add MvcDialog and deprecate MvcDialogFragment
* Better description for injection errors for fragments and services
* Allow controllers to send events to other controllers and managers
* Uplift Android support library to 24.2.0

Version: 3.1.0
* Uplift Android Support Library to 24.1.1

Version: 3.0.3:
* Rename method MvcActivity.mapControllerFragmentClass<? extends Controller> to MvcActivity.mapFragmentRouting(Class<? extends FragmentController>)
* Add some more detailed info for logging

Version: 3.0.1
* Fix issue that interim navigation pages are not popped out on back navigation correctly
* Uplift target compile SDK to 24.

Version: 3.0.0
* View and Controller are one to one mapped
* FragmentController introduced that is bound with the same lifecycle as MvcFragment. It is easier to act as a presenter of fragment
* Able to post actions to ui thread from controllers by protected uiThreadRunner field
* Navigate based on controller class type instead of string location Id. e.g. navigationManager.navigate(this).to(HomePageController.class)
* Rename BaseControllerImpl to Controller. Not necessary to create an interface for controller
injection. A concrete class can be injected straight away.
* New life cycles of controllers that match to views
* MvcComponents manage their own cached instances by themselves.
* Register providers to Graph's root component rather than to graph itself.
* Managers listen to eventBusC
* New lifecycle MvcFragment.onPopAway
* Uplift support library to 24.0.0 and remove all hack for issue reported https://code.google.com/p/android/issues/detail?id=197271

Version:2.3
* New navigation method that allow configuring the location not pushed to back stack
* Uplift Android Support Lib to 23.4.0
* Uplift espresso to 2.2.2, test runner to 0.5.0

Version:2.2.0
* Run async task with callback with more granular controls.
* Pass in monitor to Task#execute(Monitor monitor) so that the monitor can be used in concrete Task#execute body and passed to events to cancel the tasks.
* Fix the issue of onViewReady reason. Now reason.firstTime of view ready should be false when the fragment is popped out from back stack
* Add new lifecycle of fragment to config shared elements between navigating fragments
* Update support lib to 23.3.0

Version:2.1.0
* Rename post event methods to postEvent2C and postEventCV which post events to controllers and views.
* Change NavigationController to NavigationManager aligned with the new pattern. Controllers involving navigation now use injected navigation manager to navigate.

Version:2.0.0
* Controllers now are supposed to have one-to-one relationship with a view. In other words, a controller is an abstraction of a single view. Business logic should reside in controllers rather then views. Since a controller is a representation of a view, a view theoretically can be unit tested just by testing its abstraction in its corresponding controller.
* Shared logic by multiple controllers now are supposed to move to a shared manager. For example, a logged in user is commonly queried on multiple app pages(views). So controllers for these pages(views) need to access the shared logged in user state. The logged in user and login/logout functions can be wrapped in a LoginManager and shared by the controllers.
* Add BaseManagerImpl which is the base class of managers.
* BaseControllerImpl.postEventC2C renamed to BaseControllerImpl.postControllerEvent, BaseControllerImpl.postEventC2V renamed to BaseControllerImpl.postViewEvent
* Remove EventBusV2V because to views it has no difference from EventBusC2V since views don't care where the events come from. Now there are only 2 event buses. EventBusC and EventBusV which are subscribed by controllers and views respectively.
* BaseControllerImpl.getModelTypeClass() renamed to BaseControllerImpl.modelType(). So when typing method getModel() in AndroidStudio it doesn't show getModelTypeClass() by its intellisense to smooth code typing experience.

Version:1.6.0
* Add BaseManagerImpl. Logic and data shared by multiple controllers can be put into managers and injected into controllers.
* Delegate fragment's onViewReady lifecycle will be called after state of all controllers are restored if activity is killed by OS

Version:1.5.3
* MvcGraph able to inject concrete class with a public constructor
* Fix bug that sub fragments' controller do not restore state

Version:1.5.2
Fix bug to ensure all fragments are associated with correct activity

Version:1.5.1
Add convenient method for MvcActivity post v2v events

Version: 1.5.0
Add reference/dereference method to MvcGraph
Enhanced navigation controller:
* allow config controller easier
* allow call back after navigation is settled
Fix issue that controllers configured by navigationManager is not released correctly

Version: 1.4.1
* Able to initialize the controller state referenced by next navigating fragment and retain the state until the navigation is fully performed when the navigation is requested directly via NavigationController in MvcGraph.use method.
* Cached instances will be dereferenced and disposed when the instance is referenced by the fields with same variable name in both super and current class.

Version: 1.4.0
* Refactor of class AndroidMvc so that controllers can access the MvcGraph
* EventBusV2V is injectable same as EventBusC2V and EventBusC2C
* Add method "use" in MvcGraph to consume an injectable classes which doesn't require a class field marked by @Inject
* Remove BaseControllerImpl.onInitialized and replaced by onConstruct since onInitialized is also called when the controller is restored
* Add call back BaseControllerImpl.onRestored

Version: 1.3.0
* Refactor the MvcFragment.Reason object.
* Remove android-mvc-controller-retrofit

Version: 1.2.1
* Fix issue that MvcFragment and MvcDialogFragment post V2V events to C2V event bus.

Version: 1.2.0
* MvcGraph.inject throws runtime exception - MvcGraphException now. So no need to catch poke exceptions any more.
* MvcGraph.get method to get an instance. It provides cached instance if there is already an existing one, otherwise the newly created instance.
* Improve exception handling in library poke.

Version: 1.1.9
Fix bug
* When a recovering fragment in view pager its onViewReady is not called when its holding activity resume by popping out from another above activity.
* Correct the logic how to detect onReturnFromBackground

Version: 1.1.8
Fix issue that onViewReady not called in nested fragment of the popping out fragment when the activity is not killed by OS
Make sure onStartUp is called after onViewReady is finished for fragment extends DelegateFragment

Version: 1.1.7
Once root fragment is restored, it should notify all nested fragments they are not managed by the root fragment any more
Fix issue that nested fragments of popping out fragment won't call onReturnFromBackground incorrectly

Version: 1.1.6
Fix issue that onViewReady is called with incorrect reason - RESTORE which should be FIRST_TIME when the fragment is a page and recreated by FragmentPagerAdapter

Version: 1.1.5
Allow remove preference key from SharedPreferenceService
Naming convention cleaning up

Version: 1.1.4
Fix the bug that navigation may crash the app when the app just resumes from other app such as facebook login

Version: 1.1.3
* Broke Android SDK dependency out from Android Mvc Controller module
* Update Espresso to 2.2

Version: 1.1.2
Prevent a warning about failed cast to save and restore controller state which is deliberate.

Version: 1.1.1
Refactor library Poke by moving Graph.OnFreedListener to Provider.OnFreedListener and some document updates.

Version: 1.1.0
Publish below extensions
* android-mvc-service-core
* android-mvc-controller-retrofit

Version: 1.0.2
Fix issue that exceptions during posting event are not thrown out properly

Version: 1.0.1
Bug fixes

version: 1.0
Initial release