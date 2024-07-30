//package org.uu.wallet.Enum;
//
///*
// * 匹配池订单状态枚举
// * */
//public enum MatchPoolOrderStatusEnum {
//
//
//    MATCHING("1", "匹配中"),
//    COMPLETED("2", "匹配完成"),
//    CANCEL("3", "已取消"),
//    TIMEOUT("4", "匹配超时");
//
//
//    private final String code;
//
//    private final String name;
//
//
//    MatchPoolOrderStatusEnum(String code, String name) {
//        this.code = code;
//        this.name = name;
//    }
//
//    public static String getNameByCode(String code) {
//        for (MatchPoolOrderStatusEnum c : MatchPoolOrderStatusEnum.values()) {
//            if (c.getCode().equals(code)) {
//                return c.getName();
//            }
//
//        }
//        return null;
//    }
//
//    public String getCode() {
//        return code;
//    }
//
//    public String getName() {
//        return name;
//    }
//}
