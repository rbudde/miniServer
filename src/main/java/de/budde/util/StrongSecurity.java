package de.budde.util;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.fraunhofer.iais.dbc.DBC;
import de.fraunhofer.iais.dbc.DBCException;

public class StrongSecurity implements ISecurity {
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final SecretKeySpec secret;
    private final String cipher;

    /**
     * create an object to encode/decode Strings with a symmetric cipher.
     *
     * @param secret the password, not null, length must be 16 char
     * @param cipher a safe cipher, not null, corresponding cipher must exists, usually "AES"
     */
    public StrongSecurity(String secret, String cipher) {
        DBC.isTrue(secret != null && secret.length() == 16, "secret is invalid");
        byte[] key = secret.getBytes(UTF_8);
        this.secret = new SecretKeySpec(key, cipher);
        // TODO: Design alternatives if this is considered too expensive?
        try {
            Cipher.getInstance(cipher);
        } catch ( NoSuchAlgorithmException | NoSuchPaddingException e ) {
            throw new DBCException("cipher " + cipher + " doesn't exist");
        }
        this.cipher = cipher;
    }

    @Override
    public String fromClearText(String s) {
        try {
            Cipher cipherInstance = Cipher.getInstance(this.cipher);
            cipherInstance.init(Cipher.ENCRYPT_MODE, this.secret);
            byte[] encrypted = cipherInstance.doFinal(s.getBytes(UTF_8));
            return new String(Base64.getEncoder().encode(encrypted), UTF_8);
        } catch ( InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e ) {
            throw new DBCException("encryption fails", e);
        }
    }

    @Override
    public String toClearText(String s) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(s.getBytes(UTF_8));
            Cipher cipherInstance = Cipher.getInstance(this.cipher);
            cipherInstance.init(Cipher.DECRYPT_MODE, this.secret);
            byte[] decrypted = cipherInstance.doFinal(encrypted);
            return new String(decrypted, UTF_8);
        } catch ( InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e ) {
            throw new DBCException("decryption fails", e);
        }
    }
}