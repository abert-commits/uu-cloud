package org.uu.wallet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.KycPartnersDTO;
import org.uu.common.pay.req.KycPartnerIdReq;
import org.uu.common.pay.req.KycPartnerListPageReq;
import org.uu.common.pay.req.KycPartnerMemberReq;
import org.uu.common.pay.req.KycPartnerUpdateReq;
import org.uu.common.redis.util.RedisUtils;
import org.uu.wallet.Enum.MemberOnlineStatusEnum;
import org.uu.wallet.entity.KycBank;
import org.uu.wallet.entity.KycPartners;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.mapper.KycBankMapper;
import org.uu.wallet.mapper.KycPartnersMapper;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.service.IKycPartnersService;
import org.uu.wallet.util.DelegationOrderUtil;
import org.uu.wallet.util.RequestUtil;
import org.uu.wallet.util.SpringContextUtil;
import org.uu.wallet.vo.KycBankResponseVo;
import org.springframework.stereotype.Service;
import org.uu.wallet.bo.ActiveKycPartnersBO;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * kyc信息表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-04-20
 */
@Service
@Slf4j
public class KycPartnersServiceImpl extends ServiceImpl<KycPartnersMapper, KycPartners> implements IKycPartnersService {


    @Autowired
    private KycPartnersMapper kycPartnersMapper;
    @Resource
    DelegationOrderUtil delegationOrderUtil;
    private MemberInfoMapper memberInfoMapper;
    @Autowired
    private KycBankMapper kycBankMapper;
    @Resource
    RedisUtils redisUtils;

    /**
     * 获取当前会员正在连接中的kyc
     */
    @Override
    public ActiveKycPartnersBO getActiveKycPartnersByMemberId(String memberId) {

        LambdaQueryWrapper<KycPartners> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(KycPartners::getMemberId, memberId)//当前会员kyc
                .eq(KycPartners::getDeleted, 0)//未删除
                .eq(KycPartners::getStatus, 1)
        ;

        //获取正在连接中的所有kyc
        List<KycPartners> partners = kycPartnersMapper.selectList(queryWrapper);

        //提取出银行卡kyc
        List<KycPartners> bankPartners = partners.stream()
                .filter(p -> p.getCollectionType() == 1) //1表示收款类型是银行卡
                .collect(Collectors.toList());

        //提取出UPI_Kyc
        List<KycPartners> upiPartners = partners.stream()
                .filter(p -> p.getCollectionType() == 3) //3表示收款类型是UPI
                .collect(Collectors.toList());

        ActiveKycPartnersBO activeKycPartnersBO = new ActiveKycPartnersBO();
        activeKycPartnersBO.setBankPartners(bankPartners.isEmpty() ? Collections.emptyList() : bankPartners);
        activeKycPartnersBO.setUpiPartners(upiPartners.isEmpty() ? Collections.emptyList() : upiPartners);

        return activeKycPartnersBO;
    }

    /**
     * 获取KYC列表
     *
     * @param memberId
     * @return {@link List}<{@link KycPartners}>
     */
    @Override
    public List<KycPartners> getKycPartners(Long memberId) {
        return lambdaQuery()
                .eq(KycPartners::getMemberId, memberId)
                .list();
    }

    @Override
    public int getKycPartnersCount(Long memberId) {
        return lambdaQuery()
                .eq(KycPartners::getMemberId, memberId).count();
    }

    @Override
    public List<KycPartners> getKycPartners(Long memberId, Integer collectType) {
        return lambdaQuery()
                .eq(KycPartners::getMemberId, memberId)
                .eq(KycPartners::getCollectionType, collectType)
                .list();
    }

    @Override
    public List<KycPartners> getAvailableKycPartners(Long memberId, Integer collectType) {
        return lambdaQuery()
                .eq(KycPartners::getMemberId, memberId)
                .eq(KycPartners::getCollectionType, collectType)
                .eq(KycPartners::getStatus, 1)
                .list();
    }

