package com.shipdream.lib.poke;

import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kejun on 5/26/2016.
 */
public class TestProvider {
    class Book {
        @MyInject
        String title;
    }

    @Test
    public void test_constructor() {
        ScopeCache scopeCache = new ScopeCache();
        Provider p = new Provider(String.class, scopeCache) {
            @Override
            protected Object createInstance() throws ProvideException {
                return null;
            }
        };

        Assert.assertTrue(p.getScopeCache() == scopeCache);
        Assert.assertTrue(p.getQualifier() == null);
    }

    @Test
    public void test_provider_missing_exception_constructor() {
        ProviderMissingException e1 = new ProviderMissingException(String.class, null, new Throwable());
        ProviderMissingException e2 = new ProviderMissingException("msg");
        ProviderMissingException e3 = new ProviderMissingException("msg", new Throwable());
    }

    @Test
    public void should_throw_provide_exception_with_provider_providing_null_instance() throws PokeException {
        Graph graph = new Graph();

        Component c = new Component(false);
        c.register(new Provider(String.class) {
            @Override
            protected Object createInstance() throws ProvideException {
                return null;
            }
        });
        graph.setRootComponent(c);

        boolean exp = false;

        Book b = new Book();

        try {
            graph.inject(b, MyInject.class);
        } catch (ProvideException e) {
            exp = true;
        }

        Assert.assertTrue(exp);
    }
}
