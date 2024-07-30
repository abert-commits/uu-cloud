package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 会员领取任务记录
 * </p>
 *
 * @author
 * @since 2024-03-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("task_collection_record")
public class TaskCollectionRecord extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员ID
     */
    private String memberId;

    /**
     * 商户code
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 任务订单号
     */
    private String orderNo;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 任务周期
     */
    private Integer taskCycle;

    /**
     * 奖励金额
     */
    private BigDecimal rewardAmount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * 会员账号
     */
    private String memberAccount;

    /**
     * 领取IP
     */
    private String receiveIp;

    /**
     * 领取标识：1-手动领取 2-自动领取
     */
    private Integer receiveType;

    /**
     * 领取奖励日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receiveDate;

}
