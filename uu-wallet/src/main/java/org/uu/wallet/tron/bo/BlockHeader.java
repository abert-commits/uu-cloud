
package org.uu.wallet.tron.bo;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class BlockHeader implements Serializable {

    private static final long serialVersionUID = -447173656844224285L;


    /**
     * 区块头的原始数据，包含区块号（number）、时间戳（timestamp）等
     */
    private BlockData rawData;


    /**
     * 见证人的签名
     */
    private String witnessSignature;
}