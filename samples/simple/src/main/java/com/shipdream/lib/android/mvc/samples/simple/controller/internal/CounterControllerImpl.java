/*
 * Copyright 2015 Kejun Xia
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

/**
 * Note the structure of the package name. It is in a subpackage(internal) sharing the same parent
 * package as the controller interface CounterController
 */
package com.shipdream.lib.android.mvc.samples.simple.controller.internal;

import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.samples.simple.controller.CounterController;
import com.shipdream.lib.android.mvc.samples.simple.model.CounterModel;

import javax.inject.Inject;

/**
 * Note the class name is [CounterController]Impl.
 */
public class CounterControllerImpl extends BaseControllerImpl<CounterModel> implements CounterController{
    @Inject
    NavigationController navigationController;

    /**
     * Just return the class type of the model managed by this controller
     */
    @Override
    protected Class<CounterModel> getModelClassType() {
        return CounterModel.class;
    }

    @Override
    public void increment(Object sender) {
        int count = getModel().getCount();
        getModel().setCount(++count);
        //Post controller to view event to views
        postC2VEvent(new EventC2V.OnCounterUpdated(sender, count, convertNumberToEnglish(count)));
    }

    @Override
    public void decrement(Object sender) {
        int count = getModel().getCount();
        getModel().setCount(--count);
        //Post controller to view event to views
        postC2VEvent(new EventC2V.OnCounterUpdated(sender, count, convertNumberToEnglish(count)));
    }

    @Override
    public String convertNumberToEnglish(int number) {
        if (number < -3) {
            return "Less than negative three";
        } else  if (number == -3) {
            return "Negative three";
        } else  if (number == -2) {
            return "Negative two";
        } else  if (number == -1) {
            return "Negative one";
        } else if (number == 0) {
            return "Zero";
        } else if (number == 1) {
            return "One";
        } else if (number == 2) {
            return "Two";
        } else if (number == 3) {
            return "Three";
        } else {
            return "Greater than three";
        }
    }

    @Override
    public void goToAdvancedView(Object sender) {
        navigationController.navigateTo(sender, "LocationB");
    }

    @Override
    public void goBackToBasicView(Object sender) {
        navigationController.navigateBack(sender);
    }
}
