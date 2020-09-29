package zhengwei.netty.myprotocol;

import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengwei AKA Awei
 * @since 2020/5/25 19:37
 */
@Data
public class Message {
    private int magicNumber;
    private int mainVersion;
    private int subVersion;
    private int modifyVersion;
    private MessageTypeEnum messageType;
    private Map<String, String> attachments = new HashMap<>();
    private String body;
    private String sessionId;

    public Map<String, String> getAttachments() {
        return Collections.unmodifiableMap(attachments);
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments.clear();
        if (null != attachments) {
            this.attachments.putAll(attachments);
        }
    }

    public void addAttachments(String key, String val) {
        this.attachments.put(key, val);
    }
}
