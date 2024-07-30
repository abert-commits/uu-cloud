package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CustomerServiceSystemsDTO;
import org.uu.common.pay.dto.CustomerServiceSystemsListPageDTO;
import org.uu.common.pay.dto.CustomerServiceTypesDTO;
import org.uu.common.pay.req.CustomerServiceSystemsReq;
import org.uu.wallet.entity.BannerInfo;
import org.uu.wallet.entity.CustomerServiceSystems;
import org.uu.wallet.vo.CurrentCustomerServiceSystemVo;

import java.util.List;

/**
 * <p>
 * 客服系统配置表 服务类
 * </p>
 *
 * @author
 * @since 2024-04-20
 */
public interface ICustomerServiceSystemsService extends IService<CustomerServiceSystems> {

    /**
     * 新增 客服系统
     *
     * @param req
     * @return boolean
     */
    RestResult createCustomerServiceSystems(CustomerServiceSystemsReq req);


    /**
     * 根据ID查询客服系统信息
     *
     * @param id
     * @return {@link BannerInfo}
     */
    RestResult<CustomerServiceSystemsDTO> getCustomerServiceSystemsById(Long id);


    /**
     * 修改 客服系统
     *
     * @param id
     * @param req
     * @return boolean
     */
    RestResult updateCustomerServiceSystems(Long id, CustomerServiceSystemsReq req);


    /**
     * 删除 客服系统
     *
     * @param id
     * @return boolean
     */
    boolean deleteCustomerServiceSystems(Long id);


    /**
     * 禁用 客服系统
     *
     * @param id
     * @return boolean
     */
    boolean disableCustomerServiceSystems(Long id);


    /**
     * 启用 客服系统
     *
     * @param id
     * @return boolean
     */
    boolean enableCustomerServiceSystems(Long id);


    /**
     * 分页查询 客服系统列表
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link PageReturn}<{@link CustomerServiceSystemsListPageDTO}>>
     */
    RestResult<PageReturn<CustomerServiceSystemsListPageDTO>> listPage(PageRequest pageRequest);


    /**
     * 获取当前客服系统
     *
     * @return {@link RestResult}<{@link CurrentCustomerServiceSystemVo}>
     */
    RestResult<CurrentCustomerServiceSystemVo> getCurrentCustomerServiceSystem();

    /**
     * 获取客服系统类型列表
     *
     * @return {@link RestResult}<{@link List}<{@link CustomerServiceTypesDTO}>>
     */
    RestResult<List<CustomerServiceTypesDTO>> getCustomerServiceTypes();

}
