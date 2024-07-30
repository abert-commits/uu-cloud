package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.dto.CollectionInfoDTO;
import org.uu.common.pay.req.CollectionInfoIdReq;
import org.uu.common.pay.req.CollectionInfoListPageReq;
import org.uu.common.pay.req.CollectionInfoReq;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.PayTypeEnum;
import org.uu.wallet.entity.CollectionInfo;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.TradeConfig;
import org.uu.wallet.mapper.CollectionInfoMapper;
import org.uu.wallet.mapper.TradeConfigMapper;
import org.uu.wallet.service.ICollectionInfoService;
import org.uu.wallet.service.IMemberInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * @author
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/collectionInfo", "/collectionInfo"})
@Api(description = "收款信息控制器")
@Validated
@ApiIgnore
public class CollectionInfoController {

    private final ICollectionInfoService collectionInfoService;
    private final CollectionInfoMapper collectionInfoMapper;
    private final IMemberInfoService iMemberInfoService;
    private final TradeConfigMapper tradeConfigMapper;


    @PostMapping("/update")
    @ApiOperation(value = "修改会员信息")
    public RestResult<CollectionInfoDTO> update(@RequestBody @ApiParam @Valid CollectionInfoReq collectionInfoReq) {
        if(collectionInfoReq.getType() == Integer.parseInt(PayTypeEnum.INDIAN_CARD.getCode())
            && checkBankCodeLength(collectionInfoReq.getBankCardNumber())
        ){
            return RestResult.failed(ResultCode.WRONG_FORMAT_BANK_CARD_CODE);
        }
        if(collectionInfoReq.getType() == Integer.parseInt(PayTypeEnum.INDIAN_UPI.getCode())
                && collectionInfoService.checkDuplicate(collectionInfoReq.getUpiId(), null, true, collectionInfoReq.getId())){
            return RestResult.failed(ResultCode.DUPLICATE_UPI_ERROR);
        }
        if(collectionInfoReq.getType() == Integer.parseInt(PayTypeEnum.INDIAN_CARD.getCode())
                && collectionInfoService.checkDuplicate(null, collectionInfoReq.getBankCardNumber(), true, collectionInfoReq.getId())){
            return RestResult.failed(ResultCode.DUPLICATE_BANK_ERROR);
        }
        if(collectionInfoReq.getType() == Integer.parseInt(PayTypeEnum.INDIAN_CARD.getCode())
                && collectionInfoService.checkIfscCodeDuplicate( collectionInfoReq.getIfscCode(), true, collectionInfoReq.getId())){
            return RestResult.failed(ResultCode.DUPLICATE_IFSC_CODE_ERROR);
        }

        CollectionInfo collectionInfo = new CollectionInfo();
        BeanUtils.copyProperties(collectionInfoReq, collectionInfo);
        collectionInfo.setDailyLimitCount(collectionInfoReq.getDailyLimitNumber());
        collectionInfoService.updateById(collectionInfo);
        CollectionInfoDTO collectionInfoDTO = new CollectionInfoDTO();
        BeanUtils.copyProperties(collectionInfo, collectionInfoDTO);
        collectionInfoDTO.setDailyLimitNumber(collectionInfo.getDailyLimitCount());
        return RestResult.ok(collectionInfoDTO);
    }


    @PostMapping("/getInfo")
    @ApiOperation(value = "收款信息")
    public RestResult<List<CollectionInfoDTO>> getInfo(@RequestBody @ApiParam @Valid CollectionInfoIdReq collectionInfoIdReq) {
        List<CollectionInfoDTO> list = collectionInfoService.getListByUid(collectionInfoIdReq);
        return RestResult.ok(list);
    }


    @PostMapping("/delete")
    @ApiOperation(value = "删除")
    public RestResult delete(@RequestBody @ApiParam @Valid CollectionInfoIdReq collectionInfoIdReq) {
        CollectionInfo collectionInfo = new CollectionInfo();
        BeanUtils.copyProperties(collectionInfoIdReq, collectionInfo);
        collectionInfo.setDeleted(1);
        collectionInfoService.updateById(collectionInfo);
        return RestResult.ok("删除成功");
    }


    @PostMapping("/add")
    @ApiOperation(value = "新增")
    public RestResult add(@RequestBody @ApiParam @Valid CollectionInfoReq collectionInfoIdReq) {
        if(collectionInfoIdReq.getType() == Integer.parseInt(PayTypeEnum.INDIAN_CARD.getCode())
                && checkBankCodeLength(collectionInfoIdReq.getBankCardNumber())
        ){
            return RestResult.failed(ResultCode.WRONG_FORMAT_BANK_CARD_CODE);
        }
        if(collectionInfoIdReq.getType() == Integer.parseInt(PayTypeEnum.INDIAN_UPI.getCode())
                && collectionInfoService.checkDuplicate(collectionInfoIdReq.getUpiId(), null, false, null)){
            return RestResult.failed(ResultCode.DUPLICATE_UPI_ERROR);
        }
        if(collectionInfoIdReq.getType() == Integer.parseInt(PayTypeEnum.INDIAN_CARD.getCode())
                && collectionInfoService.checkDuplicate(null, collectionInfoIdReq.getBankCardNumber(), false, null)){
            return RestResult.failed(ResultCode.DUPLICATE_BANK_ERROR);
        }
        if(collectionInfoIdReq.getType() == Integer.parseInt(PayTypeEnum.INDIAN_CARD.getCode())
                && collectionInfoService.checkIfscCodeDuplicate( collectionInfoIdReq.getIfscCode(), false, null)){
            return RestResult.failed(ResultCode.DUPLICATE_IFSC_CODE_ERROR);
        }
        String updateBy = UserContext.getCurrentUserName();
        CollectionInfo collectionInfo = new CollectionInfo();
        MemberInfo memberInfo = iMemberInfoService.getMemberInfoById(collectionInfoIdReq.getMemberId());
        collectionInfo.setCreateBy(updateBy);
        collectionInfo.setUpdateBy(updateBy);
        BeanUtils.copyProperties(collectionInfoIdReq, collectionInfo);
        collectionInfo.setMemberAccount(memberInfo.getMemberAccount());
        collectionInfoMapper.insert(collectionInfo);
        return RestResult.ok();
    }

    @PostMapping("/listPage")
    @ApiOperation(value = "收款列表")
    public RestResult<CollectionInfoDTO> listPage(@RequestBody @ApiParam @Valid CollectionInfoListPageReq req) {
        PageReturn<CollectionInfoDTO> pageCollectionInfo = collectionInfoService.listPage(req);

        return RestResult.page(pageCollectionInfo);
    }

    private boolean checkBankCodeLength(String bankCode){
        if(ObjectUtils.isEmpty(bankCode)){
            return true;
        }
        int bankCodeLength = bankCode.length();
        TradeConfig config = tradeConfigMapper.selectById(1);
        Integer minBankCodeNumber = config.getMinBankCodeNumber();
        Integer maxBankCodeNumber = config.getMaxBankCodeNumber();
        return !(bankCodeLength >= minBankCodeNumber && bankCodeLength <= maxBankCodeNumber);
    }
}
