package community.india.hack.in.skipai.utils;

import android.content.Context;
import android.widget.Toast;

import community.india.hack.in.skipai.UserSettings;
import community.india.hack.in.skipai.models.AiOptions;
import community.india.hack.in.skipai.models.AiOptions;

public class PromptBuilder {




    public  static String getPrompt(AiOptions opctions , String selectedText, Context context){

        UserSettings user = new UserSettings(context);
        String lang = user.get_saved_language();
        if(lang==null) lang = "English";
        String rules = "• Never ask questions.\n" +
                "• Never require follow-up messages.\n" +
                "• Keep responses short and useful.\n" +
                "• Cover reasonable interpretations when needed.\n" +
                "• Prioritize accuracy over completeness.\n" +
                "• Never guess.\n" +

                "• Avoid unnecessary text.\n" +
                "• Provide the answer immediately.\n" +
                "• End the response after answering.\n"+
                "• You are Skip Ai, it's your identity.";




        switch (opctions){
            case ASK_AI:
                return getAskAiPrompt(selectedText,rules);
            case EXPLAIN:
                return  getExplainPrompt(selectedText,rules);
            case SIMPLIFY:
                return  getSimplifyPrompt(selectedText,rules);
            case SUMMARIZE:
                return getSummarizePrompt(selectedText,rules);
            case TRANSLATE:
                return getTranslatePrompt(selectedText,lang,rules);
            default:
                return selectedText;
        }
    }

    private static String getTranslatePrompt(String selectedText , String lang,String rules) {

        return "Translate this : "+selectedText+" :- into "+lang+" , rules: "+rules;
    }

    private static String getSummarizePrompt(String selectedText , String rules) {
        return "Summarize this text : "+ selectedText +" , while keep th following rules in mind ,"+rules;
    }

    private static String getSimplifyPrompt(String selectedText , String rules) {
        return "Simplify this text : "+ selectedText +", while keep the following rules in mind ,"+rules;
    }

    private static String getExplainPrompt(String selectedText ,String rules) {
        return "Explain this text:"+selectedText+", in simple language while keep the following rules in mind,"+rules;
    }

    private static String getAskAiPrompt(String selectedText,String rules) {
        return "question: "+selectedText+" ,keep the following rules in mind while answering , " +rules ;
    }
}
