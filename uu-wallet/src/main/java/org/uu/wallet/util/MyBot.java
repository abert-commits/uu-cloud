package org.uu.wallet.util;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.uu.wallet.entity.MerchantBindBot;
import org.uu.wallet.entity.MerchantInfo;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.service.IMerchantBindBotService;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author afei
 * @date 2024/7/29
 */
public class MyBot extends TelegramLongPollingBot {

    @Resource
    MerchantInfoMapper merchantInfoMapper;
    @Resource
    private TelegramRobotUtil telegramRobotUtil;
    @Resource
    private IMerchantBindBotService merchantBindBotService;

    private final String token = "6492239410:AAFmJGhoMv6tr8ZtYa8oIirmt1XQccGL4yQ";

    private final String botUsername = "merchantinfo_12_bot";

    public MyBot(DefaultBotOptions botOptions) {
        super(botOptions);
    }


    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText();
            long chatId = message.getChatId();
            Chat messageChat = message.getChat();
            String responseText = handleIncomingMessage(messageText, messageChat);

            SendMessage response = new SendMessage();
            response.setChatId(String.valueOf(chatId));
            response.setText(responseText);

            try {
                execute(response);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private String handleIncomingMessage(String messageText, Chat messageChat) {
        //请先绑定商户
        if (messageText.startsWith("/bind")) {
            //是否已经绑定
            Long groupId = messageChat.getId();
            boolean bind = merchantBindBotService.isBind(groupId);
            if (bind) {
                return "The current group is  bound to a merchant";
            }

            // 要绑定群组和商户
            String merchantCode = messageText.substring(5);
            String groupName = messageChat.getTitle();
            return merchantBindBotService.merchantBindBot(merchantCode, groupId, groupName);
        } else {
            Long groupId = messageChat.getId();
            //绑定之后，根据命令查询对应的商户信息
            MerchantBindBot merchantBindBot = merchantBindBotService.merchantCode(groupId);
            if (Objects.isNull(merchantBindBot)) {
                return "Please bind the current group to the merchant first";
            }
            //拿到当前群组的商户编码
            String merchantCode = merchantBindBot.getMerchantCode();


            if (messageText.equalsIgnoreCase("/info")) {
                return "This is a demo bot created by afei.";
            } else if (messageText.startsWith("/balance")) {
                return "This is a demo bot created by afei.";
            } else {
                return "I'm sorry, I didn't understand that command.";
            }
        }


    }

    public String getBalance(String merchantCode) {
        MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoById(merchantCode);
        if (Objects.isNull(merchantInfo)) {
            return "merchant does not exist";
        }
        return String.format("尊敬的商户: %s 您好，目前您账户余额: %s",
                merchantCode, merchantInfo.getBalance());
    }
}
