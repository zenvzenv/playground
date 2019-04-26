package zhengwei.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * hdfs的输入输出流的测试
 * @author  zhengwei
 * @category com.zw.hdfs
 *
 */
public class HDFSIO {
	/**
	 * 使用IO流把文件复制到hdfs上
	 */
	@Test
	public void putFileToHdfs() {
		FileSystem fs=null;
		FileInputStream fis=null;
		FSDataOutputStream fos=null;
		try {
			Configuration conf=new Configuration();
//			DistributedFileSystem fs = FileSystem.get(new URI("hdfs://"), conf, "linkage");
			//1.获取hdfs对象
//			DistributedFileSystem fs=new DistributedFileSystem(new URI("hdfs://cm-0:8020"), conf, "linkage");
			fs=FileSystem.get(new URI("hdfs://cm-0:8020"), conf, "linkage");
			//2.获取输入流
			fis=new FileInputStream(new File("C:/Users/zhengwei/Desktop/拓扑图数据.json"));
			//3.获取输出流
			fos = fs.create(new Path("hdfs://cm-0:8020/iot-3s/zwtest/input"));
			//4.拷贝流
			IOUtils.copyBytes(fis, fos, conf);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//5.关闭流
			IOUtils.closeStream(fos);
			IOUtils.closeStream(fis);
			try {
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 把文件从hdfs上复制到本地
	 */
	@Test
	public void getFileFromHdfs() {
		FileSystem fs=null;
		FSDataInputStream fis=null;
		FileOutputStream fos=null;
		try {
			Configuration conf=new Configuration();
			//1.获取hdfs对象
			fs=FileSystem.get(new URI("hdfs://cm-0:8020"), conf, "linkage");
			//2.获取输入流
			fis = fs.open(new Path("hdfs://cm-0:8020/iot-3s/zwtest/spark-examples_2.11-2.1.3.jar"));
			//3.获取输出流
			fos=new FileOutputStream(new File("c:/temp/test.jar"));
			//4.拷贝流
			IOUtils.copyBytes(fis, fos, conf);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//5.关闭流
			IOUtils.closeStream(fos);
			IOUtils.closeStream(fis);
			try {
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 读取第一块文件
	 */
	@Test
	public void readFileSheek1() {
		FileSystem fs=null;
		FSDataInputStream fis=null;
		FileOutputStream fos=null;
		try {
			Configuration conf=new Configuration();
			//1.获取hdfs对象
			fs = FileSystem.get(new URI(""), conf, "linkage");
			//2.获取输入流
			fis = fs.open(new Path("/user/zw/hadoop-2.9.1.tar.gz"));
			//3.获取输出流
			fos = new FileOutputStream(new File("C:/user/zhengwei/Destop/hadoop-2.9.1.tar.gz.part1"));
			//流的对拷
			byte[] buf=new byte[1024];
			for (int i = 0; i < 1024*128; i++) {
				fis.read(buf);
				fos.write(buf);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//5.关闭流
			IOUtils.closeStream(fos);
			IOUtils.closeStream(fis);
			try {
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
