package zhengwei.leetcode.signalweek196;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengwei AKA Awei
 * @since 2020/7/11 22:11
 */
public class LeetCode1504NumSubmat {
    public static String reformatDate(String date) {
        Map<String, String> map = new HashMap<>();
        map.put("Jan", "01");
        map.put("Feb", "02");
        map.put("Mar", "03");
        map.put("Apr", "04");
        map.put("May", "05");
        map.put("Jun", "06");
        map.put("Jul", "07");
        map.put("Aug", "08");
        map.put("Sep", "09");
        map.put("Oct", "10");
        map.put("Nov", "11");
        map.put("Dec", "12");
        String[] strs = date.split(" ");
        String res;
        final String day = strs[0].substring(0, strs[0].length() - 2);
        if (Integer.parseInt(day) < 10) {
            res = strs[2] + "-" + map.get(strs[1]) + "-0" + day;
        } else {
            res = strs[2] + "-" + map.get(strs[1]) + "-" + day;
        }
        return res;
    }

    public static int rangeSum(int[] nums, int n, int left, int right) {
        int length = n * (n + 1) / 2;
        int[] newNums = new int[length];
        int index = 0;
        for (int i = 0; i < n; i++) {
            int sum = 0;
            for (int j = i; j < n; j++) {
                sum += nums[j];
                if (j == i) {
                    newNums[index] = nums[i];
                } else {
                    newNums[index] = sum;
                }
                index++;
            }
        }
        Arrays.sort(newNums);
        System.out.println(Arrays.toString(newNums));
        int res = 0;
        for (int i = left - 1; i <= right - 1; i++) {
            res += newNums[i];
        }
        return res;
    }

    /*public int minDifference(int[] nums) {
        final int length = nums.length;
        if (length <= 4) return 0;
        Arrays.sort(nums);

    }*/

    public static int numSub(String s) {
        String[] ones = s.split("0");
        int res = 0;
        int maxLen = 0;
        for (String str : ones) {
            maxLen = Math.max(maxLen, str.length());
        }
        int len = 1;
        while (len <= maxLen) {
            for (String one : ones) {
                if (one.length() >= len) {
                    res += (one.length() - len + 1);
                }
            }
            len++;
        }
        return res % 1000000007;
    }

    public int numSub2(String s) {
        long count = 0;
        long sum = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '1') {
                count++;
            } else {
                sum = (sum + count * (count + 1) / 2) % 1000000007;
                count = 0;
            }
        }
        sum = (sum + count * (count + 1) / 2) % 1000000007;
        return (int) sum;
    }

    public static void main(String[] args) {
        System.out.println(1e9 + 7);
    }

}
