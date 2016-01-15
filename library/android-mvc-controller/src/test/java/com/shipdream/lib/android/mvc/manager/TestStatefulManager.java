package com.shipdream.lib.android.mvc.manager;

import org.junit.Assert;
import org.junit.Test;

public class TestStatefulManager {
    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_bind_null_to_stateful_manager() {
        AbstractStatefulManager manager = new AbstractStatefulManager() {
            @Override
            public Class getStateType() {
                return String.class;
            }
        };

        manager.bindState(null);
    }

    @Test
    public void should_rebind_state_after_restoring_manager() {
        AbstractStatefulManager<String> manager = new AbstractStatefulManager() {

            @Override
            public Class getStateType() {
                return String.class;
            }
        };

        Assert.assertNull(manager.getState());

        manager.restoreState("A");

        Assert.assertEquals("A", manager.getState());
    }

    @Test
    public void should_call_on_restore_call_back_after_manager_is_restored() {
        class MyManager extends AbstractStatefulManager<String> {
            private boolean called = false;

            @Override
            public Class getStateType() {
                return String.class;
            }

            @Override
            public void onRestored() {
                super.onRestored();
                called = true;
            }
        };

        MyManager manager = new MyManager();

        Assert.assertFalse(manager.called);

        manager.restoreState("A");

        Assert.assertTrue(manager.called);
    }

    public void should_create_state_instance_on_construct_when_the_state_type_is_specified_for_a_stateful_manager() {
        class MyManager extends AbstractStatefulManager<String> {
            @Override
            public Class getStateType() {
                return String.class;
            }
        };
        MyManager manager = new MyManager();

        Assert.assertNull(manager.getState());

        manager.onConstruct();

        Assert.assertNotNull(manager.getState());
    }

    public void should_NOT_create_state_instance_on_construct_when_the_state_type_is_null_for_a_stateful_manager() {
        class MyManager extends AbstractStatefulManager {
            @Override
            public Class getStateType() {
                return null;
            }
        };
        MyManager manager = new MyManager();

        Assert.assertNull(manager.getState());

        manager.onConstruct();

        Assert.assertNull(manager.getState());
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_excpetion_out_when_creating_state_failed() {
        class BadClass {
            {int x = 1 / 0;}
        }

        class MyManager extends AbstractStatefulManager<BadClass> {
            @Override
            public Class<BadClass> getStateType() {
                return BadClass.class;
            }
        };

        MyManager manager = new MyManager();

        manager.onConstruct();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_excpetion_when_binding_null_to_stateful_manager() {
        class MyManager extends AbstractStatefulManager<String> {
            @Override
            public Class<String> getStateType() {
                return String.class;
            }
        };

        MyManager manager = new MyManager();

        manager.bindState(null);
    }

    @Test
    public void should_be_able_to_successfully_bind_state_to_stateful_manager() {
        class MyManager extends AbstractStatefulManager<String> {
            @Override
            public Class<String> getStateType() {
                return String.class;
            }
        };

        MyManager manager = new MyManager();

        Assert.assertNotEquals("B", manager.getState());

        manager.bindState("B");

        Assert.assertEquals("B", manager.getState());
    }

}
