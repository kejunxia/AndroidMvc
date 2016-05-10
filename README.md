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

![AndroidMvc Layers](http://i.imgur.com/dfW8TLM.png)

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

#### Code snippets:
- **View**:
```java
public class WeatherListFragment extends BaseFragment {
    private Button buttonRefresh;
    private ProgressDialog progressDialog;

    @Inject
    private WeatherController weatherController;
    
    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        buttonRefresh = (Button) view.findViewById(R.id.fragment_weather_list_buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weatherController.updateAllCities(view);
            }
        });
        
        //Automatically update weathers of all cities on first creation.
        if (reason.isFirstTime()) {
            weatherController.updateAllCities(this);
        }
    }
    
    public void onEvent(WeatherController.EventC2V.OnWeathersUpdateBegan event) {
        ...
        progressDialog.show();
    }
    
    public void onEvent(WeatherController.EventC2V.OnWeathersUpdated event) {
        updateList();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
```
- **Controller**:
```java
public class WeatherControllerImpl extends BaseControllerImpl <WeatherModel> implements
        WeatherController{
    static final String PREF_KEY_WEATHER_CITIES = "PrefKey:Weather:Cities";
    private Gson gson = new Gson();

    @Inject
    private WeatherService weatherService;

    @Inject
    private PreferenceService preferenceService;

    @Override
    public Class<WeatherModel> modelType() {
        return WeatherModel.class;
    }

    @Override
    public void updateAllCities(final Object sender) {
        if(getModel().getWeatherWatchlist().size() == 0) {
            String cities = gson.toJson(getModel().getWeatherWatchlist());
            preferenceService.edit().putString(PREF_KEY_WEATHER_CITIES, cities).apply();
            postEvent2V(new EventC2V.OnWeathersUpdated(sender));
        } else {
            postEvent2V(new EventC2V.OnWeathersUpdateBegan(sender));

            runAsyncTask(sender, new AsyncTask() {
                @Override
                public void execute() throws Exception {
                    List<Integer> ids = new ArrayList<>();
                    for(WeatherModel.City city : getModel().getWeatherWatchlist().keySet()) {
                        ids.add(city.id());
                    }
                    for (WeatherInfo weatherInfo : weatherService.getWeathers(ids).getList()) {
                        getModel().getWeatherWatchlist().put(findCityById(weatherInfo.getId()), weatherInfo);
                    }

                    String cities = gson.toJson(getModel().getWeatherWatchlist());
                    preferenceService.edit().putString(PREF_KEY_WEATHER_CITIES, cities).apply();
                    //Weather updated, post successful event
                    postEvent2V(new EventC2V.OnWeathersUpdated(sender));
                }
            }, new AsyncExceptionHandler() {
                @Override
                public void handleException(Exception exception) {
                    //Weather failed, post error event
                    postEvent2V(new EventC2V.OnWeathersUpdateFailed(sender, exception));
                }
            });
        }
    }

    private WeatherModel.City findCityById(int id) {
        for(int i = 0; i < WeatherModel.City.values().length; i++) {
            if (WeatherModel.City.values()[i].id() == id) {
                return WeatherModel.City.values()[i];
            }
        }
        return null;
    }
}
```
- **Service**:
```java
//Just a sample, you can use anything to access cloud api e.g. Retrofit
public class WeatherServiceImpl implements WeatherService{
    private final static String APPKEY = "123213123123213213123213";
    private OkHttpClient httpClient;
    private Gson gson;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public WeatherServiceImpl() {
        httpClient = new OkHttpClient();
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
        String url = String.format("http://api.openweathermap.org/data/2.5/group?id=%s&appId=%s",
                URLEncoder.encode(idsStr, "UTF-8"), APPKEY);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response resp = httpClient.newCall(request).execute();
        String responseStr = resp.body().string();
        logger.debug("Weather Service Response: {}", responseStr);
        return gson.fromJson(responseStr, WeatherListResponse.class);
    }
}
```
