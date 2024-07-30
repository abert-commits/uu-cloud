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
import org.uu.common.pay.dto.SystemCurrencyDTO;
import org.uu.common.pay.dto.SystemCurrencyPageDTO;
import org.uu.common.pay.req.SystemCurrencyReq;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.SystemCurrency;
import org.uu.wallet.mapper.SystemCurrencyMapper;
import org.uu.wallet.service.ISystemCurrencyService;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
public class SystemCurrencyServiceImpl extends ServiceImpl<SystemCurrencyMapper, SystemCurrency> implements ISystemCurrencyService {

    @Resource
    WalletMapStruct mapStruct;

    /**
     * 获取所有未删除的币种汇率
     *
     * @return {@link List }<{@link SystemCurrency }>
     */
    @Override
    public List<SystemCurrency> getAllSystemCurrency() {
        return lambdaQuery()
                .eq(SystemCurrency::getDeleted, 0)//未删除
                .eq(SystemCurrency::getEnableFlag, 0)//可用
                .list();
    }


    @Override
    public List<SystemCurrencyDTO> allCurrency() {
        List<SystemCurrency> list = lambdaQuery()
                .orderByDesc(SystemCurrency::getCreateTime)
                .eq(SystemCurrency::getDeleted, 0)//未删除
                .eq(SystemCurrency::getEnableFlag, 0)//可用
                .list();

        return list.stream()
                .map(this::convertToCurrencyDTO)
                .collect(Collectors.toList());


    }

    @Override
    public PageReturn<SystemCurrencyPageDTO> currencyPage(PageRequest req) {
        Page<SystemCurrency> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryChainWrapper<SystemCurrency> lambdaQuery = lambdaQuery();

        List<SystemCurrency> records = lambdaQuery
                .eq(SystemCurrency::getDeleted, 0)
                .orderByDesc(SystemCurrency::getCreateTime)
                .page(page)
                .getRecords();

        List<SystemCurrencyPageDTO> dtoList = mapStruct.systemCurrencyToDto(records);

        return PageUtils.flush(page, dtoList);
    }

    @Override
    public RestResult addCurrency(SystemCurrencyReq req) {
        // 检查是否存在相同的值
        int count = lambdaQuery()
                .eq(SystemCurrency::getCurrencyCode, req.getCurrencyCode())
                .eq(SystemCurrency::getCurrencyName, req.getCurrencyName())
                .eq(SystemCurrency::getDeleted, 0)
                .count();
        if (count > 0) {
            //值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        SystemCurrency systemCurrency = new SystemCurrency();
        BeanUtils.copyProperties(req, systemCurrency);

        if (baseMapper.insert(systemCurrency) > 0) {
            return RestResult.ok();
        }

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    @Override
    public RestResult updateSystemCurrency(Long id, SystemCurrencyReq req) {
        // 检查是否存在相同的值且不是当前正在更新
        int count = lambdaQuery()
                .eq(SystemCurrency::getCurrencyCode, req.getCurrencyCode())
                .eq(SystemCurrency::getCurrencyName, req.getCurrencyName())
                .ne(SystemCurrency::getId, id)
                .eq(SystemCurrency::getDeleted, 0)
                .count();
        if (count > 0) {
            //值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        SystemCurrency systemCurrency = baseMapper.selectById(id);
        if (Objects.nonNull(systemCurrency)) {
            systemCurrency.setCurrencyCode(req.getCurrencyCode());
            systemCurrency.setCurrencyName(req.getCurrencyName());
            systemCurrency.setTimeZone(req.getTimeZone());
            boolean update = updateById(systemCurrency);

            return update ? RestResult.ok() : RestResult.failed();
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    @Override
    public boolean deleteSystemCurrency(Long id) {
        return lambdaUpdate()
                .eq(SystemCurrency::getId, id)
                .set(SystemCurrency::getDeleted, 1)
                .update();
    }

    private SystemCurrencyDTO convertToCurrencyDTO(SystemCurrency entity) {
        return SystemCurrencyDTO.builder()
                .id(entity.getId())
                .currencyCode(entity.getCurrencyCode())
                .currencyName(entity.getCurrencyName())
                .timeZone(entity.getTimeZone())
                .build();
    }

    /**
     * 获取指定货币汇率
     *
     * @param currencyName
     * @return {@link BigDecimal }
     */
    @Override
    public BigDecimal getCurrencyExchangeRate(String currencyName) {
        return lambdaQuery()
                .eq(SystemCurrency::getCurrencyCode, "U")
                .eq(SystemCurrency::getCurrencyName, currencyName)
                .eq(SystemCurrency::getDeleted, 0)
                .eq(SystemCurrency::getEnableFlag, 0)
                .last("LIMIT 1")
                .one()
                .getUsdtAuto();
    }
}
