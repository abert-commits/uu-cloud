
package org.uu.wallet.tron.bo;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class Contract implements Serializable {

    private static final long serialVersionUID = 2063403125157653238L;


    /**
     * 合约参数
     */
    private Parameter parameter;


    /**
     *  交易的类型 例如 TransferContract 表示转账合约
     */
    private String type;

}