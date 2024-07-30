package org.uu.wallet.price.binnace.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class DataBo implements Serializable {

    private String code;
    private String message;
    private String messageDetail;
    private List<PriceBo> data;
}
