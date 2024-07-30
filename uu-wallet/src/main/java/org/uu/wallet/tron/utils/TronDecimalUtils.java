package org.uu.wallet.tron.utils;

import java.math.BigDecimal;

public class TronDecimalUtils {
    
    
    private final static int SCALE = 6;
    
    /**
     * 加法
     *
     * @param b1
     * @param bn
     * @return
     */
    public static BigDecimal safeAdd(BigDecimal b1, BigDecimal... bn) {
        if (null == b1) {
            b1 = BigDecimal.ZERO;
        }
        if (null != bn) {
            for (BigDecimal b : bn) {
                b1 = b1.add(null == b ? BigDecimal.ZERO : b);
            }
        }
        return b1;
    }
    
    
    /**
     * 减法
     *
     * @param b1 被减数
     * @param bn 减数
     * @return
     */
    public static BigDecimal safeSubtract(BigDecimal b1, BigDecimal... bn) {
        return safeSubtract(true, b1, bn);
    }
    
    public static BigDecimal safeSubtract(Boolean isZero, BigDecimal b1, BigDecimal... bn) {
        if (null == b1) {
            b1 = BigDecimal.ZERO;
        }
        BigDecimal r = b1;
        if (null != bn) {
            for (BigDecimal b : bn) {
                r = r.subtract((null == b ? BigDecimal.ZERO : b));
            }
        }
        //return isZero ? (r.compareTo(BigDecimal.ZERO) == -1 ? BigDecimal.ZERO : r) : r;
        return r;
    }
    
    
    /**
     * 除法
     *
     * @param b1  被除数
     * @param b2  除数
     * @param <T>
     * @return
     */
    public static <T extends Number> BigDecimal safeDivide(BigDecimal b1, BigDecimal b2) {
        return safeDivide(b1, b2, BigDecimal.ZERO);
    }
    
    public static <T extends Number> BigDecimal safeDivide(BigDecimal b1, BigDecimal b2, BigDecimal defaultValue) {
        if (null == b1 || null == b2) {
            return defaultValue;
        }
        try {
            return b1.divide(b2, SCALE, BigDecimal.ROUND_DOWN);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static <T extends Number> BigDecimal safeDivide2(BigDecimal b1, BigDecimal b2) {
        if (null == b1 || null == b2) {
            return BigDecimal.ZERO;
        }
        try {
            return b1.divide(b2, 2, BigDecimal.ROUND_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public static <T extends Number> BigDecimal safeDivide3(BigDecimal b1, BigDecimal b2) {
        if (null == b1 || null == b2) {
            return BigDecimal.ZERO;
        }
        try {
            return b1.divide(b2, 2, BigDecimal.ROUND_DOWN);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 乘法
     *
     * @param b1
     * @param b2
     * @return
     */
    public static BigDecimal safeMultiply(BigDecimal b1, BigDecimal b2) {
        if (null == b1 || null == b2) {
            return BigDecimal.ZERO;
        }
        return b1.multiply(b2).setScale(SCALE, BigDecimal.ROUND_DOWN);
    }

    /**
     * 乘法
     *
     * @param b1
     * @param b2
     * @param myScale
     * @return
     */
    public static BigDecimal safeMultiply(BigDecimal b1, BigDecimal b2,Integer myScale) {
        if (null == b1 || null == b2) {
            return BigDecimal.ZERO;
        }
        return b1.multiply(b2).setScale(myScale==null?SCALE:myScale, BigDecimal.ROUND_DOWN);
    }
    
    
}
