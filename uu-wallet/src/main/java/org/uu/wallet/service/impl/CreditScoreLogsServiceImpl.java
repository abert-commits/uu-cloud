package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.CreditScoreLogsDTO;
import org.uu.common.pay.req.CreditScoreLogsListPageReq;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.*;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.CreditScoreLogs;
import org.uu.wallet.mapper.CreditScoreLogsMapper;
import org.uu.wallet.req.CreditScoreLogsListReq;
import org.uu.wallet.service.ICreditScoreLogsService;
import org.uu.wallet.vo.CreditScoreLogsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 信用分记录表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
@Service
@RequiredArgsConstructor
public class CreditScoreLogsServiceImpl extends ServiceImpl<CreditScoreLogsMapper, CreditScoreLogs> implements ICreditScoreLogsService {

    private final WalletMapStruct walletMapStruct;

    @Override
    public boolean addLog(Long memberId, BigDecimal changeScore, BigDecimal currentScore, Integer eventType, Integer tradeType) {
        CreditScoreLogs creditLog = createCreditLog(memberId, changeScore, currentScore, eventType, tradeType);
        return addLog(creditLog);
    }

    @Override
    public boolean addLog(CreditScoreLogs creditScoreLogs) {
        int insert = baseMapper.insert(creditScoreLogs);
        return insert == 1;
    }

    @Override
    public PageReturn<CreditScoreLogsDTO> listPage(CreditScoreLogsListPageReq req) {
        Page<CreditScoreLogs> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<CreditScoreLogs> lambdaQuery = lambdaQuery();

        if (ObjectUtils.isNotEmpty(req.getMemberId())) {
            lambdaQuery.eq(CreditScoreLogs::getMemberId, req.getMemberId());
        }

        if (ObjectUtils.isNotEmpty(req.getTradeType())) {
            lambdaQuery.eq(CreditScoreLogs::getTradeType, req.getTradeType());
        }

        if (ObjectUtils.isNotEmpty(req.getEventType())) {
            lambdaQuery.eq(CreditScoreLogs::getEventType, req.getEventType());
        }

        if (ObjectUtils.isNotEmpty(req.getChangeType())) {
            lambdaQuery.eq(CreditScoreLogs::getChangeType, req.getChangeType());
        }

        if (ObjectUtils.isNotEmpty(req.getStartTime())) {
            lambdaQuery.ge(CreditScoreLogs::getCreateTime, req.getStartTime());
        }

        if (ObjectUtils.isNotEmpty(req.getEndTime())) {
            lambdaQuery.le(CreditScoreLogs::getCreateTime, req.getEndTime());
        }
        lambdaQuery.orderByDesc(CreditScoreLogs::getId);

        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<CreditScoreLogsDTO> creditScoreLogsDTOS = new ArrayList<>();
        List<CreditScoreLogs> records = page.getRecords();
        for (CreditScoreLogs record : records) {
            CreditScoreLogsDTO dto = new CreditScoreLogsDTO();
            BeanUtils.copyProperties(record, dto);
            dto.setId(record.getMemberId());
            creditScoreLogsDTOS.add(dto);
        }

        return PageUtils.flush(page, creditScoreLogsDTOS);
    }

    @Override
    public PageReturn<CreditScoreLogsDTO> listPage(long memberId, long pageSize) {
        CreditScoreLogsListPageReq req = new CreditScoreLogsListPageReq();
        req.setPageSize(pageSize);
        req.setMemberId(memberId);
        return listPage(req);
    }

    @Override
    public PageReturn<CreditScoreLogsVo> list(CreditScoreLogsListReq req) {
        Long memberId = UserContext.getCurrentUserId();
        CreditScoreLogsListPageReq reqParam = new CreditScoreLogsListPageReq();
        reqParam.setPageNo(req.getPageNo());
        reqParam.setPageSize(req.getPageSize());
        reqParam.setMemberId(memberId);
        PageReturn<CreditScoreLogsDTO> resultPage = listPage(reqParam);
        List<CreditScoreLogsVo> resultList = new ArrayList<>();
        for (CreditScoreLogsDTO creditScoreLogsDTO : resultPage.getList()) {
            CreditScoreLogsVo vo = new CreditScoreLogsVo();
            int changeType = creditScoreLogsDTO.getChangeType();
            vo.setMemberId(creditScoreLogsDTO.getMemberId());
            String changeScoreStr = String.valueOf(creditScoreLogsDTO.getChangeScore());
            if(changeType == CreditChangeTypeEnum.ADD.getCode()){
                changeScoreStr = "+" + changeScoreStr;
            }
            vo.setChangeScore(changeScoreStr);
            vo.setChangeType(creditScoreLogsDTO.getChangeType());
            vo.setCreateTime(creditScoreLogsDTO.getCreateTime());
            vo.setEventName(CreditEventNameEnum.getCodeByEventTypeAndTradeType(creditScoreLogsDTO.getEventType(), creditScoreLogsDTO.getTradeType()));
            resultList.add(vo);
        }
        Page<CreditScoreLogsVo> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(resultPage.getTotal());
        return PageUtils.flush(page, resultList);
    }

    private CreditScoreLogs createCreditLog(Long memberId, BigDecimal changeScore, BigDecimal currentScore, Integer eventType, Integer tradeType){
        CreditScoreLogs creditScoreLogs = new CreditScoreLogs();
        creditScoreLogs.setMemberId(memberId);
        creditScoreLogs.setBeforeScore(currentScore);
        creditScoreLogs.setChangeScore(changeScore);
        creditScoreLogs.setEventType(eventType);
        creditScoreLogs.setTradeType(tradeType);
        int changeType = CreditChangeTypeEnum.DECREASE.getCode();
        if(changeScore.compareTo(BigDecimal.ZERO) > 0){
            changeType = CreditChangeTypeEnum.ADD.getCode();
        }
        creditScoreLogs.setChangeType(changeType);
        BigDecimal afterScore = currentScore.add(changeScore);
        creditScoreLogs.setAfterScore(afterScore);
        return creditScoreLogs;
    }
}
