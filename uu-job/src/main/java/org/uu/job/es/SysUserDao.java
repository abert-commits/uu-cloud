package org.uu.job.es;


import org.uu.job.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


//@Repository
//public interface SysUserDao extends ElasticsearchRepository<SysUser,String> {
//    Page<SysUser> findEsUserByRolesIn(List<String> roles, Pageable pageable);
//
//}
