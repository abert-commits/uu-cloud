package org.uu.wallet.util;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import okhttp3.*;
import org.apache.commons.lang3.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

public class RequestUtil {

    private static final OkHttpClient okHttpClient = new OkHttpClient();

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
//        String url = "http://127.0.0.1:20000/ar-wallet/paymentcenter/payment";
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

    /**
     * 发送json格式参数 并返回请求头信息
     * @param url
     * @param json
     * @return
     */
    public static Map<String, Object> HttpRestClientToJsonWithHeader(String url, String json, Headers headers) {
        Map<String, Object> result = new HashMap<>();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);

        OkHttpClient client = new OkHttpClient();
        // 创建一个请求对象
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .headers(headers)
                .build();

        // 发送请求并获取响应
        String responseStr = null;
        try {
            Response response = client.newCall(request).execute();
            // 检查响应是否成功
            if (response.isSuccessful()) {
                // 获取响应体的字符串数据
                responseStr = response.body().string();
                result.put("response", responseStr);
                result.put("header", response.headers());
            }
            // 不要忘记关闭响应体
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 发送 GET 请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 响应结果
     */
    public static String get(String url, Map<String, String> params, String cip) throws IOException {

        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("cip", cip)
                .build();

        try (Response response = client.newCall(request).execute()) {
            // 获取响应数据
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        }

        return null;
    }


    /**
     * 发送 GET 请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 响应结果
     */
    public static String getForApp(String url, Map<String, String> params, String token) throws IOException {

        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("Cookie", token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            // 获取响应数据
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        }

        return null;
    }

    /**
     * 发送 GET 请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 响应结果
     */
    public static String get(String url, Map<String, String> params, Headers headers) {

        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .headers(headers)
                .build();

        try {
            Response response = client.newCall(request).execute();
            // 获取响应数据
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String getForAuth(String url, String json) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);

        OkHttpClient client = new OkHttpClient();
        // 创建一个请求对象
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Basic YXBwOmFwcA==")
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


    public static String getForAppJson(String url, JSONObject jsonObject, Map<String, String> headersMap) {
        MediaType jtype = MediaType.parse("application/json");

        RequestBody body = RequestBody.create(jtype, JSON.toJSONString(jsonObject));
        Headers headers = setHeaders(headersMap);
        OkHttpClient client = new OkHttpClient();
        // 创建一个请求对象
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                // .header("Cookie","_ga=GA1.1.131498049.1712756416; moe_uuid=84279a8f-9233-438b-bb57-24e1589e7ad8; app_fc=uE7hVQspD47b02A-fZuobOd5UnKbzkqgRiU7OGA1FrXKhCwY9Piz2V1zKwulpi62tMKzlakxy3R6SY-3OhHlcT5dAMKKNpu2IwXr4H0VM8kQG8QeUKsV7fcyCVhr5kUf; rxVisitor=1713015823545PG74UHOSC7NPA67R211GO2G8RDF7O17O; dtSa=-; dtCookie=v_4_srv_4_sn_D514DC45271F95931D2E3F84DA2C7933_perc_100000_ol_0_mul_1_app-3Aea7c4b59f27d43eb_1; _ga_Q9NVXVJCL0=GS1.1.1713015070.3.1.1713015935.0.0.15169463; rxvt=1713017750760|1713015823547; dtPC=4$15857075_947h29vDRVAHFWCJFTORESQUWAIUMPEHUUOMRAV-0e0")
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


    public static Headers setHeaders(Map<String, String> headerMap) {
        Headers headers = null;
        okhttp3.Headers.Builder headersbuilder = new Headers.Builder();
        headerMap.forEach((key, value) -> headersbuilder.add(key, value));
        headers = headersbuilder.build();
        return headers;


    }


    public static String getForAppJson(String url, JSONObject jsonObject, Headers headers) {
        MediaType jtype = MediaType.parse("application/json");

        RequestBody body = RequestBody.create(jtype, JSON.toJSONString(jsonObject));
        OkHttpClient client = new OkHttpClient();
        // 创建一个请求对象
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                // .header("Cookie","_ga=GA1.1.131498049.1712756416; moe_uuid=84279a8f-9233-438b-bb57-24e1589e7ad8; app_fc=uE7hVQspD47b02A-fZuobOd5UnKbzkqgRiU7OGA1FrXKhCwY9Piz2V1zKwulpi62tMKzlakxy3R6SY-3OhHlcT5dAMKKNpu2IwXr4H0VM8kQG8QeUKsV7fcyCVhr5kUf; rxVisitor=1713015823545PG74UHOSC7NPA67R211GO2G8RDF7O17O; dtSa=-; dtCookie=v_4_srv_4_sn_D514DC45271F95931D2E3F84DA2C7933_perc_100000_ol_0_mul_1_app-3Aea7c4b59f27d43eb_1; _ga_Q9NVXVJCL0=GS1.1.1713015070.3.1.1713015935.0.0.15169463; rxvt=1713017750760|1713015823547; dtPC=4$15857075_947h29vDRVAHFWCJFTORESQUWAIUMPEHUUOMRAV-0e0")
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


    /**
     * 发送POST请求 (使用代理)
     * @param url
     * @param jsonObject
     * @param headers
     * @return {@link String}
     */
    public static String getForAppJsonProxy(String url, JSONObject jsonObject, Headers headers) {
        MediaType jtype = MediaType.parse("application/json");

        RequestBody body = RequestBody.create(jtype, JSON.toJSONString(jsonObject));

        // 设置带认证的代理
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("geo.iproyal.com", 12321));
        OkHttpClient client = new OkHttpClient.Builder()
                .proxy(proxy)
                .proxyAuthenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic("w1Myp2aP9gGLACHF", "fxPWAm0Q7dbCkcBB_country-in");
                        return response.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    }
                })
                .build();

        // 创建一个请求对象
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(body)
                .build();

        // 发送请求并获取响应
        String responseStr = null;
        try (Response response = client.newCall(request).execute()) {
            // 检查响应是否成功
            if (response.isSuccessful()) {
                // 获取响应体的字符串数据
                responseStr = response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseStr;
    }


    public static OrderItem getOrderItem(String column, boolean asc){
        OrderItem orderItem = new OrderItem();
        orderItem.setColumn("id");
        orderItem.setAsc(false);
        if (ObjectUtils.isNotEmpty(column)) {
            orderItem.setColumn(StrUtil.toUnderlineCase(column));
            orderItem.setAsc(asc);
        }
        return orderItem;
    }

    public static String sendPostRequest(String url, Map<String, String> params) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBodyBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBodyBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}