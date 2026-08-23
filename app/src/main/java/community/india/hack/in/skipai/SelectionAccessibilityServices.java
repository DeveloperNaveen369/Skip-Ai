package community.india.hack.in.skipai;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import community.india.hack.in.skipai.manager.FloatingWindowManager;

public class SelectionAccessibilityServices extends AccessibilityService {

    private FloatingWindowManager floatingWindowManager;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event==null) return;
        if (! isValidSelection(event)) return;

        int type = event.getEventType();
        if(type != AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) return ;
        String selectedText = getSelectedText(event);
        if (selectedText.length()<0) return;


        floatingWindowManager.updateSelection(selectedText);





    }

    @Override
    public void onInterrupt() {

    }
    @Override
    public void onServiceConnected(){
        super.onServiceConnected();
        floatingWindowManager = new FloatingWindowManager(this);


    }



    private boolean isValidSelection(AccessibilityEvent event){

        int from = event.getFromIndex();
        int to = event.getToIndex();
        if (from==to) return false;

        return  true;
    }
    private String getSelectedText(AccessibilityEvent event){
        int from = event.getFromIndex();
        int to = event.getToIndex();
        String rawText = event.getText().get(0).toString();
        try{
            if (rawText ==  null) return "";
            if(from<0 || to<0) return  "";
            if(from>=to) return "";

            return rawText.substring(from,to);

        } catch (Exception e) {
            return "";
        }




    }
}
