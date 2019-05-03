package zhengwei.algorithm;

import java.util.Arrays;
import java.util.Random;

/**
 * 对数器
 * 检验我们写的排序算法是否正确
 * @author zhengwei AKA Sherlock
 * @since 2019/4/27 8:52
 */
public class DataChecker {
    private static int[] genRandomArr(){
        Random random=new Random();
        int[] arr=new int[1000];
        for (int i=0;i<arr.length;i++){
            arr[i]=random.nextInt();
        }
        return arr;
    }
    private static void check(){
        int[] arr1=genRandomArr();
        int[] arr2=new int[arr1.length];
        System.arraycopy(arr1,0,arr2,0,arr1.length);
        Arrays.sort(arr1);
        SelectionSort.sort(arr2);
        boolean flag=true;
        for (int i=0;i<arr1.length;i++){
            if (arr1[i]!=arr2[i]) flag=false;
        }
        if (flag) System.out.println("success");
        else System.out.println("false");
    }

    public static void main(String[] args) {
        check();
    }
}
