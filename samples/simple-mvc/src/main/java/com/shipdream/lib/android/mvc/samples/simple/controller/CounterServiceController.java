package com.shipdream.lib.android.mvc.samples.simple.controller;

import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Controller;
import com.shipdream.lib.android.mvc.samples.simple.manager.CounterManager;
import com.shipdream.lib.android.mvc.samples.simple.service.Poster;

import javax.inject.Inject;

public class CounterServiceController extends Controller {
    @Override
    public Class modelType() {
        return null;
    }

    public interface View {
        void onCounterUpdated(int count);
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

                    poster.postDelayed(this, 1000);
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
    private Poster poster;

    @Inject
    private NavigationManager navigationManager;

    @Inject
    private CounterManager counterManager;

    public View view;

    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        view.onCounterUpdated(event.getCount());
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
