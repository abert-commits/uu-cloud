package org.uu.manager.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.result.RestResult;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.web.utils.UserContext;
import org.uu.common.pay.dto.AppVersionDTO;
import org.uu.manager.mapper.AppVersionManagerMapper;
import org.uu.manager.service.IAppVersionManagerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * APP版本管理 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-04-20
 */
@Service
@RequiredArgsConstructor
public class AppVersionManagerServiceImpl extends ServiceImpl<AppVersionManagerMapper, AppVersionDTO> implements IAppVersionManagerService {

    private final RedisUtils redisUtils;

    @Override
    public List<AppVersionDTO> listPage() {
        List<AppVersionDTO>  result = this.baseMapper.selectList(null);
        return result;
    }

    @Override
    public RestResult updateInfo(AppVersionDTO req) {
        req.setUpdateBy(UserContext.getCurrentUserName());
        baseMapper.updateById(req);
        List<AppVersionDTO>  result = this.baseMapper.selectList(null);
        redisUtils.set(RedisConstants.APP_VERSION_CONFIG, JSON.toJSONString(result));
        return RestResult.ok();
    }

}
