package org.uu.wallet.tron.service;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.wallet.tron.bo.BlockBo;
import org.uu.wallet.tron.bo.BlockListBo;
import org.uu.wallet.tron.utils.HttpHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BlockSyncService {

    // 每次处理的区块数量
    private static final int BATCH_SIZE = 20;


    // 查询最新的几个块  参数说明：块的数量  返回值：块的列表。
    private static final String BLOCK_LAST = "http://tron.652758.cc:8090/wallet/getblockbylatestnum";

    // 按照范围查询块  参数说明： startNum：起始块高度，包含此块  endNum：截止块高度，不包含此此块  返回值：块的列表。
    private static final String BLOCK_URL = "http://tron.652758.cc:8090/wallet/getblockbylimitnext";

    //查询最新block  参数说明: 无 返回值：solidityNode上的最新block
    private static final String NOW_URL = "http://tron.652758.cc:8090/wallet/getnowblock";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TronBlockService tronBlockService;

    public void syncBlocks() {

        try {

            final List<BlockBo> blockBos = new ArrayList<>();

            //初始化当前区块高度
            long nowBlock = 0;

            // 查询最新block  参数说明: 无 返回值：solidityNode上的最新block
            final String nowResult = HttpHelper.getNoRandMethod(NOW_URL);

            if (StringUtils.isNotBlank(nowResult) && isJSON2(nowResult)) {

                // 将获取到的最新block转换为区块实体
                BlockBo blockNow = JSONObject.parseObject(nowResult, BlockBo.class);

                //获取最新的区块高度
                nowBlock = blockNow.getBlockHeader().getRawData().getNumber();

                //将最新区块赋值到 BlockBo
                //add: 将指定元素添加到列表末尾
                //获取最新区块并添加到 blockBos 列表中是为了确保在任何情况下列表中都包含最新的区块信息。通过这种方式，可以保证数据的一致性和完整性，
                blockBos.add(blockNow);
            }

            //当系统中没有查过区块，查询最近5分钟的区块
            if (!redisTemplate.hasKey(RedisKeys.TRON_BLOCK_NUM)) {

                //请求参数 99 获取最新的99个区块 也就是5分钟 3秒钟一个区块 一分钟20个区块 5分钟100个区块
                Map<String, Object> params = new HashMap<>();
                params.put("num", 99);

                // 查询最近5分钟的区块  参数说明：块的数量  返回值：块的列表。 查询最新的99个块 (也就是最近5分钟的区块)
                final String result = HttpHelper.httpReq(BLOCK_LAST, ContentType.APPLICATION_JSON.getMimeType(), "POST", params);

                if (StringUtils.isNotBlank(result) && isJSON2(result)) {

                    // 将最近5分钟的区块列表赋值到区块列表实体
                    BlockListBo listBo = JSONObject.parseObject(result, BlockListBo.class);
                    if (listBo != null && listBo.getBlock() != null && listBo.getBlock().size() > 0) {

                        //addAll 将指定集合中的所有元素添加到列表中。addAll方法将最近5分钟内的区块列表添加到 blockBos 列表中。

                        //将最近5分钟的区块添加到blockBos开头
                        blockBos.addAll(0, listBo.getBlock());
                    }
                }

                //保存tron充值记录
                tronBlockService.transferHistory(blockBos);

                //维护当前区块高度
                redisTemplate.opsForValue().set(RedisKeys.TRON_BLOCK_NUM, nowBlock);
            } else {

                //获取上一次拉取数据的区块高度
                long hisBlock = getLongValue(RedisKeys.TRON_BLOCK_NUM);

                //如果当前高度大于 上次高度 超过20个区块 那么最多只获取20个区块数据
                if (nowBlock > hisBlock && nowBlock - hisBlock >= 20) {

                    Map<String, Object> params = new HashMap<>();

                    //起始高度: 上次高度
                    params.put("startNum", hisBlock);
                    //截止高度: 上次高度+20
                    params.put("endNum", hisBlock + 20);

                    //按照范围查询块  参数说明： startNum：起始块高度，包含此块  endNum：截止块高度，不包含此此块  返回值：块的列表。
                    final String result = HttpHelper.httpReq(BLOCK_URL, ContentType.APPLICATION_JSON.getMimeType(), "POST", params);

                    if (StringUtils.isNotBlank(result) && isJSON2(result)) {

                        BlockListBo listBo = JSONObject.parseObject(result, BlockListBo.class);

                        if (listBo != null && listBo.getBlock() != null && listBo.getBlock().size() > 0) {
                            //将范围区块数据添加到blockBos开头
                            blockBos.addAll(0, listBo.getBlock());
                        }
                    }

                    //保存tron充值记录
                    tronBlockService.transferHistory(blockBos);

                    //维护区块高度
                    redisTemplate.opsForValue().set(RedisKeys.TRON_BLOCK_NUM, hisBlock + 20);
                } else if (nowBlock > hisBlock && nowBlock - hisBlock < 20) {

                    //上次高度距最新高度相差20以内 那么获取上次高度到最新高度的区块

                    Map<String, Object> params = new HashMap<>();
                    //起始高度: 上次高度
                    params.put("startNum", hisBlock);
                    //截止高度: 最新高度
                    params.put("endNum", nowBlock);

                    //按照范围查询块  参数说明： startNum：起始块高度，包含此块  endNum：截止块高度，不包含此此块  返回值：块的列表。
                    final String result = HttpHelper.httpReq(BLOCK_URL, ContentType.APPLICATION_JSON.getMimeType(), "POST", params);

                    if (StringUtils.isNotBlank(result) && isJSON2(result)) {
                        BlockListBo listBo = JSONObject.parseObject(result, BlockListBo.class);
                        if (listBo != null && listBo.getBlock() != null && listBo.getBlock().size() > 0) {
                            //将范围区块数据添加到blockBos开头
                            blockBos.addAll(0, listBo.getBlock());
                        }
                    }

                    //保存tron充值记录
                    tronBlockService.transferHistory(blockBos);

                    //维护区块高度
                    redisTemplate.opsForValue().set(RedisKeys.TRON_BLOCK_NUM, nowBlock);
                }
            }
        } catch (Exception ex) {
            log.error("区块同步异常", ex);
        }

    }

    private static boolean isJSON2(String str) {
        boolean result = false;
        try {
            Object obj = JSONObject.parseObject(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }


    /**
     * 获取指定 key 的 long 值
     *
     * @param key Redis 的 key
     * @return key 对应的 long 值，如果 key 不存在或值不是 long 类型，返回 0
     */
    public long getLongValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                // 处理字符串无法转换为 long 的情况
                return 0L;
            }
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}