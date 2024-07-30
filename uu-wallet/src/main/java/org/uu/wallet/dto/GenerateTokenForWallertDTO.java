package org.uu.wallet.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.uu.common.core.result.ResultCode;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerateTokenForWallertDTO implements Serializable {

    /**
     * requestIp
     */
    private String requestIp;

    /**
     * memberId
     */
    private String memberId;

    /**
     * mobileNumber
     */
    private String mobileNumber;

    /**
     * mobileNumber
     */
    private String memberAccount;

    /**
     * resultCode
     */
    private ResultCode resultCode;
}
