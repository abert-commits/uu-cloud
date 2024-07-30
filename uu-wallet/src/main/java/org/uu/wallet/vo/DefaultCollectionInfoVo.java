package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.uu.wallet.entity.CollectionInfo;

@Data
@AllArgsConstructor
public class DefaultCollectionInfoVo {

    private CollectionInfo defaultBankInfo;
    private CollectionInfo defaultUpiInfo;
}