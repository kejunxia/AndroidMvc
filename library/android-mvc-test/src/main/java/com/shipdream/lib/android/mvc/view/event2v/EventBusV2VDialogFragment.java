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

package com.shipdream.lib.android.mvc.view.event2v;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.MvcDialogFragment;
import com.shipdream.lib.android.mvc.view.test.R;

public class EventBusV2VDialogFragment extends MvcDialogFragment {
    private TextView textView;
    private View button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mvc_v2v_dialog, container);
        textView = (TextView) view.findViewById(R.id.fragment_mvc_v2v_dialog_text);
        button = view.findViewById(R.id.fragment_mvc_v2v_dialog_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postEvent2V(new Events.OnFragmentTextChanged("Dialog Closed"));
                dismiss();
            }
        });
        return view;
    }

    private void onEvent(Events.OnDialogButtonChanged onButtonUpdated) {
        textView.setText(onButtonUpdated.getText());
    }
}
