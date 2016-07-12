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
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.AndroidPart;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.PrinterController2;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.internal.AndroidPartImpl;
import com.shipdream.lib.android.mvc.inject.testNameMapping.manager.internal.InkManagerImpl;
import com.shipdream.lib.android.mvc.inject.testNameMapping.service.Cartridge;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

public class TestInjectCustomControllerDependencies extends BaseTest {
    private static class PaperView {
        @Inject
        private PrinterController2 printerController;

        public void show() throws Exception {
            printerController.print();
        }
    }

    @Test
    public void testInjectionOfRealController() throws Exception {
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


    private static class AndroidView {
        @Inject
        private AndroidPart androidPart;
    }

    public class AndroidModule {
        @Provides
        @Singleton
        public AndroidPart provideAndroidPart() {
            Object fakeContext = new Object();
            return new AndroidPartImpl(fakeContext);
        }
    }

    @Test
    public void should_be_able_to_reinject_new_instance_without_default_constructor()
            throws ProvideException, ProviderConflictException, ProviderMissingException, CircularDependenciesException {
        AndroidModule component = new AndroidModule();

        graph.getRootComponent().register(component);

        AndroidView androidView = new AndroidView();
        Assert.assertNull(androidView.androidPart);

        graph.inject(androidView);
        Assert.assertNotNull(androidView.androidPart);

        graph.release(androidView);

        graph.inject(androidView);
        Assert.assertNotNull(androidView.androidPart);
    }

}
