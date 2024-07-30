package org.uu.manager.entity;

    import com.baomidou.mybatisplus.annotation.IdType;
    import com.baomidou.mybatisplus.annotation.TableId;
    import com.baomidou.mybatisplus.annotation.TableName;
    import java.time.LocalDateTime;
    import java.io.Serializable;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    import lombok.experimental.Accessors;

/**
* ip白名单
*
* @author
*/
    @Data
    @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    @TableName("sys_white")
    public class SysWhite extends BaseEntityOrder implements Serializable{

private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;

            /**
            * ip地址
            */
    private String ip;

            /**
            * 备注
            */
    private String remark;

            /**
            * 状态
            */
    private String status;


    /**
     * 后台类别
     */
    private String clientType;


}