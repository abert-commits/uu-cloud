package org.uu.manager.req;


import lombok.Data;
import org.uu.common.core.page.PageRequest;

@Data
public class RoleListPageReq extends PageRequest {
    private String keyword;
}
