package org.uu.wallet.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.uu.common.core.constant.SecurityConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.KycPartnersMapper;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.mapper.MerchantCollectOrdersMapper;
import org.uu.wallet.mapper.MerchantPaymentOrdersMapper;
import org.uu.wallet.service.*;
import org.uu.wallet.util.DelegationOrderUtil;
import org.uu.wallet.vo.MemberInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/memberInfo", "/memberInfo"})
@Api(description = "会员控制器")
//@ApiIgnore
public class MemberInfoController {

    private final IMemberInfoService memberInfoService;
    private final ICollectionOrderService collectionOrderService;

    private final IMatchingOrderService matchingOrderService;

    private final IPaymentOrderService paymentOrderService;

    private final IMemberLoginLogsService memberLoginLogsService;
    private final DelegationOrderUtil delegationOrderUtil;
    private final MemberInfoMapper memberInfoMapper;
    private final RedisTemplate redisTemplate;
    private final IMemberBlackService memberBlackService;

    @Resource
    DelegationOrderUtil delegatorOrderUtil;
    @Autowired
    private MerchantCollectOrdersMapper merchantCollectOrdersMapper;
    @Autowired
    private MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;


    @PostMapping("/createMemberInfo")
    @ApiOperation(value = "创建会员")
    public RestResult<MemberInfolistPageDTO> save(@RequestBody @ApiParam MemberInfoReq memberInfoReq, HttpServletRequest request) {

        //后台创建会员
        return memberInfoService.createMemberInfo(memberInfoReq, request);
    }

    @PostMapping(value = "/findGroupInfo")
    @ApiOperation(value = "查询会员团队信息")
    public RestResult<PageReturn<GroupInfoDTO>> findGroupInfo(@RequestBody @Validated FindGroupInfoReq requestVO) {
        return this.memberInfoService.findGroupInfo(requestVO);
    }


    @PostMapping("/update")
    @ApiOperation(value = "更新会员信息")
    public RestResult update(@RequestBody @ApiParam @Validated MemberInfoRealNameReq memberInfoReqVo) {
        if(StringUtils.isBlank(memberInfoReqVo.getMemberId())){
            RestResult.failed();
        }
        LambdaQueryWrapper<MemberInfo> lambdaQuery = new LambdaQueryWrapper<>();
        lambdaQuery.eq(MemberInfo::getIdCardNumber, memberInfoReqVo.getIdCardNumber());
        lambdaQuery.and(wq -> wq.eq(MemberInfo::getAuthenticationStatus, MemberAuthenticationStatusEnum.AUTHENTICATED.getCode())
                .or()
                .eq(MemberInfo::getAuthenticationStatus, MemberAuthenticationStatusEnum.MANUAL_AUTHENTICATION.getCode()));
        List<MemberInfo> list = memberInfoMapper.selectList(lambdaQuery);
        if(list.size() > 0){
            return RestResult.failed("证件号重复");
        }
        String updateBy = UserContext.getCurrentUserName();
        MemberInfo memberInfo = new MemberInfo();
        BeanUtils.copyProperties(memberInfoReqVo, memberInfo);
        memberInfo.setId(Long.parseLong(memberInfoReqVo.getMemberId()));
        memberInfo.setVerificationBy(updateBy);
        memberInfo.setUpdateBy(updateBy);
        memberInfo.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
        if(memberInfoReqVo.getAuthenticationStatus().equals(MemberAuthenticationStatusEnum.AUTHENTICATED.getCode())){
            memberInfo.setAuthenticationStatus(MemberAuthenticationStatusEnum.MANUAL_AUTHENTICATION.getCode());
        }

        memberInfoMapper.updateByMemberId(memberInfo);
        return RestResult.ok();
    }

//    @PostMapping("/listpage")
//    @ApiOperation(value = "获取会员列表")
//    public RestResult list(@RequestBody @ApiParam MemberInfoQueryWrapperVo memberInfoQueryWrapperVo) {
////        PageReturn<MemberInfo> payConfigPage = memberInfoService.listPage(memberInfoQueryWrapperVo);
////        return RestResult.page(payConfigPage);
//        return null;
//    }

