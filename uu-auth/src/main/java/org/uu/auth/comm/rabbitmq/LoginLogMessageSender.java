package org.uu.auth.comm.rabbitmq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.uu.auth.security.entity.CustomCorrelationData;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.common.core.dto.MemberLoginLogMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * 发送记录日志的MQ
 *
 * @author Simon
 * @date 2024/01/15
 */
@Service
@Slf4j
public class LoginLogMessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 记录前台会员登录日志
     *
     * @param memberId
     * @param username
     * @param loginTime
     * @param ipAddress
     * @param device
     * @param userAgent
     * @param authenticationMode
     * @param memberType
     * @param firstLoginIp
     */
    public void recordLoginLog(Long memberId, String username,LocalDateTime loginTime,String ipAddress,String device,String userAgent,String authenticationMode,String memberType, String firstLoginIp) {

        MemberLoginLogMessage memberLoginLogMessage = new MemberLoginLogMessage();

        //会员id
        memberLoginLogMessage.setMemberId(memberId);

        //会员账号
        memberLoginLogMessage.setUsername(username);

        //登录时间
        memberLoginLogMessage.setLoginTime(loginTime);

        //登录ip
        memberLoginLogMessage.setIpAddress(ipAddress);

        //登录设备
        memberLoginLogMessage.setDevice(device);

        //用户代理（浏览器或设备信息）
        memberLoginLogMessage.setUserAgent(userAgent);

        //登录模式 (前台模式登录)
        memberLoginLogMessage.setAuthenticationMode(authenticationMode);

        //会员类型
        memberLoginLogMessage.setMemberType(memberType);

        //登录状态
        memberLoginLogMessage.setLoginStatus("1");

        //首次登录ip
        memberLoginLogMessage.setFirstLoginIp(firstLoginIp);

        sendLoginLogMessage(memberLoginLogMessage);
    }


    /**
     * 发送 记录登录日志的MQ
     *
     * @param memberLoginLogMessage
     */
    public void sendLoginLogMessage(MemberLoginLogMessage memberLoginLogMessage) {

        String queueName = RabbitMqConstants.WALLET_MEMBER_LOGIN_LOG_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(memberLoginLogMessage);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 记录前台会员登录日志的MQ 消息内容: {}, loginLogMessage: {}", message, memberLoginLogMessage);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }

}
