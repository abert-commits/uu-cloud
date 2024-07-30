package org.uu.wallet.tron.bo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 区块实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("block")
public class Block {


    /**
     * 区块高度
     */
    @TableId(type = IdType.AUTO)
    private long number;


    /**
     * 区块中的交易列表
     */
    @TableField(exist = false)
    private List<Transaction> transactions;


    /**
     * 区块生成时间
     */
    private long timestamp;

}