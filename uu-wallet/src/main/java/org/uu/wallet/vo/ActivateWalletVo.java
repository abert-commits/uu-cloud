package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 激活钱包接口 返回数据
 *
 * @author Simon
 * @date 2023/12/29
 */
@Data
public class ActivateWalletVo implements Serializable {


    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 会员id
     */
    private String memberId;


    /**
     * 激活钱包页面地址
     */
    private String walletActivationPageUrl;


    /**
     * 签名
     */
    private String sign;
}