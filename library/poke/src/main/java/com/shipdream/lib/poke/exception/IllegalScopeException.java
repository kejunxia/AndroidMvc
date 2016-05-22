package com.shipdream.lib.poke.exception;

public class IllegalScopeException extends PokeException{
    public IllegalScopeException(String message) {
        super(message);
    }

    public IllegalScopeException(String message, Throwable cause) {
        super(message, cause);
    }
}
