package org.uu.wallet.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 退款API接口 请求参数
 *
 * @author admin
 */
@Data
public class ApiCashBackRequest implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * 签名
     */
    @NotBlank(message = "encryptedKey cannot be empty")
    private String signature;


    private String random;

    private String timestamp;
}
