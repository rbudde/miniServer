package de.budde.util;

import org.junit.Assert;
import org.junit.Test;

public class HTest {
    // @formatter:off
    private static final String[] abc = {"a","b","c"};
    private static final Integer[] ezd = {1,2,3};
    // @formatter:on

    @Test
    public void testToArray() {
        Assert.assertArrayEquals(abc, H.toArray("a", "b", "c"));
        Assert.assertArrayEquals(ezd, H.toArray(1, 2, 3));
    }

    @Test
    public void testNotEmpty() {
        Assert.assertTrue(H.notEmpty("not-empty"));
        Assert.assertFalse(H.notEmpty(" "));
        Assert.assertFalse(H.notEmpty(""));
        Assert.assertFalse(H.notEmpty(null));
    }

    @Test
    public void testSplit() {
        Assert.assertArrayEquals(H.toArray("a", "b"), H.splitter("a=b"));
        Assert.assertArrayEquals(H.toArray("a", ""), H.splitter("a="));
        Assert.assertNull(H.splitter("abc"));
    }
}
