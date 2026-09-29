package community.india.hack.in.skipai.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import community.india.hack.in.skipai.Chat;
import community.india.hack.in.skipai.ChatDao;
import community.india.hack.in.skipai.ChatMessages;
import community.india.hack.in.skipai.utils.ChatEntity;
import community.india.hack.in.skipai.utils.ChatMessagesEntity;
import community.india.hack.in.skipai.utils.ChatRequest;



public class ChatManager {
    private boolean chatsLoaded = false;
    private LoadCallback loadCallback;
    public interface LoadCallback{
        void onLoaded();
    }
    private final List<Chat> chats = new ArrayList<>();
    private Chat activeChat;

    private final ChatDao chatDao;



    public ChatManager(ChatDao chatDao) {
        this.chatDao = chatDao;
        loadChats();
    }

    private void loadChats() {
        new Thread(() -> {

            List<ChatEntity> savedChats = chatDao.getAllChats();

            for (ChatEntity entity : savedChats) {

                Chat chat = new Chat(
                        entity.id,
                        entity.title,
                        entity.createdAt,
                        entity.updatedAt
                );

                List<ChatMessagesEntity> savedMessages =
                        chatDao.getMessages(entity.id);

                for (ChatMessagesEntity messagesEntity : savedMessages) {

                    ChatMessages.Role role =
                            ChatMessages.Role.valueOf(messagesEntity.role);

                    ChatMessages message = new ChatMessages(
                            role,
                            messagesEntity.content
                    );

                    chat.addMessage(message);
                }

                chats.add(chat);
            }

            if (!chats.isEmpty()) {
                activeChat = chats.get(0);
            }else {
                createNewChat();
            }

            chatsLoaded = true;

            if (loadCallback != null) {
                loadCallback.onLoaded();
            }

        }).start();
    }

    public Chat createNewChat() {
        String id  = UUID.randomUUID().toString();
        Chat chat = new Chat(id,"New Chat");

        chats.add(chat);
        activeChat = chat;

        ChatEntity entity = new ChatEntity(chat.getId(),chat.getTitle(),chat.getCreatedAt(),chat.getUpdatedAt());
        new Thread(()->{
            chatDao.insertChat(entity);
        }).start();
        return chat;
    }
    public Chat getActiveChat(){
        return activeChat;
    }
    public void switchChat(String chatId){
        for (Chat chat: chats){
            if (chat.getId().equals(chatId)){
                activeChat  = chat;
                return;
            }
        }
    }
    public void deleteChat(String chatId) {

        Chat target = null;

        for (Chat chat : chats) {
            if (chat.getId().equals(chatId)) {
                target = chat;
                break;
            }
        }

        if (target == null) return;

        chats.remove(target);

        new Thread(() -> {
            chatDao.deleteMessages(chatId);
            chatDao.deleteChat(chatId);
        }).start();

        if (target == activeChat) {
            createNewChat();
        } else if (!chats.isEmpty()) {
            activeChat = chats.get(0);
        } else {
            activeChat = null;
        }
    }
    public List<Chat> getChats(){
        return chats;
    }
    public void addMessage(ChatMessages.Role role,String content){
        if (activeChat == null){
            return;
        }
        ChatMessages messages = new ChatMessages(role,content);

        activeChat.addMessage(messages);

        ChatMessagesEntity entity = new ChatMessagesEntity(
                activeChat.getId(),
                role.name(),
                content,System.currentTimeMillis()
        );
        long updatedAt = activeChat.getUpdatedAt();

        new Thread(() -> {
            chatDao.insertMessage(entity);
            chatDao.updateChat(
                    activeChat.getId(),
                    activeChat.getTitle(),
                    updatedAt
            );
        }).start();

        if (role == ChatMessages.Role.USER &&
                activeChat.getMessages().size() == 1) {

            String title = content.trim();

            if (title.length() > 25) {
                title = title.substring(0, 25) + "...";
            }

            activeChat.setTitle(title);

//            long updatedAt = activeChat.getUpdatedAt();

            new Thread(() -> {
                chatDao.updateChat(
                        activeChat.getId(),
                        activeChat.getTitle(),
                        updatedAt
                );
            }).start();
        }
    }
    public void setLoadCallback(LoadCallback callback) {
        this.loadCallback = callback;

        if (chatsLoaded) {
            callback.onLoaded();
        }
    }
    public String buildPrompts(Chat chat){
        StringBuilder prompt = new StringBuilder();
        prompt.append("<|im_start|>system\n");
        prompt.append("You are a helpful assistant.\n");
        prompt.append("<|im_end|>\n");

        for (ChatMessages message:chat.getMessages()){
            String role;
            if(message.getRole()== ChatMessages.Role.USER){
                role = "user";
            }else role = "assistant";

            prompt.append("<|im_start|>")
                    .append(role)
                    .append("\n");

            prompt.append(message.getContent());

            prompt.append("\n<|im_end|>\n");
        }
        prompt.append("<|im_start|>assistant\n");

        return prompt.toString();
    }
    public ChatRequest sendUserMessage(String message){
        if (activeChat == null) return null;
        Chat chat = activeChat;

//        chat.addMessage(new ChatMessages(ChatMessages.Role.USER,message));
//        String prompt = buildPrompts(chat);
//
//        return new ChatRequest(chat,prompt);
        addMessage(ChatMessages.Role.USER,message);
        return  new ChatRequest(chat);
    }
    public void addAssistantMessage(Chat chat,String response){
        if (chat==null) return;

        ChatMessages messages = new ChatMessages(
                ChatMessages.Role.ASSISTANT,response
        );

        chat.addMessage(messages);
        ChatMessagesEntity entity = new ChatMessagesEntity(
                chat.getId(),ChatMessages.Role.ASSISTANT.name(), response,System.currentTimeMillis()
        );

        new Thread(()->{
            chatDao.insertMessage(entity);
        }).start();
    }
}
