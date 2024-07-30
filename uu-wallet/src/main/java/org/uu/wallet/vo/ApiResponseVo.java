package org.uu.wallet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 钱包 API接口返回数据
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseVo implements Serializable {

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 加密后的数据
     */
    private String encryptedData;

    /**
     * 加密后的AES密钥
     */
    private String encryptedKey;
}