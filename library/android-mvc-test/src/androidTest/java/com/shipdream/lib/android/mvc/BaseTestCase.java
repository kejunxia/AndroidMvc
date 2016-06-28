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

package com.shipdream.lib.android.mvc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.shipdream.lib.android.mvc.view.LifeCycleValidator;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorA;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorB;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorC;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorD;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BaseTestCase <T extends MvcActivity> extends ActivityInstrumentationTestCase2<T> {
    protected LifeCycleValidator lifeCycleValidator;
    protected LifeCycleMonitor lifeCycleMonitorMock;

    protected LifeCycleMonitorA lifeCycleMonitorMockA;
    protected LifeCycleValidator lifeCycleValidatorA;
    protected LifeCycleMonitorB lifeCycleMonitorMockB;
    protected LifeCycleValidator lifeCycleValidatorB;
    protected LifeCycleMonitorC lifeCycleMonitorMockC;
    protected LifeCycleValidator lifeCycleValidatorC;
    protected LifeCycleMonitorD lifeCycleMonitorMockD;
    protected LifeCycleValidator lifeCycleValidatorD;

    @Inject
    protected NavigationManager navigationManager;

    protected T activity;
    protected android.app.Instrumentation instrumentation;

    public BaseTestCase(Class<T> activityClass) {
        super(activityClass);
    }

    @BeforeClass
    public static void beforeClass() {
        configureLogbackDirectly();
    }

    private static void configureLogbackDirectly() {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();

        // setup LogcatAppender
        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(lc);
        encoder2.setPattern("[%thread] %msg%n");
        encoder2.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.start();

        // backup the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(logcatAppender);

        root.setLevel(Level.ALL);
    }

    private MvcComponent lifeCycleCom;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        prepareDependencies();

        lifeCycleMonitorMock = mock(LifeCycleMonitor.class);
        lifeCycleValidator = new LifeCycleValidator(lifeCycleMonitorMock);

        lifeCycleMonitorMockA = mock(LifeCycleMonitorA.class);
        lifeCycleValidatorA = new LifeCycleValidator(lifeCycleMonitorMockA);

        lifeCycleMonitorMockB = mock(LifeCycleMonitorB.class);
        lifeCycleValidatorB = new LifeCycleValidator(lifeCycleMonitorMockB);

        lifeCycleMonitorMockC = mock(LifeCycleMonitorC.class);
        lifeCycleValidatorC = new LifeCycleValidator(lifeCycleMonitorMockC);

        lifeCycleMonitorMockD = mock(LifeCycleMonitorD.class);
        lifeCycleValidatorD = new LifeCycleValidator(lifeCycleMonitorMockD);

        lifeCycleCom = new MvcComponent("LifeCycleComponent");
        lifeCycleCom.register(new Object(){
            @Provides
            public LifeCycleMonitor provideLifeCycleMonitor() {
                return lifeCycleMonitorMock;
            }

            @Provides
            public LifeCycleMonitorA provideLifeCycleMonitorA() {
                return lifeCycleMonitorMockA;
            }

            @Provides
            public LifeCycleMonitorB provideLifeCycleMonitorB() {
                return lifeCycleMonitorMockB;
            }

            @Provides
            public LifeCycleMonitorC provideLifeCycleMonitorC() {
                return lifeCycleMonitorMockC;
            }

            @Provides
            public LifeCycleMonitorD provideLifeCycleMonitorD() {
                return lifeCycleMonitorMockD;
            }
        });

        Mvc.graph().getRootComponent().attach(lifeCycleCom, true);

        instrumentation = InstrumentationRegistry.getInstrumentation();
        injectInstrumentation(instrumentation);
        activity = getActivity();

        Mvc.graph().inject(this);
    }

    protected void prepareDependencies() throws ProvideException, ProviderConflictException {
    }

    @After
    public void tearDown() throws Exception {
        if (activity == null) {
            return;
        }

        navigationManager.navigate(this).back(null);
        navigationManager.navigate(this).back();

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        Mvc.graph().release(this);

        if (lifeCycleCom != null && lifeCycleCom.getParent() != null) {
            Mvc.graph().getRootComponent().detach(lifeCycleCom);
        }
        super.tearDown();
    }

    protected void pressHome() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        activity.startActivity(startMain);
    }

    protected void bringBack() {
        Intent i = new Intent(activity.getApplicationContext(), activity.getClass());
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(i);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    protected boolean isDontKeepActivities() throws Settings.SettingNotFoundException {
        try {
            int val;
            if (Build.VERSION.SDK_INT > 16) {
                val = Settings.System.getInt(activity.getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES);
            } else {
                val = Settings.System.getInt(activity.getContentResolver(), Settings.System.ALWAYS_FINISH_ACTIVITIES);
            }
            return val != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Thread sleeps for 0 ms by default
     * @throws InterruptedException
     */
    protected void waitTest() throws InterruptedException {
        waitTest(0);
    }

    /**
     * Thread sleeps for given ms
     * @param duration how long to wait
     * @throws InterruptedException
     */
    protected void waitTest(long duration) throws InterruptedException {
        Thread.sleep(duration);
    }
}
