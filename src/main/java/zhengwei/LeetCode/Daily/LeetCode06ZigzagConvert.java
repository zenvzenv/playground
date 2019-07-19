package zhengwei.LeetCode.Daily;

/**
 * LeetCode第6题
 * https://leetcode-cn.com/problems/zigzag-conversion/
 * 将一个给定字符串根据给定的行数，以从上往下、从左到右进行 Z 字形排列。
 * <p>
 * 比如输入字符串为 "LEETCODEISHIRING" 行数为 3 时，排列如下：
 * <p>
 * L   C   I   R
 * E T O E S I I G
 * E   D   H   N
 * 之后，你的输出需要从左往右逐行读取，产生出一个新的字符串，比如："LCIRETOESIIGEDHN"。
 * <p>
 * 请你实现这个将字符串进行指定行数变换的函数：
 * <p>
 * string convert(string s, int numRows);
 * 示例 1:
 * <p>
 * 输入: s = "LEETCODEISHIRING", numRows = 3
 * 输出: "LCIRETOESIIGEDHN"
 * 示例 2:
 * <p>
 * 输入: s = "LEETCODEISHIRING", numRows = 4
 * 输出: "LDREOEIIECIHNTSG"
 * 解释:
 * <p>
 * L     D     R
 * E   O E   I I
 * E C   I H   N
 * T     S     G
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/19 20:00
 */
public class LeetCode06ZigzagConvert {
	public String convert(String s, int numRows) {
		char[] c = s.toCharArray();
		int length = c.length;
		//横向Z字
		StringBuilder[] sb = new StringBuilder[numRows];
		//初始化StringBuilder数组
		for (int i = 0; i < sb.length; i++) {
			sb[i] = new StringBuilder();
		}
		int i = 0;
		while (i < length) {
			//Z字上横线需要遍历整个StringBuilder数组
			for (int idx = 0; idx < numRows && i < length; idx++) {
				sb[idx].append(c[i++]);
			}
			//Z字下横线只需要遍历去头和去尾之间的StringBuilder索引位
			for (int idx = numRows - 2; idx >= 1 && i < length; idx--) {
				sb[idx].append(c[i++]);
			}
		}
		for (int idx = 1; idx < sb.length; idx++) {
			sb[0].append(idx);
		}
		return sb[0].toString();
	}
}
