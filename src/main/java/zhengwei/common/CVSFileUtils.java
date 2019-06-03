package zhengwei.common;

import com.csvreader.CsvWriter;

import java.io.*;
import java.nio.charset.Charset;

/**
 * 针对输出起始IP段
 * @author zhengwei AKA Sherlock
 * @since 2019/6/3 15:49
 */
public class CVSFileUtils {
	private static final String FILE_PATH="src/main/resources/input.csv";
	private static final String CHAR_SET="UTF-8";
	private static final String CSV_SEPARATOR=",";
	private static final String FILE_OUTPUT_PATH="src/main/resources/output.csv";

	public static void main(String[] args) {
		try {
			readCSVFile(FILE_PATH,0,1,2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 针对起始IP段输出csv文件
	 * @param filePath 输入文件路径
	 * @param idIdx 用户id的文件索引(从0开始)
	 * @param srcIpIdx 起始IP索引
	 * @param destIpIdx 结束IP索引
	 * @throws IOException 异常
	 */
	static void readCSVFile(String filePath,int idIdx,int srcIpIdx,int destIpIdx) throws IOException {
		InputStreamReader isr=null;
		BufferedReader br=null;
		InputStream is=null;
		try {
			is = new FileInputStream(FILE_PATH);
//			is=CVSFileUtils.class.getClassLoader().getResourceAsStream(filePath);
			isr=new InputStreamReader(is, CHAR_SET);
			br=new BufferedReader(isr);
			String rec=null;
			String[] argsArr;
			CsvWriter csvWriter=new CsvWriter(FILE_OUTPUT_PATH, ',', Charset.forName(CHAR_SET));
			while(SysUtils.isNotNull(rec=br.readLine())) {
				String[] content=new String[3];
				argsArr=rec.split(CSV_SEPARATOR);
				String id = argsArr[idIdx];
				String srcIp = argsArr[srcIpIdx];
				String destIp = argsArr[destIpIdx];
				String srcIpStr = IpUtil.ipToLong(srcIp).toString();
				String destIpStr = IpUtil.ipToLong(destIp).toString();
				content[0]=srcIpStr;
				content[1]=destIpStr;
				content[2]=id;
				csvWriter.writeRecord(content);
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			is.close();
			br.close();
			isr.close();
		}
	}
}
