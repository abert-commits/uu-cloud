package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AnnouncementDTO;
import org.uu.common.pay.dto.AnnouncementListPageDTO;
import org.uu.common.pay.dto.MarqueeListPageDTO;
import org.uu.common.pay.req.MarqueeReq;

import java.util.List;

/**
 * @author afei
 */
@FeignClient(value = "uu-wallet", contextId = "marquee")
public interface MarqueeFeignClient {


    /**
     * 新增跑马灯
     *
     * @param req
     * @return {@link RestResult}
     */
    @PostMapping("/marquee/addMarquee")
    RestResult addMarquee(@RequestBody MarqueeReq req);

    /**
     * 根据id获取公告信息
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementDTO}>
     */
    @GetMapping("/marquee/getMarqueeById/{id}")
    RestResult<MarqueeListPageDTO> getMarqueeById(@PathVariable("id") Long id);

    /**
     * 更新公告信息
     *
     * @param id
     * @param req
     * @return {@link RestResult}
     */
    @PostMapping("/marquee/updateMarquee/{id}")
    RestResult updateMarquee(@PathVariable("id") Long id, @RequestBody MarqueeReq req);

    /**
     * 删除公告信息
     *
     * @param id
     * @return {@link RestResult}
     */
    @DeleteMapping("/marquee/{id}")
    RestResult deleteMarquee(@PathVariable("id") Long id);

    /**
     * 禁用、启用
     *
     * @param id
     * @param status
     * @return {@link RestResult}
     */
    @PostMapping("/marquee/changeStatusMarquee/{id}/{status}")
    RestResult changeStatusMarquee(@PathVariable("id") Long id, @PathVariable("status") Integer status);


    /**
     * 分页查询跑马灯列表
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link PageReturn}<{@link AnnouncementListPageDTO}>>
     */
    @PostMapping("/marquee/listHomeMarquees")
    RestResult<List<MarqueeListPageDTO>> listHomeMarquees(@RequestBody(required = false) PageRequest pageRequest);
}
