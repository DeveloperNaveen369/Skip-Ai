package community.india.hack.in.skipai;

public class ChatMessages {
    public enum Role{
        USER,
        ASSISTANT
    }
    private final Role role;
    private final String content ;
    public ChatMessages(Role role,String content){
        this.role = role;
        this.content = content;

    }
    public Role getRole(){
        return role;
    }
    public String getContent(){
        return content;
    }
}
