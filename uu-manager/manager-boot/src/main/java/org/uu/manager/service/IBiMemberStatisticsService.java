package org.uu.manager.service;

import org.uu.manager.entity.BiMemberStatistics;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 会员统计报表 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-09
 */
public interface IBiMemberStatisticsService extends IService<BiMemberStatistics> {

    List<BiMemberStatistics> listPage();
}
