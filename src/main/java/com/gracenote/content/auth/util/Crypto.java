package com.gracenote.content.auth.util;

import static org.apache.commons.codec.binary.Hex.decodeHex;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

/**
 * @author deepak on 22/12/17.
 */
public class Crypto {

    public static String dycryptData(String encryptedData) {

        char[] hex2PR = Constants.PR_KEY.toCharArray();
        byte[] encodedPR = null;

        try {
            encodedPR = decodeHex(hex2PR);
        } catch (DecoderException e) {
            e.printStackTrace();
        }

        KeyFactory rsaKeyFac = null;
        try {
            rsaKeyFac = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(encodedPR); //encodedPrivK

        PrivateKey privKey = null;
        try {
            privKey = (PrivateKey)rsaKeyFac.generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        Cipher dipher = null;
        try {
            dipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try {
            dipher.init(Cipher.DECRYPT_MODE, privKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        String dycrptedData = null;
        try {
            dycrptedData = new String(dipher.doFinal(Base64.decodeBase64(encryptedData)));
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return dycrptedData;
    }
}
