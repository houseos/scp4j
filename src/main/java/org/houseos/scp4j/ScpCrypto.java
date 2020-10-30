/*
 * secure_control_protocol
 * ScpCrypto Class
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2020 Marcel Jaehn
 */
package org.houseos.scp4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class ScpCrypto {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String DEFAULT_PASSWORD = "01234567890123456789012345678901";

    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final String ENCRYPT_ALGO = "ChaCha20-Poly1305/None/NoPadding";
    private static final int NONCE_LEN = 12; // 96 bits, 12 bytes
    private static final int MAC_LEN = 16; // 128 bits, 16 bytes

    ScpJson encryptThenEncode(String key, String message) {
        EncryptedPayload encryptedPayload = encryptMessage(key, message);
        return new ScpJson(Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8)), encryptedPayload);
    }

    EncryptedPayload encryptMessage(String key, String plainText) {
        Security.addProvider(new BouncyCastleProvider());
        try {
            // Encode Key
            byte[] secretKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "ChaCha20");
            //Encode encrypted text
            byte[] clearText = plainText.getBytes(StandardCharsets.UTF_8);
            // Encrypt
            byte[] nonce = new byte[NONCE_LEN];
            RANDOM.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

            // IV, initialization value with nonce
            IvParameterSpec iv = new IvParameterSpec(nonce);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

            byte[] encryptedText = cipher.doFinal(clearText);

            ByteBuffer bb = ByteBuffer.wrap(encryptedText);

            // This encryptedText contains chacha20 ciphertext + poly1305 MAC
            // ChaCha20 encrypted the plaintext into a ciphertext of equal length.
            byte[] originalCText = new byte[clearText.length];
            byte[] mac = new byte[MAC_LEN]; // 16 bytes , 128 bits

            bb.get(originalCText);
            bb.get(mac);

            String base64Data = base64Encode(originalCText);
            String base64Mac = base64Encode(mac);

            return new EncryptedPayload(
                    base64Data,
                    clearText.length,
                    base64Mac,
                    base64Encode(encryptedText),
                    base64Encode(nonce)
            );
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ScpCrypto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(ScpCrypto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(ScpCrypto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(ScpCrypto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(ScpCrypto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(ScpCrypto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    boolean verifyHMAC(String content, String hmac, String password) {
        String secretKey;
        if (password == null) {
            secretKey = DEFAULT_PASSWORD;
        } else {
            secretKey = password;
        }

        try {
            final byte[] byteKey = secretKey.getBytes(StandardCharsets.UTF_8);
            Mac sha512Hmac = Mac.getInstance(HMAC_SHA512);
            sha512Hmac.init(new SecretKeySpec(byteKey, HMAC_SHA512));
            byte[] macData = sha512Hmac.doFinal(content.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(macData).equals(hmac);
        } catch (IllegalStateException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    String generatePassword() {
        int passwordLengthInBytes = 32;
        int randomValueUpperBound = 256;
        int[] values = new int[passwordLengthInBytes];
        for (int i = 0; i < passwordLengthInBytes; i++) {
            values[i] = RANDOM.nextInt(randomValueUpperBound);
        }
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) values[i];
        }
        return Base64.getUrlEncoder().encodeToString(bytes).substring(0, passwordLengthInBytes);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        int maskLowestEightBitsOne = 0xFF;
        int shiftFourBits = 4;
        int maskLowestFourBitsOne = 0x0f;
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & maskLowestEightBitsOne;
            hexChars[j * 2] = HEX_ARRAY[v >>> shiftFourBits];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & maskLowestFourBitsOne];
        }
        return new String(hexChars);
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}

class EncryptedPayload {

    String base64DataWithMac;
    String base64Data;
    int dataLength;
    String base64Mac;
    String base64Nonce;

    EncryptedPayload(String base64Data, int dataLength, String base64Mac, String base64DataWithMac,
            String base64Nonce) {
        this.base64DataWithMac = base64DataWithMac;
        this.base64Data = base64Data;
        this.dataLength = dataLength;
        this.base64Mac = base64Mac;
        this.base64Nonce = base64Nonce;
    }
}

class ScpJson {

    String key;
    EncryptedPayload encryptedPayload;

    ScpJson(String key, EncryptedPayload encryptedPayload) {
        this.key = key;
        this.encryptedPayload = encryptedPayload;
    }
}
