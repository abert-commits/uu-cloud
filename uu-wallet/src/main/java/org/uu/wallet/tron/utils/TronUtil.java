package org.uu.wallet.tron.utils;

import org.bitcoinj.core.Base58;

public class TronUtil {

    private static String encode58(byte[] input) {
        byte[] hash0 = Sha256Sm3Hash.hash(input);
        byte[] hash1 = Sha256Sm3Hash.hash(hash0);
        byte[] inputCheck = new byte[input.length + 4];
        System.arraycopy(input, 0, inputCheck, 0, input.length);
        System.arraycopy(hash1, 0, inputCheck, input.length, 4);
        return Base58.encode(inputCheck);
    }

    /**
     * 十六进制地址为 Base58 地址
     *
     * @param addressHex
     * @return {@link String }
     */
    public static String addressHexToBase58(String addressHex) {
        byte[] toBytes = ByteArray.fromHexString(addressHex);
        return encode58(toBytes);
    }

}
