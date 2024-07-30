package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 进入钱包 返回数据
 *
 * @author Simon
 * @date 2023/12/29
 */
@Data
public class AccessWalletVo implements Serializable {


    /**
     * 商户号
     */
    private String merchantCode;


    /**
     * 钱包地址
     */
    private String walletAccessUrl;


    /**
     * 会员id
     */
    private String memberId;


    /**
     * 时间戳
     */
    private String timestamp;


    /**
     * 签名
     */
    private String sign;
}