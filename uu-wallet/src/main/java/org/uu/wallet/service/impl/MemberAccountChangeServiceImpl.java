package org.uu.wallet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.visitor.functions.If;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.bo.MemberAccountChangeBO;
import org.uu.common.pay.dto.MemberAccountChangeDTO;
import org.uu.common.pay.dto.MemberAccountChangeExportDTO;
import org.uu.common.pay.dto.PaymentOrderExportDTO;
import org.uu.common.pay.req.MemberAccountChangeReq;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.pay.vo.response.MemberAccountChangeDetailResponseVO;
import org.uu.common.pay.vo.response.MemberAccountChangeResponseVO;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.PayTypeEnum;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.MemberAccountChangeMapper;
import org.uu.wallet.req.ViewTransactionHistoryReq;
import org.uu.wallet.service.IMemberAccountChangeService;
import org.uu.wallet.service.IMemberInfoService;
import org.uu.wallet.vo.ViewTransactionHistoryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
* @author
*/  @RequiredArgsConstructor
    @Service
    @Slf4j
    public class MemberAccountChangeServiceImpl extends ServiceImpl<MemberAccountChangeMapper, MemberAccountChange> implements IMemberAccountChangeService {
    private final WalletMapStruct walletMapStruct;
    private final IMemberInfoService memberInfoService;

    @Override
    @SneakyThrows
    public PageReturn<MemberAccountChangeDTO> listPage(MemberAccountChangeReq req) {
        Page<MemberAccountChange> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<MemberAccountChange> lambdaQuery = lambdaQuery();
        // 新增统计金额字段总计字段
        LambdaQueryWrapper<MemberAccountChange> queryWrapper = new QueryWrapper<MemberAccountChange>()
                .select("IFNULL(sum(before_change), 0) as beforeChangeTotal,IFNULL(sum(after_change), 0) as afterChangeTotal," +
                        "IFNULL(sum(amount_change), 0) as amountChangeTotal").lambda();

        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getOrderNo())) {
            lambdaQuery.eq(MemberAccountChange::getOrderNo, req.getOrderNo());
            queryWrapper.eq(MemberAccountChange::getOrderNo, req.getOrderNo());
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getChangeType())) {
            lambdaQuery.eq(MemberAccountChange::getChangeType, req.getChangeType());
            queryWrapper.eq(MemberAccountChange::getChangeType, req.getChangeType());
        }

        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getId())) {
            lambdaQuery.eq(MemberAccountChange::getMid, req.getId());
            queryWrapper.eq(MemberAccountChange::getMid, req.getId());
        }

        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getMerchantOrder())) {
            lambdaQuery.eq(MemberAccountChange::getMerchantOrder, req.getMerchantOrder());
            queryWrapper.eq(MemberAccountChange::getMerchantOrder, req.getMerchantOrder());
        }


        if (!ObjectUtils.isEmpty(req.getAmountChangeStart())) {
            lambdaQuery.ge(MemberAccountChange::getAmountChange, req.getAmountChangeStart());
            queryWrapper.ge(MemberAccountChange::getAmountChange, req.getAmountChangeStart());
        }
        if (!ObjectUtils.isEmpty(req.getAmountChangeEnd())) {
            lambdaQuery.le(MemberAccountChange::getAmountChange, req.getAmountChangeEnd());
            queryWrapper.le(MemberAccountChange::getAmountChange, req.getAmountChangeEnd());
        }

        if (!ObjectUtils.isEmpty(req.getCreateTimeStart())) {
            lambdaQuery.ge(MemberAccountChange::getCreateTime, req.getCreateTimeStart());
            queryWrapper.ge(MemberAccountChange::getCreateTime, req.getCreateTimeStart());
        }
        if (!ObjectUtils.isEmpty(req.getCreateTimeEnd())) {
            lambdaQuery.le(MemberAccountChange::getCreateTime, req.getCreateTimeEnd());
            queryWrapper.le(MemberAccountChange::getCreateTime, req.getCreateTimeEnd());
        }

        if (!ObjectUtils.isEmpty(req.getIsManual())) {
            lambdaQuery.and(e -> e.isNotNull(MemberAccountChange::getCreateBy).or().isNotNull(MemberAccountChange::getUpdateBy));
            queryWrapper.and(e -> e.isNotNull(MemberAccountChange::getCreateBy).or().isNotNull(MemberAccountChange::getUpdateBy));
            if(req.getIsManual() == 0){
                lambdaQuery.isNull(MemberAccountChange::getCreateBy).isNull(MemberAccountChange::getUpdateBy);
                queryWrapper.isNull(MemberAccountChange::getCreateBy).isNull(MemberAccountChange::getUpdateBy);
            }
        }

        if (!ObjectUtils.isEmpty(req.getOperator())) {
            lambdaQuery.and(e -> e.eq(MemberAccountChange::getCreateBy, req.getOperator()).or().eq(MemberAccountChange::getUpdateBy, req.getOperator()));
            queryWrapper.and(e -> e.eq(MemberAccountChange::getCreateBy, req.getOperator()).or().eq(MemberAccountChange::getUpdateBy, req.getOperator()));
        }


        // 根据会员id/商户会员id/会员账号筛选
        if (!ObjectUtils.isEmpty(req.getObscureId())) {
            lambdaQuery.and(wq -> wq.eq(MemberAccountChange::getMid, req.getObscureId())
                    .or()
                    .eq(MemberAccountChange::getMemberId, req.getObscureId()))
                    .or()
                    .eq(MemberAccountChange::getMemberAccount, req.getObscureId());
            queryWrapper.and(wq -> wq.eq(MemberAccountChange::getMid, req.getObscureId())
                    .or()
                    .eq(MemberAccountChange::getMemberId, req.getObscureId()))
                    .or()
                    .eq(MemberAccountChange::getMemberAccount, req.getObscureId());
        }

        // 根据平台订单号/商户订单号筛选
        if (!ObjectUtils.isEmpty(req.getObscureOrderNo())) {
            lambdaQuery.eq(MemberAccountChange::getOrderNo, req.getObscureOrderNo())
                    .or().eq(MemberAccountChange::getMerchantOrder, req.getObscureOrderNo());
            queryWrapper.eq(MemberAccountChange::getOrderNo, req.getObscureOrderNo())
                    .or().eq(MemberAccountChange::getMerchantOrder, req.getObscureOrderNo());
        }

        // 根据所属商户筛选
        if (!ObjectUtils.isEmpty(req.getMerchantName())) {
            lambdaQuery.eq(MemberAccountChange::getMerchantName, req.getMerchantName());
            queryWrapper.eq(MemberAccountChange::getMerchantName, req.getMerchantName());
        }
        // 支付类型
        if (req.getPayType() != null) {
            if(Objects.equals(req.getPayType(), PayTypeEnum.INDIAN_CARD.getCode())){
                lambdaQuery.and(w -> w.or().eq(MemberAccountChange::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(MemberAccountChange::getPayType, PayTypeEnum.INDIAN_CARD.getCode()));
                queryWrapper.and(w -> w.or().eq(MemberAccountChange::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(MemberAccountChange::getPayType, PayTypeEnum.INDIAN_CARD.getCode()));
            }else if(Objects.equals(req.getPayType(), PayTypeEnum.INDIAN_UPI.getCode())){
                // 支持upi INDIAN_UPI, INDIAN_CARD_UPI_FIX
                lambdaQuery.and(w -> w.or().eq(MemberAccountChange::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(MemberAccountChange::getPayType, PayTypeEnum.INDIAN_UPI.getCode()));
                queryWrapper.and(w -> w.or().eq(MemberAccountChange::getPayType, PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode())
                        .or().eq(MemberAccountChange::getPayType, PayTypeEnum.INDIAN_UPI.getCode()));
            }else{
                lambdaQuery.eq(MemberAccountChange::getPayType, req.getPayType());
                queryWrapper.eq(MemberAccountChange::getPayType, req.getPayType());
            }
        }
        // 加入排序
        OrderItem orderItem = new OrderItem();
        if(org.apache.commons.lang3.StringUtils.isBlank(req.getColumn())){
            lambdaQuery.orderByDesc(MemberAccountChange::getCreateTime);
        }else {
            orderItem.setColumn(StrUtil.toUnderlineCase(req.getColumn()));
            orderItem.setAsc(req.isAsc());
            page.addOrder(orderItem);
        }

        Page<MemberAccountChange> finalPage = page;
        CompletableFuture<MemberAccountChange> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<MemberAccountChange>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture.allOf(totalFuture, resultFuture);

        page = resultFuture.get();
        MemberAccountChange totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("afterChangeTotal", totalInfo.getAfterChangeTotal().toPlainString());
        extent.put("beforeChangeTotal", totalInfo.getBeforeChangeTotal().toPlainString());
        extent.put("amountChangeTotal", totalInfo.getAmountChangeTotal().toPlainString());

        BigDecimal afterChangePageTotal = BigDecimal.ZERO;
        BigDecimal beforeChangePageTotal = BigDecimal.ZERO;
        BigDecimal amountChangePageTotal = BigDecimal.ZERO;

        List<MemberAccountChange> records = page.getRecords();
        List<MemberAccountChangeDTO> list = new ArrayList<>();
        for (MemberAccountChange record : records) {
            MemberAccountChangeDTO dto = new MemberAccountChangeDTO();
            BeanUtils.copyProperties(record, dto);
            list.add(dto);
            BigDecimal afterChange = ObjectUtils.isEmpty(record.getAfterChange()) ? BigDecimal.ZERO : record.getAfterChange();
            afterChangePageTotal = afterChangePageTotal.add(afterChange);
            BigDecimal beforeChange = ObjectUtils.isEmpty(record.getBeforeChange()) ? BigDecimal.ZERO : record.getBeforeChange();
            beforeChangePageTotal = beforeChangePageTotal.add(beforeChange);
            BigDecimal amountChange = ObjectUtils.isEmpty(record.getAmountChange()) ? BigDecimal.ZERO : record.getAmountChange();
            amountChangePageTotal = amountChangePageTotal.add(amountChange);
        }
        extent.put("afterChangePageTotal", afterChangePageTotal.toPlainString());
        extent.put("beforeChangePageTotal", beforeChangePageTotal.toPlainString());
        extent.put("amountChangePageTotal", amountChangePageTotal.toPlainString());
        return PageUtils.flush(page, list, extent);
    }

    @Override
    public PageReturn<MemberAccountChangeExportDTO> listpageForExport(MemberAccountChangeReq req) {
        PageReturn<MemberAccountChangeDTO> result = listPage(req);
        List<MemberAccountChangeDTO> list = result.getList();
        List<MemberAccountChangeExportDTO> resultList = new ArrayList<>();
        for (MemberAccountChangeDTO memberAccountChangeDTO : list) {
            MemberAccountChangeExportDTO dto = new MemberAccountChangeExportDTO();
            BeanUtils.copyProperties(memberAccountChangeDTO, dto);
            String changeType = MemberAccountChangeEnum.getNameByCode(memberAccountChangeDTO.getChangeType());
            dto.setChangeType(changeType);
            resultList.add(dto);
        }
        Page<MemberAccountChangeExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(result.getTotal());
        return PageUtils.flush(page, resultList);
    }


    /**
     * 记录会员账变
     *
     * @param mid             会员ID
     * @param changeAmount    账变金额
     * @param changeType      交易类型
     * @param orderNo         订单号
     * @param previousBalance 账变前余额
     * @param newBalance      账变后余额
     * @param merchantOrder   商户订单号
     * @param payType         支付方式
     * @param remark          remark
     * @return {@link Boolean}
     */
    @Override
    public Boolean recordMemberTransaction(String mid, BigDecimal changeAmount, String changeType, String orderNo, BigDecimal previousBalance, BigDecimal newBalance, String merchantOrder, String payType, String remark) {

        MemberAccountChange memberAccountChange = new MemberAccountChange();

        //支付方式
        memberAccountChange.setPayType(payType);

        //设置平台订单号
        memberAccountChange.setOrderNo(orderNo);

        //设置商户订单号
        memberAccountChange.setMerchantOrder(merchantOrder);

        //设置会员ID
        memberAccountChange.setMid(mid);

        //设置交易类型
        memberAccountChange.setChangeType(changeType);

        //设置账变前金额
        memberAccountChange.setBeforeChange(previousBalance);

        //设置账变后金额
        memberAccountChange.setAfterChange(newBalance);

        //设置账变金额
        memberAccountChange.setAmountChange(changeAmount);

        //备注
        memberAccountChange.setRemark(remark);

        //获取商户会员id/商户名称/会员账号
        MemberInfo memberInfo = getMemberInfo(mid);

        // 设置商户会员id
//        memberAccountChange.setMemberId(mid);
        // 非纯钱包用户需要截取商户会员id
//        if(!memberInfo.getMemberType().equals(MemberTypeEnum.WALLET_MEMBER.getCode())
//                && org.apache.commons.lang3.StringUtils.isNotBlank(memberInfo.getMemberId())
//                && org.apache.commons.lang3.StringUtils.isNotBlank(memberInfo.getMerchantCode())){
//            String externalMemberId = memberInfo.getMemberId().substring(memberInfo.getMerchantCode().length());
//            memberAccountChange.setMemberId(externalMemberId);
//        }
        // 设置商户名称
//        if(!ObjectUtils.isEmpty(memberInfo.getMerchantName())){
//            memberAccountChange.setMerchantName(memberInfo.getMerchantName());
//        }

        // 设置会员账号
        if(!ObjectUtils.isEmpty(memberInfo.getMemberAccount())){
            memberAccountChange.setMemberAccount(memberInfo.getMemberAccount());
        }

        boolean save = save(memberAccountChange);

        log.info("记录会员账变, 会员id: {}, 账变金额: {}, 交易类型: {}, 订单号: {}, 账变前余额: {}, 账变后余额: {}, sql执行结果: {}",
                mid, changeAmount, MemberAccountChangeEnum.getNameByCode(changeType), orderNo, previousBalance, newBalance, save);

        return save;
    }

    /**
     * 交易记录 此方法已废除 改用MemberInfoServiceImpl的viewTransactionHistory()方法
     * 从买入表collection_order 卖出表payment_order usdt充值表中usdt_buy_order 查询出交易记录
     *
     * @param req
     * @return {@link RestResult}<{@link PageReturn}<{@link ViewTransactionHistoryVo}>>
     */
    @Override
    public RestResult<PageReturn<ViewTransactionHistoryVo>> viewTransactionHistory(ViewTransactionHistoryReq req) {

        /*if (req == null) {
            req = new ViewTransactionHistoryReq();
        }

        Page<MemberAccountChange> pageCollectionOrder = new Page<>();
        pageCollectionOrder.setCurrent(req.getPageNo());
        pageCollectionOrder.setSize(req.getPageSize());

        LambdaQueryChainWrapper<MemberAccountChange> lambdaQuery = lambdaQuery();
//        LambdaQueryChainWrapper<CollectionOrder> lambdaQuery1 = lambdaQuery();
//        LambdaQueryChainWrapper<PaymentOrder> lambdaQuery2 = lambdaQuery();
//        LambdaQueryChainWrapper<UsdtBuyOrder> lambdaQuery3 = lambdaQuery();

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null){
            log.error("查询交易记录失败: 获取会员信息失败: {}", memberInfo);
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //查询当前会员的交易记录 从买入表collection_order 卖出表payment_order usdt充值表中查询usdt_buy_order
        lambdaQuery.eq(MemberAccountChange::getMid, memberInfo.getId());

        //--动态查询 交易类型
        if (StringUtils.isNotEmpty(req.getTransactionType())) {
            lambdaQuery.eq(MemberAccountChange::getChangeType, req.getTransactionType());
        }else{
            //如果没有传交易类型  那么查询 1:买入 2:卖出 3:usdt充值
            lambdaQuery.in(MemberAccountChange::getChangeType, new String[]{"1", "2", "3"});
        }

        //--动态查询 时间 某天
        if (StringUtils.isNotEmpty(req.getDate())){
            LocalDate localDate = LocalDate.parse(req.getDate());
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = LocalDateTime.of(localDate, LocalTime.MAX);

            lambdaQuery.ge(MemberAccountChange::getCreateTime, startOfDay);
            lambdaQuery.le(MemberAccountChange::getCreateTime, endOfDay);
        }

        // 倒序排序
        lambdaQuery.orderByDesc(MemberAccountChange::getId);

        baseMapper.selectPage(pageCollectionOrder, lambdaQuery.getWrapper());

        List<MemberAccountChange> records = pageCollectionOrder.getRecords();

        ArrayList<ViewTransactionHistoryVo> viewTransactionHistoryVoList = new ArrayList<>();

        //IPage＜实体＞转 IPage＜Vo＞
        for (MemberAccountChange memberAccountChange : records) {

            ViewTransactionHistoryVo viewTransactionHistoryVo = new ViewTransactionHistoryVo();

            //交易类型
            viewTransactionHistoryVo.setTransactionType(memberAccountChange.getChangeType());

            //时间
            viewTransactionHistoryVo.setCreateTime(memberAccountChange.getCreateTime());

            //金额
            viewTransactionHistoryVo.setAmount(memberAccountChange.getAmountChange());

            //如果交易类型是13(金额错误退回 那么改为 10退回)
            if (MemberAccountChangeEnum.AMOUNT_ERROR.getCode().equals(viewTransactionHistoryVo.getTransactionType())){
                viewTransactionHistoryVo.setTransactionType(MemberAccountChangeEnum.CANCEL_RETURN.getCode());
            }

            viewTransactionHistoryVoList.add(viewTransactionHistoryVo);
        }

        PageReturn<ViewTransactionHistoryVo> flush = PageUtils.flush(pageCollectionOrder, viewTransactionHistoryVoList);

        log.info("查询交易记录成功: 会员账号: {}, req: {}, 返回数据: {}", memberInfo.getMemberAccount(), req, flush);

        return RestResult.ok(flush);*/
        return null;
    }

    /**
     * 获取会员信息
     * @param mid 会员id
     * @return {@link MemberInfo}
     */
    public MemberInfo getMemberInfo(String mid){
        return memberInfoService.getMemberInfoById(mid);
    }

    /**
     * 根据用户ID获取买入和卖出列表
     *
     * @param uidList   用户ID
     * @param startTime 开始时间 为null则查询所有
     * @param includeCommission 是否包含返佣
     * @param includeReward 是否包含奖励(买入奖励、卖出奖励、平台分红)
     * @param includeUsdtRecharge 是否包含USDT买入
     * @return {@link List<MemberAccountChangeBO>}
     */
    @Override
    public List<MemberAccountChangeBO> queryAccountChangeListByIds(
            List<Long> uidList,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Boolean includeCommission,
            Boolean includeReward,
            Boolean includeUsdtRecharge
    ) {
        List<String> buildChangeTypeList = new ArrayList<>(Arrays.asList(
                MemberAccountChangeEnum.RECHARGE.getCode(),
                MemberAccountChangeEnum.WITHDRAW.getCode()
        ));
        if (includeUsdtRecharge) {
            buildChangeTypeList.add(MemberAccountChangeEnum.USDT_RECHARGE.getCode());
        }

        if (includeCommission) {
            buildChangeTypeList.add(MemberAccountChangeEnum.BUY_COMMISSION.getCode());
            buildChangeTypeList.add(MemberAccountChangeEnum.SELL_COMMISSION.getCode());
        }
        if (includeReward) {
            buildChangeTypeList.add(MemberAccountChangeEnum.BUY_BONUS.getCode());
            buildChangeTypeList.add(MemberAccountChangeEnum.SELL_BONUS.getCode());
            buildChangeTypeList.add(MemberAccountChangeEnum.PLATFORM_DIVIDENDS.getCode());
        }

        LambdaQueryChainWrapper<MemberAccountChange> wrapper = this.lambdaQuery();
        wrapper.in(! CollectionUtils.isEmpty(uidList), MemberAccountChange::getMid, uidList)
                .in(MemberAccountChange::getChangeType, buildChangeTypeList);
        if (Objects.nonNull(startTime)) {
            wrapper.ge(MemberAccountChange::getCreateTime, startTime);
        }

        if (Objects.nonNull(endTime)) {
            wrapper.le(MemberAccountChange::getCreateTime, endTime);
        }

        List<MemberAccountChange> changeList = wrapper.list();
        return changeList.stream()
                .filter(Objects::nonNull)
                .map(item -> MemberAccountChangeBO.builder()
                        .memberId(Long.valueOf(item.getMid()))
                        .changeType(item.getChangeType())
                        .amountChange(item.getAmountChange())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void updateCommissionFlagByOrderNo(Long memberId, Integer changeType, String orderNo, Integer commissionFlag) {
        this.baseMapper.updateCommissionFlagByOrderNo(memberId, changeType, orderNo, commissionFlag);
    }

    @Override
    public RestResult<MemberAccountChangeDetailResponseVO> selectByOrderNo(String orderNo) {
        MemberAccountChange accountChange = this.lambdaQuery()
                .eq(MemberAccountChange::getOrderNo, orderNo)
                .one();
        if (Objects.isNull(accountChange)) {
            return RestResult.failed("Order does not exist");
        }
        return RestResult.ok(
                MemberAccountChangeDetailResponseVO.builder()
                        .amount(accountChange.getAmountChange())
                        .changeType(accountChange.getChangeType())
                        .orderNo(orderNo)
                        .remark(accountChange.getRemark())
                        .completionTime(accountChange.getCreateTime())
                        .build()
        );
    }

    @Override
    public RestResult<PageReturn<MemberAccountChangeResponseVO>> commissionRewardPage(PageRequestHome requestVO) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (Objects.isNull(currentUserId)) {
            return RestResult.failed(ResultCode.RELOGIN);
        }

        Page<MemberAccountChange> changePage = new Page<>();
        changePage.setCurrent(requestVO.getPageNo())
                .setSize(requestVO.getPageSize());
        Page<MemberAccountChange> accountChangePage = this.lambdaQuery()
                .eq(MemberAccountChange::getMid, currentUserId)
                .in(MemberAccountChange::getChangeType, Arrays.asList(
                        MemberAccountChangeEnum.BUY_COMMISSION.getCode(),
                        MemberAccountChangeEnum.BUY_BONUS.getCode())
                )
                .orderByDesc(MemberAccountChange::getCreateTime)
                .page(changePage);
        List<MemberAccountChange> accountChangeList = accountChangePage.getRecords();
        List<MemberAccountChangeResponseVO> responseVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(accountChangeList)) {
            responseVOList = accountChangeList.stream()
                    .filter(Objects::nonNull)
                    .map(item -> MemberAccountChangeResponseVO.builder()
                            .changeMode(item.getChangeType())
                            .amountChange(item.getAmountChange())
                            .orderNo(item.getOrderNo())
                            .createTime(item.getCreateTime())
                            .build())
                    .collect(Collectors.toList());
        }
        return RestResult.ok(PageUtils.flush(changePage, responseVOList));
    }
}
