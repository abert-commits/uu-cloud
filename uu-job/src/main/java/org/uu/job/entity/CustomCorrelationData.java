package org.uu.job.entity;

import org.springframework.amqp.rabbit.connection.CorrelationData;

public class CustomCorrelationData extends CorrelationData {

    /**
     * 队列名称
     */
    private final String queueName;

    public CustomCorrelationData(String id, String queueName) {
        super(id);

        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }
}
