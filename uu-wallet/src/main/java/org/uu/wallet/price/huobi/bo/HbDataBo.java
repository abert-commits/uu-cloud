package org.uu.wallet.price.huobi.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class HbDataBo implements Serializable {

    private String code;
    private String message;
    private Long totalCount;
    private List<HbPriceBo> data;
}
