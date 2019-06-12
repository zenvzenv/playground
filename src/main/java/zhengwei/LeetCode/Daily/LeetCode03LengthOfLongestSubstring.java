package zhengwei.LeetCode.Daily;

/**
 * LeetCode第三题-20190604
 * https://leetcode-cn.com/problems/longest-substring-without-repeating-characters/
 * 给定一个字符串，请你找出其中不含有重复字符的 最长子串 的长度。
 * 示例 1:
 * 输入: "abcabcbb"
 * 输出: 3
 * 解释: 因为无重复字符的最长子串是 "abc"，所以其长度为 3。
 * 示例 2:
 * 输入: "bbbbb"
 * 输出: 1
 * 解释: 因为无重复字符的最长子串是 "b"，所以其长度为 1。
 * 示例 3:
 * 输入: "pwwkew"
 * 输出: 3
 * 解释: 因为无重复字符的最长子串是 "wke"，所以其长度为 3。
 *      请注意，你的答案必须是 子串 的长度，"pwke" 是一个子序列，不是子串。
 * @author zhengwei AKA Sherlock
 * @since 2019/6/4 12:44
 */
public class LeetCode03LengthOfLongestSubstring {
	public static void main(String[] args) {
		System.out.println(lengthOfLongestSubstring("pwwkew"));
	}
	static int lengthOfLongestSubstring(String s) {
		if (s==null||s.length()==0) return 0;
		//左指针，右指针，最大字串长度
		int left=0,right=0,max=0;
		boolean[] used=new boolean[128];
		while (right<s.length()){
			System.out.println(s.charAt(right));
			if (!used[s.charAt(right)]){
				used[s.charAt(right)]=true;
				right++;
			} else {
				max=Math.max(max,right-left);
				System.out.println(s.charAt(right));
				System.out.println(s.charAt(left));
				while (left<right && s.charAt(right)!=s.charAt(left)){
					used[s.charAt(left)]=false;
					left++;
				}
			}
			left++;
			right++;
		}
		return Math.max(max,right-left);
	}
}
