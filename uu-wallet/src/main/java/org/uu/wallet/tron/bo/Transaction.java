package org.uu.wallet.tron.bo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 交易实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("transaction")
public class Transaction {


    private List<Ret> ret;


    private List<String> signature;


    private String txID;


    private RawData rawData;


    private String rawDataHex;


}