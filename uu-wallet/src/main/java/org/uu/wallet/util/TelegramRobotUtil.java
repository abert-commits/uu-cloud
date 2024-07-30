package org.uu.wallet.util;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.uu.common.core.result.ResultCode;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.common.web.exception.BizException;
import org.uu.wallet.entity.MerchantInfo;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.service.IMerchantBindBotService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Admin
 */
@Slf4j
@Component
public class TelegramRobotUtil {
    @Resource
    RedissonUtil redissonUtil;
    @Resource
    MerchantInfoMapper merchantInfoMapper;
    @Resource
    private IMerchantBindBotService merchantBindBotService;

    /**
     * usdt自动出款账户余额，当低于5000U 发送消息到telegram提醒
     *
     * @param merchantCode 商户code
     * @param balance      商户余额
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean balanceReminderMessage(String merchantCode, BigDecimal balance) {

        log.info("usdt自动出款账户余额，当低于5000U 发送提醒,商户ID->{}", merchantCode);
        String key = "uu-wallet-merchant-telegram" + merchantCode;
        RLock lock = redissonUtil.getLock(key);
        boolean req = false;
        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);
            if (req) {
                MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoById(merchantCode);
                if (ObjectUtils.isEmpty(merchantInfo)) {
                    log.error("商户不存在,商户ID->{}", merchantCode);
                    throw new BizException(ResultCode.MERCHANT_NOT_EXIST);
                }
                String message = String.format("尊敬的商户:%s您好，目前您的usdt自动出款账户余额:%s，请知晓，以便代付不会因为余额不足而等待太久", merchantCode, balance);
                sendMessageTelegram(message);
            } else {
                log.info("获取锁失败回滚操作,商户ID->{},", merchantCode);
                throw new Exception("获取锁失败,回滚操作");
            }
        } catch (Exception e) {
            log.error("商户ID->{}异常信息->{}", merchantCode, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Boolean.FALSE;
        } finally {
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Boolean.TRUE;
    }

    //绑定商户
    public String merchantBindBot(String merchantCode, Chat messageChat) {
        if (Objects.nonNull(messageChat)) {
            return merchantBindBotService.merchantBindBot(merchantCode, messageChat.getId(), messageChat.getTitle());
        }
        return "group is empty";
    }

    public static void sendMessageTelegram(String message) {
        String botToken = "6492239410:AAFmJGhoMv6tr8ZtYa8oIirmt1XQccGL4yQ";
        String chatId = "7062588890";
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        String jsonPayload = "{\"chat_id\":\"" + chatId + "\",\"text\":\"" + message + "\"}";

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(jsonPayload, "UTF-8"));
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = httpClient.execute(httpPost);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
