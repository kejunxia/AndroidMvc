package com.shipdream.lib.android.mvc.view.injection.service.internal;

import com.shipdream.lib.android.mvc.MvcBean;
import com.shipdream.lib.android.mvc.view.injection.service.StorageService;

/**
 * Created by kejun on 1/13/2016.
 */
public class StorageServiceImpl extends MvcBean<StorageService.Storage> implements StorageService{
    @Override
    public Class<StorageService.Storage> modelType() {
        return StorageService.Storage.class;
    }

    @Override
    public String getContent() {
        return getModel().getContent();
    }

    @Override
    public void setContent(String content) {
        getModel().setContent(content);
    }

}
