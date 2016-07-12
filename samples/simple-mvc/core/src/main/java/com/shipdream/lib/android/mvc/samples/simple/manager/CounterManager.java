package com.shipdream.lib.android.mvc.samples.simple.manager;

import com.shipdream.lib.android.mvc.Manager;

public class CounterManager extends Manager<CounterManager.Model> {
    /**
     * Namespace the events for this controller by nested interface so that all its events would
     * be referenced as CounterController.EventC2V.BlaBlaEvent.
     */
    public interface Event2C {
        /**
         * Event2C to notify views counter has been updated
         */
        class OnCounterUpdated {
            private final int count;
            public OnCounterUpdated(int count) {
                this.count = count;
            }

            public int getCount() {
                return count;
            }
        }
    }

    public static class Model {
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
    public Class<Model> modelType() {
        return Model.class;
    }

    public void setCount(Object sender, int count) {
        getModel().setCount(count);
        postEvent2C(new Event2C.OnCounterUpdated(count));
    }

    public String convertNumberToEnglish(int number) {
        if (number < -3) {
            return "Less than negative three";
        } else  if (number == -3) {
            return "Negative three";
        } else  if (number == -2) {
            return "Negative two";
        } else  if (number == -1) {
            return "Negative one";
        } else if (number == 0) {
            return "Zero";
        } else if (number == 1) {
            return "One";
        } else if (number == 2) {
            return "Two";
        } else if (number == 3) {
            return "Three";
        } else {
            return "Greater than three";
        }
    }

}
