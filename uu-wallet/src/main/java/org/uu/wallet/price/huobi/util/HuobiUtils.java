package org.uu.wallet.price.huobi.util;

import com.alibaba.fastjson.JSONObject;
import org.uu.wallet.price.huobi.bo.HbDataBo;
import org.uu.wallet.price.huobi.bo.HbPriceBo;
import org.uu.wallet.tron.utils.HttpHelper;
import org.uu.wallet.tron.utils.TronDecimalUtils;

import java.math.BigDecimal;
import java.util.List;

public class HuobiUtils {

    final static String url = "https://www.htx.com/-/x/otc/v1/data/trade-market?coinId=%s&currency=%s&tradeType=sell&currPage=1&payMethod=0" + "&acceptOrder=0&country=&blockType=general&online=1&range=0&amount=&onlyTradable=true&isFollowed=false";

    public static BigDecimal readBinanceRate(String coinId, String currency) {
        final String req_url = String.format(url, coinId, currency);

        final String result = HttpHelper.getMethod(req_url);
        final HbDataBo dataBo = JSONObject.parseObject(result, HbDataBo.class);
        final List<HbPriceBo> dataList = dataBo.getData();
        if (dataBo.getCode().equals("200") && dataList != null && dataList.size() > 0) {
            BigDecimal priceTotal = BigDecimal.ZERO;
            dataList.remove(0);
            for (HbPriceBo priceBo : dataList) {
                priceTotal = TronDecimalUtils.safeAdd(priceTotal, priceBo.getPrice());
            }
            return TronDecimalUtils.safeDivide(priceTotal, new BigDecimal(dataList.size()));
        }
        return BigDecimal.ZERO;
    }
}
