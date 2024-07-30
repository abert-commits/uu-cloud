package org.uu.wallet.service;

import org.uu.common.core.message.CommissionAndDividendsMessage;

public interface CalcCommissionAndDividendsService {
    Boolean calcCommission(CommissionAndDividendsMessage commissionMessage);

    Boolean calcDividends(CommissionAndDividendsMessage dividendsMessage);
}
