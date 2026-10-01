package community.india.hack.in.skipai;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import community.india.hack.in.skipai.utils.ChatEntity;
import community.india.hack.in.skipai.utils.ChatMessagesEntity;

@Dao
public  interface ChatDao {
    @Insert
    void insertChat(ChatEntity chat);

    @Insert
    void insertMessage(ChatMessagesEntity messages);

    @Query("select * from chats order by updatedAt desc")
    List<ChatEntity> getAllChats();

    @Query("select * from chat_messages where chatId = :chatId order by timestamp ASC")
    List<ChatMessagesEntity> getMessages(String chatId);

    @Query("delete from chats where id= :chatId")
    void deleteChat(String chatId);

    @Query("delete from chat_messages where chatId = :chatId")
    void deleteMessages(String chatId);

    @Query("UPDATE chats SET title = :title, updatedAt = :updatedAt WHERE id = :chatId")
    void updateChat(String chatId, String title, long updatedAt);


}
