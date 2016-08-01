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

package com.shipdream.lib.android.mvc.view.viewpager.controller;

import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.UiView;
import com.shipdream.lib.android.mvc.view.viewpager.TabModel;

public class TabControllerA extends FragmentController<TabModel, UiView> {
     static final String INIT_TEXT = "Tab A";
     static final String RESTORE_TEXT = "Restored TabA";

     @Override
     public Class<TabModel> modelType() {
          return TabModel.class;
     }

     public void setName(String name) {
          getModel().setName(name);
     }

     @Override
     public void onViewReady(Reason reason) {
          super.onViewReady(reason);
          if (reason.isFirstTime()) {
               getModel().setName(INIT_TEXT);
          }
          if (reason.isRestored()) {
               getModel().setName(RESTORE_TEXT);
          }
     }

     @Override
     public TabModel getModel() {
          return super.getModel();
     }

     @Override
     public void restoreModel(TabModel restoredModel) {
          super.restoreModel(restoredModel);
     }

     @Override
     public void bindModel(TabModel tabModel) {
          super.bindModel(tabModel);
     }

     @Override
     public void onRestored() {
          super.onRestored();
     }
}
