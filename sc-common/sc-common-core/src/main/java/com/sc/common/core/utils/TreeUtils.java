package com.sc.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;

import java.util.List;

/**
 * 树形结构工具类（基于 Hutool TreeUtil 封装）
 */
public final class TreeUtils {

    private TreeUtils() {}

    /**
     * 构建树形结构
     *
     * @param list     原始列表
     * @param parentId 根节点父ID
     * @return 树形列表
     */
    public static <T> List<Tree<Long>> buildTree(List<T> list, Long parentId,
                                                   TreeNodeConfig config,
                                                   cn.hutool.core.lang.tree.parser.NodeParser<T, Long> nodeParser) {
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return TreeUtil.build(list, parentId, config, nodeParser);
    }

    /**
     * 使用默认配置构建树
     */
    public static <T> List<Tree<Long>> buildTree(List<T> list, Long parentId,
                                                   cn.hutool.core.lang.tree.parser.NodeParser<T, Long> nodeParser) {
        TreeNodeConfig config = new TreeNodeConfig();
        config.setWeightKey("orderNum");
        config.setDeep(10);
        return buildTree(list, parentId, config, nodeParser);
    }
}
