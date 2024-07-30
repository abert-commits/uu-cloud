package org.uu.wallet.req;


import lombok.Data;
import org.uu.wallet.dto.SyncedBankInfoListDTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * 激活钱包接口 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
public class ActivateWalletV2Req implements Serializable {

    /**
     * 商户号
     */
    @NotBlank(message = "merchantCode cannot be empty")
    private String merchantCode;

    /**
     * 会员id
     */
    @NotBlank(message = "memberId cannot be empty")
    private String memberId;

    /**
     * 时间戳
     */
    @NotBlank(message = "timestamp cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "timestamp cannot be empty")
    private String timestamp;

    /**
     * 返回地址
     */
    @NotBlank(message = "returnUrl cannot be empty")
    private String returnUrl;

    /**
     * 银行卡收款信息列表
     */
    private List<SyncedBankInfoListDTO> bankInfoList;

    /**
     * 签名
     */
    @NotBlank(message = "sign cannot be empty")
    private String sign;
}
