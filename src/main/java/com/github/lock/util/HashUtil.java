package com.github.lock.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wangdongbo
 * @since 2019/7/22.
 */
@Slf4j
public class HashUtil {

    private static char[] CHAR_DIGITS = {
            '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'};

    public static String convertToHashStr(long hid, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = CHAR_DIGITS[(int) ((hid & 0xff) % CHAR_DIGITS.length)];
            sb.append(c);
            hid = hid >> 6;
        }
        return sb.toString();
    }

}
