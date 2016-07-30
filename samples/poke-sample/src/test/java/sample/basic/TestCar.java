/*
 * Copyright 2016 Kejun Xia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.basic;

import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.MvcComponent;
import com.shipdream.lib.poke.Provides;

import org.junit.Test;

public class TestCar {
    private void testDrive(Car car) {
        System.out.println("Test drive starts");
        System.out.println(String.format("Speed: %.2f", car.getSpeed()));

        car.accelerate();
        System.out.println("Accelerate");
        System.out.println(String.format("Speed: %.2f", car.getSpeed()));

        car.decelerate();
        System.out.println("Decelerate");
        System.out.println(String.format("Speed: %.2f", car.getSpeed()));
    }

    @Test
    public void run_car_with_default_components() {
        Car car = new Car();
        Mvc.graph().inject(car);

        testDrive(car);
    }

    @Test
    public void run_car_with_better_components() throws Exception {
        //Prepare a new engine replacing default one
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

        //Attach the component to the graph's root component to override default providers
        boolean overrideExistingProviders = true;
        Mvc.graph().getRootComponent().attach(testComponent, overrideExistingProviders);

        Car racingCar = new Car();
        Mvc.graph().inject(racingCar);

        System.out.println("-------------------------------");
        System.out.println("Build a racing car");
        testDrive(racingCar);

        //Remove overridden providers
        Mvc.graph().getRootComponent().detach(testComponent);
        Car familyCar = new Car();
        Mvc.graph().inject(familyCar);
        System.out.println();
        System.out.println("-------------------------------");
        System.out.println("Build a family car");

        testDrive(familyCar);
    }
}
