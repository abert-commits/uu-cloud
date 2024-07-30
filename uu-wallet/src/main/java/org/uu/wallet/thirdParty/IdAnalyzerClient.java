package org.uu.wallet.thirdParty;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
@RefreshScope
@Data
@Slf4j
public class IdAnalyzerClient {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${third.party.idAnalyzer.apikey:gLzQHBghubYgnTyuk7jFVU4KS9eImpQz}")
    private String apikey;

    @Value("${third.party.idAnalyzer.urlRoot:https://api.idanalyzer.com}")
    private String urlRoot;


    /**
     *
     * @param idFront  证件照正面 使用base64编码  必传
     * @param idBack   证件照背面 使用base64编码  可以不传
     * @param face     人脸照片 使用base64编码    可以不传
     * @param name     前端传递过来的姓名  可以不传
     * @param idNumber 前端传递过来的身份证号 可以不传
     * @return
     */
    public IdAnalyzerStatus exmainIdentity(String idFront,String idBack,String face,String name,String idNumber){
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("apikey", apikey);
        if(idFront.isEmpty()){
            return new IdAnalyzerStatus(false,"id pic can't be null");
        }
        if(StringUtils.isNotEmpty(face)){
            parameterMap.put("face_base64", face);
        }
        if(StringUtils.isNotEmpty(idBack)){
            parameterMap.put("file_back_base64", idBack);
        }
        if(StringUtils.isNotEmpty(name)){
            parameterMap.put("verify_name", name);
        }
        if(StringUtils.isNotEmpty(idNumber)){
            parameterMap.put("verify_documentno", idNumber);
        }
        parameterMap.put("file_base64", idFront);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> requestEntity = new HttpEntity(parameterMap, headers);
        ResponseEntity<JSONObject> result = restTemplate.postForEntity(urlRoot, requestEntity, JSONObject.class);

        JSONObject json = result.getBody();

        log.info("请求实名认证接口, 请求地址: {}, 返回数据: {}", urlRoot, json);

        if(json.containsKey("error")){
            return new IdAnalyzerStatus("1",json.getJSONObject("error"));
        }else{
            return new IdAnalyzerStatus("2",json.getJSONObject("verification"));
        }
    }

    /**
     *
     * @param idFront  证件照正面 使用base64编码  必传
     * @param idBack   证件照背面 使用base64编码  可以不传
     * @param fileSystemResourcesArray  脸部照片
     * @param name     前端传递过来的姓名  可以不传
     * @param idNumber 前端传递过来的身份证号 可以不传
     * @return
     */
    public IdAnalyzerStatus exmainIdentity(String idFront,String idBack, FileSystemResource[] fileSystemResourcesArray, String name, String idNumber){
        MultiValueMap<String, Object> parameterMap = new LinkedMultiValueMap<>();
        parameterMap.add("apikey",apikey);
        if(idFront.isEmpty()){
            return new IdAnalyzerStatus(false,"id pic can't be null");
        }
        if(fileSystemResourcesArray != null && fileSystemResourcesArray.length > 0){
            for(int i = 0 ; i < fileSystemResourcesArray.length ; i++){
                FileSystemResource fileSystemResource = fileSystemResourcesArray[i];
                parameterMap.add("face",fileSystemResource);
            }
        }
        if(!name.isEmpty()){
            parameterMap.add("verify_name", name);
        }
        if(!idNumber.isEmpty()){
            parameterMap.add("verify_documentno", idNumber);
        }
        parameterMap.add("file_base64", idFront);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<Map> requestEntity = new HttpEntity(parameterMap, headers);
        ResponseEntity<JSONObject> result = restTemplate.postForEntity(urlRoot, requestEntity, JSONObject.class);

        JSONObject json = result.getBody();
        if(json.containsKey("error")){
            return new IdAnalyzerStatus("1",json.getJSONObject("error"));
        }else{
            return new IdAnalyzerStatus("2",json.getJSONObject("verification"));
        }
    }

    private static String readPic(String path) {
        String imagePath = path;
        //;
        FileInputStream fis = null;
        try {
            // 创建输入流对象
            fis = new FileInputStream(new File(imagePath));
            // 通过ImageIO类获取图像数据
            BufferedImage bufferedImage = ImageIO.read(fis);

            // 将BufferedImage对象转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] bytes = baos.toByteArray();

            // 使用Base64进行编码
            Base64 base64Encoder = new Base64();
            String encodedImage = base64Encoder.encodeToString(bytes);

            return encodedImage;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭输入流
            try {
                fis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }


}
