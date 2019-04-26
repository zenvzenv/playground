package zhengwei.LeetCode.Day02;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengwei
 * LeetCode 784. 字母大小写全排列
 */
public class LetterCasePermutation {
    public static List<String> letterCasePermutation(String S) {
        List<String> res=new ArrayList<>();
        dfs(S,0,new StringBuilder(),res);
        return res;
    }
    //i:索引位置
    public static void dfs(String s,int i,StringBuilder sb,List<String> res){
        if(i==s.length()){//如果长度为0直接返回
            res.add(sb.toString());
            return ;
        }
        char t=s.charAt(i);
        if((t>='a'&&t<='z')||(t>='A'&&t<='Z')){
            dfs(s,i+1,sb.append(String.valueOf(t)),res);
            sb.deleteCharAt(sb.length()-1);
            t=t>='a'&&t<='z'?(char)('A'+(t-'a')):(char)('a'+(t-'A'));
            dfs(s,i+1,sb.append(String.valueOf(t)),res);
            sb.deleteCharAt(sb.length()-1);
        }else{
            dfs(s,i+1,sb.append(String.valueOf(t)),res);
            sb.deleteCharAt(sb.length()-1);
        }
    }

    public static void main(String[] args) {
        letterCasePermutation("a1b2");
    }
}
