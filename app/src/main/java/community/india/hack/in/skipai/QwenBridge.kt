package community.india.hack.`in`.skipai

import android.R.id.message
import android.content.Context
import android.util.Log
import java.io.File
import java.util.concurrent.Executors

object QwenBridge {

    interface Callback {
        fun onToken(token: String)
        fun onSuccess(response: String)
        fun onError(error: String)
    }

    private val executor = Executors.newSingleThreadExecutor()

    init {
        System.loadLibrary("ggml-base")
        System.loadLibrary("ggml-cpu")
        System.loadLibrary("ggml")
        System.loadLibrary("llama")
        System.loadLibrary("qwen_native")
    }

    @JvmStatic
    fun generate(
        context: Context,
        prompt: String,
        callback: Callback
    ) {
        executor.execute {
            try {
                val modelFile = File(
                    context.filesDir,
                    "offline_models/qwen.gguf"
                )

                if (!modelFile.exists()) {
                    callback.onError("Qwen model not downloaded")
                    return@execute
                }

                nativeGenerate(
                    modelFile.absolutePath,
                    prompt,
                    callback
                )

            } catch (e: Exception) {
                Log.e("QWEN", "Generation error", e)
                callback.onError(e.toString())
            }
        }
    }
    @JvmStatic
    fun generateChat(
        context: Context,
        chatId: String,
        message: String,
        history: String,
        callback: Callback
    ) {
        executor.execute {
            try {
                val modelFile = File(
                    context.filesDir,
                    "offline_models/qwen.gguf"
                )

                if (!modelFile.exists()) {
                    callback.onError("Qwen model not downloaded")
                    return@execute
                }

                nativeChatGenerate(
                    modelFile.absolutePath,
                    chatId,
                    message,
                    history,
                    callback
                )

            } catch (e: Exception) {
                Log.e("QWEN", "Chat generation error", e)
                callback.onError(e.toString())
            }
        }
    }

    private external fun nativeChatGenerate(
        modelPath: String,
        chatId: String,
        message: String,
        history: String,
        callback: Callback
    )

    private external fun nativeGenerate(
        modelPath: String,
        prompt: String,
        callback: Callback
    )
//    private external fun nativeChatGenerate(
//        modelPath: String,
//        chatId: String,
//        message: String,
//        callback: Callback
//    )
}