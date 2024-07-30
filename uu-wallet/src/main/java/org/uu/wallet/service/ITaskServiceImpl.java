package org.uu.wallet.service;

import lombok.extern.slf4j.Slf4j;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.entity.TaskCollectionRecord;
import org.uu.wallet.util.RedisUtil;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.concurrent.TimeUnit;

/**
 * 任务处理
 *
 * @author Simon
 * @date 2024/03/21
 */
@Service
@Slf4j
public class ITaskServiceImpl implements ITaskService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ITaskCollectionRecordService taskCollectionRecordService;


    @Autowired
    private RedissonUtil redissonUtil;

    private static final String TASK_COMPLETE_KEY_PREFIX = RedisKeys.TASK_COMPLETED; // Redis中存储任务完成状态的key前缀









}
