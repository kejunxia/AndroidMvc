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

package com.shipdream.lib.android.mvp.presenter;

public class TestRunAsyncTask2 extends BaseTest {
//    class EventMonitor {
//        public void onEvent(MyPresenter2.ResourceLoadStarted event) {}
//        public void onEvent(MyPresenter2.ResourceLoaded event) {}
//        public void onEvent(MyPresenter2.ResourceLoadFailed event) {}
//        public void onEvent(MyPresenter2.ResourceLoadCanceled event) {}
//    }
//
//    private MyPresenter2 controller;
//    private EventMonitor eventMonitor;
//    private static final long WAIT_DURATION = MyPresenter2.LONG_TASK_DURATION + 100;
//    private int loadPercentage;
//
//    @Before
//    public void setUp() throws Exception {
//        super.setUp();
//        loadPercentage = 0;
//
//        executorService = Executors.newSingleThreadExecutor();
//
//        controller = new MyPresenter2();
//        graph.inject(controller);
//        controller.onConstruct();
//
//        eventMonitor = mock(EventMonitor.class);
//        eventBusV.register(eventMonitor);
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        executorService.shutdownNow();
//    }
//
//    @Test
//    public void should_be_able_to_run_async_task_successfully() throws Exception {
//        Monitor monitor = controller.runTask(this, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                //wait about 0.1 seconds in total
//                for (int i = 0; i < 100; i++) {
//                    Thread.sleep(1);
//                    loadPercentage = i + 1;
//                }
//            }
//        });
//
//        Thread.sleep(WAIT_DURATION);
//
//        verify(eventMonitor, times(1)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadStarted.class).capture());
//        ArgumentCaptor<MyPresenter2.ResourceLoaded> eventResourceLoaded
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoaded.class);
//        verify(eventMonitor, times(1)).onEvent(eventResourceLoaded.capture());
//
//        ArgumentCaptor<MyPresenter2.ResourceLoadFailed> eventResourceLoadFailed
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoadFailed.class);
//        verify(eventMonitor, times(0)).onEvent(eventResourceLoadFailed.capture());
//
//        ArgumentCaptor<MyPresenter2.ResourceLoadCanceled> eventResourceLoadCanceled
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoadCanceled.class);
//        verify(eventMonitor, times(0)).onEvent(eventResourceLoadCanceled.capture());
//
//        Assert.assertEquals(monitor.getState(), Monitor.State.DONE);
//        Assert.assertTrue(loadPercentage == 100);
//    }
//
//    @Test
//    public void cancelled_task_should_not_call_onSuccess() throws Exception {
//        final Task.Callback callback = mock(Task.Callback.class);
//
//        Monitor monitor = controller.runTaskCustomCallback(this, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                for (int i = 0; i < 100; i++) {
//                    loadPercentage = i + 1;
//                    Thread.sleep(2);
//                }
//
//                synchronized (callback) {
//                    callback.notify();
//                }
//            }
//        }, callback);
//
//        Thread.sleep(100);
//
//        monitor.cancel(false);
//
//        synchronized (callback) {
//            callback.wait();
//        }
//
//        Thread.sleep(100);
//
//        Assert.assertEquals(Monitor.State.CANCELED, monitor.getState());
//
//        verify(callback).onStarted();
//        verify(callback).onCancelled(eq(false));
//        verify(callback, times(0)).onSuccess();
//        verify(callback, times(0)).onException(any(Exception.class));
//        Assert.assertEquals(100, loadPercentage);
//    }
//
//    @Test
//    public void should_trigger_onException_in_custom_callback() throws Exception {
//        Task.Callback callback = mock(Task.Callback.class);
//
//        final RuntimeException e = new RuntimeException("Intentional exception");
//        Monitor monitor = controller.runTaskCustomCallback(this, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                throw e;
//            }
//        }, callback);
//
//        Thread.sleep(100);
//
//        Assert.assertEquals(Monitor.State.ERRED, monitor.getState());
//
//        verify(callback).onStarted();
//        verify(callback, times(0)).onSuccess();
//        verify(callback, times(0)).onCancelled(anyBoolean());
//        verify(callback).onException(eq(e));
//    }
//
//    @Test
//    public void should_trigger_onCancelled_with_interrupted_in_custom_callback() throws Exception {
//        final Task.Callback callback = mock(Task.Callback.class);
//
//        Monitor monitor = controller.runTaskCustomCallback(this, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                for (int i = 0; i < 100; i++) {
//                    loadPercentage = i + 1;
//                    Thread.sleep(10);
//                }
//            }
//        }, callback);
//
//        Thread.sleep(100);
//        monitor.cancel(true);
//
//        Thread.sleep(1000);
//
//        Assert.assertEquals(Monitor.State.INTERRUPTED, monitor.getState());
//
//        verify(callback).onStarted();
//        verify(callback).onCancelled(eq(true));
//
//        Assert.assertTrue(loadPercentage < 100);
//    }
//
//    @Test
//    public void should_trigger_onStarted_and_onSuccess_in_custom_callback() throws Exception {
//        Task.Callback callback = mock(Task.Callback.class);
//
//        Monitor monitor = controller.runTaskCustomCallback(this, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//
//            }
//        }, callback);
//
//        Thread.sleep(100);
//
//        Assert.assertEquals(Monitor.State.DONE, monitor.getState());
//
//        verify(callback).onStarted();
//        verify(callback).onSuccess();
//    }
//
//    @Test
//    public void should_be_able_to_run_task_without_callback() throws Exception {
//        Monitor monitor = controller.runTaskWithoutCallback(this, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//
//            }
//        });
//
//        Thread.sleep(100);
//
//        Assert.assertEquals(Monitor.State.DONE, monitor.getState());
//    }
//
//    @Test
//    public void should_detect_exception_and_post_exception_event() throws Exception {
//        final Object lock = new Object();
//
//        Monitor monitor = controller.runTask(this, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                //wait about 3 seconds in total
//                for (int i = 0; i < 100; i++) {
//                    Thread.sleep(30);
//                    loadPercentage = i + 1;
//
//                    if (i == 5) {
//                        synchronized (lock) {
//                            lock.notify();
//                        }
//                        throw new RuntimeException("Something went wrong");
//                    }
//                }
//            }
//        });
//
//        synchronized (lock) {
//            lock.wait();
//        }
//        verify(eventMonitor, times(1)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadStarted.class).capture());
//        ArgumentCaptor<MyPresenter2.ResourceLoaded> eventResourceLoaded
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoaded.class);
//        verify(eventMonitor, times(0)).onEvent(eventResourceLoaded.capture());
//
//        ArgumentCaptor<MyPresenter2.ResourceLoadFailed> eventResourceLoadFailed
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoadFailed.class);
//        verify(eventMonitor, times(1)).onEvent(eventResourceLoadFailed.capture());
//
//        ArgumentCaptor<MyPresenter2.ResourceLoadCanceled> eventResourceLoadCanceled
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoadCanceled.class);
//        verify(eventMonitor, times(0)).onEvent(eventResourceLoadCanceled.capture());
//
//        Assert.assertEquals(monitor.getState(), Monitor.State.ERRED);
//        Assert.assertTrue(loadPercentage < 100);
//    }
//
//    @Test
//    public void should_be_able_to_cancel_task_before_it_runs() throws Exception {
//        final Object lock = new Object();
//
//        ExecutorService singleThreadService = Executors.newSingleThreadExecutor();
//        final Task task1 = new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                synchronized (lock) {
//                    lock.wait(200);
//                }
//            }
//        };
//
//        abstract class T2 implements Task {
//            Runnable onDone;
//        }
//        T2 task2 = new T2() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                synchronized (lock) {
//                    lock.wait(200);
//                }
//                onDone.run();
//                synchronized (lock) {
//                    lock.notifyAll();
//                }
//            }
//
//        };
//
//        Monitor monitor2 = controller.runTask(this, singleThreadService, task2);
//        final Monitor monitor1 = controller.runTask(this, singleThreadService, task1);
//        task2.onDone = new Runnable() {
//            @Override
//            public void run() {
//                //cancel task1 before it starts
//                monitor1.cancel(false);
//            }
//        };
//
//        synchronized (lock) {
//            lock.wait();
//        }
//
//        ArgumentCaptor<MyPresenter2.ResourceLoadStarted> eventResourceLoadStarted
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoadStarted.class);
//        //Only captures once since task1 should have been cancelled to start by task2
//        verify(eventMonitor, times(1)).onEvent(eventResourceLoadStarted.capture());
//        Assert.assertTrue(task2 == eventResourceLoadStarted.getValue().getTask());
//
//        ArgumentCaptor<MyPresenter2.ResourceLoaded> eventResourceLoaded
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoaded.class);
//        verify(eventMonitor, times(1)).onEvent(eventResourceLoaded.capture());
//        Assert.assertTrue(task2 == eventResourceLoadStarted.getValue().getTask());
//
//        ArgumentCaptor<MyPresenter2.ResourceLoadFailed> eventResourceLoadFailed
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoadFailed.class);
//        verify(eventMonitor, times(0)).onEvent(eventResourceLoadFailed.capture());
//
//        ArgumentCaptor<MyPresenter2.ResourceLoadCanceled> eventResourceLoadCanceled
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoadCanceled.class);
//        verify(eventMonitor, times(1)).onEvent(eventResourceLoadCanceled.capture());
//        Assert.assertTrue(task1 == eventResourceLoadCanceled.getValue().getTask());
//
//        Assert.assertFalse(eventResourceLoadCanceled.getValue().isInterrupted());
//        Assert.assertEquals(monitor1.getState(), Monitor.State.CANCELED);
//        Assert.assertEquals(monitor2.getState(), Monitor.State.DONE);
//    }
//
//    @Test
//    public void should_be_able_to_interrupted_task_while_it_is_running() throws Exception {
//        Monitor monitor = controller.runTask(this, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                //wait about 3 seconds in total
//                for (int i = 0; i < 100; i++) {
//                    Thread.sleep(30);
//                    loadPercentage = i + 1;
//                }
//            }
//        });
//
//        Thread.sleep(WAIT_DURATION);
//
//        monitor.cancel(true);
//
//        verify(eventMonitor, times(1)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadStarted.class).capture());
//        verify(eventMonitor, times(0)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoaded.class).capture());
//        verify(eventMonitor, times(0)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadFailed.class).capture());
//
//        ArgumentCaptor<MyPresenter2.ResourceLoadCanceled> eventResourceLoadCanceled
//                = ArgumentCaptor.forClass(MyPresenter2.ResourceLoadCanceled.class);
//        verify(eventMonitor, times(1)).onEvent(eventResourceLoadCanceled.capture());
//        Assert.assertTrue(eventResourceLoadCanceled.getValue().isInterrupted());
//
//        Assert.assertEquals(monitor.getState(), Monitor.State.INTERRUPTED);
//        //Should have not finished
//        Assert.assertTrue(loadPercentage < 100);
//    }
//
//    @Test
//    public void cancel_done_task_should_has_no_effect() throws Exception {
//        final Object waiter = new Object();
//        Monitor monitor = controller.runTask(this, executorService, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                for (int i = 0; i < 100; i++) {
//                    Thread.sleep(1);
//                    loadPercentage = i + 1;
//                }
//                synchronized (waiter) {
//                    //continue the test
//                    waiter.notify();
//                }
//            }
//        });
//
//        synchronized (waiter) {
//            //Wait task finish
//            waiter.wait();
//        }
//
//        boolean cancelled = monitor.cancel(true);
//        Assert.assertFalse(cancelled);
//
//        verify(eventMonitor, times(1)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadStarted.class).capture());
//        verify(eventMonitor, times(1)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoaded.class).capture());
//        verify(eventMonitor, times(0)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadFailed.class).capture());
//        verify(eventMonitor, times(0)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadCanceled.class).capture());
//
//        Assert.assertEquals(monitor.getState(), Monitor.State.DONE);
//        //Should have not finished
//        Assert.assertEquals(100, loadPercentage);
//    }
//
//    @Test
//    public void cancel_without_interrupted_flag_on_running_task_should_has_no_effect() throws Exception {
//        Monitor monitor = controller.runTask(this, executorService, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                throw new RuntimeException("Intentional error");
//            }
//        });
//
//        Thread.sleep(200);
//
//        boolean cancelled = monitor.cancel(true);
//        Assert.assertFalse(cancelled);
//
//        verify(eventMonitor, times(1)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadStarted.class).capture());
//        verify(eventMonitor, times(0)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoaded.class).capture());
//        verify(eventMonitor, times(1)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadFailed.class).capture());
//        verify(eventMonitor, times(0)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadCanceled.class).capture());
//
//        Assert.assertEquals(monitor.getState(), Monitor.State.ERRED);
//    }
//
//    @Test
//    public void cancel_erred_task_should_has_no_effect() throws Exception {
//        Monitor monitor = controller.runTask(this, executorService, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                throw new RuntimeException("Intentional error");
//            }
//        });
//
//        Thread.sleep(200);
//
//        boolean cancelled = monitor.cancel(true);
//        Assert.assertFalse(cancelled);
//
//        verify(eventMonitor, times(1)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadStarted.class).capture());
//        verify(eventMonitor, times(0)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoaded.class).capture());
//        verify(eventMonitor, times(1)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadFailed.class).capture());
//        verify(eventMonitor, times(0)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadCanceled.class).capture());
//
//        Assert.assertEquals(monitor.getState(), Monitor.State.ERRED);
//    }
//
//    @Test
//    public void cancel_cancelled_task_should_has_no_effect() throws Exception {
//        final Object lock = new Object();
//        Task t1 = new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                Thread.sleep(200);
//
//                synchronized (lock) {
//                    lock.notify();
//                }
//            }
//        };
//
//        Task t2 = new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//            }
//        };
//
//        Monitor monitor1 = controller.runTask(this, executorService, t1);
//        Monitor monitor2 = controller.runTask(this, executorService, t2);
//
//        Assert.assertTrue(monitor2.cancel(true));
//        synchronized (lock) {
//            //wait t1 finish
//            lock.wait();
//        }
//
//        Thread.sleep(100);
//
//        Assert.assertEquals(Monitor.State.DONE, monitor1.getState());
//        Assert.assertEquals(Monitor.State.CANCELED, monitor2.getState());
//        //Cancel again should have no effect
//        Assert.assertFalse(monitor2.cancel(true));
//
//        ArgumentCaptor<MyPresenter2.ResourceLoadStarted> loaded = ArgumentCaptor.forClass(MyPresenter2.ResourceLoadStarted.class);
//        verify(eventMonitor, times(1)).onEvent(loaded.capture());
//        Assert.assertTrue(t1 == loaded.getValue().getTask());
//
//        ArgumentCaptor<MyPresenter2.ResourceLoaded> done = ArgumentCaptor.forClass(MyPresenter2.ResourceLoaded.class);
//        verify(eventMonitor, times(1)).onEvent(done.capture());
//        Assert.assertTrue(t1 == done.getValue().getTask());
//
//        verify(eventMonitor, times(0)).onEvent(ArgumentCaptor.forClass(MyPresenter2.ResourceLoadFailed.class).capture());
//
//        ArgumentCaptor<MyPresenter2.ResourceLoadCanceled> cancelEvent = ArgumentCaptor.forClass(MyPresenter2.ResourceLoadCanceled.class);
//        verify(eventMonitor, times(1)).onEvent(cancelEvent.capture());
//        Assert.assertTrue(monitor2.getTask() == cancelEvent.getValue().getTask());
//
//        Assert.assertEquals(monitor1.getState(), Monitor.State.DONE);
//    }
//
//    @Test
//    public void cancel_interrupted_task_should_has_no_effect() throws Exception {
//        final Object lock = new Object();
//        Monitor monitor = controller.runTask(this, executorService, new Task() {
//            @Override
//            public void execute(Monitor monitor) throws Exception {
//                synchronized (lock) {
//                    lock.wait();
//                }
//            }
//        });
//
//        Thread.sleep(200);
//
//        Assert.assertTrue(monitor.cancel(true));
//
//        Assert.assertEquals(monitor.getState(), Monitor.State.INTERRUPTED);
//        //Cancel again should have no effect
//        Assert.assertFalse(monitor.cancel(true));
//    }

}
