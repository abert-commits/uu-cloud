package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.TradeConfigSchemeDTO;
import org.uu.common.pay.req.TradeConfigSchemeListPageReq;
import org.uu.common.pay.req.TradeConfigSchemeReq;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.TradeConfigScheme;
import org.uu.wallet.mapper.TradeConfigSchemeMapper;
import org.uu.wallet.service.ITradeConfigSchemeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 交易配置方案表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-03-18
 */
@Service
@RequiredArgsConstructor
public class TradeConfigSchemeServiceImpl extends ServiceImpl<TradeConfigSchemeMapper, TradeConfigScheme> implements ITradeConfigSchemeService {

    private  final WalletMapStruct walletMapStruct;

    @Override
    public PageReturn<TradeConfigSchemeDTO> listPage(TradeConfigSchemeListPageReq req) {
        Page<TradeConfigScheme> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<TradeConfigScheme> lambdaQuery = lambdaQuery();
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<TradeConfigScheme> records = page.getRecords();
        List<TradeConfigSchemeDTO> listDTO = walletMapStruct.TradeConfigSchemeTransform(records);
        return PageUtils.flush(page, listDTO);
    }

    @Override
    public TradeConfigSchemeDTO getDetail(Long id) {
        TradeConfigScheme tradeConfigScheme = baseMapper.selectById(id);
        TradeConfigSchemeDTO tradeConfigSchemeDTO = new TradeConfigSchemeDTO();
        BeanUtils.copyProperties(tradeConfigScheme, tradeConfigSchemeDTO);
        return tradeConfigSchemeDTO;
    }

    @Override
    public TradeConfigSchemeDTO updateScheme(TradeConfigSchemeReq req) {
        TradeConfigScheme updateData = new TradeConfigScheme();
        BeanUtils.copyProperties(req, updateData);
        baseMapper.updateById(updateData);
        TradeConfigSchemeDTO dto = new TradeConfigSchemeDTO();
        BeanUtils.copyProperties(updateData, dto);
        return dto;
    }


    /**
     * 根据标签获取方案配置
     *
     * @param schemeTag
     * @return {@link TradeConfigScheme}
     */
    @Override
    public TradeConfigScheme getSchemeConfigByTag(String schemeTag) {
        return lambdaQuery().eq(TradeConfigScheme::getSchemeTag, schemeTag).last("LIMIT 1").one();
    }
}
