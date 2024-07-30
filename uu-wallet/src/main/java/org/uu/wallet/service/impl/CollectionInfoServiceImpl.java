package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.CollectionInfoDTO;
import org.uu.common.pay.req.CollectionInfoIdReq;
import org.uu.common.pay.req.CollectionInfoListPageReq;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.CollectionInfoStatusEnum;
import org.uu.wallet.Enum.PayTypeEnum;
import org.uu.wallet.dto.SyncedBankInfoListDTO;
import org.uu.wallet.entity.CollectionInfo;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.TradeConfig;
import org.uu.wallet.mapper.CollectionInfoMapper;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.req.*;
import org.uu.wallet.service.*;
import org.uu.wallet.vo.*;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionInfoServiceImpl extends ServiceImpl<CollectionInfoMapper, CollectionInfo> implements ICollectionInfoService {

    private final CollectionInfoMapper collectionInfoMapper;

    private final RedissonUtil redissonUtil;

    @Autowired
    private IMemberInfoService memberInfoService;

    private final RedisTemplate redisTemplate;

    @Autowired
    private MemberInfoMapper memberInfoMapper;

    @Autowired
    private ITradeConfigService tradeConfigService;

    /**
     * 开启收款时校验
     *
     * @param collectionInfo
     * @return {@link RestResult}
     */
    @Override
    public RestResult enableCollectionVerification(CollectionInfo collectionInfo, MemberInfo memberInfo) {

        String memberId = String.valueOf(memberInfo.getId());

        //先查询该收款信息是否属于该会员
        if (collectionInfo == null || !collectionInfo.getMemberId().equals(memberId)) {
            log.error("开启收款时校验失败 该收款信息不存在或收款信息不属于该会员 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);
            return RestResult.failure(ResultCode.ILLEGAL_OPERATION_COLLECTION_INFO_CHECK_FAILED);
        }

        //校验该收款信息是否已被删除
        if (collectionInfo.getDeleted() == 1) {
            log.error("开启收款时校验失败 该收款信息已被删除 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);
            return RestResult.failure(ResultCode.ILLEGAL_OPERATION_COLLECTION_INFO_CHECK_FAILED);
        }

        //校验今日收款笔数是否达到限制
        if (collectionInfo.getDailyLimitCount() != null && collectionInfo.getTodayCollectedCount() >= collectionInfo.getDailyLimitCount()) {
            log.error("开启收款时校验失败 该收款信息今日收款笔数已达到限制 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);
            return RestResult.failure(ResultCode.COLLECTION_DAILY_LIMIT_REACHED);
        }

        //判断该收款信息今日额度是否已满
        if (collectionInfo.getDailyLimitAmount() != null && collectionInfo.getTodayCollectedAmount().compareTo(collectionInfo.getDailyLimitAmount()) >= 0) {
            log.error("开启收款时校验失败 该收款信息今日额度已满 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);
            return RestResult.failure(ResultCode.COLLECTION_DAILY_AMOUNT_LIMIT_REACHED);
        }

        return null;
    }

    /**
     * 停止收款时校验
     *
     * @param collectionInfo
     * @return {@link RestResult}
     */
//    @Override
//    public RestResult stopCollectionVerification(CollectionInfo collectionInfo, MemberInfo memberInfo) {
//
//        String memberId = String.valueOf(memberInfo.getId());
//
//        //校验该收款信息是否属于该会员
//        if (collectionInfo == null || !collectionInfo.getMemberId().equals(memberId)) {
//            log.error("停止收款时校验失败: 该收款信息不存在或收款信息不属于该会员, 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);
//            return RestResult.failure(ResultCode.ILLEGAL_OPERATION_COLLECTION_INFO_CHECK_FAILED);
//        }
//
//        //查询该收款信息 匹配池中正在匹配的订单数量
//        Integer matchingOrdersMatchPoll = matchPoolService.getMatchingOrdersBycollectionId(collectionInfo.getId());
//        //查询该收款信息 卖出订单表中正在匹配的订单数量
//        Integer matchingOrdersPayment = paymentOrderService.getMatchingOrdersBycollectionId(collectionInfo.getId());
//
//        //查看该收款信息 是否有存在 匹配中的订单 (查询匹配池 卖出订单)
//        if ((matchingOrdersMatchPoll != null && matchingOrdersMatchPoll > 0) || (matchingOrdersPayment != null && matchingOrdersPayment > 0)) {
//
//            log.error("停止收款时校验失败: 当前有存在匹配中的订单, 匹配池中正在匹配的订单数量: {}, 卖出订单表中正在匹配的订单数量: {}, 会员信息: {}, 收款信息: {}", matchingOrdersMatchPoll, matchingOrdersPayment, memberInfo, collectionInfo);
//            return RestResult.failure(ResultCode.MATCHING_ORDER_IN_PROGRESS);
//        }
//
//        return null;
//    }

    /**
     * @param req
     * @return {@link RestResult}<{@link PageReturn}<{@link CollectionInfoVo}>>
     *//*
     * 获取当前用户UPI收款信息
     * */
    @Override
    public RestResult<PageReturn<CollectionInfoVo>> currentCollectionInfo(PageRequestHome req) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取当前用户UPI收款信息失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        String memberId = String.valueOf(memberInfo.getId());

        if (req == null) {
            req = new PageRequestHome();
        }

        Page<CollectionInfo> pageCollectionInfo = new Page<>();
        pageCollectionInfo.setCurrent(req.getPageNo());
        pageCollectionInfo.setSize(req.getPageSize());

        LambdaQueryChainWrapper<CollectionInfo> lambdaQuery = lambdaQuery();

        lambdaQuery.eq(CollectionInfo::getMemberId, memberId)
                .eq(CollectionInfo::getDeleted, 0)
                .eq(CollectionInfo::getType, PayTypeEnum.INDIAN_UPI.getCode())
                .orderByDesc(CollectionInfo::getId).list();

        baseMapper.selectPage(pageCollectionInfo, lambdaQuery.getWrapper());

        List<CollectionInfo> records = pageCollectionInfo.getRecords();

        //如果会员只有一个收款信息并且未设置默认收款信息, 那么将此收款信息设为默认
        if (records.size() == 1){
            CollectionInfo collectionInfo = records.get(0);

            //校验该收款信息是否属于该会员
            if (collectionInfo == null || !collectionInfo.getMemberId().equals(memberId)) {
                log.error("设置UPI默认收款信息处理失败 该收款信息不存在或收款信息不属于该会员 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);
                return RestResult.failure(ResultCode.ILLEGAL_OPERATION_COLLECTION_INFO_CHECK_FAILED);
            }

            if (collectionInfo.getDefaultStatus() == 0){
                //用户只有此收款信息 并且此收款信息不是默认, 系统自动将该收款信息设置为默认

                //清除 该会员默认收款信息
                clearDefaultCollectionInfo(memberId, collectionInfo.getType());

                //设置该会员默认收款信息
                setDefaultCollectionInfo(collectionInfo.getId());
            }
        }

        ArrayList<CollectionInfoVo> CollectionInfoVoList = new ArrayList<>();

        for (CollectionInfo collectionInfo : records) {
            CollectionInfoVo collectionInfoVo = new CollectionInfoVo();
            BeanUtils.copyProperties(collectionInfo, collectionInfoVo);
            CollectionInfoVoList.add(collectionInfoVo);
        }

        PageReturn<CollectionInfoVo> flush = PageUtils.flush(pageCollectionInfo, CollectionInfoVoList);

        log.info("获取当前用户UPI收款信息成功 会员账号: {}, 返回数据: {}", memberInfo.getMemberAccount(), flush);

        return RestResult.ok(flush);
    }

//    /**
//     * 获取当前用户在正常收款的收款信息
//     *
//     * @param req
//     * @return {@link RestResult}<{@link PageReturn}<{@link NormalCollectionInfoVo}>>
//     */
//    @Override
//    public RestResult<PageReturn<NormalCollectionInfoVo>> currentNormalCollectionInfo(PageRequestHome req) {
//
//        //获取当前会员信息
//        MemberInfo memberInfo = memberInfoService.getMemberInfo();
//
//        if (memberInfo == null) {
//            log.error("获取当前用户在正常收款的收款信息失败: 获取会员信息失败");
//            return RestResult.failure(ResultCode.RELOGIN);
//        }
//
//        String memberId = String.valueOf(memberInfo.getId());
//
//        if (req == null) {
//            req = new PageRequestHome();
//        }
//
//        Page<CollectionInfo> pageCollectionInfo = new Page<>();
//        pageCollectionInfo.setCurrent(req.getPageNo());
//        pageCollectionInfo.setSize(req.getPageSize());
//
//        LambdaQueryChainWrapper<CollectionInfo> lambdaQuery = lambdaQuery();
//
//        lambdaQuery
//                .eq(CollectionInfo::getMemberId, memberInfo.getId())
//                .eq(CollectionInfo::getDeleted, 0)
//                .eq(CollectionInfo::getCollectedStatus, CollectionInfoStatusEnum.NORMAL.getCode())
//                .orderByDesc(CollectionInfo::getId)
//                .list();
//
//        baseMapper.selectPage(pageCollectionInfo, lambdaQuery.getWrapper());
//
//        List<CollectionInfo> records = pageCollectionInfo.getRecords();
//
//        //如果会员只有一个收款信息并且未设置默认收款信息, 那么将此收款信息设为默认
//        if (records.size() == 1){
//            CollectionInfo collectionInfo = records.get(0);
//
//            //校验该收款信息是否属于该会员
//            if (collectionInfo == null || !collectionInfo.getMemberId().equals(memberId)) {
//                log.error("设置默认收款信息处理失败 该收款信息不存在或收款信息不属于该会员 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);
//                return RestResult.failure(ResultCode.ILLEGAL_OPERATION_COLLECTION_INFO_CHECK_FAILED);
//            }
//
//            if (collectionInfo.getDefaultStatus() == 0){
//                //用户只有此收款信息 并且此收款信息不是默认, 系统自动将该收款信息设置为默认
//
//                //清除 该会员默认收款信息
//                clearDefaultCollectionInfo(memberId, collectionInfo.getType());
//
//                //设置该会员默认收款信息
//                setDefaultCollectionInfo(collectionInfo.getId());
//            }
//        }
//
//        ArrayList<NormalCollectionInfoVo> normalCollectionInfoVoList = new ArrayList<>();
//
//        for (CollectionInfo collectionInfo : records) {
//            NormalCollectionInfoVo normalCollectionInfoVo = new NormalCollectionInfoVo();
//            BeanUtils.copyProperties(collectionInfo, normalCollectionInfoVo);
//            normalCollectionInfoVoList.add(normalCollectionInfoVo);
//        }
//
//        PageReturn<NormalCollectionInfoVo> flush = PageUtils.flush(pageCollectionInfo, normalCollectionInfoVoList);
//
//        log.info("获取当前用户在正常收款的收款信息成功 会员账号: {}, 返回数据: {}", memberInfo.getMemberAccount(), flush);
//
//        return RestResult.ok(flush);
//    }

    /**
     * 更新收款信息: 今日收款金额、今日收款笔数
     *
     * @param sellReq
     * @return {@link Boolean}
     */
//    @Override
//    public Boolean addCollectionInfoQuotaAndCount(SellReq sellReq, CollectionInfo collectionInfo) {
//        //添加收款信息: 已收款金额、已收款笔数、今日收款金额、今日收款笔数
//        return lambdaUpdate().eq(CollectionInfo::getId, sellReq.getCollectionInfoId())
//                .set(CollectionInfo::getTodayCollectedAmount, collectionInfo.getTodayCollectedAmount().add(sellReq.getAmount()))//添加今日收款金额
//                .set(CollectionInfo::getTodayCollectedCount, collectionInfo.getTodayCollectedCount() + 1).update();//今日收款笔数+1
//    }


    /**
     * 删除收款信息
     *
     * @param collectionInfoId
     * @return {@link Boolean}
     */
    @Override
    public Boolean deleteCollectionInfo(Long collectionInfoId) {
        return lambdaUpdate().eq(CollectionInfo::getId, collectionInfoId).set(CollectionInfo::getDeleted, 1).set(CollectionInfo::getCollectedStatus, CollectionInfoStatusEnum.CLOSE.getCode()).update();
    }


    @Override
    public PageReturn<CollectionInfoDTO> listPage(CollectionInfoListPageReq req) {
        Page<CollectionInfo> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<CollectionInfo> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(CollectionInfo::getCreateTime);
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getUpiId())) {
            lambdaQuery.eq(CollectionInfo::getUpiId, req.getUpiId());
        }

        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getMemberId())) {
            lambdaQuery.eq(CollectionInfo::getMemberId, req.getMemberId());
        }

        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getUpiName())) {
            lambdaQuery.eq(CollectionInfo::getUpiName, req.getUpiName());
        }


        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getBankCode())) {
            lambdaQuery.eq(CollectionInfo::getBankCode, req.getBankCode());
        }

        if (ObjectUtils.isNotEmpty(req.getType())) {
            lambdaQuery.eq(CollectionInfo::getType, req.getType());
        }

        if (ObjectUtils.isNotEmpty(req.getBankCardNumber())) {
            lambdaQuery.eq(CollectionInfo::getBankCardNumber, req.getBankCardNumber());
        }

        lambdaQuery.eq(CollectionInfo::getDeleted, 0);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<CollectionInfoDTO> list = new ArrayList<CollectionInfoDTO>();
        List<CollectionInfo> records = page.getRecords();
        for(CollectionInfo collectionInfo: records){
            CollectionInfoDTO collectionInfoDTO = new CollectionInfoDTO();
            BeanUtils.copyProperties(collectionInfo,collectionInfoDTO);
            collectionInfoDTO.setCollectedNumber(collectionInfo.getCollectedCount());
            list.add(collectionInfoDTO);
        }
       // List<CollectionInfoDTO> list = walletMapStruct.collectionInfoTransform(records);
        return PageUtils.flush(page, list);
    }


    @Override
    public List<CollectionInfoDTO> getListByUid(CollectionInfoIdReq req) {

        LambdaQueryChainWrapper<CollectionInfo> lambdaQuery = lambdaQuery();
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getId().toString())) {
            lambdaQuery.eq(CollectionInfo::getId, req.getId().toString());
        }
        lambdaQuery.eq(CollectionInfo::getDeleted, 0);
        List<CollectionInfo> list = baseMapper.selectList(lambdaQuery.getWrapper());
        List<CollectionInfoDTO> listDto = new ArrayList<>();
        for(CollectionInfo collectionInfo: list){
            CollectionInfoDTO collectionInfoDTO = new CollectionInfoDTO();
            BeanUtils.copyProperties(collectionInfo,collectionInfoDTO);
            collectionInfoDTO.setCollectedNumber(collectionInfo.getCollectedCount());
            listDto.add(collectionInfoDTO);
        }
        //List<CollectionInfoDTO> listDto = walletMapStruct.collectionInfoTransform(list);
        return listDto;

    }

    /**
     * 开启收款处理
     *
     * @param collectioninfoIdReq
     * @return {@link RestResult}
     */
    @Override
    @Transactional
    public RestResult enableCollectionProcessing(CollectioninfoIdReq collectioninfoIdReq) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("开启收款处理失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //获取收款信息 加上排他行锁
        CollectionInfo collectionInfo = collectionInfoMapper.selectCollectionInfoForUpdate(collectioninfoIdReq.getCollectionInfoId());

        //校验收款信息今日额度是否已满
        RestResult restResult = enableCollectionVerification(collectionInfo, memberInfo);
        if (restResult != null) {
            log.error("开启收款处理失败 会员信息: {}, 收款信息: {}, 错误信息: {}", memberInfo, collectionInfo, restResult);
            return restResult;
        }

        //将收款信息状态改为: 开启
        collectionInfo.setCollectedStatus(CollectionInfoStatusEnum.NORMAL.getCode());
        //更新收款信息
        if (updateById(collectionInfo)) {
            log.info("开启收款信息成功 会员账号: {}, req: {}, 收款信息: {}", memberInfo.getMemberAccount(), collectioninfoIdReq, collectionInfo);
            return RestResult.ok();
        }

        log.error("开启收款信息失败 会员账号: {}, req: {}, 收款信息: {}", memberInfo.getMemberAccount(), collectioninfoIdReq, collectionInfo);
        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 停止收款处理
     *
     * @param collectioninfoIdReq
     * @return {@link RestResult}
     */
//    @Override
//    @Transactional
//    public RestResult stopCollectionProcessing(CollectioninfoIdReq collectioninfoIdReq) {
//
//        //获取当前会员信息
//        MemberInfo memberInfo = memberInfoService.getMemberInfo();
//
//        if (memberInfo == null) {
//            log.error("停止收款处理失败: 获取会员信息失败");
//            return RestResult.failure(ResultCode.RELOGIN);
//        }
//
//        //获取收款信息 加上排他行锁
//        CollectionInfo collectionInfo = collectionInfoMapper.selectCollectionInfoForUpdate(collectioninfoIdReq.getCollectionInfoId());
//
//        //查看当前是否有正在进行匹配中的订单
//        RestResult restResult = stopCollectionVerification(collectionInfo, memberInfo);
//        if (restResult != null) {
//            log.error("停止收款处理失败: 会员信息: {}, 收款信息: {}, 失败信息: {}", memberInfo, collectionInfo, restResult);
//            return restResult;
//        }
//
//        //将收款信息状态改为: 关闭
//        collectionInfo.setCollectedStatus(CollectionInfoStatusEnum.CLOSE.getCode());
//        //更新收款信息
//        if (updateById(collectionInfo)) {
//            log.info("停止收款处理成功 会员账号: {}, req: {}, 收款信息: {}", memberInfo.getMemberAccount(), collectioninfoIdReq, collectionInfo);
//            return RestResult.ok();
//        }
//
//        log.error("停止收款处理失败 会员账号: {}, req: {}, 收款信息: {}", memberInfo.getMemberAccount(), collectioninfoIdReq, collectionInfo);
//        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
//    }

    /**
     * 删除收款处理
     *
     * @param collectionInfoId
     * @return {@link RestResult}
     */
    @Override
    public RestResult deleteCollectionInfoProcessing(Long collectionInfoId) {


        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("删除收款处理失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //校验该收款信息是否属于该会员
        CollectionInfo collectionInfo = getById(collectionInfoId);

        String memberId = String.valueOf(memberInfo.getId());

        if (collectionInfo != null && collectionInfo.getMemberId().equals(memberId)) {
            if (deleteCollectionInfo(collectionInfoId)) {
                log.info("删除收款信息成功, 会员账号: {}, 收款信息: {}", memberInfo.getMemberAccount(), collectionInfo);
                return RestResult.ok();
            }
        }

        log.info("删除收款信息失败, 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }


    /**
     * 添加收款信息处理
     *
     * @param frontendCollectionInfoReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult createcollectionInfoProcessing(FrontendCollectionInfoReq frontendCollectionInfoReq) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("添加收款信息处理失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //分布式锁key ar-wallet-createcollectionInfoProcessing+会员id
        String key = "ar-wallet-createcollectionInfoProcessing" + memberInfo.getId();
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {
                String validateNotNull = validateNotNull(frontendCollectionInfoReq);
                //校验参数是否为null
                if (StringUtils.isNotEmpty(validateNotNull)) {
                    return RestResult.failure(ResultCode.PARAM_VALID_FAIL, validateNotNull + " format is incorrect");
                }


                //收款信息去重校验
                CollectionInfo getPaymentDetailsByUpiIdAndUpiName = getPaymentDetailsByUpiId(frontendCollectionInfoReq.getUpiId(), frontendCollectionInfoReq.getBankCardNumber());

                if (getPaymentDetailsByUpiIdAndUpiName != null) {
                    //已存在该UPI信息了
                    log.error("添加收款信息处理失败: 收款信息重复, 会员信息: {}, 手机号: {}, 验证码: {}, req: {}", memberInfo, memberInfo.getMobileNumber(), frontendCollectionInfoReq.getVerificationCode(), frontendCollectionInfoReq);
                    return RestResult.failure(ResultCode.DUPLICATE_UPI_ERROR);
                }

                ValidateSmsCodeReq validateSmsCodeReq = new ValidateSmsCodeReq();

                //当前手机号码
                validateSmsCodeReq.setMobileNumber(memberInfo.getMobileNumber());

                //验证码
                validateSmsCodeReq.setVerificationCode(frontendCollectionInfoReq.getVerificationCode());

                //校验验证码
                if (!memberInfoService.signUpValidateSmsCode(validateSmsCodeReq)) {
                    log.error("添加收款信息处理失败: 验证码错误, 手机号: {}, 验证码: {}, req: {}", memberInfo.getMobileNumber(), frontendCollectionInfoReq.getVerificationCode(), frontendCollectionInfoReq);
                    return RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
                }

                log.info("添加收款信息: 验证码校验成功, 手机号: {}, 验证码: {}, req: {}", memberInfo.getMobileNumber(), frontendCollectionInfoReq.getVerificationCode(), frontendCollectionInfoReq);

                CollectionInfo collectionInfo = new CollectionInfo();
                BeanUtils.copyProperties(frontendCollectionInfoReq, collectionInfo);

                //查询该会员是否存在收款信息
                if (!hasCollectionInfo(String.valueOf(memberInfo.getId()), frontendCollectionInfoReq.getType())){
                    //会员不存在收款信息 将此收款信息设置为默认
                    collectionInfo.setDefaultStatus(1);
                }

                //设置会员id
                collectionInfo.setMemberId(String.valueOf(memberInfo.getId()));

                //设置会员账号
                collectionInfo.setMemberAccount(memberInfo.getMemberAccount());

                // 设置手机号 银行卡才会提交手机号过来 upi的话使用会员信息里的手机号
                collectionInfo.setMobileNumber(
                        StringUtils.isNotEmpty(validateSmsCodeReq.getMobileNumber())
                                ? validateSmsCodeReq.getMobileNumber()
                                : memberInfo.getMobileNumber()
                );

                if (save(collectionInfo)) {

                    log.info("添加收款信息处理成功 会员账号: {}, 收款信息: {}", memberInfo.getMemberAccount(), collectionInfo);

                    return RestResult.ok();
                }
            }
        } catch (Exception e) {
            log.error("添加收款信息处理失败 会员账号: {}, req: {}, e: {}", memberInfo.getMemberAccount(), frontendCollectionInfoReq, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        log.error("添加收款信息处理失败 会员账号: {}, req: {}", memberInfo.getMemberAccount(), frontendCollectionInfoReq);
        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    private String validateNotNull(FrontendCollectionInfoReq frontendCollectionInfoReq) {

        if (PayTypeEnum.INDIAN_UPI.getCode().equals(String.valueOf(frontendCollectionInfoReq.getType()))) {
            //UPI
            if (StringUtils.isEmpty(frontendCollectionInfoReq.getUpiId())) {
                return "upiId";
            } else if (StringUtils.isEmpty(frontendCollectionInfoReq.getUpiName())) {
                return "upiName";
            }
        } else {
            //银行卡
            if (StringUtils.isEmpty(frontendCollectionInfoReq.getBankCode())) {
                return "bankCode";
            } else if (StringUtils.isEmpty(frontendCollectionInfoReq.getBankName())) {
                return "bankName";
            } else if (StringUtils.isEmpty(frontendCollectionInfoReq.getBankCardOwner())) {
                return "bankCardOwner";
            } else if (StringUtils.isEmpty(frontendCollectionInfoReq.getBankCardNumber())) {
                return "bankCardNumber";
            } else if (StringUtils.isEmpty(frontendCollectionInfoReq.getIfscCode())) {
                return "ifscCode";
            } else if (frontendCollectionInfoReq.getType() == null) {
                return "type";
            }
        }

        //校验银行卡号长度
        if (StringUtils.isNotEmpty(frontendCollectionInfoReq.getBankCardNumber())) {
            //获取配置信息
            TradeConfig tradeConfig = tradeConfigService.getById(1);

            if (tradeConfig.getMinBankCodeNumber() != null || tradeConfig.getMaxBankCodeNumber() != null ){
                if (frontendCollectionInfoReq.getBankCardNumber().length() > tradeConfig.getMaxBankCodeNumber()
                        || frontendCollectionInfoReq.getBankCardNumber().length() < tradeConfig.getMinBankCodeNumber()
                ) {
                    return "bankCardNumber";
                }
            }
        }
        return null;
    }


    /**
     * 根据upi_id和upi_name获取收款信息
     *
     * @param upiId
     * @param upiName
     * @return {@link RestResult}
     */
    @Override
    public CollectionInfo getPaymentDetailsByUpiIdAndUpiName(String upiId, String upiName) {
        return lambdaQuery()
                .eq(CollectionInfo::getUpiId, upiId)
                .eq(CollectionInfo::getUpiName, upiName)
                .eq(CollectionInfo::getDeleted, 0)
                .last("LIMIT 1")
                .one();
    }


    /**
     * 查询 upi_id 或 银行卡号 是否存在
     *
     * @param upiId
     * @param bankCardNumber
     * @return {@link CollectionInfo}
     */
    @Override
    public CollectionInfo getPaymentDetailsByUpiId(String upiId, String bankCardNumber) {
        LambdaQueryChainWrapper<CollectionInfo> query = new LambdaQueryChainWrapper<>(getBaseMapper())
                .eq(CollectionInfo::getDeleted, 0)
                .last("LIMIT 1");

        if (StringUtils.isNotEmpty(upiId) && StringUtils.isEmpty(bankCardNumber)) {
            query.eq(CollectionInfo::getUpiId, upiId);
        } else if (StringUtils.isNotEmpty(bankCardNumber) && StringUtils.isEmpty(upiId)) {
            query.eq(CollectionInfo::getBankCardNumber, bankCardNumber);
        }

        return query.one();
    }

    @Override
    public boolean checkDuplicate(String upiId, String bankCardNumber, boolean isUpdate, Long id) {
        CollectionInfo collectionInfo = getPaymentDetailsByUpiId(upiId, bankCardNumber);
        if(isUpdate && ObjectUtils.isNotEmpty(id)){
            return !collectionInfo.getId().equals(id);
        }
        return !ObjectUtils.isEmpty(collectionInfo);
    }

    @Override
    public boolean checkIfscCodeDuplicate(String ifScCode,  boolean isUpdate, Long id) {
        CollectionInfo collectionInfo = getPaymentDetailsByIfScCode(ifScCode);
        if(isUpdate && ObjectUtils.isNotEmpty(id)){
            return !collectionInfo.getId().equals(id);
        }
        return !ObjectUtils.isEmpty(collectionInfo);
    }

    private CollectionInfo getPaymentDetailsByIfScCode(String ifScCode) {
        LambdaQueryChainWrapper<CollectionInfo> query = new LambdaQueryChainWrapper<>(getBaseMapper())
                .eq(CollectionInfo::getDeleted, 0)
                .last("LIMIT 1");
        if (StringUtils.isNotEmpty(ifScCode) ) {
            query.eq(CollectionInfo::getIfscCode, ifScCode);
        }

        return query.one();
    }

    /**
     * 设置默认收款信息
     *
     * @param collectioninfoIdReq
     * @return {@link RestResult}
     */
    @Override
    @Transactional
    public RestResult setDefaultCollectionInfoReq(CollectioninfoIdReq collectioninfoIdReq) {


        try {
            //获取当前会员信息
            MemberInfo memberInfo = memberInfoService.getMemberInfo();

            if (memberInfo == null) {
                log.error("设置默认收款信息处理失败: 获取会员信息失败");
                return RestResult.failure(ResultCode.RELOGIN);
            }

            //获取收款信息
            CollectionInfo collectionInfo = getById(collectioninfoIdReq.getCollectionInfoId());

            String memberId = String.valueOf(memberInfo.getId());

            //先查询该收款信息是否属于该会员
            if (collectionInfo == null || !collectionInfo.getMemberId().equals(memberId)) {
                log.error("设置默认收款信息处理失败 该收款信息不存在或收款信息不属于该会员 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);
                return RestResult.failure(ResultCode.ILLEGAL_OPERATION_COLLECTION_INFO_CHECK_FAILED);
            }

            //校验该收款信息是否已被删除
            if (collectionInfo.getDeleted() == 1) {
                log.error("设置默认收款信息处理失败 该收款信息已被删除 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);
                return RestResult.failure(ResultCode.ILLEGAL_OPERATION_COLLECTION_INFO_CHECK_FAILED);
            }

            //清除 该会员默认收款信息
            clearDefaultCollectionInfo(memberId, collectionInfo.getType());

            //设置该会员默认收款信息
            setDefaultCollectionInfo(collectioninfoIdReq.getCollectionInfoId());

            return RestResult.ok();
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("设置默认收款信息处理失败: e: {}", e);
            return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
        }
    }


    /**
     * 校验收款信息是否重复
     *
     * @param checkUpiIdDuplicateReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult<CheckUpiIdDuplicateVo> checkUpiIdDuplicate(CheckUpiIdDuplicateReq checkUpiIdDuplicateReq) {
        //收款信息去重校验
        CollectionInfo getPaymentDetailsByUpiIdAndUpiName = getPaymentDetailsByUpiId(checkUpiIdDuplicateReq.getUpiId(), checkUpiIdDuplicateReq.getCardNumber());

        CheckUpiIdDuplicateVo checkUpiIdDuplicateVo = new CheckUpiIdDuplicateVo();
        checkUpiIdDuplicateVo.setIsUpiIdDuplicate(false);

        if (getPaymentDetailsByUpiIdAndUpiName != null) {
            //已存在该UPI信息了
            checkUpiIdDuplicateVo.setIsUpiIdDuplicate(true);
            log.info("校验收款信息是否重复: 收款信息重复, req: {}", checkUpiIdDuplicateReq);
        }

        return RestResult.ok(checkUpiIdDuplicateVo);
    }


    /**
     * 获取会员默认收款信息
     *
     * @param memberId
     * @return {@link DefaultCollectionInfoVo}
     */
    @Override
    public DefaultCollectionInfoVo getDefaultCollectionInfoByMemberId(String memberId) {
        // 查询所有默认收款信息
        LambdaQueryWrapper<CollectionInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(CollectionInfo::getMemberId, memberId)
                .eq(CollectionInfo::getDefaultStatus, 1)
                .eq(CollectionInfo::getDeleted, 0)
                .in(CollectionInfo::getType, PayTypeEnum.INDIAN_UPI.getCode(), PayTypeEnum.INDIAN_CARD.getCode()); // 查询 type 为 1 和 3 的记录 upi和银行卡

        List<CollectionInfo> defaultCollectionInfos = this.list(queryWrapper);

        CollectionInfo defaultBankInfo = null;
        CollectionInfo defaultUpiInfo = null;

        // 分类处理
        if (defaultCollectionInfos != null) {
            for (CollectionInfo info : defaultCollectionInfos) {
                if (PayTypeEnum.INDIAN_CARD.getCode().equals(String.valueOf(info.getType()))) {
                    defaultBankInfo = info;
                } else if (PayTypeEnum.INDIAN_UPI.getCode().equals(String.valueOf(info.getType()))) {
                    defaultUpiInfo = info;
                }
            }
        }

        return new DefaultCollectionInfoVo(defaultBankInfo, defaultUpiInfo);
    }

    /**
     * 检查某个会员的收款信息数量是否大于0
     *
     * @param memberId 会员ID
     * @param type
     * @return true 如果数量大于0，否则false
     */
    @Override
    public Boolean hasCollectionInfo(String memberId, Integer type) {
        // 创建LambdaQueryWrapper
        LambdaQueryWrapper<CollectionInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CollectionInfo::getMemberId, memberId) // 根据会员ID过滤
                .eq(CollectionInfo::getDeleted, 0) // 过滤未删除的记录
                .eq(CollectionInfo::getType, type); // 指定类型

        // 使用count方法获取数量
        long count = this.count(queryWrapper);

        // 检查数量是否大于0
        return count > 0;
    }

    /**
     * 获取当前用户银行卡收款信息列表
     *
     * @param req
     * @return {@link RestResult }<{@link PageReturn }<{@link BankCardCollectionInfoVo }>>
     */
    @Override
    public RestResult<PageReturn<BankCardCollectionInfoVo>> getCurrentUserBankInfoList(PageRequestHome req) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取当前用户银行卡收款信息失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        String memberId = String.valueOf(memberInfo.getId());

        if (req == null) {
            req = new PageRequestHome();
        }

        Page<CollectionInfo> pageCollectionInfo = new Page<>();
        pageCollectionInfo.setCurrent(req.getPageNo());
        pageCollectionInfo.setSize(req.getPageSize());

        LambdaQueryChainWrapper<CollectionInfo> lambdaQuery = lambdaQuery();

        lambdaQuery.eq(CollectionInfo::getMemberId, memberId)
                .eq(CollectionInfo::getDeleted, 0)
                .eq(CollectionInfo::getType, PayTypeEnum.INDIAN_CARD.getCode())
                .orderByDesc(CollectionInfo::getId).list();

        baseMapper.selectPage(pageCollectionInfo, lambdaQuery.getWrapper());

        List<CollectionInfo> records = pageCollectionInfo.getRecords();

        //如果会员只有一个收款信息并且未设置默认收款信息, 那么将此收款信息设为默认
        if (records.size() == 1) {
            CollectionInfo collectionInfo = records.get(0);

            //校验该收款信息是否属于该会员
            if (collectionInfo == null || !collectionInfo.getMemberId().equals(memberId)) {
                log.error("设置银行卡默认收款信息处理失败 该收款信息不存在或收款信息不属于该会员 会员信息: {}, 收款信息: {}", memberInfo, collectionInfo);
                return RestResult.failure(ResultCode.ILLEGAL_OPERATION_COLLECTION_INFO_CHECK_FAILED);
            }

            if (collectionInfo.getDefaultStatus() == 0) {
                //用户只有此收款信息 并且此收款信息不是默认, 系统自动将该收款信息设置为默认

                //清除 该会员默认收款信息
                clearDefaultCollectionInfo(memberId, collectionInfo.getType());

                //设置该会员默认收款信息
                setDefaultCollectionInfo(collectionInfo.getId());
            }
        }

        ArrayList<BankCardCollectionInfoVo> bankCardCollectionInfoVoList = new ArrayList<>();

        for (CollectionInfo collectionInfo : records) {
            BankCardCollectionInfoVo bankCardCollectionInfoVo = new BankCardCollectionInfoVo();
            BeanUtils.copyProperties(collectionInfo, bankCardCollectionInfoVo);
            bankCardCollectionInfoVoList.add(bankCardCollectionInfoVo);
        }

        PageReturn<BankCardCollectionInfoVo> flush = PageUtils.flush(pageCollectionInfo, bankCardCollectionInfoVoList);

        log.info("获取当前用户银行卡收款信息成功 会员账号: {}, 返回数据: {}", memberInfo.getMemberAccount(), flush);

        return RestResult.ok(flush);
    }

    /**
     * 获取当前用户所有收款信息
     *
     * @return {@link RestResult }<{@link CollectionInfoVo }>
     */
    @Override
    public RestResult<AllCollectionInfoVo> getAllPaymentInfo() {
        // 获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取当前用户所有收款信息失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        List<CollectionInfo> allCollectionInfos = collectionInfoMapper.selectList(
                new LambdaQueryWrapper<CollectionInfo>()
                        .eq(CollectionInfo::getMemberId, memberInfo.getId())
                        .eq(CollectionInfo::getDeleted, 0)
                        .orderByDesc(CollectionInfo::getId));

        List<UpiCollectionInfoVo> upiCollectionInfos = new ArrayList<>();
        List<BankCardCollectionInfoVo> bankCardCollectionInfos = new ArrayList<>();

        for (CollectionInfo info : allCollectionInfos) {
            if (PayTypeEnum.INDIAN_CARD.getCode().equals(String.valueOf(info.getType()))) {
                //银行卡
                BankCardCollectionInfoVo bankCardInfo = new BankCardCollectionInfoVo();
                BeanUtils.copyProperties(info, bankCardInfo);
                bankCardCollectionInfos.add(bankCardInfo);
            } else if (PayTypeEnum.INDIAN_UPI.getCode().equals(String.valueOf(info.getType()))) {
                //UPI
                UpiCollectionInfoVo upiInfo = new UpiCollectionInfoVo();
                BeanUtils.copyProperties(info, upiInfo);
                upiCollectionInfos.add(upiInfo);
            }
        }

        AllCollectionInfoVo allCollectionInfoVo = new AllCollectionInfoVo();
        allCollectionInfoVo.setUpiCollectionInfos(upiCollectionInfos);
        allCollectionInfoVo.setBankCardCollectionInfos(bankCardCollectionInfos);

        return RestResult.ok(allCollectionInfoVo);
    }

    /**
     * 同步银行卡信息
     *
     * @param merchantMemberId 商户会员id
     * @return boolean
     */
    @Override
    @Transactional
    public boolean syncBankInfo(String merchantMemberId) {

        //分布式锁key ar-wallet-buy+商户会员id
        String key = "ar-wallet-syncBankInfo" + merchantMemberId;
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                String redisKey = RedisKeys.SYNC_BANK_INFO + merchantMemberId;

                if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {

                    //查询会员信息
                    LambdaQueryWrapper<MemberInfo> memberInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    memberInfoLambdaQueryWrapper.eq(MemberInfo::getMemberId, merchantMemberId);

                    MemberInfo memberInfo = memberInfoMapper.selectOne(memberInfoLambdaQueryWrapper);
                    if (memberInfo == null) {
                        log.error("同步银行卡信息处理失败, 获取会员信息失败, merchantMemberId: {}", merchantMemberId);
                        return true;
                    }

                    //存在待同步的数据
                    List<SyncedBankInfoListDTO> syncedBankInfoListDTOS = (List<SyncedBankInfoListDTO>) redisTemplate.opsForValue().get(redisKey);

                    log.info("同步银行卡信息处理, 获取到的redis银行卡信息: {}", syncedBankInfoListDTOS);

                    // 转换 DTO 列表为实体列表
                    List<CollectionInfo> collectionInfoList = new ArrayList<>();

                    for (SyncedBankInfoListDTO dto : syncedBankInfoListDTOS) {
                        CollectionInfo collectionInfo = new CollectionInfo();
                        //会员id
                        collectionInfo.setMemberId(String.valueOf(memberInfo.getId()));

                        //会员账号
                        collectionInfo.setMemberAccount(memberInfo.getMemberAccount());

                        //手机号 如果传手机号 就取 否则就取会员绑定的手机号
                        //判断是否是91开头
                        collectionInfo.setMobileNumber(StringUtils.isNotEmpty(dto.getMobileNumber()) ? removeIndianCountryCode(dto.getMobileNumber()) : memberInfo.getMobileNumber());

                        //类型 银行卡
                        collectionInfo.setType(Integer.valueOf(PayTypeEnum.INDIAN_CARD.getCode()));

                        //银行编码
                        collectionInfo.setBankCode(dto.getBankCode());

                        //银行名称
                        collectionInfo.setBankName(dto.getBankName());

                        //持卡人姓名
                        collectionInfo.setBankCardOwner(dto.getBankCardOwner());

                        //银行卡号
                        collectionInfo.setBankCardNumber(dto.getBankCardNumber());

                        //ifscCode
                        collectionInfo.setIfscCode(dto.getIfscCode());

                        //email
                        collectionInfo.setEmail(dto.getEmail());

                        collectionInfoList.add(collectionInfo);
                    }

                    // 批量插入收款信息表，限制批量大小为1000
                    if (!collectionInfoList.isEmpty()) {
                        boolean saveBatch = saveBatch(collectionInfoList, 1000);
                        if (!saveBatch) {
                            log.error("同步银行卡信息处理失败, 批量插入数据库失败, merchantMemberId: {}", merchantMemberId);
                            return false;
                        }
                    }

                    // 删除 Redis 中的数据
                    redisTemplate.delete(redisKey);

                    return true;
                } else {
                    log.error("同步银行卡信息处理失败, 获取redis银行卡信息失败, merchantMemberId: {}", merchantMemberId);
                }
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("同步银行卡信息处理失败, 获取redis银行卡信息失败, merchantMemberId: {}, e: {}", merchantMemberId, e.getMessage());
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return false;
    }

    /**
     * 去掉印度手机号的区号
     * @param phoneNumber 原始手机号
     * @return 去掉区号后的手机号
     */
    public static String removeIndianCountryCode(String phoneNumber) {
        if (phoneNumber != null && phoneNumber.startsWith("91") && phoneNumber.length() == 12) {
            return phoneNumber.substring(2);
        }
        return phoneNumber;
    }

    /**
     * 清除 该会员默认收款信息
     *
     * @param memberId
     * @param type
     */
    public void clearDefaultCollectionInfo(String memberId, Integer type) {
        lambdaUpdate()
                .eq(CollectionInfo::getMemberId, memberId)
                .eq(CollectionInfo::getType, type)
                .set(CollectionInfo::getDefaultStatus, 0)
                .update();
    }


    /**
     * 设置该会员默认收款信息
     *
     * @param collectionInfoId
     */
    public void setDefaultCollectionInfo(Long collectionInfoId) {

        lambdaUpdate()
                .eq(CollectionInfo::getId, collectionInfoId)
                .set(CollectionInfo::getDefaultStatus, 1)
                .update();
    }
}
