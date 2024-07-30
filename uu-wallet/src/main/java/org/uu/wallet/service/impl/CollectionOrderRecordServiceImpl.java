package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.CollectionOrderRecordDTO;
import org.uu.common.pay.dto.CollectionOrderRecordExportDTO;
import org.uu.common.pay.req.CollectionOrderRecordReq;
import org.uu.wallet.entity.CollectionOrderRecord;
import org.uu.wallet.mapper.CollectionOrderRecordMapper;
import org.uu.wallet.service.ICollectionOrderRecordService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 归集订单记录 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-20
 */
@Service
public class CollectionOrderRecordServiceImpl extends ServiceImpl<CollectionOrderRecordMapper, CollectionOrderRecord> implements ICollectionOrderRecordService {

    @Override
    public PageReturn<CollectionOrderRecordDTO> collectionOrderRecordPage(CollectionOrderRecordReq req) {
        Page<CollectionOrderRecord> page = new Page<>(req.getPageNo(), req.getPageSize());
        lambdaQuery()
                .orderByDesc(CollectionOrderRecord::getCreateTime)
                .ge(Objects.nonNull(req.getCreateTimeStart()), CollectionOrderRecord::getCreateTime, req.getCreateTimeStart())
                .le(Objects.nonNull(req.getCreateTimeEnd()), CollectionOrderRecord::getCreateTime, req.getCreateTimeEnd())
                .eq(Objects.nonNull(req.getCollectionType()), CollectionOrderRecord::getCollectionType, req.getCollectionType())
                .eq(!StringUtils.isEmpty(req.getCollectionOrderId()), CollectionOrderRecord::getCollectionOrderId, req.getCollectionOrderId())
                .page(page);

        List<CollectionOrderRecordDTO> resultList = page.getRecords().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    CollectionOrderRecordDTO dto = new CollectionOrderRecordDTO();
                    BeanUtils.copyProperties(item, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        return PageUtils.flush(page, resultList);
    }

    @Override
    public PageReturn<CollectionOrderRecordExportDTO> collectionOrderRecordPageExport(CollectionOrderRecordReq req) {
        PageReturn<CollectionOrderRecordDTO> pageReturn = collectionOrderRecordPage(req);
        List<CollectionOrderRecordExportDTO> resultList = pageReturn.getList().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    CollectionOrderRecordExportDTO dto = new CollectionOrderRecordExportDTO();
                    BeanUtils.copyProperties(item, dto);

                    if (Objects.nonNull(item.getCollectionType())) {
                        String collectionTypeText = req.getLang().equals("zh") ? (item.getCollectionType().equals(1) ? "自动" : "人工") : (item.getCollectionType().equals(1) ? "automatic" : "artificial");
                        dto.setCollectionType(collectionTypeText);
                    }
                    if (Objects.nonNull(item.getStatus())) {
                        String statusText = req.getLang().equals("zh") ? getCollectionStatus(item.getStatus()) : getCollectionEnStatus(item.getStatus());
                        dto.setStatus(statusText);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        Page<CollectionOrderRecordExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(pageReturn.getTotal());
        return PageUtils.flush(page, resultList);
    }

    //1:归集中, 2:归集成功, 3:归集失败
    private String getCollectionStatus(Integer status) {
        switch (status) {
            case 1:
                return "归集中";
            case 2:
                return "归集成功";
            default:
                return "归集失败";
        }
    }

    //1:归集中, 2:归集成功, 3:归集失败
    private String getCollectionEnStatus(Integer status) {
        switch (status) {
            case 1:
                return "collectioning";
            case 2:
                return "Collection success";
            default:
                return "Collection failed";
        }
    }
}
