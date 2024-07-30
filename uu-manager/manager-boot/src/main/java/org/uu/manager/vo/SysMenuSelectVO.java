package org.uu.manager.vo;

import lombok.Data;

import java.util.List;

@Data
public class SysMenuSelectVO {
    private Long id;
    private String label;
    private List<SysMenuSelectVO> children;

}
