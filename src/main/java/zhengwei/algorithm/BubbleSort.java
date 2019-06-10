package zhengwei.algorithm;

import java.util.Arrays;

/**
 * 冒泡排序
 * 时间复杂度：O(n^2),空间复杂度:O(1),稳定
 * 冒泡排序是一种简单的排序，重复走访要排序的数列，一次比较两个元素，如果它们的顺序错误则交换两个数的位置。
 * 走访数列的工作是重复的进行直到没有再需要的数。
 * 算法描述：
 *  1.比较相邻的两个数，如果第一个数大于第二个数，则交换俩个数的位置
 *  2.对每一对元素做同样的操作，从开始第一对到结尾最后一对，最后的数就是最大的数
 *  3.针对的所有的元素重复以上元素，除了最后一个元素
 *  4.重复1~3步骤
 * @author zhengwei AKA Sherlock
 * @since 2019/4/27 9:31
 */
public class BubbleSort {
    public static void main(String[] args) {
        int[] arr=new int[]{5,3,4,6,7,8,1,2,3,9};
        /*for (int i = arr.length-1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (arr[j] > arr[j+1]) CommonUtils.swap(j+1, j, arr);
            }
        }
        System.out.println(Arrays.toString(arr));*/
        bubbleSort1(arr);
        bubbleSort2(arr);
    }

    /**
     * 原始冒泡版本
     * @param arr 需要排序的数组
     */
    static void bubbleSort1(int[] arr){
        for (int i=0;i<arr.length;i++){
            for (int j=arr.length-1;j>i;j--){
                if (arr[j]<arr[j-1]) CommonUtils.swap(j-1,j,arr);
            }
        }
        System.out.println(Arrays.toString(arr));
    }

    /**
     * 优化的冒泡排序
     * 只要本趟排序发生了交换就把flag置为true，这样在可以减少循环次数
     * @param arr 要排序的数组
     */
    static void bubbleSort2(int[] arr){
        boolean flag;
        for (int i=0;i<arr.length-1;i++){//趟数
            flag=false;
            for (int j=arr.length-1;j>i;j--){
                if (arr[j]<arr[j-1]) {
                    CommonUtils.swap(j-1,j,arr);
                    flag=true;
                }
            }
            if (!flag) break;
        }
        System.out.println(Arrays.toString(arr));
    }
}
