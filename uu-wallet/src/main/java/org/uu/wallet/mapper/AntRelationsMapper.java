package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.uu.wallet.entity.AntRelations;

import java.util.List;

/**
 * <p>
 * 蚂蚁关系表 Mapper 接口
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
@Mapper
public interface AntRelationsMapper extends BaseMapper<AntRelations> {

    /**
     * 更新时调整右值
     * 当表中数据的右值大于等于父节点的右值 则右值 += 2
     * @param parentRValue 父节点右值
     */
    void updateRValueWhenUpdate(@Param("parentRValue") Integer parentRValue, @Param("treeFlag") Long treeFlag);

    /**
     * 更新时调整左值
     * 当表中数据的左值大于等于父节点的左值 则左值 += 2
     * @param parentRValue 父节点左值
     */
    void updateLValueWhenUpdate(@Param("parentRValue") Integer parentRValue, @Param("treeFlag") Long treeFlag);

    /**
     * 获取所有下级
     * @param leftValue 左节点值
     * @param rightValue 右节点值
     * @param treeFlag 树标识
     */
    List<AntRelations> selectChildList(@Param("leftValue")Integer leftValue,
                                       @Param("rightValue")Integer rightValue,
                                       @Param("level")Integer level,
                                       @Param("treeFlag")Long treeFlag);

    /**
     * 获取所有上级
     * @param leftValue 左节点值
     * @param rightValue 右节点值
     * @param treeFlag 树标识
     */
    List<AntRelations> selectParentList(@Param("leftValue")Integer leftValue,
                                       @Param("rightValue")Integer rightValue,
                                       @Param("treeFlag")Long treeFlag);

    /**
     * 获取指定层级的上级
     * @param leftValue 左节点值
     * @param rightValue 右节点值
     * @param treeFlag 树标识
     * @param level 层级
     */
    AntRelations selectParentByCount(@Param("leftValue")Integer leftValue,
                                           @Param("rightValue")Integer rightValue,
                                           @Param("treeFlag")Long treeFlag,
                                           @Param("level")Integer level);

    /**
     * 获取当前树的最顶级用户
     * @param treeFlag 当前树
     */
    AntRelations querySuperParent(@Param("treeFlag")Long treeFlag);

    /**
     * 获取指定层级的下级
     * @param leftValue 左节点值
     * @param rightValue 右节点值
     * @param treeFlag 树标识
     * @param level 层级
     */
    List<AntRelations> selectChildByCount(@Param("leftValue")Integer leftValue,
                                          @Param("rightValue")Integer rightValue,
                                          @Param("treeFlag")Long treeFlag,
                                          @Param("level")Integer level);

    /**
     * 获取指定层级范围内的下级
     * @param leftValue 左节点值
     * @param rightValue 右节点值
     * @param treeFlag 树标识
     * @param level 层级
     */
    List<AntRelations> rangeChildByCount(@Param("leftValue")Integer leftValue,
                                         @Param("rightValue")Integer rightValue,
                                         @Param("treeFlag")Long treeFlag,
                                         @Param("currentLevel")Integer currentLevel,
                                         @Param("level")Integer level);

    /**
     * 获取指定层级范围内的下级(包含自己)
     * @param leftValue 左节点值
     * @param rightValue 右节点值
     * @param treeFlag 树标识
     * @param level 层级
     */
    List<AntRelations> rangeChildByCountIncludeMe(@Param("leftValue")Integer leftValue,
                                         @Param("rightValue")Integer rightValue,
                                         @Param("treeFlag")Long treeFlag,
                                         @Param("level")Integer level);
}
