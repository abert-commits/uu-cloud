package org.uu.wallet.tron.bo;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 区块实体列表 包含多个区块
 *
 * @author simon
 * @date 2024/07/02
 */
@Setter
@Getter
public class BlockListBo implements Serializable {

    private static final long serialVersionUID = 2680048196281004327L;


    /**
     * 区块列表
     */
    private List<BlockBo> block;
}
