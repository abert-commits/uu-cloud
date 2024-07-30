package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * @author Admin
 */
@Data
public class UserAuthDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户状态：1-有效；0-禁用
     */
    private Integer status;

    /**
     * 用户角色编码集合 ["ROOT","ADMIN"]
     */

    private List<String> roles;


    private String googlesecret;


    @ApiModelProperty(value = "是否绑定谷歌验证码: 1-是 0-否")
    private Integer isBindGoogle;

}
