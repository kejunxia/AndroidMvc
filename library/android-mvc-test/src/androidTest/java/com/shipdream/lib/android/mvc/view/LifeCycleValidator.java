package com.shipdream.lib.android.mvc.view;

import android.os.Bundle;
import android.view.View;

import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;

import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
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
    protected int onViewReadyFirstTime;
    protected int onViewReadyRotation;
    protected int onViewReadyRestore;
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
                    case onViewReadyFirstTime:
                        onViewReadyFirstTime++;
                        break;
                    case onViewReadyRotate:
                        onViewReadyRotation++;
                        break;
                    case onViewReadyRestore:
                        onViewReadyRestore++;
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
        verify(lifeCycleMonitorMock, times(onViewReadyFirstTime)).onViewReady(any(View.class), any(Bundle.class), eq(MvcFragment.Reason.FIRST_TIME));
        verify(lifeCycleMonitorMock, times(onViewReadyRotation)).onViewReady(any(View.class), any(Bundle.class), eq(MvcFragment.Reason.ROTATE));
        verify(lifeCycleMonitorMock, times(onViewReadyRestore)).onViewReady(any(View.class), any(Bundle.class), eq(MvcFragment.Reason.RESTORE));
        verify(lifeCycleMonitorMock, times(onViewCreatedCountNotNull)).onViewCreated(any(View.class), isNotNull(Bundle.class));
        verify(lifeCycleMonitorMock, times(onPushingToBackStackCount)).onPushingToBackStack();
        verify(lifeCycleMonitorMock, times(onPoppedOutToFrontCount)).onPoppedOutToFront();
        verify(lifeCycleMonitorMock, times(onReturnForegroundCount)).onReturnForeground();
        verify(lifeCycleMonitorMock, times(onOrientationChangedCount)).onOrientationChanged(anyInt(), anyInt());
        verify(lifeCycleMonitorMock, times(onDestroyViewCount)).onDestroyView();
        verify(lifeCycleMonitorMock, times(onDestroyCount)).onDestroy();

        reset();
    }

    public void reset() {
        onCreateCountNull = 0;
        onCreateCountNotNull = 0;
        onCreateViewCountNull = 0;
        onCreateViewCountNotNull = 0;
        onViewCreatedCountNull = 0;
        onViewCreatedCountNotNull = 0;
        onViewReadyFirstTime = 0;
        onViewReadyRotation = 0;
        onViewReadyRestore = 0;
        onPushingToBackStackCount = 0;
        onPoppedOutToFrontCount = 0;
        onReturnForegroundCount = 0;
        onOrientationChangedCount = 0;
        onDestroyViewCount = 0;
        onDestroyCount = 0;

        Mockito.reset(lifeCycleMonitorMock);
    }
}
