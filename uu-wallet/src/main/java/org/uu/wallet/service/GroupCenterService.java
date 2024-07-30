package org.uu.wallet.service;


import org.uu.common.core.result.RestResult;
import org.uu.common.pay.vo.request.MyGroupRequestVO;
import org.uu.common.pay.vo.response.GroupCenterResponseVO;
import org.uu.common.pay.vo.response.MyGroupFilterBoxResponseVO;
import org.uu.common.pay.vo.response.MyGroupResponseVO;

import java.util.List;

public interface GroupCenterService {
    /**
     * 团队中心
     *
     * @param days 天数 小于等于0或为null 查询全部
     * @return {@link RestResult}<{@link GroupCenterResponseVO}>
     */
    RestResult<GroupCenterResponseVO> index(Integer days);

    /**
     * 我的团队
     *
     * @param requestVO 请求参数
     * @return {@link RestResult<MyGroupResponseVO>}
     */
    RestResult<MyGroupResponseVO> myGroup(MyGroupRequestVO requestVO);

    /**
     * 我的团队Channel筛选框
     */
    RestResult<List<MyGroupFilterBoxResponseVO>> filterBox();
}
