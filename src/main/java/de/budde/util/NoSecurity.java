package de.budde.util;

public class NoSecurity implements ISecurity {
    /**
     * create an object to encode/decode Strings without any security, e.g. for tests.
     */
    public NoSecurity() {
    }

    @Override
    public String fromClearText(String s) {
        return s;
    }

    @Override
    public String toClearText(String s) {
        return s;
    }
}