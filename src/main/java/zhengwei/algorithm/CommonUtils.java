package zhengwei.algorithm;

/**
 * 学习排序时需要的用到的一些工具类
 * @author zhengwei AKA Sherlock
 * @since 2019/4/27 9:51
 */
class CommonUtils {
    /**
     * 交换数据元素位置
     * @param front 前一个位置
     * @param behind 后一个位置
     * @param arr 比较的数组
     */
    static void swap(int front,int behind,int[] arr) {
        int temp=arr[front];
        arr[front]=arr[behind];
        arr[behind]=temp;
    }
}
