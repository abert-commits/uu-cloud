package org.uu.wallet.Enum;

public enum SwitchIdEnum {
    CHECK_ACTIVE_TASKS(1L, "查看活动任务是否开启"),
    KYC_SWITCH(2L, "KYC开关"),
    PAYMENT_VOUCHER_RECOGNITION(3L, "支付凭证识别开关"),
    REAL_NAME_VERIFICATION(4L, "实名认证开关"),
    REGISTRATION_CAPTCHA(5L, "手机注册验证码开关"),
    INVITATION_CODE_REGISTRATION(6L, "邀请码注册开关");

    private final Long switchId;
    private final String description;

    SwitchIdEnum(Long switchId, String description) {
        this.switchId = switchId;
        this.description = description;
    }

    public Long getSwitchId() {
        return switchId;
    }

    public String getDescription() {
        return description;
    }

    public static SwitchIdEnum fromSwitchId(Long id) {
        for (SwitchIdEnum type : values()) {
            if (type.getSwitchId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid switch ID: " + id);
    }
}
