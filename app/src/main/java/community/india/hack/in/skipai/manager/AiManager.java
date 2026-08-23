package community.india.hack.in.skipai.manager;


import android.content.Context;
import android.util.Log;

import community.india.hack.in.skipai.models.AiOptions;
import community.india.hack.in.skipai.models.AiResponseListener;
import community.india.hack.in.skipai.utils.PromptBuilder;

public class AiManager {


    private  OpenRouterManager manager = new OpenRouterManager();



    public  interface AiResponseListner{
        void onSucess(String response);
        void onFailure(String error);

    }
    public void getResponse(Context context, AiOptions opctions , String selectedText , AiManagerLIstener lIstener){
            String prompt = PromptBuilder.getPrompt(opctions,selectedText,context);
        Log.d("prompt",prompt);


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
