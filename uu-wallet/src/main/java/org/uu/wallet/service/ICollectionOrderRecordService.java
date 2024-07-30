package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.CollectionOrderRecordDTO;
import org.uu.common.pay.dto.CollectionOrderRecordExportDTO;
import org.uu.common.pay.req.CollectionOrderRecordReq;
import org.uu.wallet.entity.CollectionOrderRecord;

/**
 * <p>
 * 归集订单记录 服务类
 * </p>
 *
 * @author
 * @since 2024-07-20
 */
public interface ICollectionOrderRecordService extends IService<CollectionOrderRecord> {

    PageReturn<CollectionOrderRecordDTO> collectionOrderRecordPage(CollectionOrderRecordReq req);

    PageReturn<CollectionOrderRecordExportDTO> collectionOrderRecordPageExport(CollectionOrderRecordReq req);
}
