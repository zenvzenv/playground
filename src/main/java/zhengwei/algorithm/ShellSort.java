package zhengwei.algorithm;

import java.util.Arrays;

/**
 * 希尔排序，改进的插入排序
 * 时间复杂度O(n log n)，空间复杂度O(1)，不稳定
 * 插入排序的变种，希尔排序是把记录按下表的一定增量分组，对每组直接使用插入排序算法；
 * 随着增量逐渐减小，每组包含的关键词越来越多，当增量减少至1时，整个数组恰好分成一组，排序结束
 * 算法描述：
 *  1.选择增量gap=length/2，减小增量继续以gap=gap/2方式，这种增量可以用一个序列来表示{n/2,(n/2)/2...1}，称为增量序列
 *  2.选择一个增量序列，t1,t2...tn，其中ti>tj，tn=1
 *  3.按增量序列个数n,对数组进行n趟排序
 *  4.每趟排序，根据对应的增量ti，将待排序列分为长度为m的子序列，分别对各个子序列进行插入排序。
 *  5.仅当增量因子是1时，整个序列作为一个表来处理，其长度即为整个要排序数组的长度
 * @author zhengwei AKA Sherlock
 * @since 2019/5/3 15:56
 */
public class ShellSort {
    public static void main(String[] args) {
        int[] arr=new int[]{4,3,76,5,2,5,8,9,3};
        shellSort(arr);
    }

    /**
     * 希尔排序
     * @param arr 需要排序的数组
     */
    static void shellSort(int[] arr){
        int gap=arr.length>>1,temp;
        while (gap>0){
            for (int i=gap;i<arr.length;i++){
                temp=arr[i];
                int preIndex=i-gap;
                while (preIndex>=0&&arr[preIndex]>temp){
                    arr[preIndex+gap]=arr[preIndex];
                    preIndex-=gap;
                }
                arr[preIndex+gap]=temp;
            }
            gap/=2;
        }
        System.out.println(Arrays.toString(arr));
    }
}
