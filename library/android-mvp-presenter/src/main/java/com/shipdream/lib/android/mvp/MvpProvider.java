package com.shipdream.lib.android.mvp;

import com.shipdream.lib.poke.ProviderByClassType;
import com.shipdream.lib.poke.exception.ProvideException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class MvpProvider<T> extends ProviderByClassType<T> {
    private final Logger logger = LoggerFactory.getLogger(MvpGraph.class);
    private List<Bean> beans;

    public MvpProvider(List<Bean> beans, Class<T> type, Class<? extends T> implementationClass) {
        super(type, implementationClass);
        this.beans = beans;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T createInstance() throws ProvideException {
        final T newInstance = super.createInstance();

        registerOnInjectedListener(new OnInjectedListener() {
            @Override
            public void onInjected(Object object) {
                if (object instanceof Bean) {
                    Bean bean = (Bean) object;
                    bean.onConstruct();

                    logger.trace("++Bean injected - '{}'.",
                            object.getClass().getSimpleName());
                }
                unregisterOnInjectedListener(this);
            }
        });

        if (newInstance instanceof Bean) {
            beans.add((Bean) newInstance);
        }

        return newInstance;
    }
}
