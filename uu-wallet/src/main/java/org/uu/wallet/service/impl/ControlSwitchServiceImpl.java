package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.ControlSwitchDTO;
import org.uu.common.pay.req.*;
import org.uu.wallet.entity.ControlSwitch;
import org.uu.wallet.mapper.ControlSwitchMapper;
import org.uu.wallet.service.IControlSwitchService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 后台控制开关表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-03-21
 */
@Service
public class ControlSwitchServiceImpl extends ServiceImpl<ControlSwitchMapper, ControlSwitch> implements IControlSwitchService {

    @Override
    public RestResult<ControlSwitchDTO> createControlSwitch(ControlSwitchReq req) {
        // 参数判断
//        if (!paramsCheck(req)) {
//            return RestResult.failed(ResultCode.PARAM_IS_NOT_EMPTY);
//        }
        // 检查是否存在相同 开关id的 启用的 开关
        if (sameActiveSwitchCheck(req.getSwitchId())) {
            return RestResult.failed(ResultCode.SWITCH_IS_ACTIVATED);
        }
        // 创建
        ControlSwitch controlSwitch = new ControlSwitch();
        BeanUtils.copyProperties(req, controlSwitch);
        int insert = baseMapper.insert(controlSwitch);
        if (insert == 1) {
            return RestResult.ok(change(controlSwitch));
        }
        return RestResult.failed();
    }

    @Override
    public RestResult<ControlSwitchDTO> updateControlSwitchInfo(ControlSwitchUpdateReq req) {
//        if (!paramsCheck(req)) {
//            return RestResult.failed(ResultCode.PARAM_IS_NOT_EMPTY);
//        }
        ControlSwitch controlSwitch = new ControlSwitch();
        BeanUtils.copyProperties(req, controlSwitch);
        return updateSwitch(controlSwitch);
    }

    @Override
    public RestResult<ControlSwitchDTO> updateControlSwitchStatus(ControlSwitchStatusReq req) {
        ControlSwitch controlSwitch = new ControlSwitch();
        BeanUtils.copyProperties(req, controlSwitch);
        return updateSwitch(controlSwitch);
    }

    @Override
    public RestResult<ControlSwitchDTO> detail(ControlSwitchIdReq req) {
        ControlSwitch one = lambdaQuery().eq(ControlSwitch::getSwitchId, req.getSwitchId()).one();
        return RestResult.ok(change(one));
    }

    @Override
    public PageReturn<ControlSwitchDTO> listPage(ControlSwitchPageReq req) {
        Page<ControlSwitch> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<ControlSwitch> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(ControlSwitch::getCreateTime);
        page = baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<ControlSwitchDTO> resultList = new ArrayList<>();
        List<ControlSwitch> records = page.getRecords();
        for (ControlSwitch record : records) {
            ControlSwitchDTO change = change(record);
            resultList.add(change);
        }
        return PageUtils.flush(page, resultList);
    }

    /**
     * 检查指定开关是否开启
     *
     * @param switchId
     * @return boolean
     */
    @Override
    public boolean isSwitchEnabled(Long switchId) {
        int count = lambdaQuery()
                .eq(ControlSwitch::getSwitchId, switchId)
                .eq(ControlSwitch::getStatus, 1)// 状态为启用
                .count();

        // 如果记录数量大于0，则表示开关已启用
        return count > 0;
    }

    private RestResult<ControlSwitchDTO> updateSwitch(ControlSwitch controlSwitch) {
        UpdateWrapper<ControlSwitch> wrapper = new UpdateWrapper<>();
        wrapper.eq("switch_id", controlSwitch.getSwitchId());
        int i = baseMapper.update(controlSwitch, wrapper);
        if (i == 1) {
            return RestResult.ok(change(controlSwitch));
        }
        return RestResult.failed();
    }


//    private boolean paramsCheck(ControlSwitchReq req) {
//        return Objects.nonNull(req.getSwitchId()) &&
//                Objects.nonNull(req.getSwitchName()) && StringUtils.isNotBlank(req.getSwitchName());
//    }
//
//    private boolean paramsCheck(ControlSwitchUpdateReq req) {
//        return Objects.nonNull(req.getSwitchId()) &&
//                Objects.nonNull(req.getSwitchName()) && StringUtils.isNotBlank(req.getSwitchName());
//    }

    private boolean sameActiveSwitchCheck(long switchId) {
        ControlSwitch one = lambdaQuery().eq(ControlSwitch::getSwitchId, switchId).eq(ControlSwitch::getStatus, 1).one();
        return Objects.isNull(one);
    }

    private ControlSwitchDTO change(ControlSwitch controlSwitch) {
        ControlSwitchDTO controlSwitchDTO = new ControlSwitchDTO();
        BeanUtils.copyProperties(controlSwitch, controlSwitchDTO);
        return controlSwitchDTO;
    }
}
