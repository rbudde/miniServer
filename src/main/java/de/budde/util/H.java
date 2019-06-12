package de.budde.util;

/**
 * this is a helper class. Is it questionable from software engineering principles to have such a mingle-mangle of methods?<br>
 * Anyhow, it should contain pure functions:<br>
 * - public static<br>
 * - no state (especially no access to static variables which are no constants)<br>
 * - easily testable<br>
 */
public class H {
    /**
     * take objects as varargs and return them as array
     *
     * @param <T> the type of the vararg objects that should be returned as an array of that type
     * @param args the objects to be returned as an array
     * @return the array of objects
     */
    @SafeVarargs
    public static <T> T[] toArray(T... args) {
        return args;
    }

    /**
     * split a string at "=". Usable for parsing parameters of pattern "KEY=VALUE"<br>
     *
     * @param s string to be splitted
     * @return null, if s has wrong format; otherwise String[2] [0] is the key, [1] is the value. The value may be "" if s is "KEY="
     */
    public static String[] splitter(String s) {
        String[] split = s.split("\\s*=\\s*", 2);
        return split.length == 2 ? split : null;
    }

    /**
     * true, if string is <i>after trimming</i> not null and not empty
     *
     * @param test String to be tested
     */
    public static boolean notEmpty(String test) {
        return test != null && !test.trim().isEmpty();
    }
}
