package zhengwei.algorithm;

import java.util.Arrays;

/**
 * 插入排序
 * 稳定，对于基本有序的数组最好用
 * 从数组的第二个元素开始，就像扑克牌那样，如果后一个元素的值大于前一个元素，就调换元素的位置，直到后一个元素不再大于前一个元素
 * 空间复杂度O(1),时间复杂度O(n^2),稳定
 * 思想：
 * 外层循环从第二个元素开始直到整个数组的长度以来控制排序数组的长度
 * 内层循环从当前指针所指元素开始往前遍历，如果前一个元素大于后一个元素则交换元素位置
 * @author zhengwei AKA Sherlock
 * @since 2019/4/27 9:46
 */
public class InsertionSort {
    public static void main(String[] args) {
        int[] arr=new int[]{8,4,5,7,6,9,1,2,3};
        for (int i=1;i<arr.length;i++){
            for (int j=i;j>0;j--){
                if (arr[j]<arr[j-1]) CommonUtils.swap(j-1,j,arr);
            }
        }
        System.out.println(Arrays.toString(arr));
    }
}
