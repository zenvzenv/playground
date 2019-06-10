package zhengwei.algorithm;

import java.util.Arrays;

/**
 * 冒泡排序
 * 时间复杂度：O(n^2),空间复杂度:O(1),稳定
 * 思想：冒泡排序的时间复杂度虽然和选择排序相同，但是在代码实际运行中，冒泡排序的效率会比选择排序的效率要高
 * 因为冒泡排序每排完一次序之后，下次再要排序的数组长度会比上次一次的长度要短，
 * 外循环来控制需要排序的数组的长度，内循环来遍历接下来需要比较的数组内容以比较大小。
 * @author zhengwei AKA Sherlock
 * @since 2019/4/27 9:31
 */
public class BubbleSort {
    public static void main(String[] args) {
        int[] arr=new int[]{1,5,3,4,6,7,8,1,2,3,9};
        /*for (int i = arr.length-1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (arr[j] > arr[j+1]) CommonUtils.swap(j+1, j, arr);
            }
        }
        System.out.println(Arrays.toString(arr));*/
        bubbleSort(arr);
    }

    /**
     * 优化的冒泡排序
     * 只要本趟排序发生了交换就把flag置为true，这样在可以减少循环次数
     * @param arr 要排序的数组
     */
    static void bubbleSort(int[] arr){
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
