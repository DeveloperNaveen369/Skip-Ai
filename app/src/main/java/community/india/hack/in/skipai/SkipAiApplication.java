package community.india.hack.in.skipai;

import android.app.Application;

import androidx.room.Room;

import community.india.hack.in.skipai.manager.ChatManager;
import community.india.hack.in.skipai.manager.OfflineAiManager;
import community.india.hack.in.skipai.utils.SkipAiDatabase;

public class SkipAiApplication extends Application {
    private OfflineAiManager offlineAiManager;
    private static SkipAiApplication instance;
    private ChatManager chatManager;
    private ChatController chatController;
    private SkipAiDatabase database;
    @Override
    public void onCreate(){
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(),SkipAiDatabase.class,"skip_ai_database").build();
        instance = this;
        offlineAiManager = new OfflineAiManager(this);

        chatManager = new ChatManager(database.chatDao());
        chatController = new ChatController(this,chatManager);

    }
    public OfflineAiManager getOfflineAiManager(){
        return offlineAiManager;
    }
    public static SkipAiApplication getInstance(){
        return instance;
    }

    public ChatManager getChatManager(){
        return chatManager;
    }
    public ChatController getChatController(){
        return chatController;
    }
    public SkipAiDatabase getDatabase(){
        return database;
    }

}
