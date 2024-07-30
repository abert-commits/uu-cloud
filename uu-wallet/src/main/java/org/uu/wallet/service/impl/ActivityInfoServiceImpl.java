package org.uu.wallet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.ActivityInfoDTO;
import org.uu.common.pay.dto.AnnouncementDTO;
import org.uu.wallet.entity.ActivityInfo;
import org.uu.wallet.entity.Announcement;
import org.uu.wallet.mapper.ActivityInfoMapper;
import org.uu.wallet.service.IActivityInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.uu.wallet.vo.ActivityInfoVo;
import org.uu.wallet.vo.AnnouncementVo;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  活动服务实现类
 * </p>
 *
 * @author 
 * @since 2024-07-11
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements IActivityInfoService {

    @Override
    public RestResult<PageReturn<ActivityInfoVo>> getActivityInfoList(PageRequest req) {
        
        //判断分页查询请求参数对象是否为空 为空初始化
        if (req == null) {
            req = new PageRequest();
        }

        //初始化出一个分页对象
        Page<ActivityInfo> pageActivityInfo = new Page<>();
        pageActivityInfo.setCurrent(req.getPageNo());
        pageActivityInfo.setSize(req.getPageSize());

        //初始化一个lambda查询对象

        LambdaQueryChainWrapper<ActivityInfo> lambdaQuery = lambdaQuery();

        //匹配启用状态的活动
        lambdaQuery.eq(ActivityInfo::getStatus, 1);

        //获取未删除的条目 并根据 排序序号进行排序 (数字小排前面)
        lambdaQuery.eq(ActivityInfo::getDeleted, 0).orderByAsc(ActivityInfo::getSortOrder);

        //使用lambdaQuery构建的查询条件，对数据库进行分页查询，结果存储在pageActivityInfo中
        baseMapper.selectPage(pageActivityInfo, lambdaQuery.getWrapper());

        //从 pageActivityInfo 中获取记录，并将其存储到 records 列表中
        List<ActivityInfo> records = pageActivityInfo.getRecords();

        //根据 pageActivityInfo 和 records 生成一个 PageReturn<ActivityInfo> 对象
        PageReturn<ActivityInfo> flush = PageUtils.flush(pageActivityInfo, records);

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<ActivityInfoVo> activityInfoVoList = new ArrayList<>();

        for (ActivityInfo activityInfo : flush.getList()) {

            ActivityInfoVo activityInfoVo = new ActivityInfoVo();

            activityInfoVo.setId(activityInfo.getId());
            activityInfoVo.setActivityTitle(activityInfo.getActivityTitle());
            activityInfoVo.setActivityContent(activityInfo.getActivityContent());
            activityInfoVo.setCoverImageUrl(activityInfo.getCoverImageUrl());
            activityInfoVo.setCreateTime(activityInfo.getCreateTime());
            activityInfoVoList.add(activityInfoVo);
        }

        PageReturn<ActivityInfoVo> activityInfoVoListPageReturn = new PageReturn<>();
        activityInfoVoListPageReturn.setPageNo(flush.getPageNo());
        activityInfoVoListPageReturn.setPageSize(flush.getPageSize());
        activityInfoVoListPageReturn.setTotal(flush.getTotal());
        activityInfoVoListPageReturn.setList(activityInfoVoList);

        return RestResult.ok(activityInfoVoListPageReturn);
    }

    /**
     * 根据id获取活动详情页
     *
     * @param id
     * @return {@link RestResult}<{@link ActivityInfoVo}>
     */
    @Override
    public RestResult<ActivityInfoVo> findActivityInfoDetail(Long id) {
        ActivityInfo activityInfo = lambdaQuery()
                .eq(ActivityInfo::getId, id)
                .eq(ActivityInfo::getStatus, 1)
                .eq(ActivityInfo::getDeleted, 0)
                .one();

        if (activityInfo != null) {
            ActivityInfoVo activityInfoVo = new ActivityInfoVo();
            BeanUtils.copyProperties(activityInfo, activityInfoVo);

            return RestResult.ok(activityInfoVo);
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }
}
