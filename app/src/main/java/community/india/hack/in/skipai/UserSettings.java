package community.india.hack.in.skipai;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class UserSettings {
    private  Context context;

    MasterKey masterKey;
    private SharedPreferences preferences;

    public UserSettings(Context context){

        this.context = context;
        {
            try {
                masterKey = new MasterKey.Builder(context).setKeyScheme(
                        MasterKey.KeyScheme.AES256_GCM
                ).build();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            preferences = EncryptedSharedPreferences.create(
                    context,"user_local_data",masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }




    public  void save_data(String Api_key)  {



        preferences.edit().putString(
                "OPEN_ROUTER",
                 Api_key
        ).apply();



    }
    public void save_language(String lang){
        preferences.edit().putString(
                "language",
                lang
        ).apply();

    }
    public void setOfflinemode(Boolean mode){
        preferences.edit().putBoolean("offline_mode",mode).apply();
    }
    public Boolean getOfflinemode(){
        return  preferences.getBoolean("offline_mode",false);
    }

    public void setModelDownloaded(Boolean downloaded){
        preferences.edit().putBoolean("model_downloaded",downloaded);
    }
    public Boolean isModelDownloaded(){
        return preferences.getBoolean("model_downloaded",false);
    }
    public String get_saved_language(){
        String lan = preferences.getString(
                "language",
                null
        );
        return  lan;
    }
    public  String get_saved_data(){

        String api = preferences.getString(
                "OPEN_ROUTER",
                null
        );

        return api;
    }
}
