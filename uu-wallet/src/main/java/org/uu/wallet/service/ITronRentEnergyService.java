package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.TronRentEnergyDTO;
import org.uu.common.pay.dto.TronRentEnergyExportDTO;
import org.uu.common.pay.req.TronRentEnergyReq;
import org.uu.wallet.entity.TronRentEnergy;

/**
 * <p>
 * 能量租用记录表 服务类
 * </p>
 *
 * @author
 * @since 2024-07-13
 */
public interface ITronRentEnergyService extends IService<TronRentEnergy> {

    PageReturn<TronRentEnergyDTO> tronRentEnergyListPage(TronRentEnergyReq req);

    PageReturn<TronRentEnergyExportDTO> tronRentEnergyExport(TronRentEnergyReq req);
}
