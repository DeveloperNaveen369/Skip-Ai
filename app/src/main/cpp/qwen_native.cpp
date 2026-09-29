#include <jni.h>
#include <android/log.h>

#include <vector>
#include <string>
#include <cstring>
#include <chrono>
#include <atomic>

#include "llama.h"
#include <unordered_map>
#include <string>
#define TAG "TERMUX_LLAMA"


static std::atomic<bool> g_generating(false);
static llama_model *g_model = nullptr;
static bool g_backend_initialized = false;

struct ChatSession {
    llama_context *ctx = nullptr;
    int n_past = 0;
};

static std::unordered_map<std::string, ChatSession> g_chatSessions;


struct GenerationGuard {

    ~GenerationGuard() {
        g_generating = false;
    }
};


jstring utf8ToJString(
        JNIEnv *env,
        const std::string &text
) {

    jclass stringClass =
            env->FindClass("java/lang/String");

    jmethodID constructor =
            env->GetMethodID(
                    stringClass,
                    "<init>",
                    "([BLjava/lang/String;)V"
            );

    jbyteArray bytes =
            env->NewByteArray(text.size());

    env->SetByteArrayRegion(
            bytes,
            0,
            text.size(),
            reinterpret_cast<const jbyte *>(text.data())
    );

    jstring charset =
            env->NewStringUTF("UTF-8");

    jobject result =
            env->NewObject(
                    stringClass,
                    constructor,
                    bytes,
                    charset
            );

    env->DeleteLocalRef(bytes);
    env->DeleteLocalRef(charset);
    env->DeleteLocalRef(stringClass);

    return static_cast<jstring>(result);
}


static void sendError(
        JNIEnv *env,
        jobject callback,
        jmethodID onErrorMethod,
        const char *message
) {

    __android_log_print(
            ANDROID_LOG_ERROR,
            TAG,
            "%s",
            message
    );

    if (callback == nullptr ||
        onErrorMethod == nullptr) {
        return;
    }

    jstring errorString =
            utf8ToJString(
                    env,
                    std::string("ERROR: ") + message
            );

    env->CallVoidMethod(
            callback,
            onErrorMethod,
            errorString
    );

    env->DeleteLocalRef(errorString);
}


