//package org.uu.job.oss;
//
//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClientBuilder;
//import org.uu.job.util.FileNameGeneratorUtil;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//@Service
//public class OssService {
//
//    private final String IMG_URL = "https://arb-pay.oss-ap-southeast-1.aliyuncs.com/";
//
//    @Value("${oss.endpoint}")
//    private String endpoint;
//
//    @Value("${oss.accessKeyId}")
//    private String accessKeyId;
//
//    @Value("${oss.accessKeySecret}")
//    private String accessKeySecret;
//
//    @Value("${oss.bucketName}")
//    private String bucketName;
//
//    /**
//     * 上传文件
//     *
//     * @param file
//     * @return {@link String}
//     */
//    public String uploadFile(MultipartFile file) {
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//        try {
//
//            // 生成唯一文件名
//            String fileName = FileNameGeneratorUtil.generateUniqueFileName(file.getOriginalFilename());
//
//            // 上传文件流
//            ossClient.putObject(bucketName, fileName, file.getInputStream());
//
//            // 返回文件访问URL
//            return IMG_URL + fileName;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//            // 关闭OSSClient。
//            ossClient.shutdown();
//        }
//    }
//}
