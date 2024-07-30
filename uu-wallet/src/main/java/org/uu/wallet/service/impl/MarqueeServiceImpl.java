package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.MarqueeListPageDTO;
import org.uu.common.pay.req.MarqueeReq;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.Marquee;
import org.uu.wallet.mapper.MarqueeMapper;
import org.uu.wallet.service.IMarqueeService;
import org.uu.wallet.vo.MarqueeVo;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class MarqueeServiceImpl extends ServiceImpl<MarqueeMapper, Marquee> implements IMarqueeService {

    @Resource
    WalletMapStruct mapStruct;

    /**
     * 获取跑马灯列表
     *
     * @return
     */
    @Override
    public RestResult<List<MarqueeVo>> getMarqueeList() {
        List<Marquee> marqueeList = lambdaQuery().orderByDesc(Marquee::getCreateTime).eq(Marquee::getDeleted, 0).list();
        List<MarqueeVo> marqueeVoList = new ArrayList();

        for (Marquee marquee : marqueeList) {
            MarqueeVo marqueeVo = new MarqueeVo();
            marqueeVo.setContent(marquee.getContent());
            marqueeVo.setDirection(marquee.getDirection());
            marqueeVo.setLoopCount(marquee.getLoopCount());
            marqueeVo.setBehavior(marquee.getBehavior());

            marqueeVoList.add(marqueeVo);
        }
        return RestResult.ok(marqueeVoList);
    }

    @Override
    public PageReturn<MarqueeListPageDTO> listHomeMarquees(PageRequest req) {
        Page<Marquee> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<Marquee> lambdaQuery = new LambdaQueryChainWrapper<>(baseMapper);
        //获取未删除的条目 并根据 序号进行排序 (数字小排前面)
        lambdaQuery.eq(Marquee::getDeleted, 0).orderByAsc(Marquee::getSortOrder);
        Page<Marquee> marqueePage = baseMapper.selectPage(page, lambdaQuery.getWrapper());

        List<Marquee> records = marqueePage.getRecords();
        List<MarqueeListPageDTO> dtoList = mapStruct.marqueeToDto(records);
        return PageUtils.flush(page, dtoList);
    }

    @Override
    public boolean changeStatusMarquee(Long id, Integer status) {
        Marquee marquee = getById(id);
        if (Objects.nonNull(marquee)) {
            marquee.setStatus(status); // 1为启用状态
            return updateById(marquee);
        }
        return false;
    }

    @Override
    public RestResult addMarquee(MarqueeReq req) {
        // 检查是否存在相同的排序值
        int count = lambdaQuery()
                .eq(Marquee::getSortOrder, req.getSortOrder())
                .eq(Marquee::getDeleted, 0)
                .count();
        if (count > 0) {
            //排序值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        Marquee marquee = new Marquee();
        BeanUtils.copyProperties(req, marquee);

        if (baseMapper.insert(marquee) > 0) {
            return RestResult.ok();
        }

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    @Override
    public RestResult<MarqueeListPageDTO> getMarqueeById(Long id) {
        Marquee marquee = lambdaQuery()
                .eq(Marquee::getId, id)
                .eq(Marquee::getDeleted, 0)
                .one();

        if (Objects.nonNull(marquee)) {
            MarqueeListPageDTO marqueeDTO = new MarqueeListPageDTO();
            BeanUtils.copyProperties(marquee, marqueeDTO);
            return RestResult.ok(marqueeDTO);
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    @Override
    public RestResult updateMarquee(Long id, MarqueeReq req) {
        // 检查是否存在相同的排序值且不是当前正在更新
        int count = lambdaQuery()
                .eq(Marquee::getSortOrder, req.getSortOrder())
                .ne(Marquee::getId, id)
                .eq(Marquee::getDeleted, 0)
                .count();
        if (count > 0) {
            //排序值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        Marquee marquee = baseMapper.selectById(id);
        if (Objects.nonNull(marquee)) {
            marquee.setContent(req.getContent());
            marquee.setSortOrder(req.getSortOrder());
            marquee.setStatus(req.getStatus());
            boolean update = updateById(marquee);

            return update ? RestResult.ok() : RestResult.failed();
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    @Override
    public boolean deleteMarquee(Long id) {
        return lambdaUpdate().eq(Marquee::getId, id).set(Marquee::getDeleted, 1).update();
    }
}
