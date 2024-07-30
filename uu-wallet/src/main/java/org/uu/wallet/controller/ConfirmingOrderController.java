//package org.uu.wallet.controller;
//
//
//import com.alibaba.druid.util.StringUtils;
//import com.alibaba.fastjson.JSONObject;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.common.core.result.RestResult;
//import org.uu.common.core.result.ResultCode;
//import org.uu.wallet.Enum.OrderStatusEnum;
//import org.uu.wallet.entity.MatchingOrder;
//import org.uu.wallet.entity.MerchantInfo;
//import org.uu.wallet.service.ICollectionOrderService;
//import org.uu.wallet.service.IMatchingOrderService;
//import org.uu.wallet.service.IMerchantInfoService;
//import org.uu.wallet.service.IPaymentOrderService;
//import org.uu.wallet.util.IpUtil;
//import org.uu.wallet.util.SignAPI;
//import org.uu.wallet.util.SignUtil;
//import org.uu.wallet.vo.ConfirmingPayVo;
//import org.uu.wallet.vo.ConfirmingPaymentVo;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.validation.Valid;
//
//@Slf4j
//@RequiredArgsConstructor
//@RestController
//@Api(description = "确认交易控制器")
//@RequestMapping("/confirming")
//public class ConfirmingOrderController {
//
//    private final IMerchantInfoService merchantInfoService;
//    private final ICollectionOrderService collectionOrderService;
//    private final IPaymentOrderService paymentOrderService;
//    private final IMatchingOrderService matchingOrderService;
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//
//    @PostMapping("/pay")
//    @ApiOperation(value = "充值交易确认接口")
//    public RestResult<JSONObject> pay(@RequestBody @ApiParam @Valid ConfirmingPayVo confirmingPayVo, HttpServletRequest request) {
//        //获取请求IP
//        String clinetIpByReq = IpUtil.getRealIP(request);
//
//        log.info("支付确认接口: {}, 请求参数: {}, 支付确认接口请求IP: {}", confirmingPayVo.getMerchantOrder(), confirmingPayVo, clinetIpByReq);
//
//        //判断是否存在该商户
//        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(confirmingPayVo.getMerchantCode());
//        if (merchantInfo == null || StringUtils.isEmpty(merchantInfo.getCode())) {
//            return RestResult.failed("商户号不存在");
//        }
//
//        //md5验签
//        String signInfo = SignUtil.sortObject(confirmingPayVo);
//        log.info("支付确认接口:{}, 签名串: {}, ", confirmingPayVo.getMerchantOrder(), signInfo);
//        String sign = SignAPI.sign(signInfo, merchantInfo.getMd5Key());
////        if (!sign.equals(confirmingPayVo.getSign())) {
////            log.info("支付确认接口: {}, 签名串: {}, 签名错误: {}", confirmingPayVo.getMerchantOrder(), signInfo, sign);
////            return RestResult.failed("签名错误");
////        }
//
//        //判断订单是否存在
//
//        //更新支付订单状态为: 确认中
//        boolean u1 = collectionOrderService.updateOrderStatusToConfirmation(confirmingPayVo.getMerchantOrder());
//
//        //更新匹配订单支付状态为: 确认中 更新订单支付时间
//        boolean u2 = matchingOrderService.updateCollectionOrderStatusToConfirmation(confirmingPayVo.getMerchantOrder());
//
//        //接口返回结果
//        JSONObject resJson = new JSONObject();
//        if (u1 && u2) {
//
//            log.info("支付确认接口:{}, u1: {}, u2: {}, 成功: {} ", confirmingPayVo.getMerchantOrder(), u1, u2, confirmingPayVo);
//
//            //业务状态码
//            resJson.put("code", ResultCode.SUCCESS.getCode());
//            //商户订单号
//            resJson.put("merchantOrder", confirmingPayVo.getMerchantOrder());
//            //提示信息
//            resJson.put("msg", "SUCCESS");
//
//            //判断对应的代付订单是否为确认中状态, 如果为确认中状态 那么发送交易成功的回调通知商户 //TODO
//            MatchingOrder matchingOrder = matchingOrderService.getMatchingOrder("这里填写 撮合列表订单号");
//
//            //根据充值订单号 查询对应的代付状态
//            if (matchingOrder != null && OrderStatusEnum.CONFIRMATION.getCode().equals(matchingOrder.getPaymentStatus())) {
//                //当前充值订单对应的代付订单也点了确认 发送MQ交易成功回调商户
//
//                //MQ发送充值交易成功通知
////                QueueInfo collectQueueInfo = new QueueInfo(RabbitMqConstants.UU_WALLET_TRADE_COLLECT_QUEUE_NAME, matchingOrder.getId(), confirmingPayVo.getMerchantOrder());
////                rabbitTemplate.convertAndSend(RabbitMqConstants.UU_WALLET_TRADE_COLLECT_QUEUE_NAME, matchingOrder, new CorrelationData(JSON.toJSONString(collectQueueInfo)));
//
//                //MQ发送提现交易成功通知
////                QueueInfo paymentQueueInfo = new QueueInfo(RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_QUEUE_NAME, matchingOrder.getId(), matchingOrder.getPaymentMerchantOrder());
////                rabbitTemplate.convertAndSend(RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_QUEUE_NAME, matchingOrder, new CorrelationData(JSON.toJSONString(paymentQueueInfo)));
//            }
//        } else {
//
//            log.info("支付确认接口:{}, u1: {}, u2: {}, 失败: {} ", confirmingPayVo.getMerchantOrder(), u1, u2, confirmingPayVo);
//
//            //业务状态码
//            resJson.put("code", ResultCode.SYSTEM_EXECUTION_ERROR.getCode());
//            //商户订单号
//            resJson.put("merchantOrder", confirmingPayVo.getMerchantOrder());
//            //提示信息
//            resJson.put("msg", "FAILED");
//        }
//        return RestResult.ok(resJson);
//    }
//
//
//    @PostMapping("/payment")
//    @ApiOperation(value = "提现交易确认接口")
//    public RestResult<JSONObject> payment(@RequestBody @ApiParam @Valid ConfirmingPaymentVo confirmingPaymentVo, HttpServletRequest request) {
//        //获取请求IP
//        String clinetIpByReq = IpUtil.getRealIP(request);
//
//        log.info("代付确认接口: {}, 请求参数: {}, 代付确认接口请求IP: {}", confirmingPaymentVo.getMerchantOrder(), confirmingPaymentVo, clinetIpByReq);
//
//        //判断是否存在该商户
//        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(confirmingPaymentVo.getMerchantCode());
//        if (merchantInfo == null || StringUtils.isEmpty(merchantInfo.getCode())) {
//            JSONObject errorJson = new JSONObject();
//            //业务状态码
//            errorJson.put("code", ResultCode.SYSTEM_EXECUTION_ERROR.getCode());
//            //错误信息
//            errorJson.put("errorMsg", "商户号不存在");
//            log.info("代付确认接口: {}, 商户号不存在: {}", confirmingPaymentVo.getMerchantOrder(), merchantInfo);
//            return RestResult.ok(errorJson);
//        }
//
//        //md5验签
//        String signInfo = SignUtil.sortObject(confirmingPaymentVo);
//        log.info("代付确认接口:{}, 签名串: {}, ", confirmingPaymentVo.getMerchantOrder(), signInfo);
//        String sign = SignAPI.sign(signInfo, merchantInfo.getMd5Key());
//        if (!sign.equals(confirmingPaymentVo.getSign())) {
//            log.info("代付确认接口: {}, 签名串: {}, 签名错误: {}", confirmingPaymentVo.getMerchantOrder(), signInfo, sign);
//            return RestResult.failed("签名错误");
//        }
//
//
//        //更新代付订单状态为: 确认中
//        boolean u1 = paymentOrderService.updateOrderStatusToConfirmation(confirmingPaymentVo.getMerchantOrder());
//
//        //更新匹配订单代付状态为: 确认中
//        boolean u2 = matchingOrderService.updatePaymentOrderStatusToConfirmation(confirmingPaymentVo.getMerchantOrder());
//
//        //接口返回结果
//        JSONObject resJson = new JSONObject();
//        if (u1 && u2) {
//
//            log.info("代付确认接口:{}, u1:{}, u2: {}, 成功: {}, ", confirmingPaymentVo.getMerchantOrder(), u1, u2, confirmingPaymentVo);
//
//            //业务状态码
//            resJson.put("code", ResultCode.SUCCESS.getCode());
//            //商户订单号
//            resJson.put("merchantOrder", confirmingPaymentVo.getMerchantOrder());
//            //提示信息
//            resJson.put("msg", "SUCCESS");
//
//            //判断对应的支付订单是否为确认中状态, 如果为确认中状态 那么发送交易成功的回调通知商户
//            MatchingOrder matchingOrder = matchingOrderService.getPaymentMatchingOrder(confirmingPaymentVo.getMerchantOrder());
//
//            //根据提现订单号 查询对应的支付状态
//            if (matchingOrder != null && OrderStatusEnum.CONFIRMATION.getCode().equals(matchingOrder.getPayStatus())) {
//                //当前提现订单对应的支付订单也点了确认 发送MQ交易成功回调商户
//
//                //MQ发送充值交易成功通知
////                QueueInfo collectQueueInfo = new QueueInfo(RabbitMqConstants.UU_WALLET_TRADE_COLLECT_QUEUE_NAME, matchingOrder.getId(), matchingOrder.getCollectionMerchantOrder());
////                rabbitTemplate.convertAndSend(RabbitMqConstants.UU_WALLET_TRADE_COLLECT_QUEUE_NAME, matchingOrder, new CorrelationData(JSON.toJSONString(collectQueueInfo)));
//
//                //MQ发送提现交易成功通知
////                QueueInfo paymentQueueInfo = new QueueInfo(RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_QUEUE_NAME, matchingOrder.getId(), confirmingPaymentVo.getMerchantOrder());
////                rabbitTemplate.convertAndSend(RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_QUEUE_NAME, matchingOrder, new CorrelationData(JSON.toJSONString(paymentQueueInfo)));
//            }
//        } else {
//
//            log.info("代付确认接口:{}, u1:{}, u2: {}, 失败: {}, ", confirmingPaymentVo.getMerchantOrder(), u1, u2, confirmingPaymentVo);
//
//            //业务状态码
//            resJson.put("code", ResultCode.SYSTEM_EXECUTION_ERROR.getCode());
//            //商户订单号
//            resJson.put("merchantOrder", confirmingPaymentVo.getMerchantOrder());
//            //提示信息
//            resJson.put("msg", "FAILED");
//        }
//        return RestResult.ok(resJson);
//    }
//}
