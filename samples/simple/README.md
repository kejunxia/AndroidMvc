## Overview
Simple is a sample counting number to demonstrate how to use AndroidMvc.

Unlike [Sample - Note](https://github.com/kejunxia/AndroidMvc/tree/master/samples/note), to make it really simple it only contains one module.

## What's included
1. How to do dependency injection - see fragments and controllers
2. How data of a shared controller pass its model to subsequent fragment - counterController shared by [FragmentA](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple/src/main/java/com/shipdream/lib/android/mvc/samples/simple/view/FragmentA.java) and [FragmentB](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple/src/main/java/com/shipdream/lib/android/mvc/samples/simple/view/FragmentB.java) so the change in either fragment will carry the updated model to the other one.
3. How same event is broadcast to multiple views - check the below method subscribing the same event by [FragmentA](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple/src/main/java/com/shipdream/lib/android/mvc/samples/simple/view/FragmentA.java), [FragmentB](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple/src/main/java/com/shipdream/lib/android/mvc/samples/simple/view/FragmentB.java), [FragmentA_SubFragment](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple/src/main/java/com/shipdream/lib/android/mvc/samples/simple/view/FragmentA_SubFragment.java) and even an Android service [CountService](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple/src/main/java/com/shipdream/lib/android/mvc/samples/simple/view/CountService.java)
```java
private void onEvent(CounterController.EventC2V.OnCounterUpdated event)
```
4. How to treat a event as a partial ViewModel - check the method below in [CounterControllerImpl](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple/src/main/java/com/shipdream/lib/android/mvc/samples/simple/controller/internal/CounterControllerImpl.java)
```java
public String convertNumberToEnglish(int number){}
```
5. How to unit test navigation - see the test below in [TestCounterController](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple/src/test/java/com/shipdream/lib/android/mvc/samples/simple/controller/internal/TestCounterController.java)
```java
@Test
public void should_navigate_to_locationB_when_go_to_advance_view_and_back_to_locationA_after_go_to_basic_view()
```