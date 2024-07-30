package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.WithdrawTronDetailDTO;
import org.uu.common.pay.dto.WithdrawTronDetailExportDTO;
import org.uu.common.pay.req.WithdrawTronDetailReq;
import org.uu.wallet.entity.WithdrawTronDetail;
import org.uu.wallet.mapper.WithdrawTronDetailMapper;
import org.uu.wallet.service.IWithdrawTronDetailService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 代付钱包交易记录 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-18
 */
@Service
public class WithdrawTronDetailServiceImpl extends ServiceImpl<WithdrawTronDetailMapper, WithdrawTronDetail> implements IWithdrawTronDetailService {

    @Override
    public PageReturn<WithdrawTronDetailDTO> withdrawTronDetailPage(WithdrawTronDetailReq req) {
        Page<WithdrawTronDetail> page = new Page<>(req.getPageNo(), req.getPageSize());
        lambdaQuery()
                .orderByDesc(WithdrawTronDetail::getCreateTime)
                .ge(Objects.nonNull(req.getCreateTimeStart()), WithdrawTronDetail::getCreateTime, req.getCreateTimeStart())
                .le(Objects.nonNull(req.getCreateTimeEnd()), WithdrawTronDetail::getCreateTime, req.getCreateTimeEnd())
                .eq(StringUtils.isNotEmpty(req.getOrderId()), WithdrawTronDetail::getOrderId, req.getOrderId())
                .eq(StringUtils.isNotEmpty(req.getTxid()), WithdrawTronDetail::getTxid, req.getTxid())
                .page(page);

        List<WithdrawTronDetailDTO> resultList = page.getRecords().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    WithdrawTronDetailDTO dto = new WithdrawTronDetailDTO();
                    BeanUtils.copyProperties(item, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        return PageUtils.flush(page, resultList);
    }

    @Override
    public PageReturn<WithdrawTronDetailExportDTO> withdrawTronDetailPageExport(WithdrawTronDetailReq req) {
        PageReturn<WithdrawTronDetailDTO> pageReturn = withdrawTronDetailPage(req);
        List<WithdrawTronDetailExportDTO> resultList = pageReturn.getList().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    WithdrawTronDetailExportDTO dto = new WithdrawTronDetailExportDTO();
                    BeanUtils.copyProperties(item, dto);
                    dto.setStatus(Objects.nonNull(item.getStatus()) ? getOrderStatus(item.getStatus()) : "成功");
                    return dto;
                })
                .collect(Collectors.toList());

        Page<WithdrawTronDetailExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(pageReturn.getTotal());
        return PageUtils.flush(page, resultList);
    }

    //0转账中 1成功 2 失败
    private String getOrderStatus(Integer status) {
        switch (status) {
            case 0:
                return "转账中";
            case 2:
                return "失败";
            default:
                return "成功";
        }
    }
}

