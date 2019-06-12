package de.budde.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.fraunhofer.iais.dbc.DBCException;

public class ISecurityTest {
    private static final String PANGRAM1 = "Üben von Xylophon und Querflöte ist ja zweckmäßig";
    private static final String PANGRAM2 = "Съешь ещё этих мягких французских булок, да выпей чаю";
    private static final String TEST1 =
        "" //
            + "{ \"greeting\": \"Hello!\",\r\n"
            + "      \"rnd\": 30,\r\n"
            + "      \"from\": \"Pid, the cavy\",\r\n"
            + "      \"to\": \"the world\"\r\n"
            + "    }";
    private static final String TEST2 =
        "" //
            + "{ \"greeting\": \"Hello!\",\n"
            + "      \"rnd\": 30,\n"
            + "      \"from\": \"Pid, the cavy\",\n"
            + "      \"to\": \"the world\"\n"
            + "    }";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ISecurity security;

    @Test
    public void testSecretException() {
        this.thrown.expect(DBCException.class);
        this.thrown.expectMessage("secret is invalid");
        new StrongSecurity("1234567890", "AES");
    }

    @Test
    public void testCipherException() {
        this.thrown.expect(DBCException.class);
        this.thrown.expectMessage("cipher cipher-does-not-exist doesn't exist");
        new StrongSecurity("1234567890ABCDEF", "cipher-does-not-exist");
    }

    @Test
    public void testStrongSecurity() {
        this.security = new StrongSecurity("1234567890abcdef", "AES"); // this is NO strong secret :-)
        strongSecurity(PANGRAM1);
        strongSecurity(PANGRAM2);
        strongSecurity(TEST1);
        strongSecurity(TEST2);
    }

    @Test
    public void testNoSecurity() {
        this.security = new NoSecurity();
        noSecurity(PANGRAM1);
        noSecurity(PANGRAM2);
        noSecurity(TEST1);
        noSecurity(TEST2);
    }

    private void strongSecurity(String test) {
        String encrypted = this.security.fromClearText(test);
        assertFalse(test.equalsIgnoreCase(encrypted));
        String decrypted = this.security.toClearText(encrypted);
        assertTrue(test.equals(decrypted));
    }

    private void noSecurity(String test) {
        assertEquals(test, this.security.fromClearText(test));
        assertEquals(test, this.security.toClearText(test));
        assertEquals(test, this.security.toClearText(this.security.fromClearText(test)));
    }

}
