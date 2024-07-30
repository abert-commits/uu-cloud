package org.uu.wallet.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Marquee Table  跑马灯实体类
 * </p>
 *
 * @author 
 * @since 2024-07-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("marquee")
public class Marquee extends BaseEntityOrder{

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 跑马灯内容
     */
    private String content;

    /**
     * 方向 1从左到右 2从右到左
     */
    private Integer direction;

    /**
     * 速度 n像素每秒
     */
    private Integer speed;

    /**
     * 循环方式 0无限循环 n循环n次
     */
    private Integer loopCount;

    /**
     * 宽度:0 默认适配客户端 n为n像素
     */
    private String width;

    /**
     * 高度:0 默认适配客户端 n为n像素
     */
    private String height;

    /**
     * 滚动之后的延迟时间 秒
     */
    private BigDecimal delay;

    /**
     * 字体
     */
    private String fontStyle;

    /**
     * 背景颜色
     */
    private String bgColor;

    /**
     * 初始位置:0 不空格,n空n像素
     */
    private String startPosition;

    /**
     * 滚动方式:slide（滑动）、scroll（滚动）、alternate（来回切换）
     */
    private String behavior;

    /**
     * 排序权重 默认值: 0
     */
    private Integer sortOrder;

    /**
     * 是否删除 默认值: 0
     */
    private Integer deleted;

    /**
     * 状态, 0: 关闭, 1: 启用
     */
    private Integer status;


}
