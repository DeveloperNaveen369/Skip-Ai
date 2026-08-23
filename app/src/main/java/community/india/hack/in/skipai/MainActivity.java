package community.india.hack.in.skipai;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.security.crypto.MasterKey;

import com.google.android.material.chip.Chip;

import community.india.hack.in.skipai.manager.AiManager;
import community.india.hack.in.skipai.manager.AiManagerLIstener;
import community.india.hack.in.skipai.manager.OpenRouterManager;
import community.india.hack.in.skipai.models.AiOptions;

public class MainActivity extends AppCompatActivity {

    Chip overlay_per_btn,access_per_btn;
    CardView overlay_per_view;
    Spinner spinner;
    EditText Api_key_view;
    TextView get_key_url;
    Chip how_btn,issue_btn,terms_btn;
    CardView github_btn;



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








    }
    @Override
    protected  void onResume(){

        super.onResume();

    }


    private boolean isOverlayPermissionGranted(){
        return Settings.canDrawOverlays(this);
    }
    private void requestAccessibilityPermissionGranted(){
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
    public void requestOverlayPermission(){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
        startActivity(intent);

    }
}