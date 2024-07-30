package org.uu.wallet.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.CurrencyPayTypeDTO;
import org.uu.common.pay.dto.CurrencyPayTypePageDTO;
import org.uu.common.pay.req.CurrencyPayTypeReq;
import org.uu.wallet.entity.CurrencyPayType;
import org.uu.wallet.mapper.CurrencyPayTypeMapper;
import org.uu.wallet.service.ICurrencyPayTypeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 币种对应的代收代付类型 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-15
 */
@Service
public class CurrencyPayTypeServiceImpl extends ServiceImpl<CurrencyPayTypeMapper, CurrencyPayType> implements ICurrencyPayTypeService {

   
    @Override
    public List<CurrencyPayTypeDTO> currencyPayTypeListById(Long currencyId, Integer type) {
        List<CurrencyPayType> list = lambdaQuery()
                .orderByDesc(CurrencyPayType::getCreateTime)
                .eq(CurrencyPayType::getCurrencyId, currencyId)
                .eq(CurrencyPayType::getType, type)
                .eq(CurrencyPayType::getDeleted, 0)
                .list();

        return list.stream()
                .map(this::convertToCurrencyPayTypeDTO)
                .collect(Collectors.toList());
    }


    @Override
    public PageReturn<CurrencyPayTypePageDTO> currencyPayTypePage(PageRequest req) {
        Page<CurrencyPayType> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryChainWrapper<CurrencyPayType> lambdaQuery = lambdaQuery();

        List<CurrencyPayType> records = lambdaQuery
                .orderByDesc(CurrencyPayType::getCreateTime)
                .eq(CurrencyPayType::getDeleted, 0)
                .page(page)
                .getRecords();
        List<CurrencyPayTypePageDTO> dtoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(records)) {
            dtoList = records.stream()
                    .filter(Objects::nonNull)
                    .map(item -> {
                        CurrencyPayTypePageDTO tempDTO = new CurrencyPayTypePageDTO();
                        BeanUtils.copyProperties(item, tempDTO);
                        tempDTO.setUpdateBy(StringUtils.isNotEmpty(tempDTO.getUpdateBy()) ? tempDTO.getUpdateBy() : item.getCreateBy());
                        tempDTO.setUpdateTime(Objects.nonNull(tempDTO.getUpdateTime()) ? tempDTO.getUpdateTime() : item.getCreateTime());
                        return tempDTO;
                    })
                    .collect(Collectors.toList());
        }

        return PageUtils.flush(page, dtoList);
    }


    @Override
    public RestResult addCurrencyPayType(CurrencyPayTypeReq req) {
        // 检查是否存在相同的值
        int count = lambdaQuery()
                .eq(CurrencyPayType::getType, req.getType())
                .eq(CurrencyPayType::getCurrencyId, req.getCurrencyId())
                .eq(CurrencyPayType::getPayType, req.getPayType())
                .eq(CurrencyPayType::getDeleted, 0)
                .count();
        if (count > 0) {
            //值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        CurrencyPayType currencyPayType = new CurrencyPayType();
        BeanUtils.copyProperties(req, currencyPayType);

        if (baseMapper.insert(currencyPayType) > 0) {
            return RestResult.ok();
        }

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }


    @Override
    public RestResult updateCurrencyPayType(Long id, CurrencyPayTypeReq req) {
        // 检查是否存在相同的值且不是当前正在更新
        int count = lambdaQuery()
                .eq(CurrencyPayType::getType, req.getType())
                .eq(CurrencyPayType::getCurrencyId, req.getCurrencyId())
                .eq(CurrencyPayType::getPayType, req.getPayType())
                .eq(CurrencyPayType::getDeleted, 0)
                .ne(CurrencyPayType::getId, id)
                .count();
        if (count > 0) {
            //值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        CurrencyPayType currencyPayType = baseMapper.selectById(id);
        if (Objects.nonNull(currencyPayType)) {
            currencyPayType.setCurrency(req.getCurrency());
            currencyPayType.setCurrencyId(req.getCurrencyId());
            currencyPayType.setType(req.getType());
            currencyPayType.setPayType(req.getPayType());
            currencyPayType.setPayTypeName(req.getPayTypeName());
            boolean update = updateById(currencyPayType);

            return update ? RestResult.ok() : RestResult.failed();
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }


    @Override
    public boolean deleteCurrencyPayType(Long id) {
        return lambdaUpdate()
                .eq(CurrencyPayType::getId, id)
                .set(CurrencyPayType::getDeleted, 1)
                .update();
    }


    private CurrencyPayTypeDTO convertToCurrencyPayTypeDTO(CurrencyPayType entity) {
        return CurrencyPayTypeDTO.builder()
                .id(entity.getId())
                .currencyId(entity.getCurrencyId())
                .payType(entity.getPayType())
                .payTypeName(entity.getPayTypeName())
                .build();
    }
}
