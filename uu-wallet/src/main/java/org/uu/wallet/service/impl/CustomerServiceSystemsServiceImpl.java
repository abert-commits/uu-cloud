package org.uu.wallet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.CustomerServiceSystemsDTO;
import org.uu.common.pay.dto.CustomerServiceSystemsListPageDTO;
import org.uu.common.pay.dto.CustomerServiceTypesDTO;
import org.uu.common.pay.req.CustomerServiceSystemsReq;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.CustomerServiceEnum;
import org.uu.wallet.entity.BannerInfo;
import org.uu.wallet.entity.CustomerServiceSystems;
import org.uu.wallet.entity.CustomerServiceTypes;
import org.uu.wallet.mapper.CustomerServiceSystemsMapper;
import org.uu.wallet.service.ICustomerServiceSystemsService;
import org.uu.wallet.service.ICustomerServiceTypesService;
import org.uu.wallet.vo.CurrentCustomerServiceSystemVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 客服系统配置表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-04-20
 */
@Service
@Slf4j
public class CustomerServiceSystemsServiceImpl extends ServiceImpl<CustomerServiceSystemsMapper, CustomerServiceSystems> implements ICustomerServiceSystemsService {

    @Value("${oss.baseUrl}")
    private String baseUrl;

    @Autowired
    private ICustomerServiceTypesService customerServiceTypesService;

