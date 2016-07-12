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

package com.shipdream.lib.android.mvc.inject;

import com.shipdream.lib.android.mvc.BaseTest;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.PrintController;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.PrintModel;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

public class TestOverridesControllerImpl extends BaseTest {
    private static class TestView {
        @Inject
        private PrintController printController;
    }

    public static class MockPrinter extends PrintController {
        private final String printContent = "Mock content";

        @Override
        public String print() {
            return printContent;
        }
        @Override
        public Class<PrintModel> modelType() {
            return null;
        }
    }

    public static class PrinterModule  {
        private PrintController printController;

        public PrinterModule(PrintController printController) {
            this.printController = printController;
        }

        @Provides
        public PrintController providePrintController() {
            return printController;
        }
    }

    @Test
    public void controllerShouldBeOverrideAfterExplicitlyRegistration()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        graph.getRootComponent().register(new PrinterModule(new MockPrinter()));

        TestView testView = new TestView();
        graph.inject(testView);

        Assert.assertEquals(testView.printController.getClass(), MockPrinter.class);
        Assert.assertEquals(testView.printController.print(), ((MockPrinter)testView.printController).printContent);
    }

    @Test
    public void shouldRecoverDefaultUsingDefaultImplAfterExplicitlyUnregistration()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        PrinterModule component = new PrinterModule(new MockPrinter());
        graph.getRootComponent().register(component);

        TestView testView = new TestView();
        graph.inject(testView);

        Assert.assertEquals(testView.printController.getClass(), MockPrinter.class);
        Assert.assertEquals(testView.printController.print(), ((MockPrinter)testView.printController).printContent);

        graph.getRootComponent().unregister(component);
        TestView testView1 = new TestView();
        graph.inject(testView1);

        Assert.assertEquals(testView1.printController.getClass(), PrintController.class);
        PrintModel printModel = new PrintModel();
        printModel.setContent("Hello Poke");
        testView1.printController.bindModel(printModel);
        Assert.assertEquals(testView1.printController.print(), testView1.printController.getModel().getContent());
    }

}