    @GetMapping("/current")
    @ApiOperation(value = "获取当前会员信息")
    public RestResult<MemberInfoVo> currentMemberInfo() {
        MemberInfoVo memberInfoVo = memberInfoService.currentMemberInfo();
        return RestResult.ok(memberInfoVo);
    }

//    @GetMapping("/merchantNameList")
//    @ApiOperation(value = "获取商户名称列表")
//    public RestResult merchantNameList() {
////        List<MerchantNameListVo> payConfigPage = merchantInfoService.getMerchantNameList();
////        return RestResult.ok(payConfigPage);
//        return null;
//    }


    /**
     * 获取会员用户信息
     */
    @GetMapping("/username/{username}")
    @ApiOperation(value = "获取会员用户信息")
    public RestResult<MemberAuthDTO> getMemberUserByUsername(@PathVariable String username) {
        MemberAuthDTO memberAuthDTO = memberInfoService.getByUsername(username);
        return RestResult.ok(memberAuthDTO);
    }

    /**
     * 获取会员用户信息
     */
    @GetMapping("/appusername/{username}")
    @ApiOperation(value = "获取会员用户信息")
    public RestResult<MemberAuthDTO> getAppMemberUserByUsername(@PathVariable String username) {
        MemberAuthDTO memberAuthDTO = memberInfoService.getByUsername(username);
        return RestResult.ok(memberAuthDTO);
    }


    @PostMapping("/listpage")
    @ApiOperation(value = "获取会员列表")
    public RestResult<List<MemberInfolistPageDTO>> listpage(@RequestBody @ApiParam MemberInfoListPageReq req) {
        PageReturn<MemberInfolistPageDTO> payConfigPage = memberInfoService.listPage(req);
        return RestResult.page(payConfigPage);

    }

    @PostMapping("/relationMemberList")
    @ApiOperation(value = "获取关联会员信息列表")
    public RestResult<List<MemberInfolistPageDTO>> relationMemberList(@RequestBody @ApiParam MemberInfoListPageReq req) {
        PageReturn<MemberInfolistPageDTO> payConfigPage = memberInfoService.relationMemberList(req);
        return RestResult.page(payConfigPage);

    }


    @PostMapping("/merchantListPage")
    @ApiOperation(value = "获取商户会员列表")
    public RestResult<List<MerchantMemberInfoPageDTO>> merchantListPage(@RequestBody @ApiParam MemberInfoListPageReq req) {
        PageReturn<MerchantMemberInfoPageDTO> payConfigPage = memberInfoService.merchantListPage(req);
        return RestResult.page(payConfigPage);

    }

    @PostMapping("/realName")
    @ApiOperation(value = "获取会员实名列表")
    public RestResult<List<MemberInfolistPageDTO>> realName(@RequestBody @ApiParam MemberInfoRealNameListReq req) {
        PageReturn<MemberRealNamelistPageDTO> payConfigPage = memberInfoService.realName(req);
        return RestResult.page(payConfigPage);

    }

    @PostMapping("/recharge")
    @ApiOperation(value = "人工上分")
    public RestResult<MemberInfolistPageDTO> recharge(@RequestBody @ApiParam MemberInfoRechargeReq req) {
        MemberInfolistPageDTO memberInfolistPageDTO = memberInfoService.recharge(req);
        return RestResult.ok(memberInfolistPageDTO);

    }

