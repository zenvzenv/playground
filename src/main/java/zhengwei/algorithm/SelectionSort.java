package zhengwei.algorithm;

import java.util.Arrays;

/**
 * 选择排序
 * 思想：从数组的第一个元素开始遍历，如果前一个元素小于后一个元素的话就交换元素的位置
 * 空间复杂度：O(1)，时间复杂度：O(n^2)，不稳定
 * @author zhengwei AKA Sherlock
 * @since 2019/4/27 8:26
 */
public class SelectionSort {
    public static void main(String[] args) {
        int[] arr=new int[]{8,4,5,7,6,9,1,2,3};
        sort(arr);
        System.out.println(Arrays.toString(arr));
    }

    /**
     * 选择排序
     * @param arr 需要排序的数组
     */
    static void sort(int[] arr){
        for (int i=0;i<arr.length-1;i++){
            for (int j=i+1;j<arr.length;j++){
                if (arr[i]>arr[j]){
                    CommonUtils.swap(i,j,arr);
                }
            }
        }
    }
}
