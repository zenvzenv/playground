package zhengwei.leetcode.lcof;

/**
 * @author zhengwei AKA Awei
 * @since 2020/8/1 16:54
 */
public class Lcof66ConstructArr {
    public int[] constructArr(int[] a) {
        if (a.length == 0) return new int[0];
        final int length = a.length;
        int[] left = new int[length];
        left[0] = 1;
        int[] right = new int[length];
        right[length - 1] = 1;
        int[] res = new int[length];
        //构建当前索引的左边数的乘积数组
        for (int i = 1; i < length; i++) {
            left[i] = left[i - 1] * a[i - 1];
        }
        //构建当前索引右边数的乘积数组
        for (int i = length - 2; i >= 0; i--) {
            right[i] = right[i + 1] * a[i + 1];
        }
        for (int i =0;i<length;i++){
            res[i]=left[i]*right[i];
        }
        return res;
    }
}
