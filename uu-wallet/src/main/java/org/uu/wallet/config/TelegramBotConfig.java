package org.uu.wallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.uu.wallet.util.MyBot;

import javax.annotation.PostConstruct;

/**
 * @author afei
 * @date 2024/7/29
 */
@Configuration
public class TelegramBotConfig {

    @Bean
    public DefaultBotOptions defaultBotOptions() {
        DefaultBotOptions botOptions = new DefaultBotOptions();
        // @TODO 如果需要代理，配置代理
       /* botOptions.setProxyHost("127.0.0.1");
        botOptions.setProxyPort(7890);
        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);*/
        return botOptions;
    }

    @Bean
    public MyBot myBot(DefaultBotOptions botOptions) {
        return new MyBot(botOptions);
    }

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(myBot(defaultBotOptions()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
