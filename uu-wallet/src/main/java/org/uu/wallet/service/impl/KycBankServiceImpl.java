package org.uu.wallet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.KycBankDTO;
import org.uu.common.pay.req.KycBankIdReq;
import org.uu.common.pay.req.KycBankListPageReq;
import org.uu.common.pay.req.KycBankReq;
import org.uu.common.pay.req.KycPartnerIdReq;
import org.uu.wallet.entity.KycBank;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.mapper.KycBankMapper;
import org.uu.wallet.service.IKycBankService;
import org.uu.wallet.util.RequestUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2024-04-16
 */
@Service
@RequiredArgsConstructor
public class KycBankServiceImpl extends ServiceImpl<KycBankMapper, KycBank> implements IKycBankService {


    @Value("${oss.baseUrl}")
    private String baseUrl;

    /**
     * 根据bankCode获取KYC银行信息
     *
     * @param bankCode
     * @return {@link KycBank}
     */
    @Override
    public KycBank getBankInfoByBankCode(String bankCode) {
        return lambdaQuery()
                .eq(KycBank::getBankCode, bankCode)
                .eq(KycBank::getDeleted, 0)
                .one();
    }

    @Override
    public PageReturn<KycBankDTO> listPage(KycBankListPageReq req) {
        Page<KycBank> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<KycBank> lambdaQuery = lambdaQuery();
        // 查询未删除
        lambdaQuery.eq(KycBank::getDeleted, 0);
        // 排序
        page.addOrder(RequestUtil.getOrderItem(req.getColumn(), req.isAsc()));
        // 筛选

        if (ObjectUtils.isNotEmpty(req.getBankCode())) {
            lambdaQuery.eq(KycBank::getBankCode, req.getBankCode());
        }


        if (ObjectUtils.isNotEmpty(req.getServiceCode())) {
            lambdaQuery.eq(KycBank::getServiceCode, req.getServiceCode());
        }

        if (ObjectUtils.isNotEmpty(req.getStatus())) {
            lambdaQuery.eq(KycBank::getStatus, req.getStatus());
        }

        if (ObjectUtils.isNotEmpty(req.getLinkType())) {
            lambdaQuery.eq(KycBank::getLinkType, req.getLinkType());
        }

        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<KycBank> records = page.getRecords();

        ArrayList<KycBankDTO> kycPartnersDTOArrayList = new ArrayList<>();
        for (KycBank record : records) {
            KycBankDTO kycBankDTO = new KycBankDTO();
            BeanUtil.copyProperties(record, kycBankDTO);
            kycPartnersDTOArrayList.add(kycBankDTO);
        }
        return PageUtils.flush(page, kycPartnersDTOArrayList);
    }

    @Override
    public List<String> getBankCodeList() {
        QueryWrapper<KycBank> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("bank_code")
        .eq("deleted", 0)
                        .groupBy("bank_code");
        List<KycBank> kycBanks = baseMapper.selectList(queryWrapper);
        if(ObjectUtils.isNotEmpty(kycBanks) && !kycBanks.isEmpty()){
            return kycBanks.stream().map(KycBank::getBankCode).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 删除
     *
     * @param req {@link KycPartnerIdReq} req
     * @return boolean
     */
    @Override
    public boolean deleteKycBank(KycBankIdReq req) {
        return lambdaUpdate().eq(KycBank::getId, req.getId()).remove();
    }

    @Override
    public RestResult<KycBankDTO> addKycBank(KycBankReq req) {
        if(ObjectUtils.isEmpty(req.getBankCode())) {
            RestResult.failed(ResultCode.PARAM_IS_EMPTY_OR_ERROR);
        }
        // 查询当前是否已存在相同bankCode的对象
        LambdaQueryChainWrapper<KycBank> queryChainWrapper = lambdaQuery()
                .eq(KycBank::getBankCode, req.getBankCode())
                .eq(KycBank::getType, req.getType())
                ;
        KycBank kycBank = baseMapper.selectOne(queryChainWrapper.getWrapper());
        if(ObjectUtils.isNotEmpty(kycBank)) {
            RestResult.failed(ResultCode.KYC_BANK_ALREADY_EXISTS_FAILED);
        }
        KycBank saveData = new KycBank();
        BeanUtils.copyProperties(req, saveData);
        String iconUrl = getIconUrl(saveData.getIconUrl());
        saveData.setIconUrl(iconUrl);
        int insert = baseMapper.insert(saveData);
        if(insert > 0) {
            KycBankDTO kycBankDTO = new KycBankDTO();
            BeanUtil.copyProperties(saveData, kycBankDTO);
            return RestResult.ok(kycBankDTO);
        }
        return RestResult.failed();
    }

    @Override
    public RestResult<KycBankDTO> updateKycBank(KycBankReq req) {
        KycBank saveData = new KycBank();
        BeanUtils.copyProperties(req, saveData);
        String iconUrl = getIconUrl(saveData.getIconUrl());
        saveData.setIconUrl(iconUrl);
        int updateResult = baseMapper.updateById(saveData);
        if(updateResult > 0) {
            KycBankDTO kycBankDTO = new KycBankDTO();
            BeanUtil.copyProperties(saveData, kycBankDTO);
            return RestResult.ok(kycBankDTO);
        }
        return RestResult.failed();
    }

    private String getIconUrl(String icon) {
        if (icon != null && !icon.startsWith("https://")) {
            // 如果不是以"http"开头，则进行拼接
            icon = baseUrl + icon;
        }
        return icon;
    }
}
