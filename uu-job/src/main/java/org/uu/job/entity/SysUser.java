package org.uu.job.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.List;


@Document(indexName = "sys_user")
@Data
@Builder
public class SysUser implements Serializable {
    @Id
    private String id;
    private String username;
    private String password;
    private int level;
    @Field(type = FieldType.Keyword)
    private List<String> roles;
}