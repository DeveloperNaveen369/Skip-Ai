package community.india.hack.in.skipai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OfflineModelDownloader {
    public interface DownloadListner{
        void onProgress(int percent);
        Runnable onComplete(File file);
        void onError(Exception e);
    }
    public final Context context;
    public OfflineModelDownloader(Context context){
        this.context = context.getApplicationContext();

    }

    public void download(String url,DownloadListner listner){
        new Thread(()->{
            File modelDirectory = new File(context.getFilesDir(),"offline_models");

            if (!modelDirectory.exists()) modelDirectory.mkdirs();

            File modelFile = new File(modelDirectory,"qwen.gguf");


            try{
                long existingSize = modelFile.exists()?modelFile.length():0;

                URL modelUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) modelUrl.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                if (existingSize>0){
                    connection.setRequestProperty("Range","bytes=" + existingSize + "-" );
                }
                connection.connect();
                if(connection.getResponseCode() == 416 && existingSize >0){
                    connection.disconnect();
                    new Handler(Looper.getMainLooper())
                            .post(listner.onComplete(modelFile));
                    return;
                }
                if (connection.getResponseCode()!=HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL){
                    throw new Exception("Download Failed " + connection.getResponseCode());
                }
                long totalSize = connection.getContentLengthLong();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL){
                    totalSize+=existingSize;
                }else {
                    existingSize = 0;
                }



                try(
                        InputStream input = connection.getInputStream();
                        FileOutputStream output = new FileOutputStream(modelFile,existingSize>0);
                        ){
                    byte[] buffer  = new byte[128 * 1024];
                    long downloaded = existingSize;
                    int length;
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    while ((length = input.read(buffer))!= -1){
                        output.write(buffer,0,length);
                        downloaded+=length;

                        if(totalSize>0){
                            int percent = (int) (downloaded*100/totalSize);

                            mainHandler.post(()->listner.onProgress(percent));

                        }
                    }
                    output.flush();
                }
                connection.disconnect();
                new Handler(Looper.getMainLooper()).post(()-> listner.onComplete(modelFile));

            }catch (Exception e){
                new Handler(
                        Looper.getMainLooper()
                ).post(() ->
                        listner.onError(e)
                );
            }
        }).start();
    }
}
