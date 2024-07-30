package org.uu.common.core.utils;

import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.GlobalConstants;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Admin
 */
@Slf4j
public class ExcelUtil {


    public static String lang = "zh";

    /**
     * 解析实体类
     *
     * @param clazz 实体类
     * @return
     */
    public static List<List<String>> parseHead(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<List<String>> heads = new ArrayList<>();

        for (Field field : fields) {
            if (!field.getName().equals("serialVersionUID")) {
                List<String> head = new ArrayList<>();
                // 在开发我们应该少不了swagger, 这个注解是swagger提供的，当然我们也可以自定义一个注解。（作用是为了属性名映射中文名称输出到excel表头）
                ApiModelProperty apiAnnotation = field.getAnnotation(ApiModelProperty.class);
                head.add(apiAnnotation.value());
                heads.add(head);
            }
        }
        return heads;
    }


    /**
     * 设置web响应输出的文件名称
     *
     * @param response web响应
     * @param fileName 导出文件名称
     */
    public static void setResponseHeader(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        response.reset();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setCharacterEncoding("UTF-8");
    }


    public static long getTotalSize(long resultSize) {
        long compare = GlobalConstants.BATCH_SIZE;
        long totalSize = 0;
        if (resultSize > compare && resultSize % compare > 0) {
            totalSize = (resultSize / compare) + 1;
        } else if (resultSize > compare) {
            totalSize = (resultSize / compare);
        }
        return totalSize;
    }

}
