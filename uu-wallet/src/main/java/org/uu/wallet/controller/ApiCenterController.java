package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.result.ApiResponse;
import org.uu.common.core.result.ApiResponseEnum;
import org.uu.common.core.result.RestResult;
import org.uu.wallet.bo.UsdtPaymentInfoBO;
import org.uu.wallet.dto.MerchantCollectionOrderStatusDTO;
import org.uu.wallet.entity.PaymentInfo;
import org.uu.wallet.req.ApiRequest;
import org.uu.wallet.req.ApiRequestQuery;
import org.uu.wallet.req.ConfirmPaymentReq;
import org.uu.wallet.req.MerchantCollectionOrderStatusReq;
import org.uu.wallet.service.IApiCenterService;
import org.uu.wallet.service.IMerchantCollectOrdersService;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/apiCenter")
@Api(description = "商户接口控制器")
public class ApiCenterController {

    private final IApiCenterService apiCenterService;

    private final IMerchantCollectOrdersService merchantCollectOrdersService;

    @PostMapping(value = "/merchantCollectionOrderStatus")
    public RestResult<MerchantCollectionOrderStatusDTO> merchantCollectionOrderStatus(
            @RequestBody @Validated MerchantCollectionOrderStatusReq requestVO
    ) {
        return this.merchantCollectOrdersService.merchantCollectionOrderStatus(requestVO);
    }

    /**
     * 充值接口-测试
     *
     * @param request
     * @return {@link ApiResponse}
     */
    @ApiIgnore
    @GetMapping("/test/deposit/apply")
    public String testDepositApply(HttpServletRequest request) {
        //UPI测试
        return apiCenterService.testDepositApply(request, "3");
    }

    /**
     * USDT充值接口-测试
     *
     * @param request
     * @return {@link ApiResponse}
     */
    @ApiIgnore
    @GetMapping("/testUsdt/deposit/apply")
    public String testUsdtDepositApply(HttpServletRequest request) {
        //USDT测试
        return apiCenterService.testDepositApply(request, "2");
    }

    /**
     * TRX充值接口-测试
     *
     * @param request
     * @return {@link ApiResponse}
     */
    @ApiIgnore
    @GetMapping("/testTrx/deposit/apply")
    public String testTrxDepositApply(HttpServletRequest request) {
        //USDT测试
        return apiCenterService.testDepositApply(request, "6");
    }

    /**
     * 充值接口
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    @ApiIgnore
    @PostMapping("/deposit/apply")
    public ApiResponse depositApply(@RequestBody @ApiParam ApiRequest apiRequest, HttpServletRequest request) {
        return apiCenterService.depositApply(apiRequest, request);
    }

    /**
     * 提现接口
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    @ApiIgnore
    @PostMapping("/withdrawal/apply")
    public ApiResponse withdrawalApply(@RequestBody @ApiParam ApiRequest apiRequest, HttpServletRequest request) {
        return apiCenterService.withdrawalApply(apiRequest, request);
    }


    /**
     * 获取支付页面(收银台)信息接口
     *
     * @param token
     * @return {@link ApiResponse}
     */
    @GetMapping("/retrievePaymentDetails")
    @ApiOperation(value = "获取支付页面(收银台)信息接口")
    public RestResult<PaymentInfo> retrievePaymentDetails(@ApiParam(value = "订单token", required = true) @RequestParam("token") String token) {
        return apiCenterService.retrievePaymentDetails(token);
    }

    /**
     * 获取USDT支付页面(收银台)信息接口
     *
     * @param token
     * @return {@link ApiResponse}
     */
    @GetMapping("/retrieveUsdtPaymentDetails")
    @ApiOperation(value = "获取USDT支付页面(收银台)信息接口")
    public RestResult<UsdtPaymentInfoBO> retrieveUsdtPaymentDetails(@ApiParam(value = "订单token", required = true) @RequestParam("token") String token) {
        return apiCenterService.retrieveUsdtPaymentDetails(token);
    }


    /**
     * 收银台 确认支付 接口
     *
     * @param confirmPaymentReq
     * @param request
     * @return {@link RestResult}
     */
    @PostMapping("/confirmPayment")
    @ApiOperation(value = "收银台 提交utr 接口")
    public RestResult confirmPayment(@RequestBody @ApiParam ConfirmPaymentReq confirmPaymentReq, HttpServletRequest request) {
        return apiCenterService.confirmPayment(confirmPaymentReq);
    }


    /**
     * 查询充值订单
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    @ApiIgnore
    @PostMapping("/deposit/query")
    public ApiResponse depositQuery(@RequestBody @ApiParam ApiRequestQuery apiRequest, HttpServletRequest request) {
        return apiCenterService.depositQuery(apiRequest, request);
    }


    /**
     * 查询提现订单
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    @ApiIgnore
    @PostMapping("/withdrawal/query")
    public ApiResponse withdrawalQuery(@RequestBody @ApiParam ApiRequestQuery apiRequest, HttpServletRequest request) {
        return apiCenterService.withdrawalQuery(apiRequest, request);
    }

    /**
     * 捕获该控制器下的参数校验异常
     *
     * @param e
     * @return {@link ApiResponse}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse handleValidationExceptions(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        System.out.println("Error Message: " + errorMessage);
        return ApiResponse.ofMsg(ApiResponseEnum.PARAM_VALID_FAIL, errorMessage, null);
    }
}


