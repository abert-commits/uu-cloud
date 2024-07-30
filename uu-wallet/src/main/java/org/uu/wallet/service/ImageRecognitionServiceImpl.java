package org.uu.wallet.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.uu.wallet.util.RequestUtil;
import org.uu.wallet.vo.TestImageRecognitionVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RefreshScope
public class ImageRecognitionServiceImpl implements ImageRecognitionService {

    @Value("${ocr.baseUrl:}")
    private String baseUrl;

    @Value("${ocr.appId:default}")
    private String appId;

    @Value("${ocr.accessKey:j8Nmcu8CvDNfFqDagMYg}")
    private String accessKey;

    @Value("${ocr.eventId:article}")
    private String eventId;

    @Value("${ocr.type:EROTIC_ADVERT}")
    private String type;

    @Value("${ocr.lang:en}")
    private String lang;


    /**
     * 识别是否是 支付凭证截图
     *
     * @param imagePath
     * @return {@link TestImageRecognitionVo}
     */
    @Override
    public TestImageRecognitionVo isPaymentVoucher(String imagePath) {

        //图片识别

        //请求地址 目前是写死 等测试好了之后 配置到nacos去
        //String url = "http://api-img-yd.fengkongcloud.com/image/v4";

        //data数据
        JSONObject data = new JSONObject();
        data.put("tokenId", UUID.randomUUID().toString());
        data.put("img", imagePath);
        data.put("imgCompareBase", "");
        data.put("lang", lang);

        //请求参数
        JSONObject req = new JSONObject();

        req.put("accessKey", accessKey);
        req.put("appId", appId);
        req.put("eventId", eventId);
        req.put("type", type);
//        req.put("businessType","");
        req.put("data", data);
//        req.put("callback","");
        req.put("lang", lang);
        req.put("acceptLang", lang);

        long start = System.currentTimeMillis();
        log.info("ocr图片识别, 调用接口请求地址: {}, 请求参数: {}", baseUrl, JSON.toJSONString(req));
        String res = null;
        try {
            //发送请求
            res = RequestUtil.HttpRestClientToJson(baseUrl, JSON.toJSONString(req));
            log.info("ocr图片识别, 调用接口cost: {}", System.currentTimeMillis() - start);

            JSONObject resJson = JSON.parseObject(res);

            if ("1100".equals(resJson.getString("code"))) {
                //请求成功
                JSONObject riskDetail = resJson.getJSONObject("riskDetail");
                JSONObject ocrText = riskDetail.getJSONObject("ocrText");
                TestImageRecognitionVo testImageRecognitionVo = new TestImageRecognitionVo();

                //图片识别结果 Ads表示 支付凭证
                testImageRecognitionVo.setRiskLabel1(resJson.getString("riskLabel1"));

                //图片文字内容
                testImageRecognitionVo.setOcrText(ocrText.getString("text"));

                //requestId
                testImageRecognitionVo.setRequestId(resJson.getString("requestId"));

                //riskLevel
                testImageRecognitionVo.setRiskLevel(resJson.getString("riskLevel"));

                log.info("ocr图片识别 接口请求成功, 接口返回:{}, testImageRecognitionVo: {}", res, testImageRecognitionVo);

                return testImageRecognitionVo;
            } else {
                log.error("ocr图片识别, 接口请求失败, 接口返回:{}", res);
                return null;
            }
        } catch (Exception e) {
            log.error("ocr图片识别异常: 接口返回: {}, 异常信息:", res, e);
            return null;
        }

    }
}
