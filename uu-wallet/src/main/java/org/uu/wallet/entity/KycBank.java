package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author
 * @since 2024-04-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("kyc_bank")
public class KycBank extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 银行编码
     */
    private String bankCode;

    /**
     * 服务编码
     */
    private String serviceCode;

    /**
     * 删除表示 0:未删除 1:已删除
     */
    private Integer deleted;

    /**
     * 图标地址
     */
    private String iconUrl;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 说明
     */
    private String remark;

    /**
     * 连接地址
     */
    private String linkUrl;

    /**
     * 连接方式, 1: 唤醒APP, 2: 跳转H5
     */
    private String linkType;

    /**
     * 获取交易记录api地址
     */
    private String apiUrl;

    /**
     * 来源 1-app 2-H5
     */
    private Integer sourceType;

    /**
     * 短信验证码长度
     */
    private Integer smsCodeLength;

    /**
     * kyc类型 1：银行卡 3：upi
     */
    private Integer type;
}
