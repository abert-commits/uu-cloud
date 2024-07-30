package org.uu.wallet.service;

import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.ActivityInfoDTO;
import org.uu.wallet.entity.ActivityInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.wallet.vo.ActivityInfoVo;

/**
 * <p>
 *  活动服务类
 * </p>
 *
 * @author 
 * @since 2024-07-11
 */
public interface IActivityInfoService extends IService<ActivityInfo> {

    /**
     * 获取活动列表
     * @param pageRequest
     * @return
     */
    RestResult<PageReturn<ActivityInfoVo>> getActivityInfoList(PageRequest pageRequest);

    /**
     * 根据id查询活动信息
     * @param id
     * @return
     */
    RestResult<ActivityInfoVo> findActivityInfoDetail(Long id);
}
