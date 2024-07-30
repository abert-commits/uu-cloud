package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.DividendConfigDTO;
import org.uu.common.pay.req.DividendConfigReq;
import org.uu.wallet.entity.DividendConfig;

import java.util.List;

/**
 * <p>
 * 分红配置表 服务类
 * </p>
 *
 * @author Parker
 * @since 2024-07-02
 */
public interface DividendConfigService extends IService<DividendConfig> {

    List<DividendConfigDTO> dividendConfigList();

    RestResult updateDividendConfig(Long id, DividendConfigReq req);

    RestResult addDividendConfig(DividendConfigReq req);
}
