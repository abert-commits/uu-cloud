package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.BankInfoDTO;
import org.uu.common.pay.req.*;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.BankInfo;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.mapper.BankInfoMapper;
import org.uu.wallet.service.IBankInfoService;
import org.uu.wallet.vo.BankInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 银行表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-06-07
 */
@Service
public class BankInfoServiceImpl extends ServiceImpl<BankInfoMapper, BankInfo> implements IBankInfoService {

    @Value("${oss.baseUrl}")
    private String baseUrl;

    @Resource
    WalletMapStruct mapStruct;
    @Override
    public RestResult<BankInfoDTO> add(BankInfoReq req) {
        BankInfoDTO result = new BankInfoDTO();
        LambdaQueryChainWrapper<BankInfo> eq = lambdaQuery().eq(BankInfo::getBankCode, req.getBankCode());
        BankInfo bankCodeCheck = baseMapper.selectOne(eq.getWrapper());
        if(ObjectUtils.isNotEmpty(bankCodeCheck)){
            return RestResult.failed(ResultCode.BANK_CODE_HAS_BEEN_EXISTS);
        }
        BankInfo bankInfo = new BankInfo();
        BeanUtils.copyProperties(req, bankInfo);
        bankInfo.setIconUrl(setBaseUrl(req.getIconUrl()));
        int insert = baseMapper.insert(bankInfo);
        if (insert > 0) {
            BeanUtils.copyProperties(bankInfo, result);
            return RestResult.ok(result);
        }
        return RestResult.failed();
    }

    @Override
    public RestResult<BankInfoDTO> detail(BankInfoIdReq req) {
        LambdaQueryChainWrapper<BankInfo> eq = lambdaQuery().eq(BankInfo::getId, req.getId()).eq(BankInfo::getDeleted, 0);
        BankInfo bankInfo = baseMapper.selectOne(eq.getWrapper());
        if(ObjectUtils.isEmpty(bankInfo)){
            return RestResult.failed();
        }
        BankInfoDTO dto = new BankInfoDTO();
        BeanUtils.copyProperties(bankInfo,dto);
        return RestResult.ok(dto);
    }

    @Override
    public PageReturn<BankInfoDTO> listPage(BankInfoListPageReq req) {
        Page<BankInfo> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        AbstractWrapper<BankInfo, SFunction<BankInfo, ?>, LambdaQueryWrapper<BankInfo>> wrapper = lambdaQuery().eq(BankInfo::getDeleted, 0).getWrapper();
        if(ObjectUtils.isNotEmpty(req.getBankName())){
            wrapper.eq(BankInfo::getBankName, req.getBankName());
        }
        if(ObjectUtils.isNotEmpty(req.getIfscCode())){
            wrapper.eq(BankInfo::getIfscCode, req.getIfscCode());
        }
        if(ObjectUtils.isNotEmpty(req.getBankCode())){
            wrapper.eq(BankInfo::getBankCode, req.getBankCode());
        }
        // 倒序排序
        wrapper.orderByDesc(BankInfo::getId);

        baseMapper.selectPage(page, wrapper);
        List<BankInfo> records = page.getRecords();
        List<BankInfoDTO> dtoList = mapStruct.bankInfoToDto(records);
        return PageUtils.flush(page, dtoList);
    }

    @Override
    public RestResult update(BankInfoUpdateReq req) {
        BankInfo bankInfo = new BankInfo();
        BeanUtils.copyProperties(req, bankInfo);
        bankInfo.setIconUrl(setBaseUrl(req.getIconUrl()));
        int i = baseMapper.updateById(bankInfo);
        if (i > 0) {
            return RestResult.ok();
        }
        return RestResult.failed();
    }

    @Override
    public RestResult deleteInfo(BankInfoIdReq req) {
        BankInfo bankInfo = new BankInfo();
        BeanUtils.copyProperties(req, bankInfo);
        bankInfo.setDeleted(1);
        int i = baseMapper.updateById(bankInfo);
        if (i > 0) {
            return RestResult.ok();
        }
        return RestResult.failed();
    }

    @Override
    public RestResult updateStatus(BankInfoUpdateStatusReq req) {
        BankInfo bankInfo = new BankInfo();
        BeanUtils.copyProperties(req, bankInfo);
        int i = baseMapper.updateById(bankInfo);
        if (i > 0) {
            return RestResult.ok();
        }
        return RestResult.failed();
    }

    @Override
    public RestResult<Map<String, String>> getBankCodeMap() {
        LambdaQueryChainWrapper<BankInfo> eq = lambdaQuery().eq(BankInfo::getDeleted, 0);
        List<BankInfo> bankInfos = baseMapper.selectList(eq.getWrapper());
        Map<String, String> map = new HashMap<>();
        bankInfos.forEach(item -> {
            if(ObjectUtils.isNotEmpty(item.getBankCode())){
                map.put(item.getBankCode(), item.getBankName());
            }
        });
        return RestResult.ok(map);
    }

    private String setBaseUrl(String iconUrl){
        if (StringUtils.isNotEmpty(iconUrl) && !iconUrl.startsWith("http")) {
            iconUrl = baseUrl + iconUrl;
        }
        return iconUrl;
    }

    /**
     * 获取银行列表
     *
     * @return {@link RestResult }<{@link List }<{@link BankInfoVo }>>
     */
    @Override
    public RestResult<List<BankInfoVo>> getBankList() {
        // 构建查询条件，过滤掉已删除的银行和状态为非活跃的银行
        LambdaQueryWrapper<BankInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BankInfo::getBankCode, BankInfo::getBankName)
                .eq(BankInfo::getDeleted, 0);

        // 查询数据库
        List<BankInfo> banks = baseMapper.selectList(queryWrapper);

        // 转换为 BankInfoVo 列表
        List<BankInfoVo> collect = banks.stream()
                .map(this::convertToBankDto)
                .collect(Collectors.toList());

        return RestResult.ok(collect);
    }

    private BankInfoVo convertToBankDto(BankInfo bankInfo) {
        BankInfoVo bankInfoVo = new BankInfoVo();
        bankInfoVo.setBankCode(bankInfo.getBankCode());
        bankInfoVo.setBankName(bankInfo.getBankName());
        return bankInfoVo;
    }
}
