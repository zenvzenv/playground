package zhengwei.algorithm;

import java.util.Arrays;

/**
 * 选择排序
 * 空间复杂度：O(1)，时间复杂度：O(n^2)，不稳定
 * 不论什么数据输入到排序中，它所需的时间都是O(n^2)，所以要用这种排序的时候，数据量越小越好，唯一的好处就是不占用额外的空间。
 * 选择排序是一种简单直观的排序，工作原理：首先在未排序的数组中寻找最小的元素，存放到数组的起始位置，然后从剩下的元素中继续寻找最小值，放到已排序的数组的末尾，以此类推直到数组有序
 * 算法描述：
 *  1.无序区R[1...n],有序区为空
 *  2.找到当前趟的最小值，并把最小值与当前趟的元素进行替换
 *  3.n-1趟结束，数组有序化
 * @author zhengwei AKA Sherlock
 * @since 2019/4/27 8:26
 */
public class SelectionSort {
    public static void main(String[] args) {
        int[] arr=new int[]{8,4,5,7,6,9,1,2,3};
        selectionSort(arr);
    }

    /**
     * 选择排序
     * @param arr 需要排序的数组
     */
    static void selectionSort(int[] arr){
        for (int i=0;i<arr.length;i++){//趟数
            int minIndex=i;
            for (int j=i+1;j<arr.length;j++){//寻找当前趟数的最小数
                if (arr[j]<arr[minIndex]){
                    minIndex=j;//最小值索引
                }
            }
            if (minIndex!=i){//交换最小值
                int temp=arr[i];
                arr[i]=arr[minIndex];
                arr[minIndex]=temp;
            }
        }
        System.out.println(Arrays.toString(arr));
    }
}
