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
