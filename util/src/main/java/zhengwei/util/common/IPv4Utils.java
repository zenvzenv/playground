package zhengwei.util.common;

import java.util.regex.Pattern;

/**
 * IPv4 工具类
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/14 9:23
 */
public final class IPv4Utils {
    private IPv4Utils() {
    }

    private static final String chunkIPv4 = "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
    private static final Pattern pattenIPv4 = Pattern.compile("^(" + chunkIPv4 + "\\.){3}" + chunkIPv4 + "$");
    //获取 long 型数字的低32位
    private static final long LOW_32_BITS = 0x00000000ffffffffL;

    private static boolean validIPv4(String ip) {
        return pattenIPv4.matcher(ip).matches();
    }

    /**
     * 将点分格式 IP 转换成长整型 IP
     *
     * @param ip 点分格式 IP
     * @return 长整型 IP
     */
    public static long ipToLong(String ip) {
        if (!validIPv4(ip)) return -1L;
        final String[] segment = ip.split("[.]");
        return (Long.parseLong(segment[0]) << 24) +
                (Long.parseLong(segment[1]) << 16) +
                (Long.parseLong(segment[2]) << 8) +
                (Long.parseLong(segment[3]));
    }

    /**
     * 长整型 IP 转换成点分格式的 IP
     *
     * @param ipLong 长整型 IP
     * @return 点分格式 IP
     */
    public static String longToIp(long ipLong) {
        return (ipLong >> 24) + "." +
                (ipLong >> 16 & 0b11111111) + "." +
                (ipLong >> 8 & 0b11111111) + "." +
                (ipLong & 0b11111111);
    }

    /**
     * 根据子网掩码位数获取子网掩码的长整型数
     *
     * @param maskBit 子网掩码位数
     * @return 子网掩码长整型数
     */
    public static long getMaskLong(int maskBit) {
        long result = 0L;
        for (int i = maskBit; i >= 1; i--) {
            //需要使用 long 型，因为掩码最小值为 128.0.0.0，即为 2147483648，也就是 Integer.MAX_VALUE + 1，会导致溢出
            result |= (1L << (32 - i));
        }
        return result;
    }

    /**
     * 根据子网掩码位数获取点分格式的子网掩码
     *
     * @param maskBit 子网掩码位数
     * @return 点分格式子网掩码
     */
    public static String getMaskStr(int maskBit) {
        return longToIp(getMaskLong(maskBit));
    }

    /**
     * 获取一个子网掩码的位数
     *
     * @param maskIpLong 长整型子网掩码
     * @return 子网掩码的位数
     */
    public static int getMaskDigit(long maskIpLong) {
        /*int result = 0;
        while (maskIpLong != 0) {
            maskIpLong &= (maskIpLong - 1);
            result++;
        }
        return result;*/
        return Long.bitCount(maskIpLong);
    }

    /**
     * 根据点分格式子网掩码获取子网掩码的位数
     *
     * @param maskIpStr 点分格式子网掩码
     * @return 子网掩码位数
     */
    public static int getMaskDigit(String maskIpStr) {
        return getMaskDigit(ipToLong(maskIpStr));
    }

    /**
     * 获取一个网段的起始 IP
     *
     * @param ip      该网段中的一个 IP，长整型
     * @param maskBit 子网掩码位数
     * @return 起始 IP
     */
    public static long getStartIp(long ip, int maskBit) {
        return ip & getMaskLong(maskBit);
    }

    /**
     * 获取一个网段的起始点分格式 IP
     *
     * @param ip      点分格式 IP
     * @param maskBit 子网掩码位数
     * @return 网段的起始点分格式 IP
     */
    public static String getStartIp(String ip, int maskBit) {
        return longToIp(getStartIp(ipToLong(ip), maskBit));
    }

    /**
     * 获取一个网段的长整型结束 IP
     *
     * @param ip      长整型 IP
     * @param maskBit 子网掩码位数
     * @return 长整型结束 IP
     */
    public static long getEndIp(long ip, int maskBit) {
        return getStartIp(ip, maskBit) | ~getMaskLong(maskBit) & LOW_32_BITS;
    }

    /**
     * 获取一个网段的点分格式结束 IP
     *
     * @param ip      点分格式 IP
     * @param maskBit 子网掩码位数
     * @return 点分格式结束 IP
     */
    private static String getEndIp(String ip, int maskBit) {
        return longToIp(getEndIp(ipToLong(ip), maskBit));
    }

    /**
     * 获取 IP 的二进制格式
     *
     * @param ip 长整型 IP
     * @return IP 的二进制形式
     */
    private static String toBinaryString(long ip) {
        return Long.toBinaryString(ip);
    }

    private static String toBinaryString(String ip) {
        return toBinaryString(ipToLong(ip));
    }
}
