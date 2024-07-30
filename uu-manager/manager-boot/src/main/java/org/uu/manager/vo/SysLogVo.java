package org.uu.manager.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 
*
* @author 
*/
@Data
public class SysLogVo implements Serializable {


    private String ip;

    private String method;

    private LocalDateTime createTime;

    private String createBy;

    private String operate;

    private String operater;

    private String path;

    private String module;


}