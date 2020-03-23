package sort;

import java.util.Arrays;

/**
 * 冒泡排序
 *
 * @author zhengwei AKA Awei
 * @since 2020/3/23 14:56
 */
public class BubbleSort {
    public static void main(String[] args) {
        int[] array = new int[]{2, 4, 8, 9, 3, 6};
        bubbleSort(array);
    }

    private static void bubbleSort(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - 1 - i; j++) {
                if (array[j] > array[j + 1]) {
                    array[j] ^= array[j + 1];
                    array[j + 1] ^= array[j];
                    array[j] ^= array[j + 1];
                }
            }
        }
        System.out.println(Arrays.toString(array));
    }
}
