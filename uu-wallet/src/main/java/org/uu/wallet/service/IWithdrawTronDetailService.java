package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.WithdrawTronDetailDTO;
import org.uu.common.pay.dto.WithdrawTronDetailExportDTO;
import org.uu.common.pay.req.WithdrawTronDetailReq;
import org.uu.wallet.entity.WithdrawTronDetail;

/**
 * <p>
 * 代付钱包交易记录 服务类
 * </p>
 *
 * @author
 * @since 2024-07-18
 */
public interface IWithdrawTronDetailService extends IService<WithdrawTronDetail> {

    PageReturn<WithdrawTronDetailDTO> withdrawTronDetailPage(WithdrawTronDetailReq req);

    PageReturn<WithdrawTronDetailExportDTO> withdrawTronDetailPageExport(WithdrawTronDetailReq req);
}
