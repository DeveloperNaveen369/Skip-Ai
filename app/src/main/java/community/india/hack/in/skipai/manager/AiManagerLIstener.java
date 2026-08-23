package community.india.hack.in.skipai.manager;

public  interface AiManagerLIstener {
    void onSucess(String response);
    void onFailure(int responseCode ,String error);
}
