//package org.uu.wallet.controller;
//
//import com.alibaba.druid.util.StringUtils;
//import com.alibaba.fastjson.JSONObject;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.common.core.result.RestResult;
//import org.uu.wallet.Enum.MemberTypeEnum;
//import org.uu.wallet.entity.MemberInfo;
//import org.uu.wallet.entity.MerchantInfo;
//import org.uu.wallet.entity.PaymentOrder;
//import org.uu.wallet.service.IMemberInfoService;
//import org.uu.wallet.service.IMerchantInfoService;
//import org.uu.wallet.service.IPaymentOrderService;
//import org.uu.wallet.util.AmountVerifyUtil;
//import org.uu.wallet.util.IpUtil;
//import org.uu.wallet.util.SignAPI;
//import org.uu.wallet.util.SignUtil;
//import org.uu.wallet.vo.PaymentOrderVo;
//import org.springframework.beans.BeanUtils;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.validation.Valid;
//import java.math.BigDecimal;
//
///**
// * @author
// */
//@Slf4j
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/paymentcenter")
//@Api(description = "代付下单控制器")
//public class ArPaymentCenterController {
//    private final IMerchantInfoService merchantInfoService;
//    private final IPaymentOrderService paymentOrderService;
//    private final IMemberInfoService memberInfoService;
////    @Autowired
////    private MerchantSendRecommendAmount merchantSendRecommendAmount;
//
//
//    @PostMapping("/payment")
//    @ApiOperation(value = "代付下单接口")
//    public RestResult<JSONObject> pay(@RequestBody @ApiParam @Valid PaymentOrderVo paymentOrderVo, HttpServletRequest request) {
//
//        //获取请求IP
//        String clinetIpByReq = IpUtil.getRealIP(request);
//        log.info("代付下单接口: {}, 请求参数: {}, 代付下单接口请求IP: {}", paymentOrderVo.getMerchantOrder(), paymentOrderVo, clinetIpByReq);
//
//        //判断是否存在该商户
//        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(paymentOrderVo.getMerchantCode());
//        if (merchantInfo == null || StringUtils.isEmpty(merchantInfo.getCode())) {
//            return RestResult.failed("商户号不存在");
//        }
//
//        //md5验签
//        String signInfo = SignUtil.sortObject(paymentOrderVo);
//        log.info("代付下单接口:{}, 签名串: {}, ", paymentOrderVo.getMerchantOrder(), signInfo);
//        String sign = SignAPI.sign(signInfo, merchantInfo.getMd5Key());
////        if (!sign.equals(paymentOrderVo.getSign())) {
////            log.info("代付下单接口: {}, 签名串: {}, 签名错误: {}", paymentOrderVo.getMerchantOrder(), signInfo, sign);
////            return RestResult.failed("签名错误");
////        }
//
//        //校验金额是否是整百
//        if (!AmountVerifyUtil.isMultipleOfHundred(paymentOrderVo.getAmount())) {
//            return RestResult.failed("订单金额必须为整百");
//        }
//
//        PaymentOrder paymentOrder = new PaymentOrder();
//        BeanUtils.copyProperties(paymentOrderVo, paymentOrder);
//
//        //设置平台订单号
//        paymentOrder.setPlatformOrder("AR" + paymentOrderVo.getMerchantOrder());
//
//        //设置请求IP字段
//        paymentOrder.setClientIp(clinetIpByReq);
//
//        //设置订单费率
//        BigDecimal payRate = new BigDecimal(String.valueOf(merchantInfoService.getRateByCode(paymentOrder.getMerchantCode()).get("transfer_rate")));
//        paymentOrder.setOrderRate((payRate));
//
//        //设置费用 订单金额 * 费率)
//        paymentOrder.setCost(paymentOrder.getAmount().multiply((payRate.divide(BigDecimal.valueOf(100)))));
//
//        //设置商户名
//        paymentOrder.setMerchantName(merchantInfo.getUsername());
//
//        //将代付订单写入数据库
//        paymentOrderService.save(paymentOrder);
//
//        //重新推送 推荐金额列表给前端
//        // TODO 这里后面改用MQ异步去推送前端
////        merchantSendRecommendAmount.send();
//
//        MemberInfo memberInfo = new MemberInfo();
//
//        //会员ID
//        memberInfo.setMemberId(paymentOrder.getMemberId());
//
//        //会员账号
//        memberInfo.setMemberAccount(paymentOrder.getMemberAccount());
//
//        //手机号码
//        memberInfo.setMobileNumber(paymentOrder.getMobileNumber());
//
//        //真实姓名
//        memberInfo.setRealName(paymentOrder.getRealName());
//
//        //会员类型 内部商户会员
//        memberInfo.setMemberType(MemberTypeEnum.INTERNAL_MERCHANT_MEMBER.getCode());
//
//        //商户号
//        memberInfo.setMerchantCode(paymentOrder.getMerchantCode());
//
//        //商户名称
//        memberInfo.setMerchantName(paymentOrder.getMerchantName());
//
//        //UPI_ID
//        memberInfo.setUpiId(paymentOrder.getUpiId());
//
//        //UPI_Name
//        memberInfo.setUpiName(paymentOrder.getUpiName());
//
//        //记录会员信息表
//        try {
//            //这里try一下的原因是如果有重复会员 不抛出唯一索引异常 (已经记录过的会员就不再记录了)
//            //TODO 这里后面改用MQ去异步存储会员信息
//            memberInfoService.save(memberInfo);
//        } catch (Exception e) {
//
//        }
//
//        //同步返回提交成功
//        return RestResult.ok();
//    }
//}
//
//
