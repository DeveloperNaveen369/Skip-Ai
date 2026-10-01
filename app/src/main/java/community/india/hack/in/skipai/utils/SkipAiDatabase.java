package community.india.hack.in.skipai.utils;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import community.india.hack.in.skipai.ChatDao;

@Database(
        entities = {
                ChatEntity.class,
                ChatMessagesEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class SkipAiDatabase extends RoomDatabase {
    public abstract ChatDao chatDao();
}
