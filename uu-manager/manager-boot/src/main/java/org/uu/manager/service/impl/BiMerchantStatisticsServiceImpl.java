package org.uu.manager.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.manager.entity.BiMerchantPayOrderMonth;
import org.uu.manager.entity.BiMerchantStatistics;
import org.uu.manager.mapper.BiMerchantStatisticsMapper;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.service.IBiMerchantStatisticsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * <p>
 * 商户统计报表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-09
 */
@Service
public class BiMerchantStatisticsServiceImpl extends ServiceImpl<BiMerchantStatisticsMapper, BiMerchantStatistics> implements IBiMerchantStatisticsService {

    @Override
    public List<BiMerchantStatistics> listPage(MerchantDailyReportReq req) {
        LambdaQueryChainWrapper<BiMerchantStatistics> lambdaQuery = lambdaQuery();
        String dateStr = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-1), GlobalConstants.DATE_FORMAT_DAY);
        lambdaQuery.orderByDesc(BiMerchantStatistics::getDateTime, BiMerchantStatistics::getMemberNum);
        lambdaQuery.eq(BiMerchantStatistics::getDateTime, dateStr);
        List<BiMerchantStatistics> resultList = baseMapper.selectList(lambdaQuery.getWrapper());

        return resultList;
    }
}
