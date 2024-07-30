package org.uu.wallet.util;

import lombok.extern.slf4j.Slf4j;
import org.uu.wallet.Enum.MemberAuthenticationStatusEnum;
import org.uu.wallet.Enum.SchemeConfigTagEnum;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.TradeConfigScheme;
import org.uu.wallet.service.ITradeConfigSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TradeConfigHelperUtil {

    @Autowired
    private ITradeConfigSchemeService tradeConfigSchemeService;

    /**
     * 根据会员的实名认证状态获取对应的方案配置
     *
     * @param memberInfo 会员信息
     * @return 方案配置
     */
    public TradeConfigScheme getSchemeConfigByMemberTag(MemberInfo memberInfo) {
        SchemeConfigTagEnum tag = MemberAuthenticationStatusEnum.UNAUTHENTICATED.getCode().equals(memberInfo.getAuthenticationStatus())
                ? SchemeConfigTagEnum.WALLET_ACTIVATION
                : SchemeConfigTagEnum.REAL_NAME_VERIFICATION;

        log.info("获取会员配置标签: {} 会员信息: {}", tag.getLabel(), memberInfo);

        // 根据方案配置标签获取方案配置
        return tradeConfigSchemeService.getSchemeConfigByTag(tag.getCode());
    }
}
