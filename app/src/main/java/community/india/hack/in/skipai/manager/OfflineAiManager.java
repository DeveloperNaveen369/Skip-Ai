package community.india.hack.in.skipai.manager;

import android.content.Context;
import android.util.Log;

import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class OfflineAiManager {

    private final Context context;
    private static final String TAG = "OfflineAi";

    public OfflineAiManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized void ensureLoaded() throws Exception {
        File modelFile = getModelFile();

        if (!modelFile.exists()) {
            throw new Exception("Qwen model not downloaded");
        }

        Log.d(TAG, "Qwen model is ready");
    }

    public void loadModel() throws Exception {
        ensureLoaded();
    }

    public File getModelFile() {
        File modelDirectory =
                new File(context.getFilesDir(), "offline_models");

        if (!modelDirectory.exists()) {
            modelDirectory.mkdirs();
        }

        return new File(modelDirectory, "qwen.gguf");
    }

    public boolean isModelDownloaded() {
        return getModelFile().exists();
    }
}