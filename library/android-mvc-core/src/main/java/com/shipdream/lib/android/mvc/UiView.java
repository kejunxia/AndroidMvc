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
 * <p>
 * UiView represents android views. It has a single method {@link #update()}. Every {@link Controller}
 * holds an instance of {@link UiView} which is implemented by the controller's corresponding
 * concrete view. Whenever the {@link Controller} updates its state, usually it's model, the controller
 * needs to call {@link UiView}.{@link #update()}.
 * </p>
 *
 * <p>
 * Usually this is enough for most Android views since as long as the concrete view implements
 * {@link UiView}.{@link #update()} by binding the full controller model to the view, it guarantees the view's
 * graphics is always in sync with the controller's model. However, if the model of a controller is
 * large and sometimes only very limited part of the model changes, you may not always want to call
 * {@link UiView}.{@link #update()} to rebind the entire model to the view if performance becomes a concern.
 * </p>
 *
 * <p>
 * In this case, define a custom view interface extending {@link UiView} with extra method to allow
 * controllers updating specific part of the view. For example,
 * </p>
 *
 * <pre>
 * public interface PageView extends UiView {
 *      /**
 *      * Let the controller of the page view just update the title instead calling {@link UiView}.{@link #update()}
 *      * to update the whole page.
 *      * @param title The title of the page.
 *      * /
 *      void updateTitle(String title);
 * }
 * </pre>
 */
public interface UiView {
    /**
     * When a view is requested to update itself, it should read it's controller's model by
     * {@link Controller#getModel()} to bind the data to the view.
     *
     * <p>It will be automatically called when a view is ready to shown. Controllers should call this
     * method when they updated their model.</p>
     *
     * <p><b>Do NOT change values of model from view but only from controllers.</b></p>
     */
    void update();
}
