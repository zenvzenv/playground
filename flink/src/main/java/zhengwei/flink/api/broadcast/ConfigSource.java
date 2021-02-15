package zhengwei.flink.api.broadcast;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * [用户 id，[姓名， 年龄]]
 *
 * @author zhengwei AKA zenv
 * @since 2021/2/15 20:58
 */
public class ConfigSource extends RichSourceFunction<Map<String, Tuple2<String, Integer>>> {
    private volatile boolean flag = true;
    private Connection connection = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    private final String url;
    private final String user;
    private final String password;

    public ConfigSource(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        connection = DriverManager.getConnection(url, user, password);
        String sql = "select userId,userName,userAge from user_info";
        preparedStatement = connection.prepareStatement(sql);
    }

    @Override
    public void run(SourceContext<Map<String, Tuple2<String, Integer>>> ctx) throws Exception {
        while (flag) {
            resultSet = preparedStatement.executeQuery();
            Map<String, Tuple2<String, Integer>> result = new HashMap<>();
            while (resultSet.next()) {
                final String userId = resultSet.getString("userId");
                final String userName = resultSet.getString("userName");
                final int userAge = resultSet.getInt("userAge");
                result.put(userId, Tuple2.of(userName, userAge));
            }
            ctx.collect(result);
            //每隔一天更新配置信息
            TimeUnit.DAYS.sleep(1);
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }

    @Override
    public void close() throws Exception {
        if (null != connection) connection.close();
        if (null != preparedStatement) preparedStatement.close();
        if (null != resultSet) resultSet.close();
    }
}
