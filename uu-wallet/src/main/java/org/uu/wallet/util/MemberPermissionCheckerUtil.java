package org.uu.wallet.util;

import org.uu.wallet.Enum.MemberPermissionEnum;

public class MemberPermissionCheckerUtil {

    /**
     * 校验会员是否具有指定的权限。
     *
     * @param memberPermissions  会员的权限字符串，使用逗号分隔。
     * @param requiredPermission 所需的权限。
     * @return 如果会员具有所需权限，则返回 true；否则返回 false。
     */
    public static boolean hasPermission(String memberPermissions, MemberPermissionEnum requiredPermission) {
        // 首先检查传入的权限字符串是否为空
        if (memberPermissions == null || memberPermissions.isEmpty()) {
            return false;
        }

        // 将权限字符串分割成单独的权限代码
        String[] permissions = memberPermissions.split(",");

        // 遍历每个权限代码
        for (String permissionCode : permissions) {
            // 检查当前权限是否为所需的权限
            MemberPermissionEnum permission = MemberPermissionEnum.fromCode(permissionCode.trim());
            if (permission == requiredPermission) {
                return true;
            }
        }

        return false;
    }

}
