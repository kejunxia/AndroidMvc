package com.shipdream.lib.android.mvc.view.injection.service.internal;

import com.shipdream.lib.android.mvc.Constructable;
import com.shipdream.lib.android.mvc.StateManaged;
import com.shipdream.lib.android.mvc.view.injection.service.StorageService;

/**
 * Created by kejun on 1/13/2016.
 */
public class StorageServiceImpl implements StorageService, StateManaged<StorageService.Storage>,
        Constructable{
    private Storage storage;

    @Override
    public Class<Storage> getStateType() {
        return Storage.class;
    }

    @Override
    public Storage getState() {
        return storage;
    }

    @Override
    public void restoreState(Storage restoredState) {
        this.storage = restoredState;
    }

    @Override
    public String getContent() {
        return getState().getContent();
    }

    @Override
    public void setContent(String content) {
        getState().setContent(content);
    }

    @Override
    public void onConstruct() {
        storage = new Storage();
    }
}
