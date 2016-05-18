package com.shipdream.lib.android.mvp;

import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.ProviderFinderByRegistry;
import com.shipdream.lib.poke.ScopeCache;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

//TODO: documents
public class MvpProviderFinder extends ProviderFinderByRegistry {
    //TODO: registered component can have provides level scope/scope cache to override this scope cache?
    ScopeCache scopeCache;
    Map<Class, Provider> providers = new HashMap<>();
    List<Bean> beans = new CopyOnWriteArrayList<>();

    /**
     * Construct the provider finder without scope. The providers will always create a new instance
     * on each injection.
     */
    public MvpProviderFinder() {
        this(null);
    }

    /**
     * Construct the provider finder with given {@link ScopeCache}
     * @param scopeCache The scope cache. Can be null if the instances created by the providers
     *                   don't need to be scoped where every injection by the providers will be a new instance.
     */
    public MvpProviderFinder(ScopeCache scopeCache) {this.scopeCache = scopeCache;}

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

    @Override
    public <T> Provider<T> findProvider(Class<T> type, Annotation qualifier) throws ProviderMissingException {
        Provider<T> provider = super.findProvider(type, qualifier);
        if (provider == null) {
            provider = providers.get(type);
            if (provider == null) {
                Class<? extends T> impClass;
                if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                    //Non concrete class needs to find its implementation class
                    try {
                        impClass = findImplClass(type);
                    } catch (ClassNotFoundException e) {
                        String msg = String.format("Can't find implementation class for %s. Make sure class %s exists",
                                type.getName(), getClassName(type));
                        throw new ProviderMissingException(msg);
                    }
                } else {
                    //The type is a class then it's a construable by itself.
                    impClass = type;
                }

                provider = new MvpProvider<>(beans, type, impClass);
                provider.setScopeCache(scopeCache);
                providers.put(type, provider);
            }
        }
        return provider;
    }

    private static String getClassName(Class type) {
        String pkg = type.getPackage().getName();
        String implClassName = pkg + ".internal." + type.getSimpleName() + "Impl";
        return implClassName;
    }

    private static <T> Class<T> findImplClass(Class<T> type) throws ClassNotFoundException {
        return (Class<T>) Class.forName(getClassName(type));
    }
}
