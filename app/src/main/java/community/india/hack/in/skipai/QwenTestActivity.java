package community.india.hack.in.skipai;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;



public class QwenTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(()->{
            try {
                File modelFile = new File(getFilesDir(),"qwen.gguf");

                if (!modelFile.exists()){
                    InputStream input = getAssets().open("qwen.gguf");
                    FileOutputStream output = new FileOutputStream(modelFile);

                    byte[]  buffer = new byte[8192];
                    int length;
                    while ((length=input.read(buffer))!=-1){
                        output.write(buffer,0,length);
                    }
                    input.close();
                    output.close();



                }
//                Log.d("QWEN_TEST", "Model path: " + modelFile.getAbsolutePath());
//                var mode  = Llama.loadModel(modelFile.getAbsolutePath(),new LlamaConfig(
//                        2048,
//                        4
//                ));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }).start();
    }
}