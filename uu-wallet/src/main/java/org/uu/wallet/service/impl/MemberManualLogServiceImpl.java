package org.uu.wallet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.MemberLoginLogsDTO;
import org.uu.common.pay.dto.MemberManualLogDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.uu.common.pay.req.MemberManualLogsReq;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MemberLoginLogs;
import org.uu.wallet.mapper.MemberManualLogMapper;
import org.uu.wallet.service.IMemberManualLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *
 * 会员手动操作记录 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-02-29
 */
@Service
public class MemberManualLogServiceImpl extends ServiceImpl<MemberManualLogMapper, MemberManualLogDTO> implements IMemberManualLogService {

    @Override
    public PageReturn<MemberManualLogDTO> listPage(MemberManualLogsReq req) {
        Page<MemberManualLogDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        OrderItem orderItem = new OrderItem();
        LambdaQueryChainWrapper<MemberManualLogDTO> lambdaQuery = lambdaQuery();
        if(StringUtils.isNotBlank(req.getColumn())){
            orderItem.setColumn(StrUtil.toUnderlineCase(req.getColumn()));
            orderItem.setAsc(req.isAsc());
            page.addOrder(orderItem);
        }
        if (StringUtils.isNotBlank(req.getCreateBy())) {
            lambdaQuery.eq(MemberManualLogDTO::getCreateBy, req.getCreateBy());
        }
        if (ObjectUtils.isNotEmpty(req.getOpType())) {
            lambdaQuery.eq(MemberManualLogDTO::getOpType, req.getOpType());
        }
        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(MemberManualLogDTO::getCreateTime, req.getStartTime());
        }

        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(MemberManualLogDTO::getCreateTime, req.getEndTime());
        }
        lambdaQuery.orderByDesc(MemberManualLogDTO::getCreateTime);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<MemberManualLogDTO> records = page.getRecords();
        return PageUtils.flush(page, records);
    }
}
