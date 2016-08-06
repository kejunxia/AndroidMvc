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

package com.shipdream.lib.android.mvc.samples.simple.mvp.controller.internal;

import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.MvcComponent;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.TestUtil;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.controller.CounterDetailController;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.controller.CounterMasterController;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.dto.IpPayload;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.factory.ServiceFactory;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.http.IpService;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.manager.CounterManager;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.service.ResourceService;
import com.shipdream.lib.poke.Provides;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCounterMasterController extends BaseTest {
    @Inject
    private CounterManager counterManager;

    @Inject
    private NavigationManager navigationManager;

    private CounterMasterController.View view;
    private CounterMasterController controller;

    private ResourceService resourceServiceMock;
    private Call<IpPayload> ipServiceCallMock;

    //Prepare injection graph before calling setup method
    @Override
    protected void prepareGraph(MvcComponent overriddingComponent) throws Exception {
        super.prepareGraph(overriddingComponent);

        overriddingComponent.register(new Object(){
            /**
             * Mock resource service
             * @return
             */
            @Provides
            public ResourceService resourceService() {
                resourceServiceMock = mock(ResourceService.class);
                return resourceServiceMock;
            }

            /**
             * Prepare objects to mock http calls
             * @return
             * @throws IOException
             */
            @Provides
            public ServiceFactory serviceFactory() throws IOException {
                ipServiceCallMock = mock(Call.class);

                IpService ipServiceMock = mock(IpService.class);
                when(ipServiceMock.getIp(anyString())).thenReturn(ipServiceCallMock);

                ServiceFactory serviceFactoryMock = mock(ServiceFactory.class);
                when(serviceFactoryMock.createService(IpService.class)).thenReturn(ipServiceMock);
                return serviceFactoryMock;
            }
        });
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        controller = new CounterMasterController();
        Mvc.graph().inject(controller);
        controller.onCreated();

        view = mock(CounterMasterController.View.class);
        TestUtil.assignControllerView(controller, view);
    }

    @Test
    public void increment_should_post_counter_update_event_with_incremented_value() {
        //1. Prepare view
        CounterMasterController.View view = mock(CounterMasterController.View.class);
        TestUtil.assignControllerView(controller, view);

        //mock controller model for count value
        int value = new Random().nextInt();
        CounterManager.Model counterModel = new CounterManager.Model();
        counterModel.setCount(value);
        //Mock the model of manager
        counterManager.bindModel(this, counterModel);

        CounterMasterController.Model modelSpy = spy(controller.getModel());
        controller.bindModel(modelSpy);

        //2. Act
        controller.increment(this);

        //3. Verify
        verify(modelSpy, times(1)).setCount(String.valueOf(value + 1));
    }

    @Test
    public void should_navigate_correctly() {
        //Act: navigate to MasterScreen
        navigationManager.navigate(this).to(CounterMasterController.class);

        //Verify: location should be changed to MasterScreen
        Assert.assertEquals(CounterMasterController.class.getName(),
                navigationManager.getModel().getCurrentLocation().getLocationId());

        //Act: navigate to DetailScreen
        controller.goToDetailScreen(this);

        //Verify: Current location should be at the view paired with CounterDetailController
        Assert.assertEquals(CounterDetailController.class.getName(),
                navigationManager.getModel().getCurrentLocation().getLocationId());
    }

    @Test
    public void should_update_view_with_correct_ip_and_show_and_dismiss_progress_bar() throws Exception {
        //Prepare
        //Prepare a good http response
        final String fakeIpResult = "abc.123.456.xyz";

        IpPayload payload = mock(IpPayload.class);
        when(payload.getIp()).thenReturn(fakeIpResult);
        when(ipServiceCallMock.execute()).thenReturn(Response.success(payload));

        CounterMasterController.Model modelSpy = spy(controller.getModel());
        controller.bindModel(modelSpy);

        //Action
        controller.refreshIp();

        //Verify
        verify(modelSpy).setProgressVisible(true);
        //Dismissed loading progress
        verify(modelSpy).setProgressVisible(true);
        //View's ip address text view should not be updated
        verify(modelSpy, times(1)).setIpAddress(fakeIpResult);
        //Should not show error message
        verify(view, times(0)).showHttpError(anyInt(), anyString());
        //Should not show network error message
        verify(view, times(0)).showNetworkError(any(IOException.class));
    }

    @Test
    public void should_show_error_message_on_HttpError_and_show_and_dismiss_progress_bar() throws Exception {
        //Prepare
        //Return 401 in the http response
        int errorStatusCode = 401;
        ResponseBody responseBody = mock(ResponseBody.class);
        when(ipServiceCallMock.execute()).thenReturn(
                Response.<IpPayload>error(errorStatusCode, responseBody));

        CounterMasterController.Model modelSpy = spy(controller.getModel());
        controller.bindModel(modelSpy);

        //Action
        controller.refreshIp();

        //Verify
        //Showed loading progress
        verify(modelSpy).setProgressVisible(true);
        //Dismissed loading progress
        verify(modelSpy).setProgressVisible(true);
        //View's ip address text view should not be updated
        verify(modelSpy, times(0)).setIpAddress(anyString());
        //Should show http error message with given mocking data
        verify(view, times(1)).showHttpError(errorStatusCode, null);
        //Should not show network error message
        verify(view, times(0)).showNetworkError(any(IOException.class));
    }

    @Test
    public void should_show_error_message_on_NetworkError_and_show_and_dismiss_progress_bar() throws Exception {
        //Prepare
        //Throw an IOException to simulate an network error
        IOException ioExceptionMock = mock(IOException.class);
        when(ipServiceCallMock.execute()).thenThrow(ioExceptionMock);

        CounterMasterController.Model modelSpy = spy(controller.getModel());
        controller.bindModel(modelSpy);

        //Action
        controller.refreshIp();

        //Verify
        //Showed loading progress
        verify(modelSpy).setProgressVisible(true);
        //Dismissed loading progress
        verify(modelSpy).setProgressVisible(true);
        //View's ip address text view should not be updated
        verify(modelSpy, times(0)).setIpAddress(anyString());
        //Should not show http error message
        verify(view, times(0)).showHttpError(anyInt(), anyString());
        //Should show network error message with the given mocking exception
        verify(view, times(1)).showNetworkError(ioExceptionMock);
    }
}
