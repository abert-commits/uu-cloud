package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.TronRentEnergyDTO;
import org.uu.common.pay.dto.TronRentEnergyExportDTO;
import org.uu.common.pay.req.TronRentEnergyReq;
import org.uu.wallet.entity.TronRentEnergy;
import org.uu.wallet.mapper.TronRentEnergyMapper;
import org.uu.wallet.service.ITronRentEnergyService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 能量租用记录表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-13
 */
@Service
public class TronRentEnergyServiceImpl extends ServiceImpl<TronRentEnergyMapper, TronRentEnergy> implements ITronRentEnergyService {

    @Override
    public PageReturn<TronRentEnergyDTO> tronRentEnergyListPage(TronRentEnergyReq req) {
        Page<TronRentEnergy> page = new Page<>(req.getPageNo(), req.getPageSize());
        lambdaQuery()
                .orderByDesc(TronRentEnergy::getCreateTime)
                .ge(Objects.nonNull(req.getCreateTimeStart()), TronRentEnergy::getReceiveTime, req.getCreateTimeStart())
                .le(Objects.nonNull(req.getCreateTimeEnd()), TronRentEnergy::getReceiveTime, req.getCreateTimeEnd())
                .page(page);

        List<TronRentEnergyDTO> resultList = page.getRecords().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    TronRentEnergyDTO energyDTO = new TronRentEnergyDTO();
                    BeanUtils.copyProperties(item, energyDTO);
                    return energyDTO;
                })
                .collect(Collectors.toList());

        long pageAmount = resultList.stream().mapToLong(TronRentEnergyDTO::getAmount).sum();

        long amountTotal = lambdaQuery()
                .ge(Objects.nonNull(req.getCreateTimeStart()), TronRentEnergy::getReceiveTime, req.getCreateTimeStart())
                .le(Objects.nonNull(req.getCreateTimeEnd()), TronRentEnergy::getReceiveTime, req.getCreateTimeEnd())
                .list()
                .stream()
                .mapToLong(TronRentEnergy::getAmount)
                .sum();

        JSONObject extend = new JSONObject();
        extend.put("pageAmount", pageAmount);
        extend.put("amountTotal", amountTotal);

        return PageUtils.flush(page, resultList, extend);
    }

    @Override
    public PageReturn<TronRentEnergyExportDTO> tronRentEnergyExport(TronRentEnergyReq req) {
        Page<TronRentEnergy> page = new Page<>(req.getPageNo(), req.getPageSize());
        lambdaQuery()
                .orderByDesc(TronRentEnergy::getCreateTime)
                .ge(Objects.nonNull(req.getCreateTimeStart()), TronRentEnergy::getReceiveTime, req.getCreateTimeStart())
                .le(Objects.nonNull(req.getCreateTimeEnd()), TronRentEnergy::getReceiveTime, req.getCreateTimeEnd())
                .page(page);

        List<TronRentEnergyExportDTO> resultList = page.getRecords().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    TronRentEnergyExportDTO energyDTO = new TronRentEnergyExportDTO();
                    BeanUtils.copyProperties(item, energyDTO);
                    energyDTO.setStatus(item.getResultCode().equals("200") && item.getResultMessage().equals("0") ? "成功" : "失败");
                    return energyDTO;
                })
                .collect(Collectors.toList());

        return PageUtils.flush(page, resultList);
    }


}
