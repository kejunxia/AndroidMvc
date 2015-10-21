package com.shipdream.lib.android.mvc.view.eventv2v.controller;

import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.event.BaseEventC2V;

public interface V2VTestController extends BaseController {
    void updateDialogButton(Object sender, String text);

    interface EventC2V {
        class OnButtonUpdated extends BaseEventC2V {
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
}
