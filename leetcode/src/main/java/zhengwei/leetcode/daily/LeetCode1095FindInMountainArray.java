package zhengwei.leetcode.daily;

/**
 * <p>LeetCode第1095题：山脉数组中查找目标值</p>
 * <p>利用三次二分法来解决。</p>
 * <p>第一次二分法寻找山脉数组中的最大值</p>
 * <p>第二次二分法寻找左边递增序列，看左边序列是否存在target，存在即返回</p>
 * <p>第三次二分法寻找右边递减序列，如果左边序列找不到target，则在右边序列中寻找是否存在target的值</p>
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/29 9:14
 */
public class LeetCode1095FindInMountainArray {
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1};
        MountainArray mountainArray = new MountainArrayImpl(arr);
        System.out.println(findInMountainArray(2, mountainArray));
    }

    public static int findInMountainArray(int target, MountainArray mountainArr) {
        int len = mountainArr.length();
        int top = findMountainTop(mountainArr, 0, len - 1);
        int res = findTargetFromLeft(mountainArr, 0, top, target);
        if (res != -1) return res;
        return findTargetFromRight(mountainArr, top, len - 1, target);
    }

    //找到山脉数组的山峰即最大的数
    private static int findMountainTop(MountainArray mountainArray, int left, int right) {
        while (left < right) {
            int middle = left + (right - left) / 2;
            if (mountainArray.get(middle) < mountainArray.get(middle + 1)) {
                left = middle + 1;
            } else {
                right = middle;
            }
        }
        return left;
    }

    //从山峰左边寻找target是否存在t
    private static int findTargetFromLeft(MountainArray mountainArray, int left, int right, int target) {
        while (left < right) {
            int middle = left + (right - left) / 2;
            if (mountainArray.get(middle) < target) {
                left = middle + 1;
            } else {
                right = middle;
            }
        }
        if (mountainArray.get(left) == target) return left;
        return -1;
    }

    //从山峰右边寻找target是否存在
    private static int findTargetFromRight(MountainArray mountainArray, int left, int right, int target) {
        while (left < right) {
            int middle = left + (right - left) / 2;
            if (mountainArray.get(middle) == target) return middle;
            if (mountainArray.get(middle) > target) {
                left = middle + 1;
            } else {
                right = middle;
            }
        }
        if (mountainArray.get(left) == target) return left;
        return -1;
    }
}

interface MountainArray {
    int get(int index);

    int length();
}

class MountainArrayImpl implements MountainArray {
    private final int[] arr;
    private final int size;

    MountainArrayImpl(int[] arr) {
        this.arr = arr;
        this.size = arr.length;
    }

    @Override
    public int get(int index) {
        return arr[index];
    }

    @Override
    public int length() {
        return size;
    }
}