    /**
     *
     * @param req KycPartnerReq {@link KycPartnerListPageReq}
     * @return {@link PageReturn}<{@link KycPartnersDTO}>
     */
    @Override
    public PageReturn<KycPartnersDTO> listPage(KycPartnerListPageReq req) {
        Page<KycPartners> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<KycPartners> lambdaQuery = lambdaQuery();
        // 查询未删除
        lambdaQuery.eq(KycPartners::getDeleted, 0);
        // 排序
        page.addOrder(RequestUtil.getOrderItem(req.getColumn(), req.isAsc()));
        // 筛选
        if (ObjectUtils.isNotEmpty(req.getMemberId())) {
            lambdaQuery.and(e -> e.or().eq(KycPartners::getMemberId, req.getMemberId()).or().eq(KycPartners::getMemberAccount, req.getMemberId()));
        }

        if(ObjectUtils.isNotEmpty(req.getMobileNumber())){
            lambdaQuery.eq(KycPartners::getMobileNumber, req.getMobileNumber());
        }

        if(ObjectUtils.isNotEmpty(req.getBankCode())){
            lambdaQuery.eq(KycPartners::getBankCode, req.getBankCode());
        }

        if(ObjectUtils.isNotEmpty(req.getAccount())){
            lambdaQuery.eq(KycPartners::getAccount, req.getAccount());
        }

        if(ObjectUtils.isNotEmpty(req.getUpiId())){
            lambdaQuery.and(e -> e.or().eq(KycPartners::getUpiId, req.getUpiId()).or().eq(KycPartners::getBankName, req.getUpiId()));
        }

        if(ObjectUtils.isNotEmpty(req.getLinkStatus())){
            lambdaQuery.eq(KycPartners::getStatus, req.getLinkStatus());
        }

        if(ObjectUtils.isNotEmpty(req.getSellStatus())){
            lambdaQuery.eq(KycPartners::getStatus, req.getSellStatus());
        }

        if(ObjectUtils.isNotEmpty(req.getCollectionType())){
            lambdaQuery.eq(KycPartners::getCollectionType, req.getCollectionType());
        }

        if (ObjectUtils.isNotEmpty(req.getOnlineStatus())) {
            Map<Object, Object> onlineUserAccount = redisUtils.hmget(GlobalConstants.ONLINE_USER_KEY);
            List<String> accountList = new ArrayList<>();
            for (Map.Entry<Object, Object> objectObjectEntry : onlineUserAccount.entrySet()) {
                accountList.add(objectObjectEntry.getKey().toString());
            }
            if (accountList.isEmpty()) {
                accountList.add("-1");
            }
            if (req.getOnlineStatus().equals(MemberOnlineStatusEnum.ON_LINE.getCode())) {
                lambdaQuery.in(KycPartners::getMemberAccount, accountList);
            }
            if (req.getOnlineStatus().equals(MemberOnlineStatusEnum.OFF_LINE.getCode())) {
                lambdaQuery.notIn(KycPartners::getMemberAccount, accountList);
            }
        }

        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        Map<String, String> linkTypeMap = new HashMap<>();
        List<KycPartners> records = page.getRecords();
        if(!records.isEmpty()){
            List<KycBank> kycBanks = kycBankMapper.selectList(null);
            linkTypeMap = kycBanks.stream().collect(Collectors.toMap(KycBank::getBankCode, KycBank::getLinkType));
        }
        ArrayList<KycPartnersDTO> kycPartnersArrayList = new ArrayList<>();
        for (KycPartners record : records) {
            KycPartnersDTO kycPartnersDTO = new KycPartnersDTO();
            BeanUtil.copyProperties(record, kycPartnersDTO);
            if(!linkTypeMap.isEmpty() && linkTypeMap.containsKey(record.getBankCode())){
                kycPartnersDTO.setLinkType(linkTypeMap.get(record.getBankCode()));
            }
            if (redisUtils.hHasKey(GlobalConstants.ONLINE_USER_KEY, record.getMemberAccount())) {
                long value = Long.parseLong(redisUtils.hget(GlobalConstants.ONLINE_USER_KEY, record.getMemberAccount()) + "");
                long currentTime = System.currentTimeMillis();
                if (value > currentTime) {
                    // 说明该用户登录的令牌还没有过期
                    kycPartnersDTO.setOnlineStatus(MemberOnlineStatusEnum.ON_LINE.getCode());
                } else {
                    kycPartnersDTO.setOnlineStatus(MemberOnlineStatusEnum.OFF_LINE.getCode());
                }
            }
            kycPartnersArrayList.add(kycPartnersDTO);
        }
        return PageUtils.flush(page, kycPartnersArrayList);
    }

    /**
     * 删除
     *
     * @param req {@link KycPartnerIdReq} req
     * @return boolean
     */
    @Override
    public boolean delete(KycPartnerIdReq req) {
        return lambdaUpdate().eq(KycPartners::getId, req.getId()).set(KycPartners::getDeleted, 1).update();
    }


