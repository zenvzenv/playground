package zhengwei.leetcode.daily;

import java.util.ArrayList;
import java.util.List;

/**
 * 93. 复原IP地址
 */
public class LeetCode93RestoreIpAddresses {
    private static final int SEG_COUNT = 4;

    public static List<String> restoreIpAddresses(String s) {
        List<String> res = new ArrayList<>();
        //IP地址的分段信息，长度为4，代表点分格式的四个数
        int[] segArr = new int[SEG_COUNT];
        dfs(s, segArr, res, 0, 0);
        return res;
    }

    private static void dfs(String s, int[] segArr, List<String> list, int segId, int segStart) {
        //如果找到了 4 段 IP 地址并且遍历完了字符串，那么就是一种答案
        if (segId == SEG_COUNT) {
            //如果已经迭代完了四个数，并且字符串的长度和源字符串一致，则代表找到了一个IP
            if (segStart == s.length()) {
                StringBuilder ip = new StringBuilder();
                for (int i = 0; i < SEG_COUNT; i++) {
                    if (i == SEG_COUNT - 1) {
                        ip.append(segArr[i]);
                    } else {
                        ip.append(segArr[i]).append(".");
                    }
                }
                list.add(ip.toString());
            }
            return;
        }
        if (segStart == s.length()) return;
        //由于前导数字不能为0，0需要单独做处理，单独分出来
        if (s.charAt(segStart) == '0') {
            segArr[segId] = 0;
            dfs(s, segArr, list, segId + 1, segStart + 1);
        }
        //一般情况
        int addr = 0;
        for (int segEnd = segStart; segEnd < s.length(); segEnd++) {
            addr = addr * 10 + (s.charAt(segEnd) - '0');
            if (addr > 0 && addr <= 255) {
                segArr[segId] = addr;
                dfs(s, segArr, list, segId + 1, segEnd + 1);
            } else {
                break;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(restoreIpAddresses("25525511135"));
    }
}
