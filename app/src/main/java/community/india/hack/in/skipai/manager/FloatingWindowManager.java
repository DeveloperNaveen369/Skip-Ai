package community.india.hack.in.skipai.manager;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import community.india.hack.in.skipai.MarkDownManager;
import community.india.hack.in.skipai.R;

import community.india.hack.in.skipai.models.AiOptions;
import community.india.hack.in.skipai.models.AiResponseListener;
import android.view.animation.OvershootInterpolator;

public class FloatingWindowManager {

    private Context context;
    private WindowManager windowManager;
    private View floatingview;
    private  String selectedText;
    private  WindowManager.LayoutParams layoutParams;
    private  Boolean isWindowShowing = false;
    private Boolean isWindowCreated = false;
    private String previousSelectedText = "";
    private String responseText = "";
    HorizontalScrollView opctions_view;

    LinearLayout Loading_view;
    ScrollView Response_view;
    TextView Response_text_view;
    TextView Selected_text_view;
    private Chip ask_ai_btn;
    private Chip sumr_btn,trans_btn,simplify_btn;
    private  int maxHeight;
    private int displayHeight;

    private int initialX,initialY,currentX,currentY;
    private  float touchinitialX,touchinitialY;








    private enum WindowState{
        OPCTIONS,
        RESPONSE,
        LOADING
    }
    private WindowState currentWindowState = WindowState.OPCTIONS;

    public Button button;


    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AiManager aiManager = new AiManager();

    public  FloatingWindowManager(Context context){
        this.context = new ContextThemeWrapper(context,R.style.Theme_SkipAi);
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

    }
    public void showFloatingWindow(){
//        this.selectedText = selectedText;
        updateWindowPosition();

        if(! isWindowShowing){
            if(floatingview != null){

                floatingview.setVisibility(View.VISIBLE);

            }
        }


        isWindowShowing = true;



    }
    