    /**
     * 根据upi_id 获取 KYC
     *
     * @param upiId
     * @return {@link KycPartners}
     */
    @Override
    public KycPartners getKYCPartnersByUpiId(String upiId) {
        return lambdaQuery()
                .eq(KycPartners::getUpiId, upiId)//upi_id
                .eq(KycPartners::getDeleted, 0)// 未删除
                .eq(KycPartners::getStatus, 1)
                .one();
    }

    /**
     * 根据upi_id 获取 KYC
     *
     * @param kycId
     * @return {@link KycPartners}
     */
    @Override
    public KycPartners getKYCPartnersById(String kycId) {
        return lambdaQuery()
                .eq(KycPartners::getId, kycId)
                .eq(KycPartners::getDeleted, 0)
                .one();
    }

    @Override
    public void kycPartnersLink() {
        // 查询需要自动连接的kycPartner
        List<KycPartners> linkPartnerList = lambdaQuery().eq(KycPartners::getDeleted, 0).eq(KycPartners::getStatus, 1).list();
        // 遍历 查询对应信息的token
        for (KycPartners kycPartners : linkPartnerList) {
            try{
                IAppBankTransaction appBankTransaction = SpringContextUtil.getBean(kycPartners.getBankCode());
                KycBankResponseVo linkKycPartner = appBankTransaction.linkKycPartner(kycPartners.getToken());
                if(linkKycPartner.getStatus()){
                    log.info("linkKycPartner: 续token成功, phoneNumber:{}, bankCode:{}", kycPartners.getToken(), kycPartners.getBankCode());
                }else{
                    // 续token失败之后关闭link连接
                    kycPartners.setStatus(0);
                    kycPartners.setToken(null);
                    kycPartners.setUpdateTime(LocalDateTime.now());
                    int updateResult = baseMapper.updateById(kycPartners);
                    if(updateResult != 1){
                        log.info("linkKycPartner: 更改kycPartner状态失败, phoneNumber:{}, bankCode:{}, kycPartner:{}", kycPartners.getToken(), kycPartners.getBankCode(), kycPartners);
                        return;
                    }
                    // 调用关闭委托方法
                    String memberId = kycPartners.getMemberId();
                    MemberInfo memberInfoById = memberInfoMapper.getMemberInfoById(memberId);
                    delegationOrderUtil.closeDelegation(memberInfoById);
                    log.info("linkKycPartner: 续token失败, phoneNumber:{}, bankCode:{}", kycPartners.getToken(), kycPartners.getBankCode());
                }
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean checkDuplicates(Long memberId, String bankCode, String account) {
        // 判断是否有非当前用户使用该账号
        List<KycPartners> list = lambdaQuery()
                .eq(KycPartners::getDeleted, 0)
                .ne(KycPartners::getMemberId, memberId)
                .eq(KycPartners::getAccount, account)
                .list();
        // 判断当前用户是否添加过相同银行
        KycPartners one = lambdaQuery()
                .eq(KycPartners::getDeleted, 0)
                .eq(KycPartners::getBankCode, bankCode)
                .eq(KycPartners::getMemberId, memberId)
                .eq(KycPartners::getAccount, account)
                .one();
        return ObjectUtils.isNotEmpty(list) || ObjectUtils.isNotEmpty(one);
    }

    @Override
    public boolean checkDuplicatesByUpiId(String upiId) {

        // 判断是否有upi
        KycPartners one = lambdaQuery()
                .eq(KycPartners::getDeleted, 0)
                .eq(KycPartners::getUpiId, upiId)
                .one();
        return ObjectUtils.isNotEmpty(one);
    }

    @Override
    public KycPartners getKycPartnerByAccount(String account, String bankCode) {
        return lambdaQuery()
                .eq(KycPartners::getDeleted, 0)
                .eq(KycPartners::getBankCode, bankCode)
                .eq(KycPartners::getAccount, account)
                .eq(KycPartners::getStatus, 1)
                .one();
    }

    @Override
    public List<KycPartners> getKycPartnerByMemberId(String memberId) {
        return lambdaQuery()
                .eq(KycPartners::getDeleted, 0)
                .eq(KycPartners::getMemberId, memberId)
                .eq(KycPartners::getStatus, 1)
                .list();
    }

    @Override
    public List<String> getUpiIdByMemberId(String memberId) {
        List<KycPartners> list = lambdaQuery()
                .eq(KycPartners::getDeleted, 0)
                .eq(KycPartners::getMemberId, memberId)
                .eq(KycPartners::getStatus, 1)
                .list();
        return list.stream().map(KycPartners::getUpiId).collect(Collectors.toList());
    }

    @Override
    public boolean updateTransactionInfo(String kycId, BigDecimal amount, String type) {
        KycPartners kycPartners = getById(kycId);
        if(Objects.equals(type, "1")){
            // 充值
            Integer kycSellCount = 0;
            BigDecimal kycSellAmount = BigDecimal.ZERO;
            if(ObjectUtils.isNotEmpty(kycPartners.getTotalSellSuccessCount())){
                kycSellCount = kycPartners.getTotalSellSuccessCount();
            }
            if(ObjectUtils.isNotEmpty(kycPartners.getTotalSellSuccessAmount())){
                kycSellAmount = kycPartners.getTotalSellSuccessAmount();
            }
            kycPartners.setTotalSellSuccessCount(kycSellCount + 1);
            kycPartners.setTotalSellSuccessAmount(kycSellAmount.add(amount));
        }else{
            // 提现
            Integer kycBuyCount = 0;
            BigDecimal kycBuyAmount = BigDecimal.ZERO;
            if(ObjectUtils.isNotEmpty(kycPartners.getTotalBuySuccessCount())){
                kycBuyCount = kycPartners.getTotalBuySuccessCount();
            }
            if(ObjectUtils.isNotEmpty(kycPartners.getTotalSellSuccessAmount())){
                kycBuyAmount = kycPartners.getTotalSellSuccessAmount();
            }
            kycPartners.setTotalBuySuccessCount(kycBuyCount + 1);
            kycPartners.setTotalBuySuccessAmount(kycBuyAmount.add(amount));
        }
        kycPartners.setId(Long.parseLong(kycId));
        return updateById(kycPartners);
    }

    @Override
    public KycPartners updateKycPartner(KycPartnerUpdateReq req) {
        KycPartners currentKycPartners = getById(req.getId());
        KycPartners kycPartners = new KycPartners();
        BeanUtil.copyProperties(req, kycPartners);
        int i = baseMapper.updateById(kycPartners);
        if(i != 1){
            return null;
        }
        BeanUtil.copyProperties(kycPartners, currentKycPartners);
        return currentKycPartners;
    }

    @Override
    public List<KycPartnersDTO> getKycPartnerByMemberId(KycPartnerMemberReq req) {
        String memberId = req.getMemberId();
        List<KycPartners> list = lambdaQuery().eq(KycPartners::getMemberId, memberId).eq(KycPartners::getDeleted, 0).list();
        List<KycPartnersDTO> result = new ArrayList<>();
        for (KycPartners kycPartners : list) {
            KycPartnersDTO dto = new KycPartnersDTO();
            BeanUtil.copyProperties(kycPartners, dto);
            result.add(dto);
        }
        return result;
    }


    /**
     * 查看当前会员是否有正在连接中的kyc 如果有的话 返回一个kyc信息
     *
     * @param memberId
     * @return {@link Boolean }
     */
    @Override
    public KycPartners hasActiveKycForCurrentMember(Long memberId) {

        // 查询当前会员所有的kycPartner
        List<KycPartners> linkPartnerList = lambdaQuery()
                .eq(KycPartners::getDeleted, 0)//未删除
                .eq(KycPartners::getMemberId, memberId)//当前会员
                .eq(KycPartners::getStatus, 1)//卖出状态开启
                .list();

        if (linkPartnerList == null || linkPartnerList.size() == 0) {
            log.info("查询当前会员所有的kycPartner, 当前会员无kyc信息, 会员id: {}", memberId);
            return null;
        }

        // 遍历 查询对应信息的token
        for (KycPartners kycPartners : linkPartnerList) {
            try {

                //根据bankCode获取对应的实现类
                IAppBankTransaction appBankTransaction = SpringContextUtil.getBean(kycPartners.getBankCode());

                //使用token进行与银行交互
                KycBankResponseVo linkKycPartner = appBankTransaction.linkKycPartner(kycPartners.getToken());

                if (linkKycPartner.getStatus()) {
                    log.info("查询当前会员所有的kycPartner, 成功, 当前kyc信息: {}, 会员id: {}", kycPartners, memberId);
                    return kycPartners;
                } else {
                }
            } catch (Exception e) {
                log.error("查询当前会员所有的kycPartner, 失败, 当前kyc信息: {}, 会员id: {}, e: {}", kycPartners, memberId, e);
            }
        }
        return null;
    }
}
