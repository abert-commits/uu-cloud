package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.MerchantCurrencyDTO;
import org.uu.common.pay.req.MerchantCurrencyReq;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.MerchantCurrency;
import org.uu.wallet.mapper.MerchantCurrencyMapper;
import org.uu.wallet.service.IMerchantCurrencyService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 货币配置表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-15
 */
@Service
public class MerchantCurrencyServiceImpl extends ServiceImpl<MerchantCurrencyMapper, MerchantCurrency> implements IMerchantCurrencyService {

    @Resource
    WalletMapStruct mapStruct;


    @Override
    public List<MerchantCurrencyDTO> allCurrency() {
        List<MerchantCurrency> list = lambdaQuery()
                .orderByDesc(MerchantCurrency::getCreateTime)
                .eq(MerchantCurrency::getDeleted, 0)//未删除
                .list();

        return list.stream()
                .map(this::convertToCurrencyDTO)
                .collect(Collectors.toList());


    }

    @Override
    public PageReturn<MerchantCurrencyDTO> currencyPage(PageRequest req) {
        Page<MerchantCurrency> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryChainWrapper<MerchantCurrency> lambdaQuery = lambdaQuery();

        List<MerchantCurrency> records = lambdaQuery
                .eq(MerchantCurrency::getDeleted, 0)
                .orderByDesc(MerchantCurrency::getCreateTime)
                .page(page)
                .getRecords();

        List<MerchantCurrencyDTO> dtoList = mapStruct.merchantCurrencyToDto(records);

        return PageUtils.flush(page, dtoList);
    }

    @Override
    public RestResult addCurrency(MerchantCurrencyReq req) {
        // 检查是否存在相同的值
        int count = lambdaQuery()
                .eq(MerchantCurrency::getCurrencyName, req.getCurrencyName())
                .eq(MerchantCurrency::getDeleted, 0)
                .count();
        if (count > 0) {
            //值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        MerchantCurrency merchantCurrency = new MerchantCurrency();
        BeanUtils.copyProperties(req, merchantCurrency);

        if (baseMapper.insert(merchantCurrency) > 0) {
            return RestResult.ok();
        }

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    @Override
    public RestResult updateSystemCurrency(Long id, MerchantCurrencyReq req) {
        // 检查是否存在相同的值且不是当前正在更新
        int count = lambdaQuery()
                .eq(MerchantCurrency::getCurrencyName, req.getCurrencyName())
                .ne(MerchantCurrency::getId, id)
                .eq(MerchantCurrency::getDeleted, 0)
                .count();
        if (count > 0) {
            //值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        MerchantCurrency merchantCurrency = baseMapper.selectById(id);
        if (Objects.nonNull(merchantCurrency)) {
            merchantCurrency.setCurrencyCode(req.getCurrencyCode());
            merchantCurrency.setCurrencyName(req.getCurrencyName());
            boolean update = updateById(merchantCurrency);

            return update ? RestResult.ok() : RestResult.failed();
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    @Override
    public boolean deleteSystemCurrency(Long id) {
        return lambdaUpdate()
                .eq(MerchantCurrency::getId, id)
                .set(MerchantCurrency::getDeleted, 1)
                .update();
    }

    private MerchantCurrencyDTO convertToCurrencyDTO(MerchantCurrency entity) {
        return MerchantCurrencyDTO.builder()
                .id(entity.getId())
                .currencyCode(entity.getCurrencyCode())
                .currencyName(entity.getCurrencyName())
                .build();
    }
}
