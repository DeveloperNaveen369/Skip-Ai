package community.india.hack.in.skipai;

import static com.google.common.io.Files.touch;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private final String id;
    private String title;
    private final long createdAt;
    private long updatedAt;

    private final List<ChatMessages> messages =  new ArrayList<>();

    public Chat(String id,String title){
        this.id = id;
        this.title = title;

        long now = System.currentTimeMillis();

        this.createdAt = now;
        this.updatedAt = now;
    }
    public Chat(String id, String title, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public String getId(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
        touch();
    }
    public long getCreatedAt(){
        return createdAt;
    }
    public long getUpdatedAt(){
        return updatedAt;
    }
    public List<ChatMessages> getMessages(){
        return messages;
    }
    public void addMessage(ChatMessages message){
        messages.add(message);
        touch();
    }
    private void touch(){
        updatedAt = System.currentTimeMillis();
    }
}
