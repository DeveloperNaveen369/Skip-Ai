package community.india.hack.in.skipai.manager;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import community.india.hack.in.skipai.QwenBridge;
import community.india.hack.in.skipai.SkipAiApplication;
import community.india.hack.in.skipai.UserSettings;
import community.india.hack.in.skipai.models.AiOptions;
import community.india.hack.in.skipai.models.AiResponseListener;
import community.india.hack.in.skipai.utils.PromptBuilder;

public class AiManager {
    private  Context context = SkipAiApplication.getContext();

    UserSettings user = new UserSettings(context);

    private  OpenRouterManager manager = new OpenRouterManager();
//    private OfflineAiManager offlineAiManager = SkipAiApplication.getInstance().getOfflineAiManager();
    private  boolean offlineMode ;





    public  interface AiResponseListner{
        void onSucess(String response);
        void onFailure(String error);

    }
    public void setOfflineMode(boolean offlineMode){
//        this.offlineMode = user.getOfflinemode();
    }
    public void getResponse(Context context, AiOptions opctions , String selectedText , AiManagerLIstener lIstener){
            String prompt = PromptBuilder.getPrompt(opctions,selectedText,context);
        Log.d("prompt",prompt);
        offlineMode = user.getOfflinemode();

        if (offlineMode){
            Log.d("offlinemode", "offline");
            StringBuilder stringResponse = new StringBuilder();
            QwenBridge.generate(context, prompt, new QwenBridge.Callback() {
                @Override
                public void onToken(@NotNull String token) {
                    stringResponse.append(token);

                    String currentResponse = stringResponse.toString();

                    new Handler(Looper.getMainLooper()).post(() -> {
                        lIstener.onSucess(currentResponse);
                    });
                }

                @Override
                public void onSuccess(@NotNull String response) {

                }

                @Override
                public void onError(@NotNull String error) {
                    lIstener.onFailure(-1, error);
                }
            });

            return;
        }


//            geminiManager.getResponse(prompt,listner);
        manager.getResponse(context,prompt, new AiResponseListener() {
            @Override
            public void onSucess(String response) {
                lIstener.onSucess(response);
            }

            @Override
            public void onFailure(int responseCode, String error) {
                lIstener.onFailure(responseCode,error);
            }
        });
    }



}
