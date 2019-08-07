package zhengwei.algorithm.sparsearray;

/**
 * 稀松数组
 * 用来压缩大部分数据都是一样的二维数组
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/7 9:05
 */
public class SparseArray {
	public static void main(String[] args) {
		//初始化二维数组
		int[][] chessArray = new int[11][11];
		//初始化数据
		chessArray[1][2] = 1;
		chessArray[2][3] = 2;
		System.out.println("init chess is ~~");
		print(chessArray);
		//开始压缩二维数组
		int nonZeroCount = 0;
		for (int[] first : chessArray) {
			for (int value : first) {
				if (value != 0) {
					//统计非0的个数
					nonZeroCount++;
				}
			}
		}
		//稀松数组
		int[][] result = new int[nonZeroCount + 1][3];
		result[0][0] = chessArray.length;
		result[0][1] = chessArray[0].length;
		result[0][2] = nonZeroCount;
		int sparseNonZeroCount = 0;//稀松数组中非0数据的下标
		for (int i = 0; i < chessArray.length; i++) {
			for (int j = 0; j < chessArray[0].length; j++) {
				if (chessArray[i][j] != 0) {
					++sparseNonZeroCount;
					result[sparseNonZeroCount][0] = i;
					result[sparseNonZeroCount][1] = j;
					result[sparseNonZeroCount][2] = chessArray[i][j];
				}
			}
		}
		System.out.println("sparse array is ~~");
		print(result);
		//稀松数组还原
		//获取原二维数组的长和宽
		int[][] chessReduction = new int[result[0][1]][result[0][1]];
		for (int i = 1; i <= result[0][2]; i++) {
			chessReduction[result[i][0]][result[i][1]] = result[i][2];
		}
		System.out.println("reduction chess array ~~");
		print(chessReduction);
	}

	/**
	 * 打印二位数组
	 *
	 * @param arr 需要打印的二维数组
	 */
	private static void print(int[][] arr) {
		for (int[] first : arr) {
			for (int value : first) {
				System.out.printf("%d\t", value);
			}
			System.out.println();
		}
	}
}
