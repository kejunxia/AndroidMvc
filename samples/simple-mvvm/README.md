# MVVM Sample

In MVVM, not like MVP, a controller doesn't have to be aware of view since the controller notifies view by event instead of invoking view's method.

The sample demonstrate 2 ways to implement MVVM with Android MVC
- [On Master page](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple-mvvm/mobile/src/main/java/com/shipdream/lib/android/mvc/samples/simple/mvvm/view/CounterMasterScreen.java), the sample is using Android Data Binding. That's why this project doesn't divide projet into app and core modules because Android Data Binding is not decoupled from Android SDK.
- [On Detail page](https://github.com/kejunxia/AndroidMvc/blob/master/samples/simple-mvvm/mobile/src/main/java/com/shipdream/lib/android/mvc/samples/simple/mvvm/view/CounterDetailScreen.java), it's using eventbus to subscribe view events. If you want to use MVVM and also like to separate app and core module, you can try this approach.

[Unit tests are written here](https://github.com/kejunxia/AndroidMvc/tree/master/samples/simple-mvvm/mobile/src/test/java/com/shipdream/lib/android/mvc/samples/simple/mvp/controller/internal)
