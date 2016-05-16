/*
 * Copyright 2015 Kejun Xia
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

package com.shipdream.lib.android.mvc.view;

import android.os.Bundle;
import android.view.View;

import com.shipdream.lib.android.mvp.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvp.view.MvcFragment;

import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LifeCycleValidator {
    private final LifeCycleMonitor lifeCycleMonitorMock;

    public LifeCycleValidator(LifeCycleMonitor lifeCycleMonitorMock) {
        this.lifeCycleMonitorMock = lifeCycleMonitorMock;
    }

    protected int onCreateCountNull;
    protected int onCreateCountNotNull;
    protected int onCreateViewCountNull;
    protected int onCreateViewCountNotNull;
    protected int onViewCreatedCountNull;
    protected int onViewCreatedCountNotNull;
    protected int onViewReadyNewInstance;
    protected int onViewReadyFirstTime;
    protected int onViewReadyRotation;
    protected int onViewReadyRestore;
    protected int onViewReadyPopOut;
    protected int onPushingToBackStackCount;
    protected int onPoppedOutToFrontCount;
    protected int onReturnForegroundCount;
    protected int onOrientationChangedCount;
    protected int onDestroyViewCount;
    protected int onDestroyCount;

    public void expect(LifeCycle... lifeCycles) {
        if (lifeCycles != null) {
            for (int i = 0; i < lifeCycles.length; i++) {
                LifeCycle lifeCycle = lifeCycles[i];
                switch (lifeCycle) {
                    case onCreateNull:
                        onCreateCountNull++;
                        break;
                    case onCreateNotNull:
                        onCreateCountNotNull++;
                        break;
                    case onCreateViewNull:
                        onCreateViewCountNull++;
                        break;
                    case onCreateViewNotNull:
                        onCreateViewCountNotNull++;
                        break;
                    case onViewCreatedNull:
                        onViewCreatedCountNull++;
                        break;
                    case onViewCreatedNotNull:
                        onViewCreatedCountNotNull++;
                        break;
                    case onViewReadyNewInstance:
                        onViewReadyNewInstance++;
                        break;
                    case onViewReadyFirstTime:
                        onViewReadyFirstTime++;
                        break;
                    case onViewReadyRotate:
                        onViewReadyRotation++;
                        break;
                    case onViewReadyRestore:
                        onViewReadyRestore++;
                        break;
                    case onViewReadyPopOut:
                        onViewReadyPopOut++;
                        break;
                    case onPushingToBackStack:
                        onPushingToBackStackCount++;
                        break;
                    case onPoppedOutToFront:
                        onPoppedOutToFrontCount++;
                        break;
                    case onReturnForeground:
                        onReturnForegroundCount++;
                        break;
                    case onOrientationChanged:
                        onOrientationChangedCount++;
                        break;
                    case onDestroyView:
                        onDestroyViewCount++;
                        break;
                    case onDestroy:
                        onDestroyCount++;
                        break;
                }
            }
        }

        verify(lifeCycleMonitorMock, times(onCreateCountNull)).onCreate(null);
        verify(lifeCycleMonitorMock, times(onCreateCountNotNull)).onCreate(isNotNull(Bundle.class));
        verify(lifeCycleMonitorMock, times(onCreateViewCountNull)).onCreateView(any(View.class), isNull(Bundle.class));
        verify(lifeCycleMonitorMock, times(onCreateViewCountNotNull)).onCreateView(any(View.class), isNotNull(Bundle.class));
        verify(lifeCycleMonitorMock, times(onViewCreatedCountNull)).onViewCreated(any(View.class), isNull(Bundle.class));
        verify(lifeCycleMonitorMock, times(onViewCreatedCountNotNull)).onViewCreated(any(View.class), isNotNull(Bundle.class));

        verify(lifeCycleMonitorMock, times(onViewReadyNewInstance)).onViewReady(any(View.class),
                any(Bundle.class), argThat(new NewInstanceMatcher()));

        verify(lifeCycleMonitorMock, times(onViewReadyFirstTime)).onViewReady(any(View.class),
                any(Bundle.class), argThat(new FirstTimeMatcher()));

        verify(lifeCycleMonitorMock, times(onViewReadyRotation)).onViewReady(any(View.class),
                any(Bundle.class), argThat(new RotateMatcher()));

        verify(lifeCycleMonitorMock, times(onViewReadyRestore)).onViewReady(any(View.class),
                any(Bundle.class), argThat(new RestoreMatcher()));

        verify(lifeCycleMonitorMock, times(onViewReadyPopOut)).onViewReady(any(View.class),
                any(Bundle.class), argThat(new PopOutMatcher()));

        verify(lifeCycleMonitorMock, times(onViewCreatedCountNotNull)).onViewCreated(any(View.class), isNotNull(Bundle.class));
        verify(lifeCycleMonitorMock, times(onPushingToBackStackCount)).onPushingToBackStack();
        verify(lifeCycleMonitorMock, times(onPoppedOutToFrontCount)).onPoppedOutToFront();
        verify(lifeCycleMonitorMock, times(onReturnForegroundCount)).onReturnForeground();
        verify(lifeCycleMonitorMock, times(onOrientationChangedCount)).onOrientationChanged(anyInt(), anyInt());
        verify(lifeCycleMonitorMock, times(onDestroyViewCount)).onDestroyView();
        verify(lifeCycleMonitorMock, times(onDestroyCount)).onDestroy();

        reset();
    }

    private class NewInstanceMatcher extends ArgumentMatcher<MvcFragment.Reason> {
        @Override
        public boolean matches(Object argument) {
            if (argument instanceof MvcFragment.Reason) {
                if (((MvcFragment.Reason) argument).isNewInstance()) {
                    return true;
                }
            }
            return false;
        }
    }

    private class FirstTimeMatcher extends ArgumentMatcher<MvcFragment.Reason> {
        @Override
        public boolean matches(Object argument) {
            if (argument instanceof MvcFragment.Reason) {
                if (((MvcFragment.Reason) argument).isFirstTime()) {
                    return true;
                }
            }
            return false;
        }
    }

    private class RestoreMatcher extends ArgumentMatcher<MvcFragment.Reason> {
        @Override
        public boolean matches(Object argument) {
            if (argument instanceof MvcFragment.Reason) {
                if (((MvcFragment.Reason) argument).isRestored()) {
                    return true;
                }
            }
            return false;
        }
    }

    private class RotateMatcher extends ArgumentMatcher<MvcFragment.Reason> {
        @Override
        public boolean matches(Object argument) {
            if (argument instanceof MvcFragment.Reason) {
                if (((MvcFragment.Reason) argument).isRotated()) {
                    return true;
                }
            }
            return false;
        }
    }

    private class PopOutMatcher extends ArgumentMatcher<MvcFragment.Reason> {
        @Override
        public boolean matches(Object argument) {
            if (argument instanceof MvcFragment.Reason) {
                if (((MvcFragment.Reason) argument).isPoppedOut()) {
                    return true;
                }
            }
            return false;
        }
    }

    public void reset() {
        onCreateCountNull = 0;
        onCreateCountNotNull = 0;
        onCreateViewCountNull = 0;
        onCreateViewCountNotNull = 0;
        onViewCreatedCountNull = 0;
        onViewCreatedCountNotNull = 0;
        onViewReadyNewInstance = 0;
        onViewReadyFirstTime = 0;
        onViewReadyRotation = 0;
        onViewReadyRestore = 0;
        onViewReadyPopOut = 0;
        onPushingToBackStackCount = 0;
        onPoppedOutToFrontCount = 0;
        onReturnForegroundCount = 0;
        onOrientationChangedCount = 0;
        onDestroyViewCount = 0;
        onDestroyCount = 0;

        Mockito.reset(lifeCycleMonitorMock);
    }
}
