package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.FrontPageConfigInfoDTO;
import org.uu.common.pay.req.InsertFrontPageConfigReq;
import org.uu.common.pay.req.QueryFrontPageConfigReq;
import org.uu.common.pay.req.UpdateFrontPageConfigReq;
import org.uu.wallet.entity.FrontPageConfig;

import java.util.List;

public interface FrontPageConfigService extends IService<FrontPageConfig> {
    RestResult<Boolean> insertFrontPageConfig(InsertFrontPageConfigReq requestVO);

    RestResult<Boolean> removeFrontPageConfigById(Long id);

    RestResult<Boolean> updateFrontPageConfig(UpdateFrontPageConfigReq requestVO);

    RestResult<FrontPageConfigInfoDTO> queryFrontPageConfigById(Long id);

    RestResult<FrontPageConfigInfoDTO> queryFrontPageConfigByLang(Integer lang);

    RestResult<PageReturn<FrontPageConfigInfoDTO>> queryFrontPageConfigPage(QueryFrontPageConfigReq requestVO);

    RestResult<List<FrontPageConfigInfoDTO>> frontPageConfigList();
}
