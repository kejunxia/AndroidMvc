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

package com.shipdream.lib.android.mvc.samples.note.controller.internal;

import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.samples.note.controller.WeatherController;
import com.shipdream.lib.android.mvc.samples.note.model.WeatherModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.WeatherListResponse;
import com.shipdream.lib.android.mvc.samples.note.service.android.PreferenceService;
import com.shipdream.lib.android.mvc.samples.note.service.http.WeatherService;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test weather controller whether respond to different network scenarios correctly without
 * depending on real network.
 */
public class TestWeatherController extends TestControllerBase<WeatherController> {
    /**
     * Mock service to provide mock response
     */
    private WeatherService weatherServiceMock;

    private PreferenceService preferenceServiceMock;
    private PreferenceService.Editor editorMock;

    public static class TestComp extends Component {
        TestWeatherController testNoteController;

        @Singleton
        @Provides
        public WeatherService provideWeatherService() {
            return testNoteController.weatherServiceMock;
        }

        @Singleton
        @Provides
        public PreferenceService providePreferenceService() {
            return testNoteController.preferenceServiceMock;
        }
    }

    @Override
    protected void registerDependencies(MvcGraph mvcGraph) {
        weatherServiceMock = mock(WeatherService.class);

        preferenceServiceMock = mock(PreferenceService.class);
        editorMock = mock(PreferenceService.Editor.class);
        when(editorMock.putString(anyString(), anyString())).thenReturn(editorMock);
        when(preferenceServiceMock.edit()).thenReturn(editorMock);

        //Setup mock executor service mock that runs task on the same thread.
        executorService = mock(ExecutorService.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(executorService).submit(any(Runnable.class));

        //Register the injecting component to mvcGraph to override the implementation being injected
        //to controllers
        TestComp testComp = new TestComp();
        testComp.testNoteController = this;
        mvcGraph.register(testComp);
    }

    @Override
    protected WeatherController createTestingController() {
        return new WeatherControllerImpl();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRaiseSuccessEventForGoodUpdateWeathers() throws IOException {
        //---Arrange---
        //Define a subscriber class
        class Monitor {
            public void onEvent(WeatherController.EventC2V.OnWeathersUpdated event) {
            }
            public void onEvent(WeatherController.EventC2V.OnWeathersUpdateFailed event) {
            }
        }
        Monitor monitor = mock(Monitor.class);
        eventBusV.register(monitor);
        //Prepare data
        WeatherListResponse responseMock = mock(WeatherListResponse.class);
        when(weatherServiceMock.getWeathers(any(List.class))).thenReturn(responseMock);

        //---Action---
        controllerToTest.updateAllCities(this);

        //---Verify---
        //Success event should be raised
        ArgumentCaptor<WeatherController.EventC2V.OnWeathersUpdated> eventSuccess
                = ArgumentCaptor.forClass(WeatherController.EventC2V.OnWeathersUpdated.class);
        verify(monitor, times(1)).onEvent(eventSuccess.capture());
        //Failed event should not be raised
        ArgumentCaptor<WeatherController.EventC2V.OnWeathersUpdateFailed> eventFailure
                = ArgumentCaptor.forClass(WeatherController.EventC2V.OnWeathersUpdateFailed.class);
        verify(monitor, times(0)).onEvent(eventFailure.capture());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRaiseSuccessEventDirectlyWithNoCities() throws IOException {
        //---Arrange---
        //Define a subscriber class
        class Monitor {
            public void onEvent(WeatherController.EventC2V.OnWeathersUpdated event) {
            }
            public void onEvent(WeatherController.EventC2V.OnWeathersUpdateFailed event) {
            }
        }
        Monitor monitor = mock(Monitor.class);
        eventBusV.register(monitor);
        //Prepare data
        controllerToTest.getModel().getWeatherWatchlist().clear();

        //---Action---
        controllerToTest.updateAllCities(this);

        //---Verify---
        //Success event should be raised
        ArgumentCaptor<WeatherController.EventC2V.OnWeathersUpdated> eventSuccess
                = ArgumentCaptor.forClass(WeatherController.EventC2V.OnWeathersUpdated.class);
        verify(monitor, times(1)).onEvent(eventSuccess.capture());
        //Failed event should not be raised
        ArgumentCaptor<WeatherController.EventC2V.OnWeathersUpdateFailed> eventFailure
                = ArgumentCaptor.forClass(WeatherController.EventC2V.OnWeathersUpdateFailed.class);
        verify(monitor, times(0)).onEvent(eventFailure.capture());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRaiseFailEventForNetworkErrorToUpdateWeathers() throws IOException {
        //---Arrange---
        //Define a subscriber class
        class Monitor {
            public void onEvent(WeatherController.EventC2V.OnWeathersUpdated event) {
            }
            public void onEvent(WeatherController.EventC2V.OnWeathersUpdateFailed event) {
            }
        }
        Monitor monitor = mock(Monitor.class);
        eventBusV.register(monitor);
        //Prepare data
        controllerToTest.getModel().getWeatherWatchlist().put(WeatherModel.City.Beijing, null);
        when(weatherServiceMock.getWeathers(any(List.class))).thenThrow(new IOException());

        //---Action---
        controllerToTest.updateAllCities(this);

        //---Verify---
        //Success event should not be raised
        ArgumentCaptor<WeatherController.EventC2V.OnWeathersUpdated> eventSuccess
                = ArgumentCaptor.forClass(WeatherController.EventC2V.OnWeathersUpdated.class);
        verify(monitor, times(0)).onEvent(eventSuccess.capture());
        //Failed event must be raised
        ArgumentCaptor<WeatherController.EventC2V.OnWeathersUpdateFailed> eventFailure
                = ArgumentCaptor.forClass(WeatherController.EventC2V.OnWeathersUpdateFailed.class);
        verify(monitor, times(1)).onEvent(eventFailure.capture());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldSaveCitiesAfterUpdate() throws IOException {
        //---Arrange---

        //---Action---
        controllerToTest.updateAllCities(this);

        //---Verify---
        //Should call preferenceServiceMock to save the json of the city with the right key
        verify(preferenceServiceMock).edit();
        verify(editorMock).putString(eq(WeatherControllerImpl.PREF_KEY_WEATHER_CITIES), anyString());
        verify(editorMock).apply();
    }
}
