package community.india.hack.in.skipai;

import android.content.Context;
import android.widget.TextView;

import io.noties.markwon.Markwon;

public class MarkDownManager {

    private Markwon markwon;
    public MarkDownManager(Context context){
        markwon = Markwon.create(context);


    }
    public void show(String response, TextView textView){
        markwon.setMarkdown(textView,response);
    }
}
