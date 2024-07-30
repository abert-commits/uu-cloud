package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.uu.wallet.entity.AntRelations;
import org.uu.wallet.mapper.AntRelationsMapper;
import org.uu.wallet.service.AntRelationsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 蚂蚁关系表 服务实现类
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
@Service
@RequiredArgsConstructor
public class AntRelationsServiceImpl extends ServiceImpl<AntRelationsMapper, AntRelations> implements AntRelationsService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRelationsWhenInsert(Long parentAntId, Long concurrentAntId) {
        // 查询出父级节点的左右节点
        AntRelations parentNodeInfo = this.getById(parentAntId);
        // 不存在则直接添加
        if (parentNodeInfo == null) {
            this.save(

                    AntRelations.builder()
                            .antId(concurrentAntId)
                            .leftValue(1)
                            .rightValue(2)
                            .antLevel(0)
                            .treeFlag(concurrentAntId)
                            .build());
        } else {
            // 更新右值
            this.baseMapper.updateRValueWhenUpdate(parentNodeInfo.getRightValue(), parentNodeInfo.getTreeFlag());
            // 更新左值
            this.baseMapper.updateLValueWhenUpdate(parentNodeInfo.getRightValue(), parentNodeInfo.getTreeFlag());
            // 增加一条左右值记录  左值 = 父节点右值  右值 = 父节点右值 + 1  层级 = 父节点层级 + 1
            this.save(
                    AntRelations.builder()
                            .antId(concurrentAntId)
                            .leftValue(parentNodeInfo.getRightValue())
                            .rightValue(parentNodeInfo.getRightValue() + 1)
                            .antLevel(parentNodeInfo.getAntLevel() + 1)
                            .treeFlag(parentNodeInfo.getTreeFlag())
                            .build()
            );
        }
    }

    /**
     * 根据当前用户ID查询所有下级
     *
     * @param antId          用户ID
     * @param includeOneself 是否包含自己 TRUE-是 FALSE-否
     * @return {@link List<AntRelations>}
     */
    @Override
    public List<AntRelations> selectChildList(Long antId, Boolean includeOneself) {
        List<AntRelations> resulList = new ArrayList<>();
        // 根据用户ID查询当前节点信息
        AntRelations concurrentNode = this.lambdaQuery()
                .eq(AntRelations::getAntId, antId).one();
        if (Objects.isNull(concurrentNode)) {
            return resulList;
        }
        resulList = this.baseMapper.selectChildList(
                concurrentNode.getLeftValue(),
                concurrentNode.getRightValue(),
                concurrentNode.getAntLevel() + 1,
                concurrentNode.getTreeFlag()
        );
        if (includeOneself) {
            resulList.add(concurrentNode);
        }
        return resulList;
    }

    @Override
    public Integer countOfChild(Long antId) {
        // 根据用户ID查询当前节点信息
        AntRelations concurrentNode = this.lambdaQuery()
                .eq(AntRelations::getAntId, antId).one();
        if (Objects.isNull(concurrentNode)) {
            return 0;
        }
        return this.baseMapper.selectChildList(
                concurrentNode.getLeftValue(),
                concurrentNode.getRightValue(),
                concurrentNode.getAntLevel() + 1,
                concurrentNode.getTreeFlag()).size();
    }

    @Override
    public List<AntRelations> selectParentList(Long antId, Boolean includeOneself) {
        List<AntRelations> resulList = new ArrayList<>();
        // 根据用户ID查询当前节点信息
        AntRelations concurrentNode = this.lambdaQuery()
                .eq(AntRelations::getAntId, antId).one();
        if (Objects.isNull(concurrentNode)) {
            return resulList;
        }
        resulList = this.baseMapper.selectParentList(concurrentNode.getLeftValue(), concurrentNode.getRightValue(), concurrentNode.getTreeFlag());
        if (includeOneself) {
            resulList.add(concurrentNode);
        }
        return resulList;
    }

    @Override
    public AntRelations selectParentByCount(Long antId, @Min(value = 0) Integer count) {
        // 根据用户ID查询当前节点信息
        AntRelations concurrentNode = this.lambdaQuery()
                .eq(AntRelations::getAntId, antId).one();
        // 最顶级直接返回当前用户
        if (Objects.isNull(concurrentNode) || concurrentNode.getAntLevel() == 0) {
            return null;
        }
        return this.baseMapper.selectParentByCount(concurrentNode.getLeftValue(), concurrentNode.getRightValue(), concurrentNode.getTreeFlag(), concurrentNode.getAntLevel() - count);
    }

    @Override
    public List<AntRelations> selectChildByCount(Long antId, Integer count) {
        // 根据用户ID查询当前节点信息
        AntRelations concurrentNode = this.lambdaQuery()
                .eq(AntRelations::getAntId, antId).one();
        // 最顶级直接返回当前用户
        if (Objects.isNull(concurrentNode)) {
            return Collections.emptyList();
        }
        return this.baseMapper.selectChildByCount(concurrentNode.getLeftValue(), concurrentNode.getRightValue(), concurrentNode.getTreeFlag(), concurrentNode.getAntLevel() + count);
    }

    @Override
    public AntRelations querySuperParent(Long uid) {
        // 根据用户ID查询当前节点信息
        AntRelations concurrentNode = this.lambdaQuery()
                .eq(AntRelations::getAntId, uid).one();
        // 最顶级直接返回当前用户
        if (Objects.isNull(concurrentNode)) {
            return null;
        }
        if (concurrentNode.getAntLevel() == 0) {
            return concurrentNode;
        }
        return this.baseMapper.querySuperParent(concurrentNode.getTreeFlag());
    }

    @Override
    public List<AntRelations> rangeChildByCount(Long currentUserId, Integer count, Boolean includeMe) {
        // 根据用户ID查询当前节点信息
        AntRelations concurrentNode = this.lambdaQuery()
                .eq(AntRelations::getAntId, currentUserId).one();
        // 最顶级直接返回当前用户
        if (Objects.isNull(concurrentNode)) {
            return Collections.emptyList();
        }
        if (includeMe) {
            return this.baseMapper.rangeChildByCountIncludeMe(
                    concurrentNode.getLeftValue(),
                    concurrentNode.getRightValue(),
                    concurrentNode.getTreeFlag(),
                    concurrentNode.getAntLevel() + count
            );
        }
        return this.baseMapper.rangeChildByCount(
                concurrentNode.getLeftValue(),
                concurrentNode.getRightValue(),
                concurrentNode.getTreeFlag(),
                concurrentNode.getAntLevel(),
                concurrentNode.getAntLevel() + count
        );
    }

    @Override
    public List<AntRelations> queryChildOfParentOne(Long currentUserId) {
        AntRelations parentOne = this.selectParentByCount(currentUserId, 1);
        if (Objects.isNull(parentOne)) {
            return Collections.emptyList();
        }
        return this.rangeChildByCount(
                parentOne.getAntId(),
                2,
                true
        );
    }

    @Override
    public List<AntRelations> queryChildOfParentTwo(Long currentUserId) {
        AntRelations parentTwo = this.selectParentByCount(currentUserId, 2);
        if (Objects.isNull(parentTwo)) {
            return Collections.emptyList();
        }
        return this.rangeChildByCount(
                parentTwo.getAntId(),
                2,
                true
        );
    }
}
