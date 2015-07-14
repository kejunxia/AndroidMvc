# Poke - Dependency Injection with Monitors

## Overview
**Poke** is a dependency injection framework inspired by - [Dagger](http://square.github.io/dagger/).

Why reinvent the wheel?
* The main reason is to incorporate with [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) framework, it needs to do **reference count**. Since [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) automatically saves and restores state of controllers, when a controller is not used its state won't need to be managed any more. So [AndroidMvc](http://kejunxia.github.io/AndroidMvc/) needs to know when a controller is not referenced.
* Another reason: Dagger requires to register implementations manually to satisfy all injectable objects. The reason behind this is that Android apps need to scan through the whole dex file to find out implementations. This is expensive which also slows down startup of Android apps. To avoid scanning the whole dex file, manual registration is more efficient for runtime.
 
  But it is a little tedious. To find the balance between simpler code and runtime performance, **Poke** can use naming convention to automatically locate implementations. So we don't need to register implementations one by one in application. Still, **Poke** allows registering implementation manually when needed with various ways. This is helpful either for dynamical replacement of implementation or for mocking in unit tests.


## Get started

#### Name convention to locate implementation of injectable object
By default, **Poke** is trying to locate implementation by naming convention. The rule is, to find the concrete class of an interface, it looks for the implementation class with the same as the interface but with a suffix "Impl" under the "internal" sub package which locates on the same level as the interface. 

For example, when we have an interface called Robot under com.xyz. To let Poke find its implementation automatically, the concrete class should call Robot**Impl** and resides under com.xyz.**internal***. See the file structure below

```
--com
  --xyz
    Robot.java
    --internal
      RobotImpl.java
  
```

#### Example to inject a Robot into a Factory

##### The interface defining Robot
```java
package com.xyz        //Note the package

public interface Robot {
    void work();
}
```

##### Implementation of Food which should be under package com.xyz.**internal**
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

##### Custom injection annotation
Poke allows us to use any annotations to mark injectable class fields. Here we define an annotation called @MyInject. Of course the standard @Inject is also working. 

Not hard coding the annotation could help us handle different kinds of injection specifically. For example, @InjectView and @InjectController could do different things besides basic injection. It's up to your implementation.

```java
@Target({FIELD })
@Retention(RUNTIME)
@Documented
public @interface MyInject {
}
```

##### Factory wants a Robot instance injected.
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


##### Instantiate a Factory object and call the injection
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

## Injection registration
Poke allows directly registering implementation manually by methods below if we don't want to use default naming convention based rule to locate implementation.
```java
SimpleGraph.register(Class<T>type, String implementationClassName)
```
Or
```java
SimpleGraph.register(Class<T> type, Class<S extends T> implementationClass)
```

But as you can see non of the methods above including the naming convention solution doesn't allow us to config the instance to be injected. To target this issue, there is another more flexible way to register the binding between interface and its implmemention.

#### Component
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

##### @Singleton
You may noticed, in the component above providesPlaneRobot is also annotated by @Singleton. This indicates the instance of the providing Robot will be singleton. However, by default the it's not absolutly singleton, it's singleton relativly to the instance of the component. In otherwords, as long as the IndustrialComponent is not recreated all injected Robot will share the same instance, but once IndustrialComponent is reinstantiated, new Robots will be injected.

To guarantee a robot is singleton absolutely, we can either make sure the IndustrialComponent is created only once. In the example above, since it's the fields of Main class and created in main method once, it could make the robot absolutly singleton. Another way to do so it to create assign a ScopeCache to the component which is guaranteed to be singleton then multiple components could provide the same instance cached by the shared ScopeCache too. See example below

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
    
    	//The static scope cache gurantees when
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