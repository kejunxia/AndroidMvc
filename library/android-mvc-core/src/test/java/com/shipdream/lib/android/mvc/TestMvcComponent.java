package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.inject.test.Phone;
import com.shipdream.lib.android.mvc.inject.test.Robot;
import com.shipdream.lib.android.mvc.inject.test.Smart;

import org.junit.Test;

import javax.inject.Inject;

public class TestMvcComponent {
    @Test(expected = MvcGraphException.class)
    public void should_throw_provider_missing_exception_when_locate_an_qualified_class() {
        class Shop {
            @Inject
            private Phone nexus6;
        }

        Mvc.graph().inject(new Shop());
    }

    @Test(expected = MvcGraphException.class)
    public void should_throw_provider_missing_exception_when_locate_an_unqualified_class() {
        class Shop {
            @Inject
            @Smart
            private Robot nexus6;
        }

        Mvc.graph().inject(new Shop());
    }
}
