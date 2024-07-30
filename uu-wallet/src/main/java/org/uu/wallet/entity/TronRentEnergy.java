package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 能量租用记录表
 * </p>
 *
 * @author
 * @since 2024-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tron_rent_energy")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TronRentEnergy extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 钱包地址
     */
    private String address;

    /**
     * 租用数量
     */
    private Long amount;

    /**
     * 租用时长
     */
    private String rentTime;

    /**
     * 能量提供商
     */
    private String energyProvider;

    /**
     * 请求信息
     */
    private String requestInfo;

    /**
     * 响应信息
     */
    private String responseInfo;

    /**
     * 响应编码
     */
    private String resultCode;

    /**
     * 响应信息
     */
    private String resultMessage;

    /**
     * 到账时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

    /**
     * 是否可用 0 可用 ,1 不可用
     */
    private Integer enableFlag;

    /**
     * 是否删除 0 未删除 ,1 已删除
     */
    private Integer deleted;
}
