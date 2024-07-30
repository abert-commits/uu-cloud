package org.uu.wallet.req;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 钱包API接口 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
public class ApiRequest implements Serializable {

    private static final long serialVersionUID = 1L; // 显式序列化版本ID

    /**
     * 商户号
     */
    @NotBlank(message = "merchantCode cannot be empty")
    private String merchantCode;

    /**
     * 加密后的数据
     */
    @NotBlank(message = "encryptedData cannot be empty")
    private String encryptedData;

    /**
     * 加密后的AES密钥
     */
    @NotBlank(message = "encryptedKey cannot be empty")
    private String encryptedKey;

    /**
     * 支付方式
     */
    @NotBlank(message = "channel cannot be empty")
    private String channel;

}
