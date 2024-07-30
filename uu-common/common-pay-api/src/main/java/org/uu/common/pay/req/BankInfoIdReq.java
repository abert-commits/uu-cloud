package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.uu.common.core.page.PageRequest;


/**
 * @author Admin
 */
@Data
@ApiModel(description = "银行信息请求参数")
public class BankInfoIdReq extends PageRequest {

    /**
     * id
     */
    private Long id;

}
