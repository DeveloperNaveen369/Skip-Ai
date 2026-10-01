package community.india.hack.in.skipai;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import community.india.hack.in.skipai.manager.FloatingWindowManager;

public class SelectionAccessibilityServices extends AccessibilityService {

    private FloatingWindowManager floatingWindowManager;
    private AccessibilityNodeInfo focusedInputNode;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event==null) return;
        focusedInputNode = event.getSource();
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if(root!=null)
            focusedInputNode = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // Prompt user to grant "Display over other apps" permission
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Toast.makeText(this, "Skip Ai: Display over other apps permission", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else {
                // Permission already granted -> proceed to show floating window
                floatingWindowManager = new FloatingWindowManager(this);
            }
        }




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
    public void writeResponseToInput(String response){
        if (focusedInputNode==null) return;

        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,response);

        focusedInputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,arguments);
    }
    public String readResponseFromInput(){
        if (focusedInputNode==null) return null;
        Bundle arguments = new Bundle();
        CharSequence charSequence = focusedInputNode.getText();
        String text;
        if (charSequence!=null)  text = charSequence.toString();
        else text = null;
        return  text;
    }
    public void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // Prompt user to grant "Display over other apps" permission
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );
                startActivity(intent);
            } else {
                // Permission already granted -> proceed to show floating window
            }
        }
    }
}
