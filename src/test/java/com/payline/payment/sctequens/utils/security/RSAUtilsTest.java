package com.payline.payment.sctequens.utils.security;

import com.payline.payment.sctequens.exception.PluginException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RSAUtilsTest {
    private static final String ALGORITHM = "AES";

    private RSAUtils rsaUtils = RSAUtils.getInstance();

    @Test
    void encryptAndDecryptTest() {
        String text = "I am a plain text message";

        String key = rsaUtils.generateKey();
        String enc = rsaUtils.encrypt(text, key);
        String dec = rsaUtils.decrypt(enc, key);

        Assertions.assertEquals(text, dec);
    }

    @Test
    void encryptFailureTest() {
        Assertions.assertThrows(PluginException.class, () -> rsaUtils.encrypt("message", "badKey"));
    }

    @Test
    void decryptFailureTest() {
        Assertions.assertThrows(PluginException.class, () -> rsaUtils.decrypt("message", "badKey"));
    }

}