package org.uu.common.pay.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uu.common.core.page.PageRequest;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2024-04-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class KycBankUpdateReq extends PageRequest {

    @ApiModelProperty("主键")
    private Long id;
    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 银行编码
     */
    private String bankCode;

    /**
     * 服务编码
     */
    private String serviceCode;




}
