package com.github.lock.util;

import java.util.UUID;

/**
 * @author wangdongbo
 * @since 2019/7/22.
 */
public class UUIDUtil {
    /**
     * 生成10位UUID
     *
     * @return
     */
    public static String getID() {
        UUID uuid = UUID.randomUUID();

        // 改变uuid的生成规则
        return HashUtil.convertToHashStr(uuid.getMostSignificantBits(), 5)
                + HashUtil.convertToHashStr(uuid.getLeastSignificantBits(), 5);
    }
}
