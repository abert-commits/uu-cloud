package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
public class CollectionOrderReq extends PageRequest {


    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付方式 默认值: UPI
     */
    private String payType = "3";

    /**
     * 商户订单号
     */
    private String merchantOrder;

    /**
     * 平台订单号
     */
    private String platformOrder;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单费率
     */
    private BigDecimal orderRate;

    /**
     * 订单状态 默认状态: 待支付
     */
    private String orderStatus;

    /**
     * 交易回调状态 默认状态: 未回调
     */
    private String tradeCallbackStatus;

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 交易回调地址
     */
    private String tradeNotifyUrl;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 签名key
     */
    @TableField(exist = false)
    private String key;

    /**
     * 交易回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeCallbackTime;

    /**
     * 订单费用
     */
    private BigDecimal cost;

    /**
     * 交易回调是否发送 默认值为: 未发送
     */
    private String tradeNotifySend;

    /**
     * 会员id
     */
    private String memberId;

    /**
     * UTR
     */
    private String utr;

    /**
     * 奖励
     */
    private String bonus;

    /**
     * 实际金额
     */
    private BigDecimal actualAmount;

    /**
     * 会员账号
     */
    private String memberAccount;

    /**
     * 完成时长
     */
    private String completeDuration;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 凭证
     */
    private String voucher;


    private String beginTime;

    public String getAmountStr() {
        return this.getAmount().toString();
    }
}