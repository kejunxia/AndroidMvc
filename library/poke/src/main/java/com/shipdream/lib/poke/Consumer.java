package com.shipdream.lib.poke;

/**
 * Consumer to consume an injected object
 * @param <T>
 */
public abstract class Consumer <T> {
    public abstract void consume(T instance);
}
