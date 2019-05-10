package zhengwei.common;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * @author zhengwei AKA Sherlock
 */
public final class IpUtil {
	// 掩码位数
//	public static int markCode = 30;
	private static Pattern ipValidPattern = Pattern.compile("([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3})");


	public static boolean validIpAddress(String ip){

		return ipValidPattern.matcher(ip).find();

	}
	// ip 到10 进制转换
	public static Long ipToLong(String ip) {
		if(! validIpAddress(ip)) {
			return 0L;
		}
		long tmpIp = 0;
		String[] tmpStrArr = ip.split("\\.");
		if(tmpStrArr.length != 4) {
			return tmpIp;
		}
		for (String aTmpStrArr : tmpStrArr) {
			tmpIp = tmpIp << 8;
			tmpIp += Long.parseLong(aTmpStrArr);
		}
		return tmpIp;
	}

	// ip 10进制 到 String 转换
	public static String ipLongToString(Long ipLong) {
		if(ipLong == null) {
			return "";
		}
		return String.valueOf(ipLong >> 24) + "." +
				(ipLong >> 16 & 0b11111111) + "." +
				(ipLong >> 8 & 0b11111111) + "." +
				(ipLong & 0b11111111);
	}

	// 获取相邻 ip (30位掩码，取反)
	public static Long getNeighborIp(Long srcIp) {
		if(srcIp == null || srcIp == 0L) {
			return 0L;
		}
		long lastTwo = srcIp & 0b11;
		if(lastTwo == 0 || lastTwo == 0b11) {
			return 0L;
		}
		return srcIp ^ 0b11;
	}

	// 获取掩码 ip
	public static long getMaskIp(Long srcIp, int markBit) {
		int ipv4MarkBit = 32 - markBit;
		return (srcIp >> ipv4MarkBit) << ipv4MarkBit;
	}

	// 判断私网IP
	public static boolean judgePrivateIp(Long ip) {
//		if(ip == 0L){
//			return true;
//		}
		// 24位区块	单个A类网络	10.0.0.0/8
		if ((ip >> 24) == 10) {
			return true;
		}
		// 20位区块	B类网络		172.16.0.0/12
		if ((ip >> 20) == 2753) {
			return true;
		}
		// 16位区块	C类网络		192.168.0.0/16
		if ((ip >> 16) == 49320) {
			return true;
		}
		// 22位区块	电信级NAT场景	100.64.0.0/10
		if ((ip >> 22) == 401) {
			return true;
		}
		// 1.1.1.1
		// 1.1.1.2
		// 2.2.2.1
		// 2.2.2.2
		return ip == 16843009 || ip == 16843010 || ip == 33686017 || ip == 33686018;
	}

	// 判断私网IP
	public static boolean judgePrivateIp(String ipStr) {
		long ip = ipToLong(ipStr);
		return judgePrivateIp(ip);
	}

	// 判断 ip 是否在一条线上
	public static boolean ipEqualJudge(String ip1, String ip2) {
		if (ip1.equals(ip2)) {
			return true;
		} else {
			return ipEqualJudge(ipToLong(ip1), ipToLong(ip2));
		}
	}

	// 判断 ip 是否在一条线上
	public static boolean ipEqualJudge(Long ip1, Long ip2) {
		if (ip1.equals(ip2)) {
			return true;
		} else {
			return ip1.equals(getNeighborIp(ip2));
		}
	}
	/**
	 * 把long转成点分格式的IP
	 * @param number 需要转换的long
	 * @return 转换完的点分格式的IP
	 */
	public static String numberToIp(Long number) {
		StringBuilder ip = new StringBuilder();
		for (int i = 3; i >= 0; i--) {
			ip.append(String.valueOf((number & 0xff)));
			if (i != 0) {
				ip.append(".");
			}
			number = number >> 8;
		}
		return ip.toString();
	}
	/**
	 * 根据掩码位数获取掩码
	 */
	public static String getNetMask(String mask) {
		int inetMask =Integer.parseInt(mask);
		if(inetMask > 32){
			return null;
		}
		//子网掩码为1占了几个字节
		int num1 = inetMask/8;
		//子网掩码的补位位数
		int num2 = inetMask%8;
		int array[] = new int[4];
		for (int i = 0; i < num1; i++) {
			array[i] = 255;
		}
		for (int i = num1; i < 4; i++) {
			array[i] = 0;
		}
		for (int i = 0; i < num2; num2--) {
			array[num1] += Math.pow(2, 8-num2);
		}
		return array[0] + "." + array[1] + "." + array[2] + "." + array[3];
	}

