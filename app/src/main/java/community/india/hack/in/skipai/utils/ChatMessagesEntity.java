package community.india.hack.in.skipai.utils;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessagesEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String chatId;
    public String role;
    public String content;
    public long timestamp;

    public ChatMessagesEntity(String chatId,String role,String content , long timestamp){
        this.chatId = chatId;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

}
