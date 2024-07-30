package org.uu.wallet.util;

import com.alibaba.cloud.commons.lang.StringUtils;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件相关处理工具类
 *
 * @author Simon
 * @date 2023/11/13
 */
public class FileUtil {


    /**
     * 所有图片MIME类型
     */
    private static final List<String> IMAGE_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif",
            "image/bmp", "image/webp", "image/svg+xml",
            "image/tiff", "image/x-icon", "image/heif"
    );


    /**
     * 所有图片文件扩展名
     */
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            ".jpeg", ".jpg", ".png",
            ".gif", ".bmp", ".webp",
            ".svg", ".tiff", ".tif",
            ".ico", ".heif", ".heic"
    );


    /**
     * 所有视频MIME类型
     */
    private static final List<String> VIDEO_MIME_TYPES = Arrays.asList(
            "video/mp4", "video/x-msvideo", "video/mpeg",
            "video/quicktime", "video/webm", "video/x-flv",
            "video/x-ms-wmv", "video/x-matroska", "video/3gpp",
            "video/ogg"
    );


    /**
     * 所有视频文件扩展名
     */
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
            ".mp4", ".avi", ".mpeg", ".mpg",
            ".mov", ".webm", ".flv",
            ".wmv", ".mkv", ".3gp",
            ".ogv"
    );

    /**
     * 根据MIME类型和文件扩展名校验文件是否是图片
     *
     * @param file
     * @return boolean
     */
    public static boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        String extension = filename != null ? filename.substring(filename.lastIndexOf('.')).toLowerCase() : "";

        return IMAGE_MIME_TYPES.contains(contentType) && IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * 根据MIME类型和文件扩展名校验文件是否是视频
     *
     * @param file
     * @return boolean
     */
    public static boolean isValidVideo(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        String extension = filename != null ? filename.substring(filename.lastIndexOf('.')).toLowerCase() : "";

        return VIDEO_MIME_TYPES.contains(contentType) && VIDEO_EXTENSIONS.contains(extension);
    }


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

    /**
     * 校验文件
     *
     * @param file 文件
     * @param size 文件最大size
     * @param type 文件类型
     * @return {@link RestResult}
     */
    public static RestResult validateFile(MultipartFile file, Integer size, String type) {

        // 检查文件是否为空
        if (file.isEmpty()) {
            return RestResult.failure(ResultCode.FILE_CANNOT_BE_EMPTY);
        }

        // 检查文件大小是否超过限制
        if (file.getSize() > size) {
            return RestResult.failure(ResultCode.FILE_SIZE_EXCEEDS_LIMIT, "File size cannot exceed " + size / 1000000 + "MB");
        }

        //判断要校验的是图片还是视频
        if ("image".equals(type)) {
            //校验文件是否为图片
            if (!isValidImage(file)) {
                return RestResult.failure(ResultCode.FILE_MUST_BE_IMAGE);
            }
        } else if ("video".equals(type)) {
            //校验文件是否为视频
            if (!isValidVideo(file)) {
                return RestResult.failure(ResultCode.FILE_MUST_BE_VIDEO);
            }
        }

        return null;
    }


    /**
     * 检查给定的文件名是否有一个有效的图片扩展名。
     *
     * @param fileName 要检查的文件名
     * @return 如果文件扩展名有效，则返回true；否则返回false。
     */
    public static boolean isValidImageExtension(String fileName) {

        //文件名是空或者没有包含. 直接返回失败
        if (StringUtils.isEmpty(fileName) || !fileName.contains(".")) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        return IMAGE_EXTENSIONS.contains(extension);
    }


    /**
     * 检查给定的文件名是否有一个有效的视频文件扩展名。
     *
     * @param fileName 要检查的文件名
     * @return 如果文件扩展名是视频格式，则返回true；否则返回false。
     */
    public static boolean isValidVideoExtension(String fileName) {
        //文件名是空或者没有包含. 直接返回失败
        if (StringUtils.isEmpty(fileName) || !fileName.contains(".")) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        return VIDEO_EXTENSIONS.contains(extension);
    }

}
