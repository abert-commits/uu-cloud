package org.uu.job.util;


import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RequestUtil {
    /**
     * 获取Json流请求参数
     *
     * @param request
     * @return
     */
    public static JSONObject getJsonParameters(HttpServletRequest request) {
        BufferedReader bReader = null;
        InputStreamReader isr = null;
        try {
            InputStream iis = request.getInputStream();
            isr = new InputStreamReader(iis, "utf-8");
            bReader = new BufferedReader(isr);
            String str;
            StringBuffer buffer = new StringBuffer();

            while ((str = bReader.readLine()) != null) {
                buffer.append(str).append("\n");
            }
            JSONObject json = JSONObject.parseObject(buffer.toString());
            return json;
        } catch (IOException e) {
        } finally {
            try {
                bReader.close();
            } catch (IOException e) {
            }
            try {
                isr.close();
            } catch (IOException e) {
            }
        }
        return new JSONObject();
    }

    /*
     * 接口测试
     * */
//    public static void main(String[] args) {
//        String url = "http://127.0.0.1:20000/uu-wallet/paymentcenter/payment";
//        JsonObject entries = new JsonObject();
//
//        for (int i = 0; i < 1000; i++) {
//            Random random = new Random();
//            int randomNumber = random.nextInt(30001) * 100;
//            entries.put("merchantOrder", "1000002815" + i);
//            entries.put("amount", randomNumber + ".00");
//            entries.put("upiId", "213124124");
//            entries.put("upiName", "pext");
//            entries.put("realName", "alex");
//            entries.put("merchantCode", "test");
//            entries.put("memberId", "a101");
//            entries.put("memberAccount", "yb10202");
//            entries.put("mobileNumber", "13909210921");
//            entries.put("matchNotifyUrl", "http://www.baidu1.com");
//            entries.put("tradeNotifyUrl", "http://www.baidu2.com");
//            entries.put("timestamp", "1696946009");
//            entries.put("sign", "123");
//
//            String s = HttpRestClientToJson(url, entries.toString());
//            System.out.println("s: " + s);
//        }
//    }

    //发送POST JSON格式请求
    public static String HttpRestClientToJson(String url, String json) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);

        OkHttpClient client = new OkHttpClient();
        // 创建一个请求对象
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // 发送请求并获取响应
        String responseStr = null;
        try {
            Response response = client.newCall(request).execute();
            // 检查响应是否成功
            if (response.isSuccessful()) {
                // 获取响应体的字符串数据
                responseStr = response.body().string();
            }
            // 不要忘记关闭响应体
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseStr;
    }
}