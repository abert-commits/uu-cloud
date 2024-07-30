package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.wallet.entity.AntRelations;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * <p>
 * 蚂蚁关系表 服务类
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
public interface AntRelationsService extends IService<AntRelations> {

    /**
     * 新增蚂蚁时调整树结构
     *
     * @param parentAntId     上级蚂蚁ID
     * @param concurrentAntId 新增蚂蚁ID
     */
    void updateRelationsWhenInsert(Long parentAntId, Long concurrentAntId);

    /**
     * 根据当前用户ID查询所有下级
     * @param antId 用户ID
     * @param includeOneself 是否包含自己 TRUE-是 FALSE-否
     * @return {@link List <AntRelations>}
     */
    List<AntRelations> selectChildList(Long antId, Boolean includeOneself);

    /**
     * 根据当前用户ID统计下级数量
     * @param antId 用户ID
     * @return 下级数量
     */
    Integer countOfChild(Long antId);

    /**
     * 根据当前用户ID查询所有上级
     * @param antId 用户ID
     * @param includeOneself 是否包含自己 TRUE-是 FALSE-否
     * @return {@link List <AntRelations>}
     */
    List<AntRelations> selectParentList(Long antId, Boolean includeOneself);

    /**
     * 获取前count级的上级   如果当前用户是最顶级则只返回当前用户
     * @param antId 用户ID
     * @param count 前n级
     */
    AntRelations selectParentByCount(Long antId, @Min(value = 0) Integer count);

    /**
     * 获取后count级的下级
     * @param antId 用户ID
     * @param count 后n级
     */
    List<AntRelations> selectChildByCount(Long antId, @Min(value = 0) Integer count);

    /**
     * 获取当前用户的顶层上级
     * @param uid 用户ID
     */
    AntRelations querySuperParent(Long uid);

    /**
     * 获取count层级范围内的下级
     * @param currentUserId 当前用户ID
     * @param count count级
     */
    List<AntRelations> rangeChildByCount(Long currentUserId, @Min(value = 0) Integer count, Boolean includeMe);

    /**
     * 查询当前用户上级的下级和下下级
     * @param currentUserId 当前用户ID
     * @return 当前用户上级的下级和下下级
     */
    List<AntRelations> queryChildOfParentOne(Long currentUserId);

    /**
     * 查询当前用户上上级的下级和下下级
     * @param currentUserId 当前用户ID
     * @return 当前用户上上级的下级和下下级
     */
    List<AntRelations> queryChildOfParentTwo(Long currentUserId);
}
