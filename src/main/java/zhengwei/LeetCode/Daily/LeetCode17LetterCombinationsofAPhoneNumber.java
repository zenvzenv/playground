package zhengwei.LeetCode.Daily;

import java.util.*;

/**
 * 回溯法
 * 给定一个仅包含数字 2-9 的字符串，返回所有它能表示的字母组合。
 * 给出数字到字母的映射如下（与电话按键相同）。注意 1 不对应任何字母。
 * 输入："23"
 * 输出：["ad", "ae", "af", "bd", "be", "bf", "cd", "ce", "cf"].
 *
 * @author zhengwei AKA Awei
 * @since 2019/7/28 16:37
 */
public class LeetCode17LetterCombinationsofAPhoneNumber {
	private static final Map<Character, List<Character>> map = new HashMap<>();

	static {
		map.put('2', Arrays.asList('a', 'b', 'c'));
		map.put('3', Arrays.asList('d', 'e', 'f'));
		map.put('4', Arrays.asList('g', 'h', 'i'));
		map.put('5', Arrays.asList('j', 'k', 'l'));
		map.put('6', Arrays.asList('m', 'n', 'o'));
		map.put('7', Arrays.asList('p', 'q', 'r', 's'));
		map.put('8', Arrays.asList('t', 'u', 'v'));
		map.put('9', Arrays.asList('w', 'x', 'y', 'z'));
	}

	@org.jetbrains.annotations.NotNull
	static List<String> letterCombinations(String digits) {
		List<String> result=new ArrayList<>();
		DFS("",0,digits,result,map);
		return result;
	}

	private static void DFS(String curr, int currIndex, String digits, List<String> result, Map<Character, List<Character>> map) {
		if (currIndex == digits.length()) {
			result.add(curr);
		} else {
			char c = digits.charAt(currIndex);
			map.get(c).forEach(character -> {
				DFS(curr + character, currIndex + 1, digits, result, map);
			});
		}
	}
}
