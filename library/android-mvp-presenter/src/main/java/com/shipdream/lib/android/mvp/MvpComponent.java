package com.shipdream.lib.android.mvp;

import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.ProviderByClassType;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//TODO: documents
public class MvpComponent extends Component {
    private Logger logger = LoggerFactory.getLogger(getClass());
    List<Bean> beans = new CopyOnWriteArrayList<>();

    public MvpComponent(String name) {
        super(name);
    }

    /**
     * Save model of all injected objects
     * @param beanKeeper The model keeper managing the model
     */
    public void saveAllBeans(BeanKeeper beanKeeper) {
        int size = beans.size();
        for (int i = 0; i < size; i++) {
            Bean bean = beans.get(i);
            beanKeeper.saveBean(bean);
        }
    }

    /**
     * Restore beans injected by this provider finder.
     * @param beanKeeper The model keeper managing the model
     */
    @SuppressWarnings("unchecked")
    public void restoreAllBeans(BeanKeeper beanKeeper) {
        int size = beans.size();
        for (int i = 0; i < size; i++) {
            Bean bean = beans.get(i);
            Object model = beanKeeper.retrieveBean(bean.modelType());
            if(model != null) {
                beans.get(i).restoreModel(model);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Provider<T> findProvider(final Class<T> type, Annotation qualifier) throws ProviderMissingException {
        Provider<T> provider = null;
        try {
            provider = super.findProvider(type, qualifier);
        } catch (ProviderMissingException e) {
            //ignore since we will try to auto locate the impl class
        }
        if (provider == null) {
            Class<? extends T> impClass;
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                //Non concrete class needs to find its implementation class
                try {
                    impClass = (Class<T>) Class.forName(getClassName(type));
                } catch (ClassNotFoundException e) {
                    String msg = String.format("Can't find implementation class for %s. Make sure class %s exists",
                            type.getName(), getClassName(type));
                    throw new ProviderMissingException(msg);
                }
            } else {
                //The type is a class then it's a construable by itself.
                impClass = type;
            }

            provider = new ProviderByClassType<T>(type, impClass) {
                @Override
                public T createInstance() throws ProvideException {
                    T instance = super.createInstance();
                    if (instance instanceof Bean) {
                        final Bean bean = (Bean) instance;

                        //TODO: should register the listener to provider found from super as well

                        registerOnReferencedListener(new ReferencedListener<T>() {
                            @Override
                            public void onReferenced(Provider<T> provider, T instance) {
                                bean.onConstruct();
                                unregisterOnReferencedListener(this);
                            }
                        });

                        logger.trace("+++Bean instantiated - '{}'.",
                                type().getSimpleName());
                    }
                    return instance;
                };
            };

            try {
                super.register(provider);
            } catch (ProviderConflictException e) {
                //Should not happen since otherwise it should have been found already
                e.printStackTrace();
            }
        }
        return provider;
    }

    private static String getClassName(Class type) {
        String pkg = type.getPackage().getName();
        String implClassName = pkg + ".internal." + type.getSimpleName() + "Impl";
        return implClassName;
    }

}
