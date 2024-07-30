package org.uu.wallet.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class JsoupUtil {


    /**
     * 清洗用户输入
     * 去除任何潜在的恶意脚本
     *
     * @param input
     * @return {@link String}
     */
    public static String clean(String input) {
        //Safelist.none() 不保留任何html标签
        return Jsoup.clean(input, Safelist.none());
    }


    /**
     * 对用户输出进行编码
     * 当显示用户输入的内容时，确保进行HTML编码以防止任何脚本执行
     *
     * @param input
     * @return {@link String}
     */
    public static String encodeForHTML(String input) {
        return StringEscapeUtils.escapeHtml4(input);
    }
}
