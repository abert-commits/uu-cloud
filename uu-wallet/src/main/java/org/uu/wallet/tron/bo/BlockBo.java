package org.uu.wallet.tron.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 区块的实体 一个块对应一个实体
 *
 * @author simon
 * @date 2024/07/02
 */
@Setter
@Getter
public class BlockBo implements Serializable {


    private static final long serialVersionUID = 5693580626676011157L;


    /**
     * 区块的唯一标识符
     */
    private String blockID;


    /**
     *  区块头，包含区块的元数据，如区块号、时间戳、见证人签名等
     */
    private BlockHeader blockHeader;


    /**
     * 交易列表。每个区块中都会包含若干笔交易
     * 每笔交易都包含了交易的详细信息，比如发送方、接收方、交易金额、交易类型等
     */
    private List<BlockTransaction> transactions;
}
