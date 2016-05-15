package com.shipdream.lib.android.mvc.samples.simple.mvp.manager;

import com.shipdream.lib.android.mvc.event.BaseEventC;
import com.shipdream.lib.android.mvc.manager.internal.BaseManagerImpl;

public class CounterManager extends BaseManagerImpl<CounterManager.CounterModel>{
    /**
     * Namespace the events for this controller by nested interface so that all its events would
     * be referenced as CounterController.EventC2V.BlaBlaEvent.
     */
    public interface Event2C {
        /**
         * Event2C to notify views counter has been updated
         */
        class OnCounterUpdated extends BaseEventC {
            private final int count;
            public OnCounterUpdated(Object sender, int count) {
                super(sender);
                this.count = count;
            }

            public int getCount() {
                return count;
            }
        }
    }

    public static class CounterModel {
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    /**
     * Just return the class type of the model managed by this controller
     */
    @Override
    public Class<CounterModel> modelType() {
        return CounterModel.class;
    }

    public void setCount(Object sender, int count) {
        getModel().setCount(count);
        postEvent2C(new Event2C.OnCounterUpdated(sender, count));
    }
}
