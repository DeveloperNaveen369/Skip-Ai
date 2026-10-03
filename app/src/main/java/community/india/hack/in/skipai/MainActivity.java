package community.india.hack.in.skipai;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import community.india.hack.in.skipai.manager.AiManager;
import community.india.hack.in.skipai.manager.OfflineAiManager;

//import community.india.hack.in.skipai.manager.OfflineAiManager;


public class MainActivity extends AppCompatActivity {

    Chip overlay_per_btn,access_per_btn;
    CardView overlay_per_view;
    Spinner spinner;
    EditText Api_key_view;
    TextView get_key_url;
    Chip how_btn,issue_btn,terms_btn;
    CardView github_btn;
    Button download_model ;
    ProgressBar download_progress_bar;
    TextView download_per,text_mode_api,text_mode_local;
    MaterialButton launch_chat;
    LinearLayout down_cont,mode_cont;
    SwitchMaterial switchMaterial;
    View AccessibilityDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        UserSettings user = new UserSettings(this);


        overlay_per_btn = findViewById(R.id.overlay_per_btn);
        access_per_btn= findViewById(R.id.access_per_btn);
        overlay_per_view = findViewById(R.id.overlya_per_view);
        spinner = findViewById(R.id.spinnerLang);
        Api_key_view =findViewById(R.id.edit_api_view);
        get_key_url = findViewById(R.id.get_key_url);
        github_btn = findViewById(R.id.github_btn_card);
        how_btn=findViewById(R.id.how_btn);
        issue_btn = findViewById(R.id.issue_btn);
        terms_btn = findViewById(R.id.terms_btn);

        download_model  = findViewById(R.id.download_model_btn);
        download_progress_bar = findViewById(R.id.download_progress_bar);
        download_per = findViewById(R.id.download_percentage);
        text_mode_api = findViewById(R.id.text_mode_api);
        text_mode_local = findViewById(R.id.text_mode_local);
        launch_chat = findViewById(R.id.test_ai);
        down_cont = findViewById(R.id.down_cont);
        mode_cont = findViewById(R.id.mode_cont);
        switchMaterial = findViewById(R.id.mode_switch);

        AccessibilityDialog = getLayoutInflater().inflate(R.layout.accessibility_declaration,null);
        AiManager aiManager = new AiManager();

        OfflineAiManager offlineAi =  SkipAiApplication.getInstance().getOfflineAiManager();
        boolean modelDownloaded = offlineAi.isModelDownloaded();
        switchMaterial.setEnabled(modelDownloaded);



// 3. Put key-value pairs



        if (modelDownloaded && user.getOfflinemode()){

            switchMaterial.setChecked(true);
            text_mode_local.setTextColor(Color.RED);
            text_mode_api.setTextColor(Color.RED);



        }else {
            switchMaterial.setChecked(false);
            text_mode_local.setTextColor(Color.WHITE);
            text_mode_api.setTextColor(Color.RED);

        }
        if (modelDownloaded) {
            launch_chat.setVisibility(View.VISIBLE);
            mode_cont.setVisibility(View.VISIBLE);
            down_cont.setVisibility(View.GONE);

        }else{
            down_cont.setVisibility(View.VISIBLE);
            mode_cont.setVisibility(View.GONE);
            launch_chat.setVisibility(View.GONE);
        }

        String key = user.get_saved_data();
        if(key!=null) Api_key_view.setText(key);


