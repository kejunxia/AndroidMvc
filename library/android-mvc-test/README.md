# AndroidMvc instrumentation tests

This project is used for testing AndroidMvc module on emulator and devices with [Espresso](https://developer.android.com/training/testing/ui-testing/espresso-testing.html). To start the test, run 
```groovy
gradlew cC
```

It's recommended to run at least 2 emulator
- A normal emulator
- An emulator turns on [Don't keep activity](http://stackoverflow.com/questions/22400859/dont-keep-activities-what-is-it-for) in the dev option. 
This is an important test scenario to prevent crash when app is relaunched after the it is killed by OS in the background.

Some test cases **may fail occasionally** in a batch run by
```groovy
gradlew cC
```

It doesn't have mean the test cases really fail. It may  It's caused by some race condition during multiple activities are running together 
**in the INSTRUMENT TESTING environment**. 
As long as rerunning the failed cases individually can pass, the test cases should be considered passed.

I haven't found the best way to prevent it. I highly appreciate anyone can contribute some great ideas and code to make the tests yield result more consitently.
