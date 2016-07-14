package com.shipdream.lib.android.mvc.samples.simple.controller;

import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.UiView;
import com.shipdream.lib.android.mvc.samples.simple.manager.CounterManager;

import javax.inject.Inject;

public class CounterServiceController extends AbstractController<Void, CounterServiceController.View> {
    @Override
    public Class modelType() {
        return null;
    }

    public interface View extends UiView{
        void counterFinished();
    }

    private class AutoCounter implements Runnable {
        private int count = 0;
        private boolean canceled = false;

        @Override
        public void run() {
            if (!canceled) {
                if (count ++ <= AUTO_FINISH_COUNT) {
                    counterManager.setCount(this, counterManager.getModel().getCount() + 1);

                    uiThreadRunner.postDelayed(this, 1000);
                } else {
                    view.counterFinished();
                }
            }
        }

        private void cancel() {
            canceled = true;
        }
    }

    private static final int AUTO_FINISH_COUNT = 10;
    private AutoCounter autoCounter;

    @Inject
    private NavigationManager navigationManager;

    @Inject
    private CounterManager counterManager;

    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        view.update();
    }

    public int getCount() {
        return counterManager.getModel().getCount();
    }

    public void startAutoIncrement() {
        stopAutoIncrement();
        autoCounter = new AutoCounter();
        autoCounter.run();
    }

    public void stopAutoIncrement() {
        if (autoCounter != null) {
            autoCounter.cancel();
        }
    }
}
