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

/**
 * Reason of binding model to controller
 */
public class Reason {
    boolean isNewInstance;
    boolean isFirstTime;
    boolean isRestored;
    boolean isRotated;
    boolean isPoppedOut;

    /**
     * @return Indicates whether the controller and corresponding fragment is a new instance that all its fields
     * need to be reinitialized and configured. This could happen when a fragment is created for the first
     * time (when {@link #isFirstTime()} = true) or the fragment is recreated on restoration
     * after its holding activity was killed by OS (when {@link #isRestored()} = true).
     * <p>
     */
    public boolean isNewInstance() {
        return this.isNewInstance;
    }

    /**
     * @return Indicates whether the controller and corresponding fragment view is created when the
     * fragment is created for the first time. When this flag is true it's a good time to initialize
     * the state fragment.
     * <p>
     * <p>false will be returned when the view is created by rotation, back navigation or restoration</p>
     */
    public boolean isFirstTime() {
        return this.isFirstTime;
    }

    /**
     * @return Indicates whether the controller and corresponding fragment is created after the activity is killed by
     * OS and restored. <br><br>
     * <p>
     * <p>Note that the model of the controller will be restored automatically by the framework. So
     * it's safe to use the model straight away which represents the state before the fragment's
     * holding activity was killed.</p>
     */
    public boolean isRestored() {
        return this.isRestored;
    }

    /**
     * @return Indicates whether the fragment view is created when the fragment was pushed to
     * back stack and just popped out.
     * <p>
     * <p>Note that, when a fragment is popped out, it will reuses its previous instance and the
     * fields of the instance, so {@link #isNewInstance()} won't be true in this case. This is
     * because Android OS won't call onDestroy when a fragment is pushed into back stack.</p>
     */
    public boolean isPoppedOut() {
        return this.isPoppedOut;
    }

    /**
     * @return Indicates whether the fragment view is created after its orientation changed.
     */
    public boolean isRotated() {
        return this.isRotated;
    }

    @Override
    public String toString() {
        return "Reason: {" +
                "newInstance: " + isNewInstance() +
                ", firstTime: " + isFirstTime() +
                ", restore: " + isRestored() +
                ", popOut: " + isPoppedOut() +
                ", rotate: " + isRotated() +
                '}';
    }
}
