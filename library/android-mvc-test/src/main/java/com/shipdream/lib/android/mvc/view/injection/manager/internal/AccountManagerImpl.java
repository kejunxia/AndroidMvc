package com.shipdream.lib.android.mvc.view.injection.manager.internal;

import com.shipdream.lib.android.mvc.Constructable;
import com.shipdream.lib.android.mvc.StateManaged;
import com.shipdream.lib.android.mvc.view.injection.manager.AccountManager;
import com.shipdream.lib.android.mvc.view.injection.service.StorageService;

import javax.inject.Inject;

public class AccountManagerImpl implements AccountManager, StateManaged<AccountManager.Session>, Constructable {
    private Session session;

    @Inject
    private StorageService storageService;

    @Override
    public void setUserId(long id) {
        session.setUserId(id);
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
    public void onConstruct() {
        session = new Session();
    }

    @Override
    public Class<Session> getStateType() {
        return Session.class;
    }

    @Override
    public Session getState() {
        return session;
    }

    @Override
    public void restoreState(Session restoredState) {
        this.session = restoredState;
    }
}
