package org.uu.wallet.service;


import org.uu.common.core.dto.AntLoginRecordMessage;

public interface ProcessAntLogService {
    boolean processAntLoginRecord(AntLoginRecordMessage antLoginRecordMessage);
}
