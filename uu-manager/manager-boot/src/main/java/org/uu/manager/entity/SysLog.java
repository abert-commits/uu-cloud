package org.uu.manager.entity;

    import com.baomidou.mybatisplus.annotation.TableName;
    import java.time.LocalDateTime;
    import java.io.Serializable;
    import java.util.Date;

    import io.swagger.annotations.ApiModelProperty;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    import lombok.experimental.Accessors;

/**
* 
*
* @author 
*/
    @Data
    @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    @TableName("sys_log")
    public class SysLog extends BaseEntity implements Serializable {

private static final long serialVersionUID = 1L;

    private String ip;

    private String method;



    private String path;

    private String module;

    private String requestParam;

    private String responseResult;

    @ApiModelProperty(value = "操作内容")
    private String content;


}