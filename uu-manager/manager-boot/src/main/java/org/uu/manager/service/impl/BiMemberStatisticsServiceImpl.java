package org.uu.manager.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.manager.entity.BiMemberStatistics;
import org.uu.manager.entity.BiMerchantPayOrderMonth;
import org.uu.manager.entity.BiMerchantStatistics;
import org.uu.manager.mapper.BiMemberStatisticsMapper;
import org.uu.manager.service.IBiMemberStatisticsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * <p>
 * 会员统计报表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-09
 */
@Service
public class BiMemberStatisticsServiceImpl extends ServiceImpl<BiMemberStatisticsMapper, BiMemberStatistics> implements IBiMemberStatisticsService {

    @Override
    public List<BiMemberStatistics> listPage() {

        LambdaQueryChainWrapper<BiMemberStatistics> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(BiMemberStatistics::getDateTime).last(" limit 1");

        List<BiMemberStatistics> result = baseMapper.selectList(lambdaQuery.getWrapper());
        return result;
    }
}
