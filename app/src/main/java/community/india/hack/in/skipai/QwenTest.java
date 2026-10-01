package community.india.hack.in.skipai;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class QwenTest {
    public static void test(Context context){
        new Thread(()->{
            try {
                File modelFile =
                        new File(context.getFilesDir(), "qwen.gguf");

                if (!modelFile.exists()) {

                    InputStream input =
                            context.getAssets().open("qwen.gguf");

                    FileOutputStream output =
                            new FileOutputStream(modelFile);

                    byte[] buffer = new byte[8192];
                    int length;

                    while ((length = input.read(buffer)) != -1) {
                        output.write(buffer, 0, length);
                    }

                    input.close();
                    output.close();
                }

                Log.d("QWEN_TEST",
                        "Model: " + modelFile.getAbsolutePath());
            } catch (Exception e) {
                Log.e("QWEN_TEST", "FAILED", e);
            }
        }).start();
    }
}
