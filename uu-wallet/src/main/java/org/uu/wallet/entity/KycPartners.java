package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * <p>
 * kyc信息表
 * </p>
 *
 * @author
 * @since 2024-04-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("kyc_partners")
public class KycPartners extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员id
     */
    private String memberId;

    /**
     * 会员账号
     */
    private String memberAccount;

    /**
     * 银行登录令牌
     */
    private String token;

    /**
     * 手机号
     */
    private String mobileNumber;

    /**
     * 银行编码
     */
    private String bankCode;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * upi_id
     */
    private String upiId;

    /**
     * 账户姓名
     */
    private String name;

    /**
     * 账户
     */
    private String account;


    /**
     * 备注
     */
    private String remark;


    /**
     * 删除表示: 0: 未删除, 1: 已删除
     */
    private Integer deleted;



    /**
     * 图标地址
     */
    private String iconUrl;

    /**
     * 来源 1-app 2-H5
     */
    private Integer sourceType;


    /**
     * 收款方式 1-银行卡 3-upi
     */
    private Integer collectionType;

    /**
     * 银行卡号
     */
    private String bankCardNumber;

    /**
     * 银行持卡人
     */
    private String bankCardOwner;

    /**
     * ifsc
     */
    private String bankCardIfsc;

    /**
     * 总买入次数
     */
    private Integer totalBuySuccessCount;

    /**
     * 总买入金额
     */
    private BigDecimal totalBuySuccessAmount;

    /**
     * 总卖出数量
     */
    private Integer totalSellSuccessCount;

    /**
     * 总卖出金额
     */
    private BigDecimal totalSellSuccessAmount;

    private Integer status;

}
