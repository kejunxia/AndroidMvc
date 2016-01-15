package com.shipdream.lib.android.mvc.view.injection.manager.internal;

import com.shipdream.lib.android.mvc.Constructable;
import com.shipdream.lib.android.mvc.StateManaged;
import com.shipdream.lib.android.mvc.manager.AbstractStatefulManager;
import com.shipdream.lib.android.mvc.view.injection.manager.AccountManager;
import com.shipdream.lib.android.mvc.view.injection.service.StorageService;

import javax.inject.Inject;

public class AccountManagerImpl extends AbstractStatefulManager<AccountManager.Session>
        implements AccountManager {

    @Inject
    private StorageService storageService;

    @Override
    public void setUserId(long id) {
        getState().setUserId(id);
    }

    @Override
    public long getUserId() {
        return getState().getUserId();
    }

    @Override
    public String getContent() {
        return storageService.getContent();
    }

    @Override
    public void setContent(String content) {
        storageService.setContent(content);
    }

    @Override
    public Class<Session> getStateType() {
        return Session.class;
    }
}
