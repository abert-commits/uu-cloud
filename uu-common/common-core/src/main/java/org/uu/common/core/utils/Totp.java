package org.uu.common.core.utils;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

                public class Totp {


                    private static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};


                    private static byte[] hmac_sha(String crypto, byte[] keyBytes, byte[] text) {
                        try {
                            Mac hmac;
                            hmac = Mac.getInstance(crypto);
                            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
                            hmac.init(macKey);
                            return hmac.doFinal(text);
                        } catch (GeneralSecurityException gse) {
                            throw new UndeclaredThrowableException(gse);
                        }
                    }

                    /**
                     * This method converts a HEX string to Byte[]
                     *
                     * @param hex : the HEX string
                     * @return: a byte array
                     */
                    private static byte[] hexStr2Bytes(String hex) {
                        // Adding one byte to get the right conversion
                        // Values starting with "0" can be converted
                        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

                        // Copy all the REAL bytes, not the "first"
                        byte[] ret = new byte[bArray.length - 1];
                        System.arraycopy(bArray, 1, ret, 0, ret.length);
                        return ret;
                    }


                    public static String generateTOTP(String key, String time, String returnDigits, String crypto) {
                        int codeDigits = Integer.decode(returnDigits);
                        String result = null;

                        // Using the counter
                        // First 8 bytes are for the movingFactor
                        // Compliant with base RFC 4226 (HOTP)
                        while (time.length() < 16)
                            time = "0" + time;

                        // Get the HEX in a Byte[]
                        byte[] msg = hexStr2Bytes(time);
                        byte[] k = hexStr2Bytes(key);
                        byte[] hash = hmac_sha(crypto, k, msg);

                        // put selected bytes into result int
                        int offset = hash[hash.length - 1] & 0xf;

                        int binary = ((hash[offset] & 0x7f) << 24)
                                | ((hash[offset + 1] & 0xff) << 16)
                                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

                        int otp = binary % DIGITS_POWER[codeDigits];

                        result = Integer.toString(otp);
                        while (result.length() < codeDigits) {
                            result = "0" + result;
                        }
                        return result;
                    }}