    /**
     * 新增 客服系统
     *
     * @param req
     * @return boolean
     */
    @Override
    public RestResult createCustomerServiceSystems(CustomerServiceSystemsReq req) {

        // 检查是否存在相同的排序值
        int count = lambdaQuery()
                .eq(CustomerServiceSystems::getSortOrder, req.getSortOrder())
                .eq(CustomerServiceSystems::getDeleted, 0)
                .count();
        if (count > 0) {
            //排序值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        CustomerServiceSystems customerServiceSystems = new CustomerServiceSystems(); // 转换req到entity


        BeanUtils.copyProperties(req, customerServiceSystems);

        //根据客服系统类型设置客服系统名称
        customerServiceSystems.setServiceSystemName(CustomerServiceEnum.getNameByCode(String.valueOf(customerServiceSystems.getType())));

        if (StringUtils.isNotEmpty(customerServiceSystems.getIconUrl())) {
            //拼接icon图片链接
            customerServiceSystems.setIconUrl(baseUrl + customerServiceSystems.getIconUrl());
        }


        // 设置bannerInfo的属性
        if (save(customerServiceSystems)) {
            return RestResult.ok();
        }

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }


    /**
     * 根据ID查询客服系统信息
     *
     * @param id
     * @return {@link BannerInfo}
     */
    @Override
    public RestResult<CustomerServiceSystemsDTO> getCustomerServiceSystemsById(Long id) {

        CustomerServiceSystems customerServiceSystems = lambdaQuery()
                .eq(CustomerServiceSystems::getId, id)
                .eq(CustomerServiceSystems::getDeleted, 0)
                .one();

        if (customerServiceSystems != null) {
            CustomerServiceSystemsDTO customerServiceSystemsDTO = new CustomerServiceSystemsDTO();
            BeanUtils.copyProperties(customerServiceSystems, customerServiceSystemsDTO);

            return RestResult.ok(customerServiceSystemsDTO);
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    /**
     * 更新 客服系统信息
     *
     * @param id
     * @param req
     * @return boolean
     */
    @Override
    public RestResult updateCustomerServiceSystems(Long id, CustomerServiceSystemsReq req) {

        // 检查是否存在相同的排序值且不是当前正在更新的客服系统
        int count = lambdaQuery()
                .eq(CustomerServiceSystems::getSortOrder, req.getSortOrder())
                .ne(CustomerServiceSystems::getId, id) // 排除当前正在更新的banner
                .eq(CustomerServiceSystems::getDeleted, 0)
                .count();
        if (count > 0) {
            //排序值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        String iconUrl = null;

        if (StringUtils.isNotEmpty(req.getIconUrl())) {
            // 检查iconUrl是否以"http"开头
            iconUrl = req.getIconUrl();

            if (iconUrl != null && !iconUrl.startsWith("https://")) {
                // 如果不是以"http"开头，则进行拼接
                iconUrl = baseUrl + iconUrl;
            }
        }

        CustomerServiceSystems customerServiceSystems = getById(id);
        if (customerServiceSystems != null) {
            // 修改客服系统的属性
            customerServiceSystems.setType(req.getType());
            customerServiceSystems.setSortOrder(req.getSortOrder());
            customerServiceSystems.setServiceSystemUrl(req.getServiceSystemUrl());

            if (StringUtils.isNotEmpty(iconUrl)){
                customerServiceSystems.setIconUrl(iconUrl);
            }

            //根据客服系统类型设置客服系统名称
            customerServiceSystems.setServiceSystemName(CustomerServiceEnum.getNameByCode(String.valueOf(req.getType())));

            boolean update = updateById(customerServiceSystems);

            return update ? RestResult.ok() : RestResult.failed();
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    /**
     * 删除 客服系统
     *
     * @param id
     * @return boolean
     */
    @Override
    public boolean deleteCustomerServiceSystems(Long id) {
        return lambdaUpdate().eq(CustomerServiceSystems::getId, id).set(CustomerServiceSystems::getDeleted, 1).update();
    }

    /**
     * 禁用 客服系统
     *
     * @param id
     * @return boolean
     */
    @Override
    public boolean disableCustomerServiceSystems(Long id) {

        CustomerServiceSystems customerServiceSystems = getById(id);

        if (customerServiceSystems != null) {
            customerServiceSystems.setActive(0); // 0为禁用状态
            return updateById(customerServiceSystems);
        }
        return false;
    }


    /**
     * 启用 客服系统
     *
     * @param id
     * @return boolean
     */
    @Override
    @Transactional
    public boolean enableCustomerServiceSystems(Long id) {

        // 首先，将所有记录的 active 设为 0
        boolean update = update(new UpdateWrapper<CustomerServiceSystems>().set("active", 0));

        CustomerServiceSystems customerServiceSystems = getById(id);
        if (customerServiceSystems != null) {
            customerServiceSystems.setActive(1); // 1为启用状态
            if (update) {
                return updateById(customerServiceSystems);
            }
        }

        log.error("启用 客服系统失败");
        return false;
    }


    /**
     * 分页查询 客服系统列表
     *
     * @param req
     * @return {@link RestResult}<{@link PageReturn}<{@link CustomerServiceSystemsListPageDTO}>>
     */
    @Override
    public RestResult<PageReturn<CustomerServiceSystemsListPageDTO>> listPage(PageRequest req) {

        //获取当前用户id
        Long currentUserId = UserContext.getCurrentUserId();

        if (currentUserId == null) {
            log.error("分页查询 客服系统列表失败: 获取当前用户id失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        if (req == null) {
            req = new PageRequest();
        }

        Page<CustomerServiceSystems> pageCustomerServiceSystems = new Page<>();
        pageCustomerServiceSystems.setCurrent(req.getPageNo());
        pageCustomerServiceSystems.setSize(req.getPageSize());

        LambdaQueryChainWrapper<CustomerServiceSystems> lambdaQuery = lambdaQuery();


        //获取未删除的条目 并根据 序号进行排序 (数字小排前面)
        lambdaQuery.eq(CustomerServiceSystems::getDeleted, 0).orderByAsc(CustomerServiceSystems::getSortOrder);

        baseMapper.selectPage(pageCustomerServiceSystems, lambdaQuery.getWrapper());

        List<CustomerServiceSystems> records = pageCustomerServiceSystems.getRecords();

        PageReturn<CustomerServiceSystems> flush = PageUtils.flush(pageCustomerServiceSystems, records);

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<CustomerServiceSystemsListPageDTO> customerServiceSystemsListPageDTOList = new ArrayList<>();

        for (CustomerServiceSystems customerServiceSystems : flush.getList()) {

            CustomerServiceSystemsListPageDTO customerServiceSystemsListPageDTO = new CustomerServiceSystemsListPageDTO();


            BeanUtil.copyProperties(customerServiceSystems, customerServiceSystemsListPageDTO);

            //最后更新时间
            if (customerServiceSystemsListPageDTO.getUpdateTime() == null) {
                customerServiceSystemsListPageDTO.setUpdateTime(customerServiceSystems.getCreateTime());
            }

            //操作人
            if (StringUtils.isEmpty(customerServiceSystemsListPageDTO.getUpdateBy())) {
                customerServiceSystemsListPageDTO.setUpdateBy(customerServiceSystems.getCreateBy());
            }
            customerServiceSystemsListPageDTOList.add(customerServiceSystemsListPageDTO);
        }

        PageReturn<CustomerServiceSystemsListPageDTO> customerServiceSystemsListPageDTOPageReturn = new PageReturn<>();
        customerServiceSystemsListPageDTOPageReturn.setPageNo(flush.getPageNo());
        customerServiceSystemsListPageDTOPageReturn.setPageSize(flush.getPageSize());
        customerServiceSystemsListPageDTOPageReturn.setTotal(flush.getTotal());
        customerServiceSystemsListPageDTOPageReturn.setList(customerServiceSystemsListPageDTOList);

        log.info("分页查询 banner列表成功: 用户id: {}, req: {}, 返回数据: {}", currentUserId, req, customerServiceSystemsListPageDTOPageReturn);

        return RestResult.ok(customerServiceSystemsListPageDTOPageReturn);
    }


    /**
     * 获取当前客服系统
     *
     * @return {@link RestResult}<{@link CurrentCustomerServiceSystemVo}>
     */
    @Override
    public RestResult<CurrentCustomerServiceSystemVo> getCurrentCustomerServiceSystem() {

        CustomerServiceSystems customerServiceSystems = lambdaQuery()
                .eq(CustomerServiceSystems::getActive, 1)
                .last("LIMIT 1")
                .one();

        CurrentCustomerServiceSystemVo currentCustomerServiceSystemVo = new CurrentCustomerServiceSystemVo();

        BeanUtils.copyProperties(customerServiceSystems, currentCustomerServiceSystemVo);

        return RestResult.ok(currentCustomerServiceSystemVo);
    }

    /**
     * 获取客服系统类型列表
     *
     * @return {@link RestResult}<{@link List}<{@link CustomerServiceTypesDTO}>>
     */
    @Override
    public RestResult<List<CustomerServiceTypesDTO>> getCustomerServiceTypes() {
        List<CustomerServiceTypes> customerServiceTypesList = customerServiceTypesService.lambdaQuery().list();

        if (customerServiceTypesList == null) {
            customerServiceTypesList = new ArrayList<>();
        }

        ArrayList<CustomerServiceTypesDTO> customerServiceTypesDTOList = new ArrayList<>();

        for (CustomerServiceTypes customerServiceTypes : customerServiceTypesList) {
            CustomerServiceTypesDTO customerServiceTypesDTO = new CustomerServiceTypesDTO();
            BeanUtils.copyProperties(customerServiceTypes, customerServiceTypesDTO);

            customerServiceTypesDTOList.add(customerServiceTypesDTO);
        }

        return RestResult.ok(customerServiceTypesDTOList);
    }
}
