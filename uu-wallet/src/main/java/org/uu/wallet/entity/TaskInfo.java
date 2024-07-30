package org.uu.wallet.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class TaskInfo implements Serializable {

    /*
     * 队列名称
     * */
//    private String queueName;

    /*
     * 订单号
     * */
    private String orderNo;

    /*
     * 任务类型
     * */
    private String taskType;

    /**
     * 匹配时间戳 (每次匹配时 都要更新这个值) 避免MQ延时消息问题 还有定时任务兜底方案扫描 也是扫这个值
     */
    private Long lastUpdateTimestamp;
}
