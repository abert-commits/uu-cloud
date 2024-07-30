package org.uu.manager.service;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AppVersionDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

/**
 * <p>
 * APP版本管理 服务类
 * </p>
 *
 * @author 
 * @since 2024-04-20
 */
public interface IAppVersionManagerService extends IService<AppVersionDTO> {

    List<AppVersionDTO> listPage();

    RestResult updateInfo(AppVersionDTO req);
}
