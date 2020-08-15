package zhengwei.hadoop.hdfs;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
public final class HdfsUtils {
    /**
     * 如果集群需要kerberos认证的话，可使用此方法进行kerberos认证
     *
     * @param conf             Hadoop的相关配置
     * @param krb5ConfFilePath krb5.conf文件的完全绝对路径
     * @param kerberosUserName kerberos的用户名
     * @param keytabFilePath   keytab文件的完全绝对路径
     */
    public static void initKerberosENV(Configuration conf,
                                       String krb5ConfFilePath,
                                       String kerberosUserName,
                                       String keytabFilePath) {
        System.setProperty("java.security.krb5.conf", krb5ConfFilePath);
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        try {
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab(kerberosUserName, keytabFilePath);
            log.info("kerberos login information {}", UserGroupInformation.getCurrentUser());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取hdfs客户端配置文件，以初始化一个Hadoop的Configuration对象
     *
     * @param confFilePaths core-site.xml,hdfs-site.xml,yarn-site.xml等配置文件的路径
     * @return Hadoop配置对象
     */
    public static Configuration initConf(String... confFilePaths) {
        Configuration conf = new Configuration();
        for (String filePath : confFilePaths) {
            conf.addResource(filePath);
        }
        return conf;
    }

    public static Configuration initConf(InputStream... confFileInputStreams) {
        Configuration conf = new Configuration();
        for (InputStream is : confFileInputStreams) {
            conf.addResource(is);
        }
        return conf;
    }

    public static Configuration initConf(Map<String, String> map) {
        Configuration conf = new Configuration();
        map.forEach(conf::set);
        return conf;
    }
}
