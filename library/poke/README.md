# Poke - Dependency Injection with Monitors

## Overview
Poke is a dependency injection library inspired by - [Dagger](http://square.github.io/dagger/).

Why reinvent the wheel?
* The main reason is incorporate with [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) framework, it needs to do **reference count**. Since [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) automatically saves and restores state of controllers, when a controller is not used any more it should stop doing so. So [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) needs to know when a controller is not used anymore.

* Another reason: Dagger requires to register implementations manually to satisfy all injectable objects. This is a little tedious. The reason behind this is that Android apps need to scan through the whole dex file to find out implementations. This is expensive which also slows down startup of Android apps. To find the balance of simpler code and performance, poke can use naming convention to locate implementation of injectable objects. So we don't need to register implementation of injectable objects one by one in application. Still, we can register implementation manually when needed with various ways. This is helpful either for dynamical replacement of implementation or for mocking in unit tests.

## Get started
By default, poke is trying to locate implementation by naming convention. The rule is, to find the concrete class of an interface, it looks for the implementation named as the same as the interface needs with suffix "Impl" under the "internal" sub package which locates on the same level as the interface. For example,

#### Custom defined injection annotation
```java
@Target({FIELD })
@Retention(RUNTIME)
@Documented
public @interface MyInject {
}
```

#### Fridge object wants a food instance injected into its field - apple.
```java
import com.xyz.Robot

public class Factory {
    @MyInject
    private Robot robot;

    public Robot getRobot() {
        return robot;
    }
}
```

#### The interface defining Food
```java
package com.xyz        //Note the package

public interface Robot {
    void work();
}
```

#### Implementation of Food which should be under package com.xyz.**internal**
```java
package com.xyz.internal        //Note the package

import com.xyz.Robot

public class RobotImpl implements Robot {
    @Override
    public void work() {
        System.io.println("Assemble a car.");
    }
}
```

#### Instantiate a Factory object and call the injection
```java
package com.xyz.internal        //Note the package

import com.xyz.Robot

public class Main {
    public static void main(String[] args) {
        Factory factory = new Factory();

        //Create an instance of SimpleGraph and make sure it's singleton for the entire app.
        //So it's best to instantiate it at the app's entry point
        SimpleGraph graph = new SimpleGraph();

        //Inject dependencies into factory
        graph.inject(factory, MyInject.class);

        //Now factory has its robot instance inject and ready to use
        factory.getRobot().work();  // Print "Assemble a car." in console
    }
}
```