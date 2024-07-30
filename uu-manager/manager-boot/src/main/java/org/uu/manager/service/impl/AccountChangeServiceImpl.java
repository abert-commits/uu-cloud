//package org.uu.manager.service.impl;
//
//import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import org.uu.common.core.page.PageReturn;
//import org.uu.common.mybatis.util.PageUtils;
//import org.uu.manager.entity.AccountChange;
//import org.uu.manager.entity.MerchantInfo;
//import org.uu.manager.mapper.AccountChangeMapper;
//import org.uu.manager.req.MerchantInfoReq;
//import org.uu.manager.service.IAccountChangeService;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
///**
// * @author
// */
//@Service
//public class AccountChangeServiceImpl extends ServiceImpl<AccountChangeMapper, AccountChange> implements IAccountChangeService {
//
//    @Override
//    public PageReturn<MerchantInfo> listPage(MerchantInfoReq req) {
//        Page<MerchantInfo> page = new Page<>();
//        page.setCurrent(req.getPageNo());
//        page.setSize(req.getPageSize());
//        LambdaQueryChainWrapper<MerchantInfo> lambdaQuery = lambdaQuery();
//        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getCode())) {
//            lambdaQuery.eq(MerchantInfo::getCode, req.getCode());
//        }
//        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getUsername())) {
//            lambdaQuery.eq(MerchantInfo::getUsername, req.getUsername());
//        }
//        baseMapper.selectPage(page, lambdaQuery.getWrapper());
//        List<MerchantInfo> records = page.getRecords();
//        return PageUtils.flush(page, records);
//    }
//
//}
