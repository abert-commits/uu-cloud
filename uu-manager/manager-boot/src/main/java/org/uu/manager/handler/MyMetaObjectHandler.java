package org.uu.manager.handler;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.uu.common.web.utils.UserContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 新增填充创建时间、操作人
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        if (StringUtils.isNotEmpty(UserContext.getCurrentUserName())){
            this.strictInsertFill(metaObject, "createBy", String.class, UserContext.getCurrentUserName());
            this.strictInsertFill(metaObject, "updateBy", String.class, UserContext.getCurrentUserName());
        }
    }

    /**
     * 更新填充更新时间、操作人
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        //this.strictUpdateFill(metaObject, "updateTime",LocalDateTime.class, LocalDateTime.now(ZoneId.systemDefault()));
//        this.strictUpdateFill(metaObject, "updateTime", () -> LocalDateTime.now(), LocalDateTime.class);
        if (StringUtils.isNotEmpty(UserContext.getCurrentUserName())){
            this.setFieldValByName("updateBy", UserContext.getCurrentUserName(), metaObject);
//            this.strictUpdateFill(metaObject, "updateBy",String.class, UserContext.getCurrentUserName());
        }
    }

}