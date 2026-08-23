package community.india.hack.in.skipai.models;



    public  interface AiResponseListener{
        void onSucess(String response);
        void onFailure(int responseCode,String error);

    }

