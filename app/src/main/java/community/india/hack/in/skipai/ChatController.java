package community.india.hack.in.skipai;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import community.india.hack.in.skipai.manager.ChatManager;
import community.india.hack.in.skipai.utils.ChatRequest;

public class ChatController {
    public interface Callback{
        void onToken(String token);
        void onSuccess(String response);
        void onError(String error);
    }
    private final Context context;
    private final ChatManager chatManager;
    public ChatController(Context context,ChatManager chatManager){
        this.context = context.getApplicationContext() ;
        this.chatManager = chatManager;
    }
    public void sendMessage(String message, Callback callback){

        ChatRequest request = chatManager.sendUserMessage(message);

        if (request == null){
            callback.onError("No active chat");
            return;
        }

        String history = chatManager.buildPrompts(request.getChat());

        QwenBridge.generateChat(
                context,
                request.getChat().getId(),
                message,
                history,
                new QwenBridge.Callback() {

                    @Override
                    public void onToken(@NotNull String token) {
                        callback.onToken(token);
                    }

                    @Override
                    public void onSuccess(@NotNull String response) {
                        chatManager.addAssistantMessage(request.getChat(), response);
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(@NotNull String error) {
                        callback.onError(error);
                    }
                }
        );
    }
}
