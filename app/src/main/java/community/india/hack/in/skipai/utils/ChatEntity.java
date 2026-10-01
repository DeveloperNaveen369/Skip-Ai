package community.india.hack.in.skipai.utils;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import androidx.annotation.NonNull;

@Entity(tableName = "chats")
public class ChatEntity {
    @NonNull
    @PrimaryKey
    public String id;

    public String title;
    public long createdAt;
    public long updatedAt;

    public ChatEntity( @NonNull  String id,String title,long createdAt , long updatedAt){
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
