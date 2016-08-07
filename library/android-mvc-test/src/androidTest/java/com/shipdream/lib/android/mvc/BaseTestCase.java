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

package com.shipdream.lib.android.mvc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;
import com.shipdream.lib.android.mvc.view.LifeCycleValidator;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorA;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorB;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorC;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorD;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
@LargeTest
public abstract class BaseTestCase<T extends TestActivity> extends ActivityInstrumentationTestCase2<T> {
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
    @EventBusV
    private EventBus eventBusV;

    protected abstract Class<T> getActivityClass();

    private static class Waiter {
        boolean skip = false;
        private String name;
        private long timeout;

        public Waiter(String name) {
            this (name, 1000);
        }

        public Waiter(String name, long timeout) {
            this.name = name;
            this.timeout = timeout;
        }

        private void skip() {
            skip = true;
        }

        private void waitNow() {
            long start = System.currentTimeMillis();
            while (true) {
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed > timeout) {
                    Log.w("TrackLifeSync", name + " times out by " + timeout + "ms");
                    break;
                }
                if (skip) {
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Inject
    protected NavigationManager navigationManager;

    protected T activity;
    protected android.app.Instrumentation instrumentation;

    public BaseTestCase(Class<T> activityClass) {
        super(activityClass);
    }

    protected MvcComponent component;

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

    @Before
    public void setUp() throws Exception {
        super.setUp();

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

        component = new MvcComponent("UnitTestComponent");
        component.register(new Object() {
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

        prepareDependencies(component);
        Mvc.graph().getRootComponent().attach(component, true);

        instrumentation = InstrumentationRegistry.getInstrumentation();
        injectInstrumentation(instrumentation);
        activity = getActivity();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Mvc.graph().inject(BaseTestCase.this);
                eventBusV.register(BaseTestCase.this);
            }
        });

        instrumentation.waitForIdleSync();
    }

    protected void prepareDependencies(MvcComponent testComponent) throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        navigationManager.navigate(this).back(null);
        navigationManager.navigate(this).back();
        try {
            Mvc.graph().getRootComponent().getCache().clear();
            Mvc.graph().getRootComponent().detach(component);
        } catch (Component.MismatchDetachException e) {
            e.printStackTrace();
        }

        super.tearDown();
    }

    protected void navTo(final Class cls) {
        navTo(cls, null);
    }

    protected void navTo(final Class cls, final Forwarder forwarder) {
        final Waiter waiter = new Waiter("NavTo " + cls.getSimpleName() + " ", 200);
        navigationManager.navigate(this).onSettled(new Navigator.OnSettled() {
            @Override
            public void run() {
                waiter.skip();
                Log.v("TrackLifeSync:NavTo", "skip");
            }
        }).to(cls, forwarder);

        waiter.waitNow();
        Log.v("TrackLifeSync:NavTo", "finish");
    }

    protected void navigateBackByFragment() throws InterruptedException {
        final Waiter waiter = new Waiter("NavBack");
        navigationManager.navigate(this).onSettled(new Navigator.OnSettled() {
            @Override
            public void run() {
                waiter.skip();
            }
        }).back();

        waiter.waitNow();
        waitTest();
    }

    protected String pressHome() {
        String ticket = "PressHome: " + UUID.randomUUID();
        TestActivity.ticket = ticket;

        final Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        final Waiter waiter = new Waiter("PressHome", 500);
        TestActivity.Proxy proxy = new TestActivity.Proxy() {
            @Override
            protected void onPause() {
                waiter.skip();
                Log.v("TrackLifeSync:Home", "Pause and skip");
                activity.removeProxy(this);
            }
        };
        activity.addProxy(proxy);

        activity.startActivity(startMain);
        Log.v("TrackLifeSync:Home", "Start home activity");

        TestActivity.State state = activity.getState();
        if (state != null && state.ordinal() >= TestActivity.State.PAUSE.ordinal()) {
            //ready
        } else {
            Log.v("TrackLifeSync:Home", "Start wait");
            waiter.waitNow();
        }
        activity.removeProxy(proxy);

        Log.v("TrackLifeSync:Home", "Finish");

        try {
            waitTest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ticket;
    }

    private Map<String, Waiter> bringBackWaiters = new ConcurrentHashMap<>();
    private void onEvent(TestActivity.Event.OnFragmentsResumed event) {
        Waiter waiter = bringBackWaiters.get(event.sender);
        if (waiter != null) {
            waiter.skip();
            Log.v("TrackLifeSync:BringBack", "Skip waiting bringBack " + event.sender);
            bringBackWaiters.remove(event.sender);
        }
    }

    protected void bringBack(String ticket) {
        final Intent i = new Intent(activity, activity.getClass());
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        boolean kill = false;
        try {
            kill = isDontKeepActivities();
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (kill) {
            final Waiter waiter = new Waiter("BringBack", 2000);
            bringBackWaiters.put(ticket, waiter);
            Log.v("TrackLifeSync:BringBack", "Ticket: " + ticket);

            activity.startActivity(i);
            waiter.waitNow();

            bringBackWaiters.remove(ticket);
        } else {
            final Waiter waiter = new Waiter("BringBack");
            TestActivity.Proxy proxy = new TestActivity.Proxy() {
                @Override
                protected void onResumeFragments() {
                    waiter.skip();
                    activity.removeProxy(this);
                }
            };
            activity.addProxy(proxy);

            activity.startActivity(i);

            TestActivity.State state = activity.getState();
            if (state != null && state.ordinal() >= TestActivity.State.RESUME_FRAGMENTS.ordinal()) {
                //ready
                activity.removeProxy(proxy);
            } else {
                waiter.waitNow();
            }
        }
        try {
            waitTest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void waitActivityResume(final TestActivity activity) {
        final Waiter waiter = new Waiter("ActivityResume");
        TestActivity.Proxy proxy = new TestActivity.Proxy() {
            @Override
            protected void onResume() {
                waiter.skip();
            }
        };
        activity.addProxy(proxy);
        waiter.waitNow();
        activity.removeProxy(proxy);
    }

    protected void startActivity(Intent intent) {
        activity.startActivity(intent);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void rotateMainActivity(final int orientation) {
        final Waiter waiter = new Waiter("Rotate", 500);
        activity.delegateFragment.registerOnViewReadyListener(new Runnable() {
            @Override
            public void run() {
                waiter.skip();
                activity.delegateFragment.unregisterOnViewReadyListener(this);
            }
        });

        activity.setRequestedOrientation(orientation);

        waiter.waitNow();
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
     *
     * @throws InterruptedException
     */
    protected void waitTest() throws InterruptedException {
        getInstrumentation().waitForIdleSync();
    }
}
