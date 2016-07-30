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

public class Main {
    public static void main(String[] args) {
        runCarWithDefaultComponent();
    }

    static void runCarWithDefaultComponent() {
        Car car = new Car();
        Mvc.graph().inject(car);

        System.out.println("New car built");
        System.out.println(String.format("Speed: %f", car.getSpeed()));

        car.accelerate();
        System.out.println("Accelerate");
        System.out.println(String.format("Speed: %f", car.getSpeed()));

        car.decelerate();
        System.out.println("Decelerate");
        System.out.println(String.format("Speed: %f", car.getSpeed()));
    }
}
