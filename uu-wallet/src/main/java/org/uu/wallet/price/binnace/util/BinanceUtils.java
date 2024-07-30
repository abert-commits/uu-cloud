package org.uu.wallet.price.binnace.util;

import com.alibaba.fastjson.JSONObject;
import org.uu.wallet.price.binnace.bo.DataBo;
import org.uu.wallet.price.binnace.bo.PriceBo;
import org.uu.wallet.tron.utils.TronDecimalUtils;
import org.uu.wallet.util.RequestUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从币安的P2P平台上获取USDT的买卖汇率
 *
 * @author simon
 * @date 2024/07/15
 */
public class BinanceUtils {

    // Binance P2P平台的API URL
    final static String url = "https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search";

    /**
     * 读取Binance P2P平台上的汇率
     *
     * @param fiat      法币类型，例如 USD、CNY 等
     * @param tradeType 交易类型：BUY 或 SELL
     * @return 返回指定条件下的平均汇率
     */
    public static BigDecimal readBinanceRate(String fiat, String tradeType) {

        // 构建请求参数
        Map<String, Object> params = new HashMap<String, Object>() {{
            put("proMerchantAds", "false"); // 是否只包含专业商家的广告
            put("page", 1); // 页码
            put("rows", 11); // 每页显示的广告数量
            put("payTypes", null); // 支付方式
            put("countries", null); // 国家
            put("publisherType", "merchant"); // 发布者类型（商家）
            put("asset", "USDT"); // 资产类型（USDT）
            put("fiat", fiat); // 法币类型
            put("tradeType", tradeType); // 交易类型（BUY 或 SELL）
        }};

        JSONObject jsonObject = new JSONObject(params);

        // 发送HTTP POST请求
        String result = RequestUtil.HttpRestClientToJson(url, JSONObject.toJSONString(jsonObject));
//        final String result = HttpHelper.postMethod(url, params);

        System.out.println("result:" + result);

        // 将返回结果解析为 DataBo 对象
        final DataBo dataBo = JSONObject.parseObject(result, DataBo.class);

        // 获取广告数据列表
        final List<PriceBo> dataList = dataBo.getData();

        // 检查返回结果代码和数据列表是否有效
        if (dataBo.getCode().equals("000000") && dataList != null && dataList.size() > 0) {
            BigDecimal priceTotal = BigDecimal.ZERO;

            // 移除第一个广告（通常为置顶广告或不考虑在平均价格计算中的广告）
            dataList.remove(0);

            // 移除第二个广告
            dataList.remove(1);

            // 确保数据列表至少有10个广告
            int count = Math.min(dataList.size(), 10);

            // 累加接下来的10个广告的价格
            for (int i = 0; i < count; i++) {
                PriceBo priceBo = dataList.get(i);
                priceTotal = TronDecimalUtils.safeAdd(priceTotal, priceBo.getAdv().getPrice());
            }

            // 计算平均价格
            return TronDecimalUtils.safeDivide(priceTotal, new BigDecimal(count));
        }

        // 如果数据无效或无结果，返回 0
        return BigDecimal.ZERO;
    }


//    public static void main(String[] args) throws Exception {
//        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
//        List<Logger> loggerList = loggerContext.getLoggerList();
//        loggerList.forEach(logger -> {
//            logger.setLevel(Level.INFO);
//        });
//        System.out.println(readBinanceRate("CNY", "BUY"));
//        System.out.println(readBinanceRate("CNY", "SELL"));
//    }

}
