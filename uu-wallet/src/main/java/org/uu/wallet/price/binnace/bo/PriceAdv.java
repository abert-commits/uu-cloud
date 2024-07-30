package org.uu.wallet.price.binnace.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class PriceAdv implements Serializable {

    private BigDecimal price;
}
