package org.uu.job.util;

import java.util.UUID;

public class FileNameGeneratorUtil {
    
    /**
     * 生成唯一文件名
     *
     * @param originalFileName
     * @return {@link String}
     */
    public static String generateUniqueFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return uuid + extension;
    }

}
