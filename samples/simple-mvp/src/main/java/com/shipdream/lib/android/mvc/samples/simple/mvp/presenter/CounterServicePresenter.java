package com.shipdream.lib.android.mvc.samples.simple.mvp.presenter;

import com.shipdream.lib.android.mvp.NavigationManager;
import com.shipdream.lib.android.mvp.AbstractPresenter;
import com.shipdream.lib.android.mvc.samples.simple.mvp.manager.CounterManager;
import com.shipdream.lib.android.mvc.samples.simple.mvp.service.Poster;

import javax.inject.Inject;

public class CounterServicePresenter extends AbstractPresenter {
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
