package org.uu.wallet.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.websocket.send.member.MemberWebSocketSendMessage;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.enums.MemberWebSocketMessageTypeEnum;
import org.uu.common.core.message.CommissionAndDividendsMessage;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.req.KycAutoCompleteReq;
import org.uu.wallet.service.IKycCenterService;
import org.uu.wallet.util.AESUtils;
import org.uu.wallet.util.RsaUtil;
import org.uu.wallet.webSocket.MemberMessageSender;
import org.uu.wallet.webSocket.massage.OrderStatusChangeMessage;

import javax.annotation.Resource;
import java.io.Serializable;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TestController {

    private final RabbitMQService rabbitMQService;

    private final MemberMessageSender notifyOrderStatusChangeSend;

    public static void main(String[] args) {
        System.out.println(ZoneId.systemDefault());
    }

    @GetMapping(value = "/send2Mq")
    public Object send2MQ(@RequestBody CommissionAndDividendsMessage commissionAndDividendsMessage) {
        this.rabbitMQService.sendCommissionDividendsQueue(commissionAndDividendsMessage);
        return true;
    }

    @GetMapping(value = "/send")
    public void send(String type,Long memberId) {
        MemberWebSocketMessageTypeEnum memberWebSocketMessageTypeEnum = MemberWebSocketMessageTypeEnum.buildMemberWebSocketMessageTypeEnumByMessageType(type);
        this.notifyOrderStatusChangeSend.send(
                MemberWebSocketSendMessage.buildMemberWebSocketMessage(
                        memberWebSocketMessageTypeEnum.getMessageType(),
                        memberId.toString(),
                        OrderStatusChangeMessage.builder()
                                .orderNo("sahvdj27386275428973")
                                .orderStatus("3")
                                .build()
                )
        );
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Test implements Serializable {
        private static final long serialVersionUID = -6021577321755042777L;

        private Integer userId;

        private String username;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime birthday;
    }

    @GetMapping(value = "/test")
    public RequestTest test() {
        rabbitMQService.sendCommissionDividendsQueue(
                CommissionAndDividendsMessage.builder()
                        .uid(580332L)
                        .amount(new BigDecimal("200.00"))
                        .changeType(MemberAccountChangeEnum.RECHARGE)
                        .orderNo("MR2024071313552201142")
                        .build()
        );
        return null;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class RequestTest implements Serializable {
        private static final long serialVersionUID = 1507313809438752338L;
        private String age;
        private Date requestDate;
        private Date responseDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class RequestTest1 implements Serializable {
        private static final long serialVersionUID = 1507313809438752338L;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime requestDate;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime responseDate;
    }

    @GetMapping(value = "timeZone")
    public Object timeZone(@RequestBody RequestTest requestTest) {
        if (requestTest.getRequestDate() != null) {
            SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("requestDate:::" + simpleDateTimeFormat.format(requestTest.getRequestDate()));
            Date responseDate = new Date();
            requestTest.setResponseDate(responseDate);
            System.out.println("responseDate:::" + simpleDateTimeFormat.format(responseDate));
            return requestTest;
        }
        return requestTest.getAge();
    }

    @GetMapping(value = "timeZone1")
    public RequestTest1 timeZone1(@RequestBody RequestTest1 requestTest) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("requestDate:::" + dateTimeFormatter.format(requestTest.getRequestDate()));
        LocalDateTime responseDate = LocalDateTime.now();
        requestTest.setResponseDate(responseDate);
        System.out.println("responseDate:::" + dateTimeFormatter.format(responseDate));
        return requestTest;
    }

    @Resource
    ArProperty arProperty;
    @GetMapping(value = "testAes")
    public RequestTest1 testAes() throws Exception {
        String key = arProperty.getKycAesKey();
        String encode = AESUtils.encryptFroKyc("{\"amount\":100.0,\"createTime\":\"2024-07-25T04:24:07\",\"detail\":\"{\\\"date\\\":1721881447000,\\\"acquirerVPA\\\":\\\"XXXXXXXXXXX9417\\\",\\\"transactionGroup\\\":\\\"100\\\",\\\"mobileNumber\\\":\\\"\\\",\\\"balanceBeforeUpdate\\\":0.0,\\\"description\\\":\\\"Paid to Bharath C\\\",\\\"txnType\\\":\\\"UPI_PAY\\\",\\\"mode\\\":\\\"debit\\\",\\\"recharge\\\":false,\\\"beneficiaryName\\\":\\\"Bharath C\\\",\\\"helpCategoryDeepLink\\\":\\\"mobikwik://help/Upi?txnid=OMK263bc346fdcf7d1\\\",\\\"iconUrl\\\":\\\"http://static.mobikwik.com/appdata/history/upi.png\\\",\\\"amount\\\":100.0,\\\"wallet\\\":[],\\\"transactionId\\\":\\\"OMK263bc346fdcf7d1\\\",\\\"rrn\\\":\\\"420713668650\\\",\\\"helpCategory\\\":\\\"Upi\\\",\\\"showCreditedTo\\\":false,\\\"txnParent\\\":false,\\\"invoice\\\":true,\\\"category\\\":\\\"Upi Pay\\\",\\\"multipleTxn\\\":false,\\\"showInvoiceFromHistory\\\":false,\\\"paymentInstrumentDetails\\\":[{\\\"amount\\\":100.0,\\\"paymentModes\\\":\\\"upi\\\",\\\"paymentInfo\\\":\\\"\\\"}],\\\"status\\\":\\\"success\\\"}\",\"mode\":\"2\",\"orderStatus\":\"1\",\"recipientUPI\":\"9417\",\"uTR\":\"420713668650\"}", key);
        System.err.println(encode);
        String s = AESUtils.decryptForKyc(encode, key);
        System.err.println(s);
        return null;
    }

    @Resource
    IKycCenterService kycCenterService;
    @GetMapping(value = "/startPull")
    public RequestTest1 startPullTransaction() throws Exception {
        KycAutoCompleteReq req = new KycAutoCompleteReq();
        req.setBuyerMemberId("580393");
        req.setSellerOrder("W2024072511300500571");
        req.setSellerMemberId("test7122702");
        req.setBuyerOrder("MR2024072511302500572");
        req.setCurrency("INR");
        req.setOrderAmount(new BigDecimal(150));
        req.setType("2");
        req.setWithdrawUpi("1866");
        req.setKycId("4");
        kycCenterService.startPullTransaction(req);
        return null;
    }
}