        Api_key_view.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                user.save_data(Api_key_view.getText().toString());

            }
        });
        get_key_url.setPaintFlags(get_key_url.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        get_key_url.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://openrouter.ai/"));
            startActivity(intent);
        });

        if (! isOverlayPermissionGranted()) {
                overlay_per_btn.setBackgroundColor(Color.RED);

        }else{
                overlay_per_btn.setBackgroundColor(Color.GREEN);
        }
        overlay_per_btn.setOnClickListener(v->{
            requestOverlayPermission();
        });
        access_per_btn.setOnClickListener(v->{
            requestAccessibilityPermissionGranted();

        });
        Intent intent_info = new Intent(MainActivity.this, Text_information.class);
        terms_btn.setOnClickListener(v->{

            intent_info.putExtra("terms",true);
            startActivity(intent_info);

        });
        how_btn.setOnClickListener(v->{
            intent_info.putExtra("terms",false);
            startActivity(intent_info);
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String lang = parent.getItemAtPosition(position).toString();
                user.save_language(lang);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        github_btn.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/india-hack-in-owner"));
            startActivity(intent);
        });
        issue_btn.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://t.me/indiaHackIn"));
            startActivity(intent);
        });





        String lang = user.get_saved_language();
        if(lang!=null){

            int pos = adapter.getPosition(lang);
            spinner.setSelection(pos);
        }

        download_model.setOnClickListener(v->{
            download_model.setEnabled(false);
            download_model.setVisibility(View.GONE);
            download_progress_bar.setVisibility(View.VISIBLE);


            String modelUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf?download=true";

            OfflineModelDownloader downloader = new OfflineModelDownloader(this);
            downloader.download(modelUrl, new OfflineModelDownloader.DownloadListner() {
                @Override
                public void onProgress(int percent) {
                    download_progress_bar.setProgress(percent);
                    download_per.setText(percent+"%");

                }

                @Override
                public Runnable onComplete(File file) {
                    runOnUiThread(()->{
                        download_progress_bar.setProgress(100);
                        download_per.setText("100%");
                        download_model.setText("Model Downloaded");
                        download_model.setEnabled(false);
                        download_model.setVisibility(View.VISIBLE);
                        Log.d("OfflineMOdel", "onComplete: download completed ");
                        user.setOfflinemode(true);
                        switchMaterial.setChecked(true);
                        launch_chat.setVisibility(View.VISIBLE);
                        mode_cont.setVisibility(View.VISIBLE);
                        down_cont.setVisibility(View.GONE);
                        user.setModelDownloaded(true);
                    });

                    return null;
                }

                @Override
                public void onError(Exception e) {
                    download_model.setEnabled(true);
                    download_progress_bar.setVisibility(View.GONE);
                    download_per.setVisibility(View.GONE);
                    Log.e(
                            "OfflineModelDownload",
                            "Download failed",
                            e
                    );
                }
            });
        });

        findViewById(R.id.test_ai).setOnClickListener(v->{
            OfflineAiManager offlineAiManager = new OfflineAiManager(this);
            Intent intent = new Intent(MainActivity.this, ChatUi.class);
            startActivity(intent);



//            if(offlineAiManager.isModelDownloaded()){
//                Toast.makeText(this, "Trying to load ai ", Toast.LENGTH_SHORT).show();
////                new Thread(()->{
////                    try {
////                        offlineAiManager.loadModel();
////                        String response = offlineAiManager.generate("Hello!, Tell me about Your Self");
////                        Log.d(
////                                "OfflineAiTest",
////                                "Response: " + response
////                        );
////                    } catch (Exception e) {
////                        Log.e(
////                                "OfflineAiTest",
////                                "Offline AI error",
////                                e
////                        );
////                    }
////                }).start();
//                QwenBridge.generate(
//                        MainActivity.this,
//                        "Explain what an Api is in one short sentence",
//                        new QwenBridge.Callback() {
//                            @Override
//                            public void onToken(@NotNull String token) {
//
//                            }
//
//                            @Override
//                            public void onSuccess(@NotNull String response) {
//
//                            }
//
//                            @Override
//                            public void onError(@NotNull String error) {
//
//                            }
//                        }
//                );
//            }else Toast.makeText(this, "Model Not Downloaded", Toast.LENGTH_SHORT).show();
        });

        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {

            user.setOfflinemode(isChecked);
            if (isChecked){
                text_mode_local.setTextColor(Color.RED);;
                text_mode_api.setTextColor(Color.WHITE);
            }else{
                text_mode_local.setTextColor(Color.WHITE);;
                text_mode_api.setTextColor(Color.RED);
            }
        });
//        new Thread(() -> {
//            try {
//                File modelFile = new File(getFilesDir(), "qwen.gguf");
//
//                if (!modelFile.exists()) {
//                    InputStream input = getAssets().open("qwen.gguf");
//                    FileOutputStream output = new FileOutputStream(modelFile);
//
//                    byte[] buffer = new byte[8192];
//                    int length;
//
//                    while ((length = input.read(buffer)) != -1) {
//                        output.write(buffer, 0, length);
//                    }
//
//                    input.close();
//                    output.close();
//                }
//
//
//                runOnUiThread(()->{
//
//                    QwenBridge.test(
//                            MainActivity.this,
//                            new QwenBridge.Callback() {
//                                @Override
//                                public void onSuccess(@NotNull String response) {
//                                    Log.d("QWEN_TEST", "RESPONSE: " + response);
//                                }
//
//                                @Override
//                                public void onError(@NotNull String error) {
//                                    Log.e("QWEN_TEST", "ERROR: " + error);
//                                }
//                            }
//                    );
//                });
//
//            } catch (Exception e) {
//                Log.e("QWEN_TEST", "MODEL COPY FAILED", e);
//            }
//        }).start();



    }
    @Override
    protected  void onResume(){

        super.onResume();

    }


    private boolean isOverlayPermissionGranted(){
        return Settings.canDrawOverlays(this);
    }
    private void requestAccessibilityPermissionGranted(){
        if (AccessibilityDialog.getParent() != null) {
            ((ViewGroup) AccessibilityDialog.getParent()).removeView(AccessibilityDialog);
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setView(AccessibilityDialog).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 3. Find buttons inside your layout and set click actions
        Button btnAllow = AccessibilityDialog.findViewById(R.id.btnAllow);
        Button btnNotNow = AccessibilityDialog.findViewById(R.id.btnNotNow);
        btnAllow.setOnClickListener(v->{
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            dialog.dismiss();
        });
        btnNotNow.setOnClickListener(v->{
            dialog.dismiss();
        });
        dialog.show();

    }
    public void requestOverlayPermission(){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
        startActivity(intent);

    }
}