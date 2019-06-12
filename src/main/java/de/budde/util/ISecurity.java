package de.budde.util;

public interface ISecurity {
    /**
     * take a clear text String and convert it to a translated String, which can be sent over an insecure channel<br>
     * Usually an implementation provides: take a String, encrypt with a strong cipher and apply base64 encoding afterwards
     *
     * @param s the clear text String
     * @return the translated String
     */
    String fromClearText(String s);

    /**
     * take a translated String and convert it to a clear text String. The translated string could have arrived from an insecure channel<br>
     * Usually the implementation provides: take a String, apply base64 decoding and decrypt with a strong cipher afterwards
     *
     * @param s the translated String
     * @return the clear text String
     */
    String toClearText(String s);
}