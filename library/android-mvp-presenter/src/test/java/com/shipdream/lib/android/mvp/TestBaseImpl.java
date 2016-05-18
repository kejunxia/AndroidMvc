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

package com.shipdream.lib.android.mvp;

import com.shipdream.lib.android.mvp.presenter.BaseTest;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class TestBaseImpl extends BaseTest {

    @Test
    public void should_return_getState_and_getStateType_correctly() throws Exception {
        NavigationManager navigationManager = new NavigationManager();

        Assert.assertTrue(navigationManager.getModel() == navigationManager.getModel());
        Assert.assertTrue(navigationManager.modelType() == navigationManager.modelType());
    }

    @Test
    public void should_rebind_model_on_restoration_when_state_class_type_is_NOT_null() throws Exception {
        NavigationManager navigationManager = new NavigationManager();

        NavigationManager.Model restoreState = mock(NavigationManager.Model.class);
        Assert.assertNotEquals(restoreState, navigationManager.getModel());

        navigationManager.restoreModel(restoreState);
        Assert.assertEquals(restoreState, navigationManager.getModel());
    }

    class StatelessPresenter extends AbstractPresenter {
        @Override
        public Class modelType() {
            return null;
        }
    }

    @Test
    public void should_rebind_model_on_restoration_when_state_class_type_is_null() throws Exception {
        StatelessPresenter controller = new StatelessPresenter();
        //Pre-verify
        Assert.assertNull(controller.modelType());
        Assert.assertNull(controller.getModel());

        controller.restoreModel("Non-Null State");
        Assert.assertNull(controller.getModel());
    }

    @Test (expected = IllegalArgumentException.class)
    public void should_throw_exception_on_binding_null_model() throws Exception {
        NavigationManager navigationManager = new NavigationManager();

        navigationManager.bindModel(this, null);
    }

    private static class PrivateModel {
        private PrivateModel() {}
    }

    class BadPresenter extends AbstractPresenter<PrivateModel> {
        @Override
        public Class<PrivateModel> modelType() {
            return PrivateModel.class;
        }
    }

    @Test (expected = RuntimeException.class)
    public void should_throw_out_runtime_exception_when_unable_to_create_model_instance() throws Exception {
        BadPresenter presenter = new BadPresenter();
        graph.inject(presenter);
        presenter.onConstruct();
    }
}
