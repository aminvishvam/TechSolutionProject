package com.example.techsolutionproject.Common;

import android.location.Location;

import com.example.techsolutionproject.Remote.FCMClient;
import com.example.techsolutionproject.Remote.IFCMService;

public class Common {
    public static String address;
    public static String EngineerID="";
    public static Location mLastLocation = null;
    public static final String driver_tbl = "EngineerLocation"; //engineersLocation_tbl="EngineersLocation"
    public static final String user_engineer_tbl = "EngineersInformation"; //engineer_tbl="EngineersInformation";
    public static final String user_rider_tbl = "UsersInformation";//user_tbl="UsersInformation"
    public static final String pickup_request_tbl = "PickupRequest";//Tokens
    public static final String token_tbl = "Token";//Tokens

    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";

    public static IFCMService getFCMService() {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

  /* public static void sendRequestToEngineer(String EngineerID, final IFCMService mservice, final Context context, final Location currentLocation) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);

        tokens.orderByKey().equalTo(EngineerID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Token token = postSnapshot.getValue(Token.class);

                            String userToken= FirebaseInstanceId.getInstance().getToken();

                            Map<String,String> content=new HashMap<>();
                            content.put("customer",userToken);
                            content.put("lat",String.valueOf(currentLocation.getLatitude()));
                            content.put("lng",String.valueOf(currentLocation.getLongitude()));

                            DataMessage dataMessage=new DataMessage(token.getToken(),content);
                            /*String json_lat_lng = new Gson().toJson(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                            String info = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            Notification nf = new Notification("JUNIPER", json_lat_lng, info);
                            Sender content = new Sender(nf, token.getToken());

                            mservice.sendMessage(dataMessage)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                                            if (response.body().success == 1)
                                                Toast.makeText(context, "Request sent", Toast.LENGTH_SHORT).show();

                                            else
                                                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();

                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }

                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }*/

}
