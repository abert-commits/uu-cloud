package org.uu.wallet.bo;

import lombok.Data;
import org.uu.wallet.entity.KycPartners;

import java.io.Serializable;
import java.util.List;

/**
 * 当前用户正在连接中的kyc列表
 *
 * @author simon
 * @date 2024/07/07
 */
@Data
public class ActiveKycPartnersBO implements Serializable {

    /**
     * 连接中的银行卡kyc列表
     */
    private List<KycPartners> bankPartners;

    /**
     * 连接中的upiKyc列表
     */
    private List<KycPartners> upiPartners;

}
