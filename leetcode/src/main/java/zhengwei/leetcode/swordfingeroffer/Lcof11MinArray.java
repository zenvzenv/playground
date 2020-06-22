package zhengwei.leetcode.swordfingeroffer;

/**
 * 剑指offer第11题：旋转数组中的最小数字
 *
 * @author zhengwei AKA Awei
 * @since 2020/6/16 16:24
 */
public class Lcof11MinArray {
    private static int minArray(int[] numbers) {
        int left = 0;
        int right = numbers.length - 1;
        while (left < right) {
            int middle = (left + right) >>> 1;
            //对于middle明确大于有边界，那么middle下次迭代则可以不包含在内
            if (numbers[middle] > numbers[right])
                left = middle + 1;
            //对于middle小于右边界，middle说不定是最小值，不能忽略
            else if (numbers[middle] < numbers[right])
                right = middle;
            else
                right -= 1;
        }
        return numbers[left];
    }
}
