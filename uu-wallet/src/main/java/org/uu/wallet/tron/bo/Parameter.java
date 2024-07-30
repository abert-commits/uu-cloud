
package org.uu.wallet.tron.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
public class Parameter implements Serializable {

    private static final long serialVersionUID = 7270108174076152112L;


    /**
     * 合约的具体值，如转账金额、发送方地址、接收方地址等
     */
    private TronValue value;


    /**
     * 合约类型的 URL 标识符
     */
    private String typeUrl;

}