    public void hideFloatingWindow(){

        if ( ! isWindowCreated) return;
        if(! isWindowShowing) return;
        if(floatingview !=null) floatingview.setVisibility(View.GONE);

        isWindowShowing = false;
    }
    private void  showLoadingState(){

        opctions_view.setVisibility(View.GONE);
        Response_view.setVisibility(View.GONE);
        Loading_view.setVisibility(View.VISIBLE);

        currentWindowState = WindowState.LOADING;
    }
    private void showResponseState(String response){
            responseText = response;



            showResponseState();


    }
    private  void createFloatingWindow() {
        isWindowCreated = true;
        floatingview = LayoutInflater.from(context).inflate(R.layout.floating_window, null);

        layoutParams = new WindowManager.LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.x = 0;
        layoutParams.y = 100;
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        if (floatingview != null) windowManager.addView(floatingview, layoutParams);

        isWindowShowing= true;
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        displayHeight = metrics.heightPixels;
        maxHeight = displayHeight/2;


        button = floatingview.findViewById(R.id.close_btn);

        button.setOnClickListener(v -> {

            hideFloatingWindow();
        } );
        opctions_view = floatingview.findViewById(R.id.opctions_view);
        Loading_view = floatingview.findViewById(R.id.loading_view);
        Response_view = floatingview.findViewById(R.id.response_view);
        Response_text_view = floatingview.findViewById(R.id.respone_text_view);
        Selected_text_view = floatingview.findViewById(R.id.selected_view);
        ask_ai_btn = floatingview.findViewById(R.id.Ask_ai_btn);
        sumr_btn = floatingview.findViewById(R.id.summarize_btn);
        trans_btn = floatingview.findViewById(R.id.translation_btn);
        simplify_btn  = floatingview.findViewById(R.id.simplify_btn);

        opctions_view.smoothScrollTo(500,0);
        opctions_view.smoothScrollBy(200,0);



        ask_ai_btn.setOnClickListener(v->{
            Log.d("Skip AI", "Asl Ai button clicked ");
            requestAiResponse(AiOptions.ASK_AI);
        });

        sumr_btn.setOnClickListener(v->{
            requestAiResponse(AiOptions.SUMMARIZE);
        });
        simplify_btn.setOnClickListener(v->{
            requestAiResponse(AiOptions.SIMPLIFY);

        });
        trans_btn.setOnClickListener(v->{
            requestAiResponse(AiOptions.TRANSLATE);
        });

        Selected_text_view.setOnTouchListener((v,event)->{

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    Log.d(" drag","dragging");
                    initialX = currentX;
                    initialY = currentY;
                    touchinitialX = event.getRawX();
                    touchinitialY = event.getRawY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    int xDifference,yDifference;
                    xDifference = (int) (event.getRawX() - touchinitialX);
                    yDifference = (int) (event.getRawY()- touchinitialY);
                    Log.d("drag action ", ""+xDifference +" "+ yDifference);
                    layoutParams.x = initialX + xDifference;
                    layoutParams.y = initialY + yDifference;
                    windowManager.updateViewLayout(floatingview,layoutParams);

                    currentX = layoutParams.x;
                    currentY = layoutParams.y;
                    return  true;

                case MotionEvent.ACTION_UP:

                    Log.d("DRAG","UP");

                    return true;

            }
            return true;
        });


    }
    public  void updateSelection(String selectedText){
        this.selectedText = selectedText;


        if(selectedText.equals(previousSelectedText) && isWindowShowing ) return;
        previousSelectedText  = selectedText;
        if(selectedText==null) return;
        if(selectedText.trim().isEmpty()) return;
        if(! isWindowCreated){
            createFloatingWindow();

        }
        else if(! isWindowShowing){
            updateFloatingWindow();
            showFloatingWindow();
        }
        resetFloatingWindow();
        updateFloatingWindow();
        Log.d("Skip Ai ", "updateSelection: ");


    }
    private  void updateFloatingWindow(){
        updateWindowState();
    }
    private void updateWindowPosition(){

    }
    public String getSelectedText(){
        return selectedText;
    }
    private void resetFloatingWindow(){
        currentWindowState = WindowState.OPCTIONS;

    }
    private  void updateWindowState(){
        switch (currentWindowState){
            case LOADING:
                showLoadingState();
                break;
            case OPCTIONS:
                showOpctionsState();
                break;
            case RESPONSE:
                showResponseState();
                break;
        }
    }
    private void changeWindowState(WindowState windowState){
        currentWindowState = windowState;
        updateFloatingWindow();
    }
    private void showResponseState(){

        opctions_view.setVisibility(View.GONE);
        Loading_view.setVisibility(View.GONE);
        Response_view.setVisibility(View.VISIBLE);
//        Response_text_view.setText(responseText);
        MarkDownManager markDownManager = new MarkDownManager(context);
        markDownManager.show(responseText,Response_text_view);


        floatingview.post(()->{
            Log.d("height ", String.valueOf(Response_text_view.getHeight()));
            Log.d("Max", String.valueOf(maxHeight));
            ViewGroup.LayoutParams params = Response_view.getLayoutParams();

            if(Response_text_view.getHeight() > maxHeight){
                params.height = maxHeight;

            }else{
                params.height = WRAP_CONTENT;

            }
            Response_view.setLayoutParams(params);
        });
        currentWindowState = WindowState.RESPONSE;
    }
    private void showOpctionsState(){
        if (selectedText.length()>20){
            String temp_selected = selectedText.substring(0,18);
            Selected_text_view.setText(temp_selected+" . . .");
        }else {
            Selected_text_view.setText(selectedText);
        }

        opctions_view.setVisibility(View.VISIBLE);
        Loading_view.setVisibility(View.GONE);
        Response_view.setVisibility(View.GONE);
        currentWindowState = WindowState.OPCTIONS;
    }
    private void requestAiResponse(AiOptions options){

        showLoadingState();
        aiManager.getResponse(context,options, selectedText, new AiManagerLIstener() {
            @Override
            public void onSucess(String response) {
               showResponseState(response);
            }

            @Override
            public void onFailure(int responseCode, String error) {
                 responseText = responseCode +" : "+error;
                 showResponseState(responseText);
            }
        });
    }

    private void animateAppeare(){

        floatingview.setAlpha(0f);

        floatingview.animate()
                .scaleX(1f).scaleY(1f)
                .alpha(1f).setDuration(210)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
    }


}
