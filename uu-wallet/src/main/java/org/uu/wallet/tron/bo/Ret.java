package org.uu.wallet.tron.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
public class Ret implements Serializable {

    private static final long serialVersionUID = -1207176448945105867L;


    /**
     * 交易结果的返回码，表示交易执行的状态
     */
    private String contractRet;
}