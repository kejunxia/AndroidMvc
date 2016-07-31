/*
 * Copyright 2016 Kejun Xia
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

package com.shipdream.lib.android.mvc;

import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.ProviderByClassType;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

/**
 * A component manages injectable objects. It is able to locate implementation class automatically by
 * <ul>
 * <ui>The injecting class is a concrete class and has empty constructor</ui>
 * <ui>The injecting class is an interface or abstract class and there is concrete class is named
 * with suffix "impl" and sitting in the subpackage "internal" which is at the
 * same level of the interface or abstract. For instance, the interface is
 * a.b.c.Car and there is an concrete class at a.b.c.internal.CarImpl</ui>
 * <ui>The injecting class is registered by {@link #register(Object)} or {@link #register(Provider)}</ui>
 * </ul>
 */
public class MvcComponent extends Component {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Construct a MvcComponent with the give name with a cope cache so that providers registered
     * to this component will supply an injectable object with the same instance since its first
     * copy is created for injection until last instance is released.
     *
     * @param name Name of the component, can be null. But it's recommended to supply a name in order
     *             to identify which component supplies an instance
     */
    public MvcComponent(String name) {
        super(name);
    }

    /**
     * Construct a MvcComponent with the give name and specify whether this component cache instances
     * created by providers registered to this component.
     *
     * @param name        Name of the component, can be null. But it's recommended to supply a name in order
     *                    to identify which component supplies an instance
     * @param enableCache If cache is enabled it will supply an
     *                    injectable object with the same instance since its first copy is created for injection until
     *                    last instance is released. Otherwise all providers registered by this component will always
     *                    generate new instances.
     */
    public MvcComponent(String name, boolean enableCache) {
        super(name, enableCache);
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
                    String msg = String.format("Can't find implementation class for %s. Make sure class %s exists, or its implementation is registered to Mvc.graph().getRootComponent()",
                            type.getName(), getClassName(type));
                    throw new ProviderMissingException(msg);
                }
            } else {
                //The type is a class then it's a construable by itself.
                impClass = type;
            }

            provider = new ProviderByClassType<>(type, impClass);

            if ((qualifier != null && !qualifier.equals(provider.getQualifier()))
                    || provider.getQualifier() != null) {
                String msg;
                if (qualifier == null) {
                    msg = String.format("Can't find implementation class for %s. Make sure class %s without qualifier %s exists, or its implementation is registered to graph's root component.",
                            type.getName(), getClassName(type), provider.getQualifier().toString());
                } else {
                    msg = String.format("Can't find implementation class for %s. Make sure class %s with qualifier %s exists, or its implementation is registered to graph's root component.",
                            type.getName(), getClassName(type), qualifier.toString());
                }
                throw new ProviderMissingException(msg);
            }

            try {
                register(provider);
            } catch (ProviderConflictException e) {
                //Should not happen since otherwise it should have been found already
                e.printStackTrace();
            }
        }
        return provider;
    }

    @Override
    public Component register(@NotNull Provider provider) throws ProviderConflictException {
        super.register(provider);
        provider.registerCreationListener(new Provider.CreationListener() {
            @Override
            public void onCreated(Provider provider, Object instance) {
                if (instance instanceof Bean) {
                    final Bean bean = (Bean) instance;

                    bean.onCreated();
                    logger.trace("+++Bean created - '{}'.",
                            provider.type().getSimpleName());
                }
            }
        });
        return this;
    }

    private static String getClassName(Class type) {
        String pkg = type.getPackage().getName();
        String implClassName = pkg + ".internal." + type.getSimpleName() + "Impl";
        return implClassName;
    }

}
