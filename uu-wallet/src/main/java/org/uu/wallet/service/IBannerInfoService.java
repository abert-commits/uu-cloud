package org.uu.wallet.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BannerInfoDTO;
import org.uu.common.pay.dto.BannerInfoListPageDTO;
import org.uu.common.pay.req.BannerInfoReq;
import org.uu.common.pay.req.BannerPageReq;
import org.uu.wallet.entity.BannerInfo;
import org.uu.wallet.vo.BannerListVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Banner信息表 服务类
 * </p>
 *
 * @author
 * @since 2024-02-28
 */
public interface IBannerInfoService extends IService<BannerInfo> {

    /**
     * 新增 Banner
     *
     * @param req
     * @return boolean
     */
    RestResult createBanner(BannerInfoReq req);


    /**
     * 根据ID查询Banner信息
     *
     * @param id
     * @return {@link BannerInfo}
     */
    RestResult<BannerInfoDTO> getBannerById(Long id);


    /**
     * 修改 Banner
     *
     * @param id
     * @param req
     * @return boolean
     */
    RestResult updateBanner(Long id, BannerInfoReq req);


    /**
     * 删除 Banner
     *
     * @param id
     * @return boolean
     */
    boolean deleteBanner(Long id);


    /**
     * 禁用 Banner
     *
     * @param id
     * @return boolean
     */
    boolean disableBanner(Long id);


    /**
     * 启用 Banner
     *
     * @param id
     * @return boolean
     */
    boolean enableBanner(Long id);


    /**
     * 分页查询 banner列表
     *
     * @param pageRequest
     * @return {@link PageReturn}<{@link BannerInfoListPageDTO}>
     */
    RestResult<PageReturn<BannerInfoListPageDTO>> listPage(BannerPageReq pageRequest);

    /**
     * 获取 Banner列表
     *
     * @return {@link RestResult}<{@link BannerListVo}>
     */
    RestResult<Map<String, List<BannerListVo>>> getBannerList();
}
