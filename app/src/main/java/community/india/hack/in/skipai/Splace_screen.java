package community.india.hack.in.skipai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import community.india.hack.in.skipai.manager.OfflineAiManager;

public class Splace_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splace_screen);
        Intent intent ;
        OfflineAiManager manager = new OfflineAiManager(getApplicationContext());
        if (manager.isModelDownloaded()==true) intent = new Intent(this, ChatUi.class);
        else intent = new Intent(this, MainActivity.class);
        new Handler().postDelayed(()->{
            startActivity(intent);
            finish();
        },3100);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}