package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.uu.wallet.entity.CollectionInfo;

/**
 * @author
 */
@Mapper
public interface CollectionInfoMapper extends BaseMapper<CollectionInfo> {

    /**
     * 查询收款信息 加上排他行锁
     *
     * @param id
     * @return {@link CollectionInfo}
     */
    @Select("SELECT * FROM collection_info WHERE id = #{id} FOR UPDATE")
    CollectionInfo selectCollectionInfoForUpdate(Long id);
}
