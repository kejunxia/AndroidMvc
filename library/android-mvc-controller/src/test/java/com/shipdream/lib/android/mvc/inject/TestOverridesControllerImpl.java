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

package com.shipdream.lib.android.mvc.inject;

import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.PrintController;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.PrintModel;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.internal.PrintControllerImpl;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

public class TestOverridesControllerImpl extends BaseTestCases {
    private static class TestView {
        @Inject
        private PrintController printController;
    }

    public static class MockPrinter extends BaseControllerImpl<PrintModel> implements PrintController {
        private final String printContent = "Mock content";

        @Override
        public String print() {
            return printContent;
        }
        @Override
        public Class<PrintModel> getModelClassType() {
            return null;
        }
        @Override
        public void bindModel(Object sender, PrintModel printModel) {
        }
    }

    public static class PrinterComponent extends Component {
        private PrintController printController;

        public PrinterComponent(PrintController printController) {
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
        MvcGraph graph = new MvcGraph(new BaseControllerDependencies());

        graph.register(new PrinterComponent(new MockPrinter()));

        TestView testView = new TestView();
        graph.inject(testView);

        Assert.assertEquals(testView.printController.getClass(), MockPrinter.class);
        Assert.assertEquals(testView.printController.print(), ((MockPrinter)testView.printController).printContent);
    }

    @Test
    public void shouldRecoverDefaultUsingDefaultImplAfterExplicitlyUnregistration()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        MvcGraph graph = new MvcGraph(new BaseControllerDependencies());

        PrinterComponent component = new PrinterComponent(new MockPrinter());
        graph.register(component);

        TestView testView = new TestView();
        graph.inject(testView);

        Assert.assertEquals(testView.printController.getClass(), MockPrinter.class);
        Assert.assertEquals(testView.printController.print(), ((MockPrinter)testView.printController).printContent);

        graph.unregister(component);
        TestView testView1 = new TestView();
        graph.inject(testView1);

        Assert.assertEquals(testView1.printController.getClass(), PrintControllerImpl.class);
        PrintModel printModel = new PrintModel();
        printModel.setContent("Hello Poke");
        testView1.printController.bindModel(this, printModel);
        Assert.assertEquals(testView1.printController.print(), testView1.printController.getModel().getContent());
    }

}
