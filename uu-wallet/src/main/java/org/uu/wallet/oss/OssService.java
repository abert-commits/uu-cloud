package org.uu.wallet.oss;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import org.uu.wallet.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Date;

@Service
@RefreshScope
public class OssService {

    @Value("${oss.baseUrl}")
    private String baseUrl;

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.accessKeyId}")
    private String accessKeyId;

    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件
     *
     * @param file
     * @return {@link String}
     */
    public String uploadFile(MultipartFile file) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {

            // 生成唯一文件名
            String fileName = FileUtil.generateUniqueFileName(file.getOriginalFilename());

            // 上传文件流
            ossClient.putObject(bucketName, fileName, file.getInputStream());

            // 返回文件访问URL
            return baseUrl + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            // 关闭OSSClient。
            ossClient.shutdown();
        }
    }

    /**
     * 生成上传文件签名url
     *
     * @param objectName
     * @param contentType
     * @return {@link String}
     */
    public String generatePresignedUrl(String objectName, String contentType) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            Date expiration = new Date(new Date().getTime() + 3600 * 1000);  // 设置URL过期时间为1小时。
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.PUT);
            request.setExpiration(expiration);

            if (StringUtils.isNotEmpty(contentType)) {
                request.setContentType(contentType);
            }

            URL signedUrl = ossClient.generatePresignedUrl(request);
            return signedUrl.toString();
        } catch (ClientException e) {
            // 日志记录或其他异常处理
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