    @PostMapping("/withdrawal")
    @ApiOperation(value = "人工下分")
    public RestResult<MemberInfolistPageDTO> withdrawal(@RequestBody @ApiParam MemberInfoWithdrawalReq req) {
        MemberInfo memberInfo = memberInfoMapper.getMemberInfoById(req.getId().toString());
        if(memberInfo.getBalance().compareTo(req.getSellAmount())== -1){
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo,memberInfolistPageDTO);
            return RestResult.result("999999","下分失败可用余额不足",memberInfolistPageDTO);
        }
        MemberInfolistPageDTO memberInfolistPageDTO = memberInfoService.withdrawal(req);
        return RestResult.ok(memberInfolistPageDTO);

    }


    @PostMapping("/freeze")
    @ApiOperation(value = "冻结")
    public RestResult<MemberInfolistPageDTO> freeze(@RequestBody @ApiParam MemberInfoFreezeReq req) {
        MemberInfo memberInfo = memberInfoMapper.getMemberInfoById(req.getId().toString());
        if(memberInfo.getBalance().compareTo(req.getFrozenAmount())== -1){
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo,memberInfolistPageDTO);
            return RestResult.result("999999","冻结失败可用余额不足",memberInfolistPageDTO);
        }
        try{
            MemberInfolistPageDTO memberInfolistPageDTO = memberInfoService.freeze(req);
            return RestResult.ok(memberInfolistPageDTO);
        }catch (Exception e){
            return RestResult.failed();
        }
    }

    @PostMapping("/unfreeze")
    @ApiOperation(value = "解冻")
    public RestResult<MemberInfolistPageDTO> unfreeze(@RequestBody @ApiParam MemberInfoFreezeReq req) {
        MemberInfo memberInfo = memberInfoMapper.getMemberInfoById(req.getId().toString());
        if(memberInfo.getBiFrozenAmount().compareTo(req.getFrozenAmount())== -1){
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo,memberInfolistPageDTO);
            return RestResult.result("999999","解冻失败冻结额度不足",memberInfolistPageDTO);
        }
        try{
            MemberInfolistPageDTO memberInfolistPageDTO = memberInfoService.unfreeze(req);
            return RestResult.ok(memberInfolistPageDTO);
        }catch (Exception e){
            return RestResult.failed();
        }
    }

    @PostMapping("/bonus")
    @ApiOperation(value = "奖励")
    public RestResult<MemberInfolistPageDTO> bonus(@RequestBody @ApiParam MemberInfoBonusReq req) {
        MemberInfolistPageDTO memberInfolistPageDTO = memberInfoService.bonus(req);
        return RestResult.ok(memberInfolistPageDTO);

    }

    @PostMapping("/resetPayPwd")
    @ApiOperation(value = "重置支付密码")
    public RestResult<MemberInfolistPageDTO> resetPayPwd(@RequestBody @ApiParam MemberInfoIdReq req) {
        MemberInfolistPageDTO memberInfolistPageDTO = memberInfoService.resetPayPwd(req);
        return RestResult.ok(memberInfolistPageDTO);

    }

    @PostMapping("/resetpwd")
    @ApiOperation(value = "重置密码")
    public RestResult<MemberInfolistPageDTO> resetpwd(@RequestBody @ApiParam MemberInfoIdReq req) {
        MemberInfolistPageDTO memberInfolistPageDTO = memberInfoService.resetpwd(req);
        return RestResult.ok(memberInfolistPageDTO);

    }

    @PostMapping("/updatePassword")
    @ApiOperation(value = "修改密码")
    public RestResult<Void> updatePassword(@RequestBody @Validated UpdateLoginPasswordReq requestVO) {
        return this.memberInfoService.updatePassword(requestVO);
    }


    @PostMapping("/remark")
    @ApiOperation(value = "备注")
    public RestResult<MemberInfolistPageDTO> remark(@RequestBody @ApiParam MemberInfoIdReq req) {
        MemberInfolistPageDTO memberInfolistPageDTO = memberInfoService.remark(req);
        return RestResult.ok(memberInfolistPageDTO);

    }


    @PostMapping("/getInfo")
    @ApiOperation(value = "会员信息")
    public RestResult<MemberInfoDTO> getInfo(@RequestBody @ApiParam MemberInfoIdGetInfoReq req) {
        MemberInfoDTO memberInfoDTO = memberInfoService.getInfo(req);
        if(ObjectUtils.isEmpty(memberInfoDTO)){
            return RestResult.failed("Member does not exist");
        }else {
            return RestResult.ok(memberInfoDTO);
        }
    }

    @PostMapping("/disableBatch")
    @ApiOperation(value = "批量禁用")
    public RestResult<List<MemberInfolistPageDTO>> disableBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        List<MemberInfo> list = memberInfoService.lambdaQuery().in(MemberInfo::getId, req.getIds()).list();
        //list.stream().forEach(m -> m.setStatus("0"));
        //memberInfoService.updateBatchById(list);
        List<MemberInfolistPageDTO> listDto = new ArrayList<MemberInfolistPageDTO>();
        for (MemberInfo memberInfo : list) {
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
            listDto.add(memberInfolistPageDTO);

            String jti = (String) redisTemplate.opsForValue().get(SecurityConstants.LOGIN_USER_ID + memberInfo.getMemberAccount());
            if(StringUtils.isNotBlank(jti)){
                log.info("强制踢下线玩家->{}", memberInfo.getId());
                redisTemplate.delete(SecurityConstants.BLACKLIST_TOKEN_PREFIX + jti);
            }

            // 加入黑名单
            MemberBlack memberBlack = BeanUtil.toBean(memberInfo, MemberBlack.class);
            memberBlack.setOperator(UserContext.getCurrentUserName());
            memberBlack.setOpTime(LocalDateTime.now());
            memberBlack.setMemberId(memberInfo.getId().toString());
            memberBlack.setSellStatus(Integer.parseInt(SellStatusEnum.DISABLE.getCode()));
            memberBlack.setStatus(Integer.parseInt(MemberStatusEnum.DISABLE.getCode()));
            memberBlack.setBuyStatus(Integer.parseInt(BuyStatusEnum.DISABLE.getCode()));
            if(StringUtils.isNotBlank(memberInfo.getMemberId()) && StringUtils.isNotBlank(memberInfo.getMerchantCode()) &&
                    memberInfo.getMemberId().contains(memberInfo.getMerchantCode())){
                String externalMemberId = memberInfo.getMemberId().substring(memberInfo.getMerchantCode().length());
                memberBlack.setMerchantMemberId(externalMemberId);
            }
            memberBlack.setRemark("【会员管理禁用】" + UserContext.getCurrentUserName());
            memberInfoService.disableMember(memberInfo.getId().toString(), UserContext.getCurrentUserName(), "【会员管理禁用】" + UserContext.getCurrentUserName());
            // 关闭委托
            delegatorOrderUtil.disableMemberAndCloseOrders(memberInfo);
        }

        //批量禁用，关闭委托
        list.parallelStream().forEach(delegationOrderUtil::disableMemberAndCloseOrders);

        return RestResult.ok(listDto);

    }


    @PostMapping("/enableBatch")
    @ApiOperation(value = "批量启用")
    public RestResult<List<MemberInfolistPageDTO>> enableBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        List<MemberInfo> list = memberInfoService.lambdaQuery().in(MemberInfo::getId, req.getIds()).list();
        list.stream().forEach(m -> m.setStatus("1"));
        memberInfoService.updateBatchById(list);
        List<MemberInfolistPageDTO> listDto = new ArrayList<MemberInfolistPageDTO>();
        for (MemberInfo memberInfo : list) {
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
            listDto.add(memberInfolistPageDTO);
            MemberBlackReq memberBlack = new MemberBlackReq();
            memberBlack.setMemberId(memberInfo.getId().toString());
            memberBlackService.removeBlack(memberBlack);
        }
        return RestResult.ok(listDto);

    }


    @PostMapping("/disableBuyBatch")
    @ApiOperation(value = "批量禁用买入")
    public RestResult<List<MemberInfolistPageDTO>> disableBuyBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        List<MemberInfo> list = memberInfoService.lambdaQuery().in(MemberInfo::getId, req.getIds()).list();
        list.stream().forEach(m -> m.setBuyStatus("0"));
        memberInfoService.updateBatchById(list);
        List<MemberInfolistPageDTO> listDto = new ArrayList<MemberInfolistPageDTO>();
        for (MemberInfo memberInfo : list) {
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
            listDto.add(memberInfolistPageDTO);
        }
        return RestResult.ok(listDto);

    }

    @PostMapping("/enableBuyBatch")
    @ApiOperation(value = "批量启用买入")
    public RestResult<List<MemberInfolistPageDTO>> enableBuyBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        List<MemberInfo> list = memberInfoService.lambdaQuery().in(MemberInfo::getId, req.getIds()).list();
        list.stream().forEach(m -> m.setBuyStatus("1"));
        memberInfoService.updateBatchById(list);
        List<MemberInfolistPageDTO> listDto = new ArrayList<MemberInfolistPageDTO>();
        for (MemberInfo memberInfo : list) {
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
            listDto.add(memberInfolistPageDTO);
        }
        return RestResult.ok(listDto);

    }


    @PostMapping("/disableSellBatch")
    @ApiOperation(value = "批量禁用卖出")
    public RestResult<List<MemberInfolistPageDTO>> disableSellBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        List<MemberInfo> list = memberInfoService.lambdaQuery().in(MemberInfo::getId, req.getIds()).list();
        list.stream().forEach(m -> m.setSellStatus("0"));
        memberInfoService.updateBatchById(list);
        List<MemberInfolistPageDTO> listDto = new ArrayList<MemberInfolistPageDTO>();
        for (MemberInfo memberInfo : list) {
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
            listDto.add(memberInfolistPageDTO);
            // 关闭委托
            delegatorOrderUtil.disableMemberAndCloseOrders(memberInfo);
        }
        return RestResult.ok(listDto);

    }

    @PostMapping("/enableSellBatch")
    @ApiOperation(value = "批量启用卖出")
    public RestResult<List<MemberInfolistPageDTO>> enableSellBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        List<MemberInfo> list = memberInfoService.lambdaQuery().in(MemberInfo::getId, req.getIds()).list();
        list.stream().forEach(m -> m.setSellStatus("1"));
        memberInfoService.updateBatchById(list);
        List<MemberInfolistPageDTO> listDto = new ArrayList<MemberInfolistPageDTO>();
        for (MemberInfo memberInfo : list) {
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
            listDto.add(memberInfolistPageDTO);
        }
        return RestResult.ok(listDto);

    }


    @PostMapping("/buyStatusChange")
    @ApiOperation(value = "买入状态")
    public RestResult<MemberInfolistPageDTO> buyStatusChange(@RequestBody @ApiParam MemberInfoBuyStatusReq req) {
        MemberInfo memberInfo = new MemberInfo();
        BeanUtils.copyProperties(req, memberInfo);
        memberInfo = memberInfoService.getById(memberInfo);
        memberInfo.setBuyStatus(req.getBuyStatus());
        memberInfoService.updateById(memberInfo);
        MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
        BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
        return RestResult.ok(memberInfolistPageDTO);
    }

    @PostMapping("/sellStatusChange")
    @ApiOperation(value = "卖出状态修改")
    public RestResult<MemberInfolistPageDTO> sellStatusChange(@RequestBody @ApiParam MemberInfoSellStatusReq req) {
        MemberInfo memberInfo = new MemberInfo();
        BeanUtils.copyProperties(req, memberInfo);
        memberInfo = memberInfoService.getById(memberInfo);
        memberInfo.setSellStatus(req.getSellStatus());
        memberInfoService.updateById(memberInfo);
        MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
        BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
        return RestResult.ok(memberInfolistPageDTO);

    }


    @PostMapping("/lastBuyRecord")
    @ApiOperation(value = "最后买入记录")
    public RestResult<List<MemberInfoOrderDTO>> lastBuyRecord(@RequestBody @ApiParam MemberInfoIdReq req) {
        List<CollectionOrder> list = collectionOrderService.lambdaQuery().eq(CollectionOrder::getMemberId, req.getId()).orderByDesc(CollectionOrder::getId).last(" limit 3").list();
       // if (list.size() >= 3) list = list.subList(0, 2);
        List<MemberInfoOrderDTO> listDto = new ArrayList<>();
        List<String> platformOrderList = new ArrayList<>();
        for (CollectionOrder collectionOrder : list) {
            MemberInfoOrderDTO memberInfoOrderDTO = new MemberInfoOrderDTO();
            BeanUtils.copyProperties(collectionOrder, memberInfoOrderDTO);
            memberInfoOrderDTO.setStatus(OrderStatusEnum.getNameByCode(collectionOrder.getOrderStatus()));
            platformOrderList.add(collectionOrder.getMerchantOrder());
            listDto.add(memberInfoOrderDTO);
        }
        // 查询匹配表获取对应匹配的订单会员信息
        if(!platformOrderList.isEmpty()){
            LambdaQueryWrapper<MerchantPaymentOrders> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(MerchantPaymentOrders::getPlatformOrder, platformOrderList);
            List<MerchantPaymentOrders> merchantPaymentOrders = merchantPaymentOrdersMapper.selectList(wrapper);
            Map<String, String> matchMemberId = merchantPaymentOrders.stream().collect(Collectors.toMap(MerchantPaymentOrders::getPlatformOrder, MerchantPaymentOrders::getExternalMemberId));
            for (MemberInfoOrderDTO memberInfoOrderDTO : listDto) {
                memberInfoOrderDTO.setMatchMemberId(matchMemberId.get(memberInfoOrderDTO.getMerchantOrder()));
            }
        }
        return RestResult.ok(listDto);

    }


    @PostMapping("/lastSellRecord")
    @ApiOperation(value = "最后卖出记录")
    public RestResult<List<MemberInfoOrderDTO>> lastSellRecord(@RequestBody @ApiParam MemberInfoIdReq req) {
//
        List<PaymentOrder> list = paymentOrderService.lambdaQuery().eq(PaymentOrder::getMemberId, req.getId()).orderByDesc(PaymentOrder::getId).last( " limit 3").list();
//        if (list.size() >= 3) list =
        List<String> platformOrderList = new ArrayList<>();
        List<MemberInfoOrderDTO> listDto = new ArrayList<MemberInfoOrderDTO>();
        for (PaymentOrder paymentOrder : list) {
            MemberInfoOrderDTO memberInfoOrderDTO = new MemberInfoOrderDTO();
            BeanUtils.copyProperties(paymentOrder, memberInfoOrderDTO);
            platformOrderList.add(paymentOrder.getMerchantOrder());
            memberInfoOrderDTO.setStatus(OrderStatusEnum.getNameByCode(paymentOrder.getOrderStatus()));
            listDto.add(memberInfoOrderDTO);
        }
        // 查询匹配表获取对应匹配的订单会员信息
        if(!platformOrderList.isEmpty()){
            LambdaQueryWrapper<MerchantCollectOrders> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(MerchantCollectOrders::getPlatformOrder, platformOrderList);
            List<MerchantCollectOrders> merchantCollectOrders = merchantCollectOrdersMapper.selectList(wrapper);
            Map<String, String> matchMemberId = merchantCollectOrders.stream().collect(Collectors.toMap(MerchantCollectOrders::getPlatformOrder, MerchantCollectOrders::getExternalMemberId));
            for (MemberInfoOrderDTO memberInfoOrderDTO : listDto) {
                memberInfoOrderDTO.setMatchMemberId(matchMemberId.get(memberInfoOrderDTO.getMerchantOrder()));
            }
        }
        return RestResult.ok(listDto);

    }


    @PostMapping("/lastLogin")
    @ApiOperation(value = "最后登录记录")
    public RestResult<List<MemberInfoLoginDTO>> lastLogin(@RequestBody @ApiParam MemberInfoIdReq req) {


        List<MemberLoginLogs> list = memberLoginLogsService.lambdaQuery().eq(MemberLoginLogs::getMemberId,req.getId()).orderByDesc(MemberLoginLogs::getId).last(" limit 3").list();

        List<MemberInfoLoginDTO> rlist= new ArrayList<MemberInfoLoginDTO>();
        for(MemberLoginLogs memberLoginLogs : list){
            MemberInfoLoginDTO memberInfoLoginDTO = new MemberInfoLoginDTO();
            memberInfoLoginDTO.setCreateTime(memberLoginLogs.getLoginTime());
            memberInfoLoginDTO.setIp(memberLoginLogs.getIpAddress());
            memberInfoLoginDTO.setCount(1);
            rlist.add(memberInfoLoginDTO);
        }
        return RestResult.ok(rlist);

    }

    @PostMapping("/updateCreditScore")
    @ApiOperation(value = "更新信用分")
    public  RestResult<MemberInfolistPageDTO> updateCreditScore(@RequestBody @ApiParam MemberInfoCreditScoreReq req) {
        return memberInfoService.updateCreditScore(req);
    }

    @PostMapping("/getCreditScoreInfo")
    @ApiOperation(value = "获取信用分信息")
    public  RestResult<MemberCreditScoreInfoDTO> getCreditScoreInfo(@RequestBody @ApiParam MemberCreditScoreInfoIdReq req) {
        return memberInfoService.getCreditScoreInfo(req);
    }

    @PostMapping("/cashBackBatch")
    @ApiOperation(value = "批量退回余额")
    public RestResult<List<CashBackOrderProcessDTO>> cashBackBatch(@RequestBody @ApiParam MemberInfoBatchIdReq req) {
        return memberInfoService.cashBackBatch(req);
    }


}
