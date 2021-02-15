package zhengwei.flink.api.sql.pojo;

import lombok.*;

/**
 * @author zhengwei AKA zenv
 * @since 2021/2/14 15:45
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WordCount {
    private String word;
    private int frequency;
}
