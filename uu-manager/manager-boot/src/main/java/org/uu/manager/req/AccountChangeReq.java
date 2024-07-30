package org.uu.manager.req;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "账变参数")
public class AccountChangeReq implements Serializable {



    /**
     * 商户号
     */
    private String merchantCode;


    /**
     * 账变类型：add-增加, sub-支出
     */
    private Integer changeType;

    /**
     * 商户订单号
     */
    private String orderNo;

  
    /**
     * 开始时间
     */
    private String startTime;


    /**
     * 结束时间
     */
    private String endTime;




}