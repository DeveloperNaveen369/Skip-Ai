package community.india.hack.in.skipai.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import community.india.hack.in.skipai.UserSettings;
import community.india.hack.in.skipai.models.AiResponseListener;
import community.india.hack.in.skipai.utils.Constants;

public class OpenRouterManager {

    public void getResponse(Context context,String prompt , AiResponseListener listener){

        UserSettings user = new UserSettings(context);
        String saved_key = user.get_saved_data();
            new Thread(()->{
                try {

                    String api_key = Constants.OPEN_ROUTER_API;
                    if(saved_key!=null && saved_key.length()>50){
                         api_key = saved_key;

                    }

                    URL url = new URL(
                            "https://openrouter.ai/api/v1/chat/completions"
                    );
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type","application/json");
                    connection.setRequestProperty(
                            "Authorization",
                            "Bearer " + api_key
                    );
                    connection.setDoOutput(true);
                    String jsonBody=

                            "{"

                                    +"\"model\":\"openrouter/auto\","
                                    +"\"messages\":["
                                    +"{"
                                    +"\"role\":\"user\","
                                    +"\"content\":\""
                                    + prompt +
                                    "\""
                                    +"}"
                                    +"]"
                                    +"}";
                    Log.d(
                            "Prompt",
                            prompt
                    );
                    Log.d("json body ", jsonBody);

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(jsonBody.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    Log.d(
                            "Prompt",
                            prompt
                    );
                    Log.d("json body ", jsonBody);
                    int responseCode = connection.getResponseCode();


                    Log.d("Response Code", String.valueOf(responseCode));

                    InputStream inputStream;
                    if(responseCode==200) {
                        inputStream = connection.getInputStream();

                    }
                    else{
                        inputStream = connection.getErrorStream();

                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line=reader.readLine())!=null) response.append(line);
                    String result = response.toString();
                    Log.d("Ai response ", result);
                    if(responseCode==200){
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray choices = jsonObject.getJSONArray("choices");
                        JSONObject choice =  choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        String respons = message.getString("content");
                        Log.d("Ai response is ", respons);
                        new Handler(

                                Looper.getMainLooper()

                        ).post(()->{

                            listener.onSucess(respons);

                        });


                    }
                    else{
                        new Handler(

                                Looper.getMainLooper()

                        ).post(()->{

                            listener.onFailure(responseCode,result);

                        });
                    }




                }catch (Exception e){
                    new Handler(

                            Looper.getMainLooper()

                    ).post(()->{
                        listener.onFailure(-1,e.getMessage());

                    });

                }
            }).start();
    }


    public  void testConnection(){
        new Thread(()->{
            try{

                String api_key = Constants.OPEN_ROUTER_API;
                URL url = new URL(
                                "https://openrouter.ai/api/v1/chat/completions"
                        );
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setRequestProperty(
                        "Authorization",
                        "Bearer " + api_key
                );
                connection.setDoOutput(true);
                String jsonBody =

                        "{"

                                + "\"model\":\"openrouter/auto\","

                                + "\"messages\":["

                                + "{"

                                + "\"role\":\"user\","

                                + "\"content\":\"Hello\""

                                + "}"

                                + "]"

                                + "}";



                Log.d(
                        "JSON BODY",
                        jsonBody
                );
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonBody.getBytes());
                outputStream.flush();
                outputStream.close();
                int responseCode = connection.getResponseCode();
                Log.d("open resoponse code ", String.valueOf(responseCode));
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()
                ));
                StringBuilder response = new StringBuilder();
                String line;
                while((line=reader.readLine())!=null) response.append(line);
                reader.close();
                Log.d(
                        "AI RESPONSE",
                        response.toString()
                );



            }catch (Exception e){
                Log.d("Open router erro ", e.toString());
            }
        }).start();
    }
}
