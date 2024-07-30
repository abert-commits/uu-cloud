package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.wallet.entity.MerchantBindBot;

/**
 * <p>
 * 商户绑定机器人 服务类
 * </p>
 *
 * @author
 * @since 2024-07-29
 */
public interface IMerchantBindBotService extends IService<MerchantBindBot> {

    String merchantBindBot(String merchantCode, Long groupId, String groupName);

    MerchantBindBot merchantCode(Long groupId);

    boolean isBind(Long groupId);
}
