package org.uu.wallet.service;

import org.uu.common.core.message.ExecuteCommissionAndDividendsMessage;

public interface ExecuteCommissionOrDividendsService {
    Boolean executeCommissionOrDividends(ExecuteCommissionAndDividendsMessage message);
}
