package org.uu.wallet.service;

public interface ValidateCodeService {
    Boolean  validate(String phone, String code);
}
