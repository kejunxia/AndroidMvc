# Poke - Dependency Injection with Monitors

## Overview
**Poke** is a dependency injection framework inspired by - [Dagger](http://square.github.io/dagger/).

Why reinvent the wheel?
* The main reason is to incorporate with [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) framework, it needs to do **reference count**. Since [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) automatically saves and restores state of controllers, when a controller is not used its state won't need to be managed any more. So [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) needs to know when a controller is not referenced.
* Another reason: Dagger requires to register implementations manually to satisfy all injectable objects. The reason behind this is that Android apps need to scan through the whole dex file to find out implementations. This is expensive and slows down the startup of Android apps. To avoid scanning the whole dex file, manual registration is more efficient for runtime.
 
  But it is a little tedious. To find the balance between simpler code and runtime performance, **Poke** can use naming convention to automatically locate implementations. So we don't need to register implementations one by one in **real** application. However, **Poke** still allows registering implementation manually. This is helpful either for dynamical replacement of implementations or mocking injectable dependencies in unit tests.


## How to inject
As mentioned, Poke allows bind the contract and its implementations automatically and manually. Let's have a look how to achieve them with the examples below.
### 1. Automatically locate implementation by naming convention
By default, **Poke** is trying to locate implementations by naming convention. The rule is, to find the concrete class of an interface, it looks for the implementation class with the same as the interface but with a suffix "Impl" under the "internal" sub package which locates on the same level as the interface.

For example, when we have an interface called Robot under package com.xyz. To let Poke find its implementation automatically, the concrete class should call Robot**Impl** and resides under com.xyz.**internal***. See the file structure below

```
--com
  --xyz
    Robot.java
    --internal
      RobotImpl.java
  
```

Example to inject a Robot into a Factory

The interface defining Robot
```java
package com.xyz        //Note the package

public interface Robot {
    void work();
}
```

Implementation of Food which should be under package com.xyz.**internal**
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

Poke allows us to use any annotations to mark injectable class fields. Here we define an annotation called @MyInject. Of course the standard @Inject is also working. 

Not hard coding the annotation could help us handle different kinds of injection specifically. For example, @InjectView and @InjectController may need to do different things besides basic injection. How to spice up for different inject annoations is up to the implementation.

```java
@Target({FIELD})
@Retention(RUNTIME)
@Documented
public @interface MyInject {
}
```

Factory wants a Robot instance injected.
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


Instantiate a Factory object and call the injection
```java
package com.xyz.internal        //Note the package

import com.xyz.Robot

public class Main {
    public static void main(String[] args) {
        Factory factory = new Factory();

        //Create an instance of SimpleGraph and make sure it's singleton for the entire app.
        //Best place to instantiate is at the app's entry point
        SimpleGraph graph = new SimpleGraph();

        //Inject dependencies into factory from graph. Graph will find RobotImpl as the implementation of Robot based on the naming convention mentioned above
        //Note we are using the custom annotation @MyInject here
        graph.inject(factory, MyInject.class);		

        //Now factory has its robot instance injected and ready to use
        factory.getRobot().work();  // Print "Assemble a car." in console
    }
}
```

### 2. Manually bind contract and implementation
If automatic implementation binding is not enough, Poke also allows manually registering bindings of interfaces and implementation classes. There are 2 ways as below

##### 2.1. Direct registration the binding of interface and implementation class
```java
SimpleGraph.register(Class<T>type, String implementationClassName)
```
Or
```java
SimpleGraph.register(Class<T> type, Class<S extends T> implementationClass)
```

But as you can see non of the methods above including the naming convention solution doesn't allow us to config the instance to be injected. To target this issue, there is another more flexible way to register the binding between interface and its implementation.

##### 2.2. Component with provide methods
Create a class extending Component and define methods annotated by @Provides to tie up interface and its implementation. See the example below

```java
public class IndustrialComponent extends Component {
	private String componentName;
    
    public IndustrialComponent(String componentName) {
    	this.componentName = componentName;
    }

    @Provides
    @Singleton	//The provided instance is singleton relative to the instance of this compnent
    public Robot providesPlaneRobot() {
    	return new Robot() {
        	@Override
            public void work() {
                System.io.println(String.format("Assemble a plane by %s", this.componentName));
            }
        };
    }
}
```

Then inject it
```java
public class Main {
	private IndustrialComponent industrialComponent;

    public static void main(String[] args) {
        Factory factory = new Factory();

        SimpleGraph graph = new SimpleGraph();
        
        //Instantiate the component with arguments which also impact the injecting instance of Robot
        industrialComponent = new IndustrialComponent("A cool component");

		//Register the component to the graph
		graph.register(industrialComponent);

        //Inject dependencies into factory from graph.
        graph.inject(factory, MyInject.class);		

        //Now factory has its robot instance injected and ready to use
        factory.getRobot().work();  // Print "Assemble a plane by A cool component." in console
    }
}
```

## Monitor of injection and release
Poke provides a monitor to notify observers when an instance with injectable fields is about to be injected, released.
```java
SimpleGraph graph = new SimpleGraph();

//The monitor watches the target object when it's injected or released
Graph.Monitor monitor = new Graph.Monitor() {
    @Override
    public void onInject(Object target) {
        System.io.println("This target is injected +1");
    }

    @Override
    public void onRelease(Object target) {
        System.io.println("This target is released +1");
    }
};

//Register the monitor to the graph
graph.registerMonitor(monitor);
```

To be notified when a provider is not used any more, use
```java
SimpleGraph graph = new SimpleGraph();

OnFreedListener onFreed = new OnFreedListener() {
    @Override
    public void onFreed(Provider provider) {
        System.io.println("Provides the is not used any more. Shall we release something?");
    }
};
graph.registerProviderFreedListener(onFreed);
```

To be notified when a provider is used for injection for the first time, use
```java
Provider.registerOnInjectedListener(new OnInjectedListener() {
    @Override
    public void onInjected(T object) {
        System.io.println("Provides the first instance injecting to " + object.toString());
    }
});
```

## @Singleton
You may noticed, in the component above providesPlaneRobot is also annotated by @Singleton. This indicates the instance of the providing Robot will be singleton. However, by default the it's not absolutely singleton, it's singleton relatively to the instance of the component. In other words, as long as the IndustrialComponent is not recreated all injected Robot will share the same instance, but once IndustrialComponent is re-instantiated, new Robots will be injected.

To guarantee a robot is singleton absolutely, we can either make sure the IndustrialComponent is created only once. In the example above, since it's the fields of Main class and created in main method once, it could make the robot absolutely singleton. Another way to do so it to create assign a ScopeCache to the component which is guaranteed to be singleton then multiple components could provide the same instance cached by the shared ScopeCache too. See example below

```java
public class AnotherComponent extends Component {
    @Provides
    @Singleton
    public Robot providesCleanRobot() {
        return new Robot() {
            @Override
            public void work() {
                System.io.println(String.format("Clean floor"));
            }
        };
    }
}

public class Component1 extends Component {
}

public class Main {
	private static ScopeCache scopeCache = new ScopeCache();

    public static void main(String[] args) {
    	Factory factory1 = new Factory();
        Factory factory2 = new Factory();
    
    	//The static scope cache guarantees robot absolute singleton
        AnotherComponent component1 = new AnotherComponent(scopeCache);
        AnotherComponent component2 = new AnotherComponent(scopeCache);
    
        SimpleGraph graph1 = new SimpleGraph();
		//Register the component1 to the graph1
		graph1.register(component1);
        //Inject dependencies into factory1 from graph1.
        graph1.inject(factory1, MyInject.class);	
        
        SimpleGraph graph2 = new SimpleGraph();
		//Register the component2 to the graph2
		graph2.register(component2);
        //Inject dependencies into factory2 from graph2.
        graph2.inject(factory2, MyInject.class);	
        
        //Factory1 and factory2 will share the same robot
        factory1.getRobot().work();
        factory2.getRobot().work();
    }
}
```

## @Qualifier
To inject various implementations for the same interface, standard @Qualifier and @Named(String) defined in javax.inject can be used to mark providers and injectable fields. See the example below,

###### Interface of OS
This is the type of the fields that will be injected with multiple variants of implementation
```java
public interface Os {
}
```

###### Define qualifier annotations
Qualifiers will match up the provider of implementations and injectable fields
```java
@Qualifier
@Documented
@Retention(RUNTIME)
@interface Google {
}

@Qualifier
@Documented
@Retention(RUNTIME)
@interface Microsoft {
}
```

###### Mark injectable with different qualifiers
```java
public class DevelopmentTraining {
    @MyInject
    Os ios;

    @Google
    @MyInject
    Os android;

    @Microsoft
    @MyInject
    Os windows;
}
```

To supply variants of OS, there are two ways as below

###### 1. Qualify implementation classes directly

```java
//No qualifier
public class iOs implements Os {
}

//Qualified as Google OS
@Google
public class Android implements Os {
}

//Qualified as Microsoft OS
@Microsoft
public class Windows implements Os {
}
 ```

```java
SimpleGraph graph = new SimpleGraph();
graph.register(Os.class, iOs.class);
graph.register(Os.class, Android.class);
graph.register(Os.class, Windows.class);

DevelopmentTraining training = new DevelopmentTraining();
graph.inject(training, MyInject.class);

//Default variant is iOS
Assert.assertEquals(training.ios.getClass(), iOs.class);
//android field is assigned by an Android instance
Assert.assertEquals(training.android.getClass(), Android.class);
//windows field is assigned by an Windows instance
Assert.assertEquals(training.windows.getClass(), Windows.class);
```

###### 2. Qualify @Provide methods of Component
Qualify provide methods is more flexible because
1. provide method can configure the injecting instance
2. it doesn't hard bind interface and qualified implementation classes. For example, a provide method can qualify a windows OS to a Android injectable instance.

**Note that, when using qualifier with provide methods the qualifier annotation of the implementation classes will be ignored.**

```java
Component component = new Component() {
    @Provides
    public OS providesOS1() {
        return new iOs();
    }

    @Google
    @Provides
    public OS providesOS2() {
        //Note, we can provide a Windows OS to the android field of DevelopmentTraining then
        return new Windows();
    }

    @Microsoft
    @Provides
    public OS providesOS3() {
        //Note, we can provide a Android OS to the windows field of DevelopmentTraining then
        return new Android();
    }
};

SimpleGraph graph = new SimpleGraph();
graph.register(component);

DevelopmentTraining training = new DevelopmentTraining();
graph.inject(training, MyInject.class);

//Default variant is iOS
Assert.assertEquals(training.ios.getClass(), iOs.class);
//android field is assigned by an Windows instance
Assert.assertEquals(training.android.getClass(), Windows.class);
//windows field is assigned by an Android instance
Assert.assertEquals(training.windows.getClass(), Android.class);
```