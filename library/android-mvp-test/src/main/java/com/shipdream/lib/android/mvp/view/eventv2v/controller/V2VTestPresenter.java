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

package com.shipdream.lib.android.mvp.view.eventv2v.controller;

import com.shipdream.lib.android.mvp.AbstractPresenter;
import com.shipdream.lib.android.mvp.event.BaseEventV;

public class V2VTestPresenter extends AbstractPresenter {
    public interface View {
        void updateDialogButton(String text);
    }

    interface EventC2V {
        class OnButtonUpdated extends BaseEventV {
            private final String text;

            public OnButtonUpdated(Object sender, String text) {
                super(sender);
                this.text = text;
            }

            public String getText() {
                return text;
            }
        }
    }

    public View view;

    @Override
    public Class modelType() {
        return null;
    }

    public void updateDialogButton(Object sender, String text) {
        view.updateDialogButton(text);
    }
}
