package org.uu.wallet.price.huobi.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class HbPriceBo implements Serializable {

    private BigDecimal price;
}
