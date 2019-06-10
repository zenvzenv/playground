package zhengwei.algorithm;

import java.util.Arrays;

/**
 * 插入排序
 * 空间复杂度O(1),时间复杂度O(n^2),稳定
 * 稳定，对于基本有序的数组最好用
 * 工作原理：通过构建有序序列，对于未排序序列，在已排序序列中从后往前扫描，找到相应位置并插入。
 * 算法描述：
 *  1.从第一个元素开始，该元素可以认定已被排序
 *  2.取出下一个元素，在已排序的序列中从后往前扫描即可
 *  3.如果该元素(已排序)大于新元素，将该已排序的元素往后挪一位
 *  4.重复步骤3，直到已排序的元素小于或等于新元素的位置
 *  5.将新元素插入该位置
 *  6.重复2~5
 * @author zhengwei AKA Sherlock
 * @since 2019/4/27 9:46
 */
public class InsertionSort {
    public static void main(String[] args) {
        int[] arr=new int[]{8,4,5,7,6,9,1,2,3};
        insertionSort(arr);
    }

    /**
     * 插入排序
     * @param arr 需要排序的数组
     */
    static void insertionSort(int[] arr){
        for (int i=0;i<arr.length-1;i++){
            for (int j=i+1;j>0;j--){
                if (arr[j]<arr[j-1]){
                    int temp=arr[j];
                    arr[j]=arr[j-1];
                    arr[j-1]=temp;
                }
            }
        }
        System.out.println(Arrays.toString(arr));
    }
}
