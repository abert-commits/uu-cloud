package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.uu.wallet.entity.MerchantBindBot;
import org.uu.wallet.mapper.MerchantBindBotMapper;
import org.uu.wallet.service.IMerchantBindBotService;

import java.util.Objects;

/**
 * <p>
 * 商户绑定机器人 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-29
 */
@Service
public class MerchantBindBotServiceImpl extends ServiceImpl<MerchantBindBotMapper, MerchantBindBot> implements IMerchantBindBotService {


    @Override
    public String merchantBindBot(String merchantCode, Long groupId, String groupName) {
        MerchantBindBot bindBot = lambdaQuery()
                .eq(MerchantBindBot::getGroupId, groupId)
                .eq(MerchantBindBot::getDeleted, 0)
                .one();
        if (Objects.nonNull(bindBot)) {
            return String.format("The current group has been bound to a merchant: %s", bindBot.getMerchantCode());
        }

        //商户绑定群组
        MerchantBindBot merchantBindBot = new MerchantBindBot();
        merchantBindBot.setMerchantCode(merchantCode);
        merchantBindBot.setGroupName(groupName);
        merchantBindBot.setGroupId(groupId);
        boolean save = save(merchantBindBot);
        return save ? "bind merchant success" : "bind merchant fail";
    }

    @Override
    public MerchantBindBot merchantCode(Long groupId) {
        return lambdaQuery()
                .eq(MerchantBindBot::getGroupId, groupId)
                .eq(MerchantBindBot::getDeleted, 0)
                .one();

    }

    @Override
    public boolean isBind(Long groupId) {
        MerchantBindBot bindBot = lambdaQuery()
                .eq(MerchantBindBot::getGroupId, groupId)
                .eq(MerchantBindBot::getDeleted, 0)
                .one();
        return Objects.nonNull(bindBot);
    }
}
