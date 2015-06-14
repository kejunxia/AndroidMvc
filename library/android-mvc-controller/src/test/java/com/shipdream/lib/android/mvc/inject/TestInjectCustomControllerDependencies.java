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
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.PrinterController2;
import com.shipdream.lib.android.mvc.inject.testNameMapping.manager.internal.InkManagerImpl;
import com.shipdream.lib.android.mvc.inject.testNameMapping.service.Cartridge;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

public class TestInjectCustomControllerDependencies extends BaseTestCases {
    private static class PaperView {
        @Inject
        private PrinterController2 printerController;

        public void show() throws Exception {
            printerController.print();
        }
    }

    @Test
    public void testInjectionOfRealController() throws Exception {
        MvcGraph graph = new MvcGraph(new BaseControllerDependencies());

        PaperView testView = new PaperView();
        graph.inject(testView);

        Assert.assertNotNull(testView.printerController.getInkManager());
        testView.printerController.getInkManager().refill(100);
        Assert.assertNotNull(testView.printerController);
        Assert.assertNotNull(testView.printerController.getInkManager());
        Cartridge cartridge = ((InkManagerImpl) testView.printerController.getInkManager()).getCartridge();
        Assert.assertNotNull(cartridge);

        Assert.assertNull(cartridge.getInkManager());
        testView.printerController.getInkManager().setup();
        Assert.assertNotNull(cartridge.getInkManager());

        testView.show();
    }

}
