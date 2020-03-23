package sort;

import java.util.Arrays;

/**
 * @author zhengwei AKA Awei
 * @since 2020/3/23 15:51
 */
public class SelectionSort {
    public static void main(String[] args) {
        int[] array = new int[]{2, 4, 8, 9, 3, 6};
        selectionSort(array);
    }

    private static void selectionSort(int[] array) {
        int minIndex;
        for (int i = 0; i < array.length - 1; i++) {
            minIndex = i;
            for (int j = i + 1; j < array.length; j++) {
                if (array[j] < array[minIndex]) {
                    minIndex = j;
                }
            }
            if (i == minIndex) continue;
            array[i] ^= array[minIndex];
            array[minIndex] ^= array[i];
            array[i] ^= array[minIndex];
        }
        System.out.println(Arrays.toString(array));
    }
}