extern "C"
JNIEXPORT void JNICALL
Java_community_india_hack_in_skipai_QwenBridge_nativeGenerate(
        JNIEnv *env,
        jobject /* thiz */,
        jstring modelPath,
        jstring prompt,
        jobject callback
) {

    if (g_generating.exchange(true)) {

        sendError(
                env,
                callback,
                nullptr,
                "Another AI request is already running."
        );

        return;
    }

    GenerationGuard guard;


    jclass callbackClass =
            env->GetObjectClass(callback);

    jmethodID onTokenMethod =
            env->GetMethodID(
                    callbackClass,
                    "onToken",
                    "(Ljava/lang/String;)V"
            );

    jmethodID onSuccessMethod =
            env->GetMethodID(
                    callbackClass,
                    "onSuccess",
                    "(Ljava/lang/String;)V"
            );

    jmethodID onErrorMethod =
            env->GetMethodID(
                    callbackClass,
                    "onError",
                    "(Ljava/lang/String;)V"
            );


    __android_log_print(
            ANDROID_LOG_INFO,
            TAG,
            "Starting Qwen inference test..."
    );


    /*
     * Initialize backend only once.
     */
    if (!g_backend_initialized) {

        llama_backend_init();

        g_backend_initialized = true;

        __android_log_print(
                ANDROID_LOG_INFO,
                TAG,
                "Qwen backend initialized"
        );
    }


    const char *model_path =
            env->GetStringUTFChars(
                    modelPath,
                    nullptr
            );


    /*
     * Load model only once.
     */
    if (g_model == nullptr) {

        __android_log_print(
                ANDROID_LOG_INFO,
                TAG,
                "Loading Qwen model..."
        );

        llama_model_params model_params =
                llama_model_default_params();

        g_model =
                llama_model_load_from_file(
                        model_path,
                        model_params
                );


        if (!g_model) {

            __android_log_print(
                    ANDROID_LOG_ERROR,
                    TAG,
                    "Qwen model load FAILED"
            );

            env->ReleaseStringUTFChars(
                    modelPath,
                    model_path
            );

            sendError(
                    env,
                    callback,
                    onErrorMethod,
                    "Qwen model could not be loaded. The device may not have enough available memory."
            );

            return;
        }


        __android_log_print(
                ANDROID_LOG_INFO,
                TAG,
                "Qwen model loaded successfully"
        );

    } else {

        __android_log_print(
                ANDROID_LOG_INFO,
                TAG,
                "Qwen model already loaded - reusing"
        );
    }


    llama_model *model = g_model;


    __android_log_print(
            ANDROID_LOG_INFO,
            TAG,
            "Qwen model loaded successfully"
    );


    /*
     * Create a fresh context for this request.
     */
    llama_context_params ctx_params =
            llama_context_default_params();

    ctx_params.n_threads = 2;
    ctx_params.n_threads_batch = 2;

    ctx_params.n_ctx = 2048;
    ctx_params.n_batch = 1024;


    auto contextStart =
            std::chrono::steady_clock::now();


    llama_context *ctx =
            llama_init_from_model(
                    model,
                    ctx_params
            );


    auto contextEnd =
            std::chrono::steady_clock::now();


    double contextSeconds =
            std::chrono::duration<double>(
                    contextEnd - contextStart
            ).count();


    __android_log_print(
            ANDROID_LOG_INFO,
            TAG,
            "CONTEXT CREATED IN %.3f SEC",
            contextSeconds
    );


    if (!ctx) {

        env->ReleaseStringUTFChars(
                modelPath,
                model_path
        );

        sendError(
                env,
                callback,
                onErrorMethod,
                "Qwen context creation failed. Device may not have enough available memory."
        );

        return;
    }


    const char *prompt_text =
            env->GetStringUTFChars(
                    prompt,
                    nullptr
            );


    /*
     * Qwen chat format.
     */
    std::string formatted_prompt =
            "<|im_start|>system\n"
            "You are a helpful assistant.\n"
            "<|im_end|>\n"
            "<|im_start|>user\n" +
            std::string(prompt_text) +
            "\n<|im_end|>\n"
            "<|im_start|>assistant\n";


    const llama_vocab *vocab =
            llama_model_get_vocab(model);


    /*
     * Tokenize prompt.
     */
    std::vector<llama_token> tokens(256);


    int n_tokens =
            llama_tokenize(
                    vocab,
                    formatted_prompt.c_str(),
                    formatted_prompt.size(),
                    tokens.data(),
                    tokens.size(),
                    true,
                    true
            );


    /*
     * Buffer wasn't large enough.
     */
    if (n_tokens < 0) {

        n_tokens = -n_tokens;

        tokens.resize(n_tokens);


        n_tokens =
                llama_tokenize(
                        vocab,
                        formatted_prompt.c_str(),
                        formatted_prompt.size(),
                        tokens.data(),
                        tokens.size(),
                        true,
                        true
                );
    }


    /*
     * Tokenization failed.
     */
    if (n_tokens <= 0) {

        __android_log_print(
                ANDROID_LOG_ERROR,
                TAG,
                "TOKENIZATION FAILED: %d",
                n_tokens
        );


        llama_free(ctx);


        env->ReleaseStringUTFChars(
                prompt,
                prompt_text
        );

        env->ReleaseStringUTFChars(
                modelPath,
                model_path
        );


        sendError(
                env,
                callback,
                onErrorMethod,
                "Qwen could not tokenize the prompt."
        );

        return;
    }


    tokens.resize(n_tokens);


    __android_log_print(
            ANDROID_LOG_INFO,
            TAG,
            "PROMPT TOKENS: %d",
            n_tokens
    );


    /*
     * Create batch.
     */
    llama_batch batch =
            llama_batch_init(
                    ctx_params.n_batch,
                    0,
                    1
            );


    for (size_t i = 0; i < tokens.size(); ++i) {

        batch.token[i] = tokens[i];

        batch.pos[i] = i;

        batch.n_seq_id[i] = 1;

        batch.seq_id[i][0] = 0;

        batch.logits[i] = false;
    }


    batch.logits[tokens.size() - 1] = true;

    batch.n_tokens = tokens.size();


    /*
     * Prompt decode.
     */
    auto promptStart =
            std::chrono::steady_clock::now();


    if (llama_decode(ctx, batch) != 0) {

        llama_batch_free(batch);

        llama_free(ctx);


        env->ReleaseStringUTFChars(
                prompt,
                prompt_text
        );

        env->ReleaseStringUTFChars(
                modelPath,
                model_path
        );


        sendError(
                env,
                callback,
                onErrorMethod,
                "Qwen inference failed while processing the prompt."
        );

        return;
    }


    auto promptEnd =
            std::chrono::steady_clock::now();


    double promptSeconds =
            std::chrono::duration<double>(
                    promptEnd - promptStart
            ).count();


    __android_log_print(
            ANDROID_LOG_INFO,
            TAG,
            "PROMPT DECODE IN %.3f SEC",
            promptSeconds
    );


    /*
     * Sampler.
     */
    llama_sampler_chain_params sampler_params =
            llama_sampler_chain_default_params();


    llama_sampler *sampler =
            llama_sampler_chain_init(
                    sampler_params
            );


    llama_sampler_chain_add(
            sampler,
            llama_sampler_init_temp(0.7f)
    );


    llama_sampler_chain_add(
            sampler,
            llama_sampler_init_dist(1234)
    );


    /*
     * Temporary generation limit.
     */
    const int max_tokens = 256;


    std::string response;

    int generated = 0;


    auto start =
            std::chrono::steady_clock::now();


    /*
     * Generate response.
     */
    for (int i = 0; i < max_tokens; ++i) {

        llama_token token =
                llama_sampler_sample(
                        sampler,
                        ctx,
                        -1
                );


        /*
         * Stop at EOS.
         */
        if (token == llama_vocab_eos(vocab)) {
            break;
        }


        char piece[256];


        int n =
                llama_token_to_piece(
                        vocab,
                        token,
                        piece,
                        sizeof(piece),
                        0,
                        true
                );


        std::string tokenText(
                piece,
                n
        );


        if (n > 0) {

            /*
             * Save complete response.
             */
            response.append(tokenText);


            /*
             * Stream token to Kotlin.
             */
            if (callback != nullptr &&
                onTokenMethod != nullptr) {

                jstring tokenString =
                        utf8ToJString(
                                env,
                                tokenText
                        );


                env->CallVoidMethod(
                        callback,
                        onTokenMethod,
                        tokenString
                );


                env->DeleteLocalRef(
                        tokenString
                );
            }
        }


        /*
         * Prepare next token.
         */
        batch.n_tokens = 0;

        batch.token[0] = token;

        batch.pos[0] =
                tokens.size() + generated;

        batch.n_seq_id[0] = 1;

        batch.seq_id[0][0] = 0;

        batch.logits[0] = true;

        batch.n_tokens = 1;


        /*
         * Decode next token.
         */
        if (llama_decode(ctx, batch) != 0) {

            __android_log_print(
                    ANDROID_LOG_ERROR,
                    TAG,
                    "GENERATION DECODE FAILED"
            );


            llama_batch_free(batch);

            llama_sampler_free(sampler);

            llama_free(ctx);


            env->ReleaseStringUTFChars(
                    modelPath,
                    model_path
            );

            env->ReleaseStringUTFChars(
                    prompt,
                    prompt_text
            );


            sendError(
                    env,
                    callback,
                    onErrorMethod,
                    "Qwen inference stopped while generating the response."
            );

            return;
        }


        generated++;
    }


    auto end =
            std::chrono::steady_clock::now();


    double seconds =
            std::chrono::duration<double>(
                    end - start
            ).count();


    double tok_per_sec =
            seconds > 0
            ? generated / seconds
            : 0.0;


    __android_log_print(
            ANDROID_LOG_INFO,
            TAG,
            "GENERATED %d TOKENS IN %.2f SEC = %.2f TOK/S",
            generated,
            seconds,
            tok_per_sec
    );


    __android_log_print(
            ANDROID_LOG_INFO,
            TAG,
            "RESPONSE: %s",
            response.c_str()
    );


    /*
     * Generation completed successfully.
     * Send the complete response to Kotlin.
     */
    if (callback != nullptr &&
        onSuccessMethod != nullptr) {

        jstring responseString =
                utf8ToJString(
                        env,
                        response
                );


        env->CallVoidMethod(
                callback,
                onSuccessMethod,
                responseString
        );


        env->DeleteLocalRef(
                responseString
        );
    }


    /*
     * Request-specific cleanup.
     *
     * Model and backend remain alive
     * for future requests.
     */
    llama_batch_free(batch);

    llama_sampler_free(sampler);

    llama_free(ctx);


    env->ReleaseStringUTFChars(
            modelPath,
            model_path
    );


    env->ReleaseStringUTFChars(
            prompt,
            prompt_text
    );
}
static void sendError(
        JNIEnv *env,
        jobject callback,
        const char *message
) {
    if (callback == nullptr) {
        return;
    }

    jclass callbackClass =
            env->GetObjectClass(callback);

    jmethodID onErrorMethod =
            env->GetMethodID(
                    callbackClass,
                    "onError",
                    "(Ljava/lang/String;)V"
            );

    if (onErrorMethod == nullptr) {
        return;
    }

    jstring errorString =
            utf8ToJString(
                    env,
                    std::string("ERROR: ") + message
            );

    env->CallVoidMethod(
            callback,
            onErrorMethod,
            errorString
    );

    env->DeleteLocalRef(errorString);
}
extern "C"
JNIEXPORT void JNICALL
Java_community_india_hack_in_skipai_QwenBridge_nativeChatGenerate(
        JNIEnv *env,
        jobject /* thiz */,
        jstring modelPath,
        jstring chatId,
        jstring message,
        jstring history,
        jobject callback) {

    if (g_generating.exchange(true)) {
        sendError(
                env,
                callback,
                "Another AI request is already running."
        );
        return;
    }

    GenerationGuard guard;

    jclass callbackClass =
            env->GetObjectClass(callback);

    jmethodID onTokenMethod =
            env->GetMethodID(
                    callbackClass,
                    "onToken",
                    "(Ljava/lang/String;)V"
            );

    jmethodID onSuccessMethod =
            env->GetMethodID(
                    callbackClass,
                    "onSuccess",
                    "(Ljava/lang/String;)V"
            );

    jmethodID onErrorMethod =
            env->GetMethodID(
                    callbackClass,
                    "onError",
                    "(Ljava/lang/String;)V"
            );

    const char *model_path =
            env->GetStringUTFChars(
                    modelPath,
                    nullptr
            );

    const char *chat_id =
            env->GetStringUTFChars(
                    chatId,
                    nullptr
            );

    const char *message_text =
            env->GetStringUTFChars(
                    message,
                    nullptr
            );

    const char *history_text =
            env->GetStringUTFChars(
                    history,
                    nullptr
            );

    std::string historyPrompt(history_text);

    env->ReleaseStringUTFChars(
            history,
            history_text
    );

    std::string sessionId(chat_id);

    /*
     * Initialize backend once.
     */
    if (!g_backend_initialized) {

        llama_backend_init();

        g_backend_initialized = true;

        __android_log_print(
                ANDROID_LOG_INFO,
                TAG,
                "Qwen backend initialized"
        );
    }

    /*
     * Load the shared model once.
     */
    if (g_model == nullptr) {

        __android_log_print(
                ANDROID_LOG_INFO,
                TAG,
                "Loading Qwen model..."
        );

        llama_model_params model_params =
                llama_model_default_params();

        g_model =
                llama_model_load_from_file(
                        model_path,
                        model_params
                );

        if (!g_model) {

            sendError(
                    env,
                    callback,

                    "Qwen model could not be loaded."
            );

            env->ReleaseStringUTFChars(
                    modelPath,
                    model_path
            );

            env->ReleaseStringUTFChars(
                    chatId,
                    chat_id
            );

            env->ReleaseStringUTFChars(
                    message,
                    message_text
            );

            return;
        }

        __android_log_print(
                ANDROID_LOG_INFO,
                TAG,
                "Qwen model loaded successfully"
        );
    }

    llama_model *model = g_model;

    const llama_vocab *vocab =
            llama_model_get_vocab(model);

    /*
     * Find existing chat session.
     */
    auto sessionIt =
            g_chatSessions.find(sessionId);

    bool restoredHistory = false;

    /*
     * Create a new context for a new chat.
     */
    if (sessionIt == g_chatSessions.end()) {

        llama_context_params ctx_params =
                llama_context_default_params();

        ctx_params.n_threads = 2;
        ctx_params.n_threads_batch = 2;

        ctx_params.n_ctx = 2048;
        ctx_params.n_batch = 1024;

        llama_context *ctx =
                llama_init_from_model(
                        model,
                        ctx_params
                );

        if (!ctx) {

            sendError(
                    env,
                    callback,
                    "Qwen chat context could not be created."
            );

            env->ReleaseStringUTFChars(
                    modelPath,
                    model_path
            );

            env->ReleaseStringUTFChars(
                    chatId,
                    chat_id
            );

            env->ReleaseStringUTFChars(
                    message,
                    message_text
            );

            return;
        }

        ChatSession session;

        session.ctx = ctx;
        session.n_past = 0;

        auto result =
                g_chatSessions.emplace(
                        sessionId,
                        session
                );

        sessionIt = result.first;

        __android_log_print(
                ANDROID_LOG_INFO,
                TAG,
                "Created native chat session: %s",
                sessionId.c_str()
        );

        /*
         * Restore the complete conversation history.
         *
         * The history already contains the current user message
         * and the assistant start token, so we do not feed the
         * current message separately when this is a fresh session.
         */
        if (historyPrompt.empty()) {

            sendError(
                    env,
                    callback,
                    "Qwen conversation history is empty."
            );

            llama_free(ctx);
            g_chatSessions.erase(sessionIt);

            env->ReleaseStringUTFChars(modelPath, model_path);
            env->ReleaseStringUTFChars(chatId, chat_id);
            env->ReleaseStringUTFChars(message, message_text);

            return;
        }

        std::vector<llama_token> historyTokens(256);

        int n_history =
                llama_tokenize(
                        vocab,
                        historyPrompt.c_str(),
                        historyPrompt.size(),
                        historyTokens.data(),
                        historyTokens.size(),
                        true,
                        true
                );

        if (n_history < 0) {

            n_history = -n_history;
            historyTokens.resize(n_history);

            n_history =
                    llama_tokenize(
                            vocab,
                            historyPrompt.c_str(),
                            historyPrompt.size(),
                            historyTokens.data(),
                            historyTokens.size(),
                            true,
                            true
                    );
        }

        if (n_history <= 0) {

            sendError(
                    env,
                    callback,
                    "Qwen failed to tokenize conversation history."
            );

            llama_free(ctx);
            g_chatSessions.erase(sessionIt);

            env->ReleaseStringUTFChars(modelPath, model_path);
            env->ReleaseStringUTFChars(chatId, chat_id);
            env->ReleaseStringUTFChars(message, message_text);

            return;
        }

        historyTokens.resize(n_history);

        if (n_history >= ctx_params.n_ctx) {

            sendError(
                    env,
                    callback,
                    "Conversation history is too long for the Qwen context."
            );

            llama_free(ctx);
            g_chatSessions.erase(sessionIt);

            env->ReleaseStringUTFChars(modelPath, model_path);
            env->ReleaseStringUTFChars(chatId, chat_id);
            env->ReleaseStringUTFChars(message, message_text);

            return;
        }

        __android_log_print(
                ANDROID_LOG_INFO,
                TAG,
                "Restoring chat history: %d tokens",
                n_history
        );

        llama_batch historyBatch =
                llama_batch_init(
                        ctx_params.n_batch,
                        0,
                        1
                );

        size_t historyOffset = 0;

        while (historyOffset < historyTokens.size()) {

            const size_t remaining =
                    historyTokens.size() - historyOffset;

            const size_t chunkSize =
                    remaining < static_cast<size_t>(ctx_params.n_batch)
                    ? remaining
                    : static_cast<size_t>(ctx_params.n_batch);

            historyBatch.n_tokens = chunkSize;

            for (size_t i = 0; i < chunkSize; ++i) {

                historyBatch.token[i] =
                        historyTokens[historyOffset + i];

                historyBatch.pos[i] =
                        static_cast<llama_pos>(historyOffset + i);

                historyBatch.n_seq_id[i] = 1;
                historyBatch.seq_id[i][0] = 0;
                historyBatch.logits[i] = false;
            }

            // We only need logits from the final history token because
            // that is where the assistant response will begin.
            if (historyOffset + chunkSize == historyTokens.size()) {
                historyBatch.logits[chunkSize - 1] = true;
            }

            if (llama_decode(ctx, historyBatch) != 0) {

                llama_batch_free(historyBatch);

                sendError(
                        env,
                        callback,
                        "Qwen failed while restoring conversation history."
                );

                llama_free(ctx);
                g_chatSessions.erase(sessionIt);

                env->ReleaseStringUTFChars(modelPath, model_path);
                env->ReleaseStringUTFChars(chatId, chat_id);
                env->ReleaseStringUTFChars(message, message_text);

                return;
            }

            historyOffset += chunkSize;
        }

        llama_batch_free(historyBatch);

        sessionIt->second.n_past = n_history;
        restoredHistory = true;
    }

    ChatSession &session =
            sessionIt->second;

    llama_context *ctx =
            session.ctx;

    /*
     * Reusable batch for feeding the new user message and
     * for decoding generated tokens.
     */
    llama_batch batch =
            llama_batch_init(
                    1024,
                    0,
                    1
            );

    if (!restoredHistory) {
        /*
         * Only send the NEW user message.
         */
        std::string userPrompt =
                "<|im_start|>user\n" +
                std::string(message_text) +
                "\n<|im_end|>\n"
                "<|im_start|>assistant\n";

        std::vector<llama_token> tokens(256);

        int n_tokens =
                llama_tokenize(
                        vocab,
                        userPrompt.c_str(),
                        userPrompt.size(),
                        tokens.data(),
                        tokens.size(),
                        false,
                        true
                );

        if (n_tokens < 0) {

            n_tokens = -n_tokens;

            tokens.resize(n_tokens);

            n_tokens =
                    llama_tokenize(
                            vocab,
                            userPrompt.c_str(),
                            userPrompt.size(),
                            tokens.data(),
                            tokens.size(),
                            false,
                            true
                    );
        }

        if (n_tokens <= 0) {

            llama_batch_free(batch);

            sendError(
                    env,
                    callback,
                    "Qwen could not tokenize the chat message."
            );

            env->ReleaseStringUTFChars(
                    modelPath,
                    model_path
            );

            env->ReleaseStringUTFChars(
                    chatId,
                    chat_id
            );

            env->ReleaseStringUTFChars(
                    message,
                    message_text
            );

            return;
        }

        tokens.resize(n_tokens);

        /*
         * Feed the new user message into the existing context.
         */
        for (size_t i = 0;
             i < tokens.size();
             ++i) {

            batch.token[i] =
                    tokens[i];

            batch.pos[i] =
                    session.n_past + i;

            batch.n_seq_id[i] = 1;

            batch.seq_id[i][0] = 0;

            batch.logits[i] = false;
        }

        batch.logits[
                tokens.size() - 1
        ] = true;

        batch.n_tokens =
                tokens.size();

        if (llama_decode(
                ctx,
                batch
        ) != 0) {

            llama_batch_free(batch);

            sendError(
                    env,
                    callback,
                    "Qwen failed while processing the chat message."
            );

            env->ReleaseStringUTFChars(
                    modelPath,
                    model_path
            );

            env->ReleaseStringUTFChars(
                    chatId,
                    chat_id
            );

            env->ReleaseStringUTFChars(
                    message,
                    message_text
            );

            return;
        }

        session.n_past +=
                tokens.size();

    }

    /*
     * Sampler.
     */
    llama_sampler_chain_params sampler_params =
            llama_sampler_chain_default_params();

    llama_sampler *sampler =
            llama_sampler_chain_init(
                    sampler_params
            );

    llama_sampler_chain_add(
            sampler,
            llama_sampler_init_temp(0.7f)
    );

    llama_sampler_chain_add(
            sampler,
            llama_sampler_init_dist(1234)
    );

    const int max_tokens = 256;

    std::string response;

    int generated = 0;

    for (int i = 0;
         i < max_tokens;
         ++i) {

        llama_token token =
                llama_sampler_sample(
                        sampler,
                        ctx,
                        -1
                );

        if (token == llama_vocab_eos(vocab)) {
            break;
        }

        char piece[256];

        int n =
                llama_token_to_piece(
                        vocab,
                        token,
                        piece,
                        sizeof(piece),
                        0,
                        true
                );

        std::string tokenText(
                piece,
                n
        );

        if (n > 0) {

            response.append(tokenText);

            if (callback != nullptr &&
                onTokenMethod != nullptr) {

                jstring tokenString =
                        utf8ToJString(
                                env,
                                tokenText
                        );

                env->CallVoidMethod(
                        callback,
                        onTokenMethod,
                        tokenString
                );

                env->DeleteLocalRef(
                        tokenString
                );
            }
        }

        batch.n_tokens = 1;

        batch.token[0] = token;

        batch.pos[0] =
                session.n_past;

        batch.n_seq_id[0] = 1;

        batch.seq_id[0][0] = 0;

        batch.logits[0] = true;

        if (llama_decode(
                ctx,
                batch
        ) != 0) {

            llama_batch_free(batch);
            llama_sampler_free(sampler);

            sendError(
                    env,
                    callback,
                    "Qwen stopped while generating the response."
            );

            env->ReleaseStringUTFChars(
                    modelPath,
                    model_path
            );

            env->ReleaseStringUTFChars(
                    chatId,
                    chat_id
            );

            env->ReleaseStringUTFChars(
                    message,
                    message_text
            );

            return;
        }

        session.n_past++;
        generated++;
    }

    llama_batch_free(batch);

    llama_sampler_free(sampler);

    __android_log_print(
            ANDROID_LOG_INFO,
            TAG,
            "CHAT %s: generated %d tokens, n_past=%d",
            sessionId.c_str(),
            generated,
            session.n_past
    );

    /*
     * Tell Java/Kotlin that generation is complete.
     */
    if (callback != nullptr &&
        onSuccessMethod != nullptr) {

        jstring responseString =
                utf8ToJString(
                        env,
                        response
                );

        env->CallVoidMethod(
                callback,
                onSuccessMethod,
                responseString
        );

        env->DeleteLocalRef(
                responseString
        );
    }

    env->ReleaseStringUTFChars(
            modelPath,
            model_path
    );

    env->ReleaseStringUTFChars(
            chatId,
            chat_id
    );

    env->ReleaseStringUTFChars(
            message,
            message_text
    );
}