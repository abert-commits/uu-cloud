package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author
 */
@Data
@ApiModel(description ="申诉")
public class AppealOrderReq extends PageRequest {

    private long id;

    /**
     * 会员id
     */
    private String mid;

    /**
     * 会员账号
     */
    private String mAccount;

    /**
     * 所属商户code
     */
    private String belongMerchantCode;

    /**
     * 提现订单号
     */
    private String withdrawOrderNo;

    /**
     * 充值订单号
     */
    private String rechargeOrderNo;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 申诉类型: 1-提现申诉 2-充值申诉
     */
    private Integer appealType;

    /**
     * 申诉状态: 1-代处理 2-申诉成功 3-申诉失败
     */
    private Integer appealStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 申诉原因
     */
    private String reason;

    /**
     * 图片信息
     */
    private String picInfo;

    /**
     * 视频url
     */
    private String videoUrl;


}