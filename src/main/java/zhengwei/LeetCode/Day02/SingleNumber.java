package zhengwei.LeetCode.Day02;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengwei
 */
public class SingleNumber {
    public int singleNumber(int[] nums) {
        int result=0;
        Map<Integer,Integer> map=new HashMap<Integer,Integer>();
        for(int i=0;i<nums.length;i++){
            if(!map.containsKey(nums[i])){
                map.put(nums[i],1);
            }else{
                Integer v = map.get(nums[i]);
                v++;
                map.put(nums[i],v);
            }
        }
        for (Map.Entry<Integer,Integer> entry:map.entrySet()){
            if (entry.getValue()==1){
                result=entry.getKey();
            }
        }
        return result;
    }
}
