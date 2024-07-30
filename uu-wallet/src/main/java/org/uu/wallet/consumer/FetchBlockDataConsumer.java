package org.uu.wallet.consumer;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.tron.service.BlockSyncService;


/**
 * 拉取区块数据-消费者
 *
 * @author Simon
 * @date 2024/06/13
 */
@Service
public class FetchBlockDataConsumer {

    //拉取区块数据 服务
    private final BlockSyncService blockSyncService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    public FetchBlockDataConsumer(
            BlockSyncService blockSyncService
    ) {
        this.blockSyncService = blockSyncService;
    }

    @RabbitListener(queues = RabbitMqConstants.UU_WALLET_FETCH_BLOCK_DATA_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {

        try {
            //拉取区块数据
            logger.info("Rabbit 消费队列消息: 拉取区块数据");

            //区块同步
            blockSyncService.syncBlocks();

            logger.info("拉取区块数据, 消息消费成功");
            //处理成功 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Rabbit 消费 拉取区块数据失败 : e: {}", e.getMessage());
            try {
                //消费失败了 也手动确认消息 不进行重试
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e1) {
                logger.error("Rabbit 消费 拉取区块数据失败, 手动确认消息失败 : e: {}", e.getMessage());
            }
        }
    }
}
