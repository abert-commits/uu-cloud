package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.ApplyDistributedDTO;
import org.uu.common.pay.req.ApplyDistributedListPageReq;
import org.uu.wallet.entity.ApplyDistributed;

import java.util.List;
import java.util.Map;


/**
 * @author
 */
public interface IApplyDistributedService extends IService<ApplyDistributed> {

    PageReturn<ApplyDistributedDTO> listPage(ApplyDistributedListPageReq req);

    PageReturn<ApplyDistributedDTO> listRecordPage(ApplyDistributedListPageReq req);


    ApplyDistributedDTO listRecordTotal(ApplyDistributedListPageReq req);


    ApplyDistributedDTO distributed(ApplyDistributed applyDistributed);


    ApplyDistributedDTO noDistributed(ApplyDistributed applyDistributed);

    Map<String, List<ApplyDistributed>> applyDistributedMap(String merchantCode);
}