	/**
	 * 根据子网掩码获取起始IP
	 * @param ipinfo IP段
	 * @param netMask 子网掩码
	 * @return 起始IP
	 */
	public static String getStartAddr(String ipinfo, String netMask) {
		String lowAddr = "";
		int ipArray[] = new int[4];
		int netMaskArray[] = new int[4];
		if(4 != ipinfo.split("\\.").length || "".equals(netMask)){
			return null;
		}
		System.out.println(ipinfo);
		for (int i = 0; i < 4; i++) {
			try{
				String s = StringUtils.splitPreserveAllTokens(ipinfo,".")[i];
				ipArray[i] = Integer.parseInt(s);
			}catch(NumberFormatException e){
				String ip = ipinfo.replaceAll("\n", "");
				ipArray[i] = Integer.parseInt(ip.split("[.]")[i]);
			}
			netMaskArray[i] = Integer.parseInt(netMask.split("[.]")[i]);
			if(ipArray[i] > 255 || ipArray[i] < 0 || netMaskArray[i] > 255 || netMaskArray[i] < 0){
				return null;
			}
			ipArray[i] = ipArray[i]&netMaskArray[i];
		}
		//构造最小地址
		for (int i = 0; i < 4; i++){
			if(i == 3){
				ipArray[i] = ipArray[i] + 1;
			}
			if ("".equals(lowAddr)){
				lowAddr +=ipArray[i];
			} else{
				lowAddr += "." + ipArray[i];
			}
		}
		return lowAddr;
	}

	/**
	 * 获取结束IP
	 * @param ipinfo IP段
	 * @param netMask 子网掩码
	 * @return 结束IP
	 */
	public static String getEndAddr(String ipinfo, String netMask) {
		String lowAddr = getStartAddr(ipinfo, netMask);
		int hostNumber = getHostNumber(netMask);
		if("".equals(lowAddr) || hostNumber == 0){
			return null;
		}
		int lowAddrArray[] = new int[4];
		for (int i = 0; i < 4; i++) {
			assert lowAddr != null;
			lowAddrArray[i] = Integer.parseInt(lowAddr.split("\\.")[i]);
			if(i == 3){
				lowAddrArray[i] = lowAddrArray[i] - 1;
			}
		}
		lowAddrArray[3] = lowAddrArray[3] + (hostNumber - 1);
		if(lowAddrArray[3] >255){
			int k = lowAddrArray[3] / 256;
			lowAddrArray[3] = lowAddrArray[3] % 256;
			lowAddrArray[2] = lowAddrArray[2] + k;
		}
		if(lowAddrArray[2] > 255){
			int  j = lowAddrArray[2] / 256;
			lowAddrArray[2] = lowAddrArray[2] % 256;
			lowAddrArray[1] = lowAddrArray[1] + j;
			if(lowAddrArray[1] > 255){
				int  k = lowAddrArray[1] / 256;
				lowAddrArray[1] = lowAddrArray[1] % 256;
				lowAddrArray[0] = lowAddrArray[0] + k;
			}
		}
		String highAddr = "";
		for(int i = 0; i < 4; i++){
			if(i == 3){
				lowAddrArray[i] = lowAddrArray[i] - 1;
			}
			if("".equals(highAddr)){
				highAddr = lowAddrArray[i]+"";
			}else{
				highAddr += "." + lowAddrArray[i];
			}
		}
		return highAddr;
	}

	/**
	 * 获取实际可使用的IP数量
	 * @param netMask 子网掩码
	 * @return 可用的子网数量
	 */
	public static int getHostNumber(String netMask) {
		int hostNumber = 0;
		int netMaskArray[] = new int[4];
		for (int i = 0; i < 4 ; i++) {
			netMaskArray[i] = Integer.parseInt(netMask.split("\\.")[i]);
			if(netMaskArray[i] < 255){
				hostNumber =  (int) (Math.pow(256,3-i) * (256 - netMaskArray[i]));
				break;
			}
		}
		return hostNumber;
	}
}
