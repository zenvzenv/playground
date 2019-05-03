package zhengwei.algorithm;

/**
 * 学习排序时需要的用到的一些工具类
 * @author zhengwei AKA Sherlock
 * @since 2019/4/27 9:51
 */
public class CommonUtils {
    /**
     * 交换数据元素位置
     * @param small 较小元素位置
     * @param big 较大元素位置
     * @param arr 比较的数组
     */
    static void swap(int small,int big,int[] arr) {
        int temp=arr[small];
        arr[small]=arr[big];
        arr[big]=temp;
    }
}
