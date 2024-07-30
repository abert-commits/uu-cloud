package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MarqueeListPageDTO;
import org.uu.common.pay.req.MarqueeReq;
import org.uu.wallet.entity.Marquee;
import org.uu.wallet.vo.MarqueeVo;

import java.util.List;

public interface IMarqueeService extends IService<Marquee> {

    /**
     * 获取跑马灯列表
     *
     * @return
     */
    RestResult<List<MarqueeVo>> getMarqueeList();

    /**
     * 分页查询 首页跑马灯
     */
    PageReturn<MarqueeListPageDTO> listHomeMarquees(PageRequest pageRequest);

    /**
     * 启用、禁用跑马灯
     *
     * @param id
     * @return boolean
     */
    boolean changeStatusMarquee(Long id, Integer status);

    /**
     * 新增跑马灯
     *
     * @param req
     * @return
     */
    RestResult addMarquee(MarqueeReq req);

    /**
     * 根据id获取跑马灯信息
     */
    RestResult<MarqueeListPageDTO> getMarqueeById(Long id);

    /**
     * 更新 跑马灯信息
     *
     * @param id
     * @param req
     * @return {@link RestResult}
     */
    RestResult updateMarquee(Long id, MarqueeReq req);

    /**
     * 删除跑马灯
     *
     * @param id
     * @return boolean
     */
    boolean deleteMarquee(Long id);
}
