package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.CreditScoreLogsDTO;
import org.uu.common.pay.req.CreditScoreLogsListPageReq;
import org.uu.wallet.entity.CreditScoreLogs;
import org.uu.wallet.req.CreditScoreLogsListReq;
import org.uu.wallet.vo.CreditScoreLogsVo;
import scala.Int;

import java.math.BigDecimal;

/**
 * <p>
 * 信用分记录表 服务类
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
public interface ICreditScoreLogsService extends IService<CreditScoreLogs> {

    boolean addLog(Long memberId, BigDecimal changeScore, BigDecimal currentScore, Integer eventType, Integer tradeType);

    boolean addLog(CreditScoreLogs creditScoreLogs);

    PageReturn<CreditScoreLogsDTO> listPage(CreditScoreLogsListPageReq req);

    PageReturn<CreditScoreLogsDTO> listPage(long memberId, long pageSize);

    PageReturn<CreditScoreLogsVo> list(CreditScoreLogsListReq req);

}
