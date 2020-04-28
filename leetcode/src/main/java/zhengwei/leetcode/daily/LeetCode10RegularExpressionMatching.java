package zhengwei.leetcode.daily;

/**
 * Given an input string (s) and a pattern (p), implement regular expression matching with support for '.' and '*'.
 * <p>
 * '.' Matches any single character.
 * '*' Matches zero or more of the preceding element.
 * The matching should cover the entire input string (not partial).
 * <p>
 * Note:
 * <p>
 * s could be empty and contains only lowercase letters a-z.
 * p could be empty and contains only lowercase letters a-z, and characters like . or *.
 * <p>
 * Example 1:
 * <p>
 * Input:
 * s = "aa"
 * p = "a"
 * Output: false
 * Explanation: "a" does not match the entire string "aa".
 * <p>
 * Example 2:
 * <p>
 * Input:
 * s = "aa"
 * p = "a*"
 * Output: true
 * Explanation: '*' means zero or more of the preceding element, 'a'. Therefore, by repeating 'a' once, it becomes "aa".
 * <p>
 * Example 3:
 * <p>
 * Input:
 * s = "ab"
 * p = ".*"
 * Output: true
 * Explanation: ".*" means "zero or more (*) of any character (.)".
 * <p>
 * Example 4:
 * <p>
 * Input:
 * s = "aab"
 * p = "c*a*b"
 * Output: true
 * Explanation: c can be repeated 0 times, a can be repeated 1 time. Therefore, it matches "aab".
 * <p>
 * Example 5:
 * <p>
 * Input:
 * s = "mississippi"
 * p = "mis*is*p*."
 * Output: false
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/22 18:43
 */
public class LeetCode10RegularExpressionMatching {
	public boolean isMatch(String s, String p) {
		if (s == null || p == null) return false;
		boolean[][] match = new boolean[s.length() + 1][p.length() + 1];
		match[0][0] = true;
		for (int i = 1; i <= p.length(); i++) {
			if (p.charAt(i - 1) == '*') {
				match[0][i] = match[0][i - 2];
			}
		}
		for (int si = 1; si <= s.length(); si++) {
			for (int pi = 1; pi <= p.length(); pi++) {
				if (p.charAt(pi - 1) == '.' || p.charAt(pi - 1) == s.charAt(si - 1)) {
					match[si][pi] = match[si - 1][pi - 1];
				} else if (p.charAt(pi - 1) == '*') {
					if (p.charAt(pi - 2) == s.charAt(si - 2) || p.charAt(pi - 2) == '.') {
						match[si][pi] = match[si][pi] || match[si - 1][pi];
					} else {
						match[si][pi] = match[si][pi - 2];
					}
				}
			}
		}
		return match[s.length()][p.length()];
	}
}
