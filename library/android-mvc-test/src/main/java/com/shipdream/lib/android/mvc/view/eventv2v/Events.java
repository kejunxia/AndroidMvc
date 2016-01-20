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

package com.shipdream.lib.android.mvc.view.eventv2v;

import com.shipdream.lib.android.mvc.event.BaseEventV;

public interface Events {
    abstract class OnTextChanged extends BaseEventV {
        private final String text;
        protected OnTextChanged(Object sender, String text) {
            super(sender);
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    class OnFragmentTextChanged extends OnTextChanged {
        protected OnFragmentTextChanged(Object sender, String text) {
            super(sender, text);
        }
    }

    class OnDialogButtonChanged extends OnTextChanged {
        protected OnDialogButtonChanged(Object sender, String text) {
            super(sender, text);
        }
    }

}
