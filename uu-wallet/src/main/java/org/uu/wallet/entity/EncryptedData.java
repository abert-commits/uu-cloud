package org.uu.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 封装加密后的数据和AES密钥
 *
 * @author Simon
 * @date 2023/12/29
 */
@Data
@AllArgsConstructor
public class EncryptedData implements Serializable {

    /**
     * 加密后的数据
     */
    private String encryptedData;

    /**
     * 加密后的AES密钥
     */
    private String encryptedKey;

}
