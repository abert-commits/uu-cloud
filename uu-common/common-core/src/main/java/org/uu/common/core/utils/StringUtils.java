
package org.uu.common.core.utils;


    public class StringUtils {
        public static boolean isEmpty(Object s) {
            if (s == null || "".equals(s)) {
                return true;
            }
            return false;
        }


    }