package org.uu.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.utils.AssertUtil;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.*;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.entity.BiOverViewStatisticsDaily;
import org.uu.manager.entity.MerchantInfo;
import org.uu.manager.mapper.ManagerMerchantInfoMapper;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.req.MerchantInfoReq;
import org.uu.manager.service.*;
import org.uu.manager.vo.MerchantInfoVo;
import org.uu.manager.vo.MerchantNameListVo;

import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class MerchantInfoServiceImpl extends ServiceImpl<ManagerMerchantInfoMapper, MerchantInfo> implements IMerchantInfoService {
    private final PasswordEncoder passwordEncoder;

//    private final AdminMapStruct adminMapStruct;
//    private final ICollectionOrderService collectionOrderService;
//    private final IPaymentOrderService paymentOrderService;
    private final IBiMerchantPayOrderDailyService iBiMerchantPayOrderDailyService;
    private final IBiMerchantWithdrawOrderDailyService iBiMerchantWithdrawOrderDailyService;
    private final IBiOverViewStatisticsDailyService iBiOverViewStatisticsDailyService;




    @Override
    public PageReturn<MerchantInfo> listPage(MerchantInfoReq req) {
        Page<MerchantInfo> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<MerchantInfo> lambdaQuery = lambdaQuery();
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getCode())) {
            lambdaQuery.eq(MerchantInfo::getCode, req.getCode());
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getUsername())) {
            lambdaQuery.eq(MerchantInfo::getUsername, req.getUsername());
        }
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<MerchantInfo> records = page.getRecords();
        return PageUtils.flush(page, records);
    }

    @Override
    public List<MerchantInfo> getAllMerchantByStatus() {
        LambdaQueryChainWrapper<MerchantInfo> lambdaQuery = lambdaQuery();
        List<MerchantInfo> list = lambdaQuery().eq(MerchantInfo::getStatus, "1").list();
        return list;
    }


    @Override
    public String getMd5KeyByCode(String merchantCode) {
        QueryWrapper<MerchantInfo> MerchantInfoQueryWrapper = new QueryWrapper<>();
        MerchantInfoQueryWrapper.select("md5_key").eq("code", merchantCode);
        return getOne(MerchantInfoQueryWrapper).getMd5Key();
    }



    @Override
    public boolean getIp(String code, String addr) {
        MerchantInfo merchantInfo = lambdaQuery().eq(MerchantInfo::getCode, code).one();
        if (merchantInfo != null && !StringUtils.isNullOrEmpty(merchantInfo.getWhiteList())) {
            String whiteStr = merchantInfo.getWhiteList();
            List<String> list = Arrays.asList(",");
            if (list.contains(addr)) return true;
        }
        return false;
    }

    @Override
    public MerchantInfo getMerchantInfoByCode(String code) {
        MerchantInfo merchantInfo = lambdaQuery().eq(MerchantInfo::getCode, code).one();
        return merchantInfo;
    }


    @Override
    public List<MerchantNameListVo> getMerchantNameList() {
        //获取当前商户id
        Long currentUserId = UserContext.getCurrentUserId();
        //查询当前商户名称和商户号
        QueryWrapper<MerchantInfo> merchantInfoQueryWrapper = new QueryWrapper<>();
        merchantInfoQueryWrapper.select("code", "username").eq("id", currentUserId);
        List<Map<String, Object>> maps = listMaps(merchantInfoQueryWrapper);
        ArrayList<MerchantNameListVo> merchantNameListVos = new ArrayList<>();

        for (Map<String, Object> map : maps) {
            MerchantNameListVo merchantNameListVo = new MerchantNameListVo();
            merchantNameListVo.setValue(String.valueOf(map.get("code")));
            merchantNameListVo.setLabel(String.valueOf(map.get("username")));
            merchantNameListVos.add(merchantNameListVo);
        }
        return merchantNameListVos;
    }


    @Override
    public MerchantInfoVo currentMerchantInfo() {
        Long currentUserId = UserContext.getCurrentUserId();
        AssertUtil.notEmpty(currentUserId, ResultCode.RELOGIN);
        MerchantInfo merchantInfo = userDetail(currentUserId);
        //MerchantInfo merchantInfo = lambdaQuery().eq(MerchantInfo::getId, currentUserId).one();
        AssertUtil.notEmpty(merchantInfo, ResultCode.USERNAME_OR_PASSWORD_ERROR);
        // 查询绑定的菜单


        MerchantInfoVo merchantInfoVo = new MerchantInfoVo();
        BeanUtils.copyProperties(merchantInfo, merchantInfoVo);




        return merchantInfoVo;
    }



    @Override
    public MerchantInfo userDetail(Long userId) {
        MerchantInfo merchantInfo = lambdaQuery().eq(MerchantInfo::getId, userId).one();
        AssertUtil.notEmpty(merchantInfo, ResultCode.USERNAME_OR_PASSWORD_ERROR);

        // 查询绑定的角色IDs


        return merchantInfo;
    }


    @Override
    public UserAuthDTO getByUsername(String username) {
        UserAuthDTO userAuthDTO = this.baseMapper.getByUsername(username);
        return userAuthDTO;
    }

    @Override
    @SneakyThrows
    public RestResult<BiOverViewStatisticsDailyDTO> getMerchantOrderOverview(MerchantDailyReportReq req) {
        BiOverViewStatisticsDailyDTO dto = iBiOverViewStatisticsDailyService.getDataByDate(req.getStartTime(), req.getEndTime());
        return RestResult.ok(dto);
    }
}
