package com.example.techsolutionproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.techsolutionproject.Common.Common;
import com.example.techsolutionproject.Helper.CustomeInfoWindow;
import com.example.techsolutionproject.Remote.IFCMService;
import com.example.techsolutionproject.model.Engineer;
import com.example.techsolutionproject.model.Token;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Home<location extends FusedLocationProviderClient> extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback
        , ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener, GoogleMap
                .OnInfoWindowClickListener {

    SupportMapFragment mapFragment;

    //Location
    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 2598;
    private static final int PLAY_SERVICE_RES_REQUEST = 1998;

    private LocationRequest mLocationRequest;
    private GoogleApi mGoogleApiClient;


    private FusedLocationProviderClient mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ref;

    Marker mUserMarker;

    //bottomSheet
    ImageView imgExpandable;
    BottomSheetUserFragment mBottomSheet;
    Button btnPickupRequest;

    boolean isEngineerFound = false;
    String EngineerID = "";
    int radius = 1;//1km
    int distance = 1;
    private static final int LIMIT = 3;

    IFCMService mservice;
    DatabaseReference engineerAvailable;
    GeoFire geoFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        mservice = com.example.techsolutionproject.Common.Common.getFCMService();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Init view
        imgExpandable = (ImageView) findViewById(R.id.imgExpandable);
        mBottomSheet = (BottomSheetUserFragment) BottomSheetUserFragment.newInstance("User Bottom Sheet");
        imgExpandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }
        });

        btnPickupRequest = (Button) findViewById(R.id.btnPickupRequest);

        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isEngineerFound)
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());

                //com.example.techsolutionproject.Common.Common sendRequestToEngineer(com.example.techsolutionproject.Common.Common.EngineerID,mservice,getBaseContext(),mLastLocation);
            }
        });
        setUpLocation();
        updateFirebaseToken();


    }

    private void updateFirebaseToken() {
        try {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference tokens = db.getReference(Common.token_tbl);

            Token token = new Token(FirebaseMessaging.getInstance().getToken());
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

   /* private void sendRequestToEngineer(String engineerID) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);

        tokens.orderByKey().equalTo(engineerID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Token token = postSnapshot.getValue(Token.class);

                            User user = new User();
                            String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                            String customerToken = FirebaseInstanceId.getInstance().getToken();
                            String info = Common.address + "-- Wants Service";
                            Notification nf = new Notification(customerToken, json_lat_lng, info);
                            Sender content = new Sender(nf, token.getToken());

                            mservice.sendMessage(content)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            try {
                                                if (response.body().success == 1)
                                                    Toast.makeText(Home.this, "Request sent", Toast.LENGTH_SHORT).show();

                                                else
                                                    Toast.makeText(Home.this, "Failed", Toast.LENGTH_SHORT).show();
                                            } catch (NullPointerException ignored) {
                                            }
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

    private void requestPickupHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(com.example.techsolutionproject.Common.Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(locationResult.getResult().getLatitude(), locationResult.getResult().getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (mUserMarker.isVisible()) {
                    mUserMarker.remove();

                    //add new marker
                    mUserMarker = mMap.addMarker(new MarkerOptions()
                            .title("Pickup here")
                            .snippet("")
                            .position(new LatLng(locationResult.getResult().getLatitude(), locationResult.getResult().getLongitude()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    mUserMarker.showInfoWindow();

                    btnPickupRequest.setText("Getting your Engineer...");

                    findEngineer();
                }
            }
        });

    }

    private void findEngineer() {
        final DatabaseReference engineers = FirebaseDatabase.getInstance().getReference(com.example.techsolutionproject.Common.Common.driver_tbl);
        GeoFire gfEngineers = new GeoFire(engineers);

        GeoQuery geoQuery = gfEngineers.queryAtLocation(new GeoLocation(locationResult.getResult().getLatitude(), locationResult.getResult().getLongitude())
                , radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //if found
                if (!isEngineerFound) {
                    isEngineerFound = true;
                    EngineerID = key;
                    btnPickupRequest.setText("CALL ENGINEER");
                    Toast.makeText(Home.this, "" + key, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //if still not found engineer,increase distance
                if (!isEngineerFound) {
                    radius++;
                    findEngineer();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        //buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    private void setUpLocation() {
        try{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                //request runtime permission
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CALL_PHONE
                }, MY_PERMISSION_REQUEST_CODE);
            } else {
                if (checkPlayServices()) {
                    //buildGoogleApiClient();
                    createLocationRequest();
                    displayLocation();
                }
            }
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    Task<Location> locationResult;

    private void displayLocation() {

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLastLocation = LocationServices.getFusedLocationProviderClient(this);
            locationResult = mLastLocation.getLastLocation();

            if (locationResult != null) {

                //presence system
                engineerAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
                engineerAvailable.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        loadAllAvailableEngineer();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                final double latitude = locationResult.getResult().getLatitude();
                final double longitude = locationResult.getResult().getLongitude();
                LatLng myCoordinate = new LatLng(latitude, longitude);
                String cityname = getCityName(myCoordinate);

                //add marker
                if (mUserMarker != null)
                    mUserMarker.remove();
                mUserMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title("UserLocation")
                        .snippet("Address: " + Common.address));
                //move camera
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

                loadAllAvailableEngineer();

                Log.d("TechSolution", String.format("Your location was changed : @f / @f", latitude, longitude));
            } else
                Log.d("ERROR", "Cannot get your location");
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private String getCityName(LatLng myCoordinate) {
        String mycity = "";
        Geocoder geocoder = new Geocoder(Home.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(myCoordinate.latitude, myCoordinate.longitude, 1);
            com.example.techsolutionproject.Common.Common.address = addresses.get(0).getAddressLine(0);
            mycity = addresses.get(0).getLocality();
            Log.v("mylog", "complete address: " + addresses.toString());
            Log.v("mylog", "Address: " + com.example.techsolutionproject.Common.Common.address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mycity;
    }

    private void loadAllAvailableEngineer() {

        try{
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(locationResult.getResult().getLatitude(), locationResult.getResult().getLongitude()))
                .title("You"));


        //load all available eng.
        DatabaseReference engineerLocation = FirebaseDatabase.getInstance().getReference(com.example.techsolutionproject.Common.Common.driver_tbl);
        GeoFire gf = new GeoFire(engineerLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(locationResult.getResult().getLatitude(), locationResult.getResult().getLongitude()), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                //use key to get email from table Engineer
                FirebaseDatabase.getInstance().getReference(Common.user_engineer_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Engineer engineer = null;
                                //add engineer to map
                                try {
                                    engineer = dataSnapshot.getValue(Engineer.class);
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(location.latitude, location.longitude))
                                            .flat(true)
                                            .title("Name : " + engineer.getName() + "\nPhone : " + engineer.getPhone())
                                            .snippet("Engineer ID : " + dataSnapshot.getKey())
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.usericon)));

                                } catch (NullPointerException ignored) {

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <= LIMIT) {
                    distance++;
                    loadAllAvailableEngineer();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

//    private void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApi.Settings.Builder().build()
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        mGoogleApiClient.connect();
//    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, PLAY_SERVICE_RES_REQUEST).show();
            } else {
                Toast.makeText(this, "this device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        try{
            getMenuInflater().inflate(R.menu.home, menu);

        }catch (Exception e){
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_payment) {
            //PaymentRedirect();

        } else if (id == R.id.search_engineer) {
            //searchEngineer();

        } else if (id == R.id.custom_search) {
            //customSearch();

        } else if (id == R.id.nav_sign_out_user) {
            signOutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//    private void searchEngineer() {
//        Intent intent=new Intent(Home.this,EngineerListActivity.class);
//        startActivity(intent);
//        finish();
//    }
//    private void customSearch() {
//        Intent intent=new Intent(Home.this,CustomSearch.class);
//        startActivity(intent);
//        finish();
//    }

//    private void PaymentRedirect() {
//        Intent intent=new Intent(Home.this,Paypal.class);
//        startActivity(intent);
//        finish();
//    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Home.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomeInfoWindow(this));
        mMap.setOnInfoWindowClickListener(this);

    }

    @Override
    public void onLocationChanged(Location location) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation.setMockLocation(location);

        displayLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
//        startLocationUpdates();
    }

//    private void startLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
////        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//        Task<Void> fusedLocationClient = LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//    }

    @Override
    public void onConnectionSuspended(int i) {
//        mGoogleApiClient.connect();
//
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (!marker.getTitle().equals("You")) {
//            Intent intent = new Intent(Home.this, CallEngineer.class);
//            intent.putExtra("EngineerID", marker.getSnippet());
//            intent.putExtra("lat", mLastLocation.getLatitude());
//            intent.putExtra("lng", mLastLocation.getLongitude());
//            startActivity(intent);
        }
    }
}