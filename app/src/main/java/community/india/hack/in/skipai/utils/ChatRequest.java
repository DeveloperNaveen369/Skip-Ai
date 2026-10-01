package community.india.hack.in.skipai.utils;

import community.india.hack.in.skipai.Chat;

public class ChatRequest {
    private final Chat chat;
//    private final String prompt;

    public ChatRequest(Chat chat){
        this.chat = chat;
//        this.prompt = prompt;
    }

    public Chat getChat(){
        return chat;
    }
//    public String getPrompt(){
//        return prompt;
//    }
}
