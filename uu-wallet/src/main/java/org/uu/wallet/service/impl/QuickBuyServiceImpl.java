package org.uu.wallet.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.web.exception.BizException;
import org.uu.wallet.Enum.BuyStatusEnum;
import org.uu.wallet.Enum.MemberStatusEnum;
import org.uu.wallet.Enum.PayTypeEnum;
import org.uu.wallet.Enum.SwitchIdEnum;
import org.uu.wallet.entity.MatchPool;
import org.uu.wallet.req.BuyReq;
import org.uu.wallet.service.*;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.vo.BuyListVo;
import org.uu.wallet.vo.MemberInformationVo;
import org.uu.wallet.vo.QuickBuyMatchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

import static org.uu.common.core.result.ResultCode.*;

@Service
@Slf4j
public class QuickBuyServiceImpl implements QuickBuyService {

    @Autowired
    private IMemberInfoService memberInfoService;
    @Autowired
    private IBuyService buyService;

    @Autowired
    private IControlSwitchService controlSwitchService;



//    /**
//     * 确认买入
//     *
//     * @param buyReq
//     * @return
//     */
//    @Override
//    @Transactional
//    public RestResult confirmBuy(BuyReq buyReq, HttpServletRequest request) {
//        MemberInformationVo currentMemberInfo = memberInfoService.getCurrentMemberInfo().getData();
//        validateAmount(currentMemberInfo, buyReq.getAmount());
//        return buyService.buyProcessor(buyReq, request);
//    }

    private void validateAmount(MemberInformationVo memberInfo, BigDecimal amount) {

        if (controlSwitchService.isSwitchEnabled(SwitchIdEnum.REAL_NAME_VERIFICATION.getSwitchId())) {
            // memberInfo里存的认证值不一样,不能用枚举
            if ("0".equals(memberInfo.getAuthenticationStatus())) {
                throw new BizException(MEMBER_NOT_VERIFIED);
            }
        }
        if (amount.compareTo(new BigDecimal(memberInfo.getQuickBuyMinLimit())) < 0) {
            throw new BizException(NOT_MORE_THAN_MIN_LIMIT);
        }

        if (amount.compareTo(new BigDecimal(memberInfo.getQuickBuyMaxLimit())) > 0) {
            throw new BizException(NOT_LESS_THAN_MAX_LIMIT);
        }

        //判断当前会员状态和买入状态是否可用
        if (BuyStatusEnum.DISABLE.getCode().equals(memberInfo.getBuyStatus())) {
            throw new BizException(MEMBER_BUY_STATUS_NOT_AVAILABLE);
        }

        if (MemberStatusEnum.DISABLE.getCode().equals(memberInfo.getStatus())) {
            throw new BizException(MEMBER_STATUS_NOT_AVAILABLE);
        }
    }

}
