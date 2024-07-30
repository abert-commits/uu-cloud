package org.uu.manager.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * 国际化解析
 */
public class MessageLocaleResolver implements LocaleResolver {

    private static final String LANG = "lang";



    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale locale;
        String language = request.getHeader(LANG);
        //中文language=zh_CN
        if (StringUtils.isNotEmpty(language)) {
           String[] arr = language.split("-");
            locale = new Locale(arr[0],arr[1]);
        } else {
            locale = new Locale("zh","CN");
        }

        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {

    }

}
