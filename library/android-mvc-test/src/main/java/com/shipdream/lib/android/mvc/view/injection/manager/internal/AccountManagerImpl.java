package com.shipdream.lib.android.mvc.view.injection.manager.internal;

import com.shipdream.lib.android.mvc.manager.BaseManagerImpl;
import com.shipdream.lib.android.mvc.view.injection.manager.AccountManager;
import com.shipdream.lib.android.mvc.view.injection.service.StorageService;

import javax.inject.Inject;

public class AccountManagerImpl extends BaseManagerImpl<AccountManager.Session>
        implements AccountManager {

    @Inject
    private StorageService storageService;

    @Override
    public void setUserId(long id) {
        getModel().setUserId(id);
    }

    @Override
    public long getUserId() {
        return getModel().getUserId();
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
    public Class<Session> modelType() {
        return Session.class;
    }
}
