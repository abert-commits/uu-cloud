package org.uu.job.entity;

import org.springframework.amqp.rabbit.connection.CorrelationData;

public class CustomCorrelationData2 extends CorrelationData {
    /**
     * 订单号
     */
    private final String orderNo;

    /**
     * 订单超时类型
     */
    private final String taskType;

    /**
     * 队列名称
     */
    private final String queueName;

    public CustomCorrelationData2(String id, String orderNo, String taskType, String queueName) {
        super(id);
        this.orderNo = orderNo;
        this.taskType = taskType;
        this.queueName = queueName;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getQueueName() {
        return queueName;
    }
}
