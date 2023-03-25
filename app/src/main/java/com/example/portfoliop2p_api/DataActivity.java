package com.example.portfoliop2p_api;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest; //Do not use android.location.LocationRequest
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;



public class DataActivity extends AppCompatActivity implements View.OnClickListener {

    FusedLocationProviderClient fusedLocationClient;

    String command;
    String serverIp;


    TextView text;
    String textinfo;

    ScrollView scrollText;

    Button backButton, getDataButton, addDataButton, clearDataButton;

    String locationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        //Setting the views
        text = findViewById(R.id.clientoutput);
        scrollText = findViewById(R.id.scrollViewClient);
        backButton = findViewById(R.id.dataBackButton);
        getDataButton = findViewById(R.id.getLocationDataButton);
        addDataButton = findViewById(R.id.addLocationDataButton);
        clearDataButton = findViewById(R.id.clearLocationDataButton);

        //set listeners
        backButton.setOnClickListener(this);
        getDataButton.setOnClickListener(this);
        addDataButton.setOnClickListener(this);
        clearDataButton.setOnClickListener(this);

        textinfo = "";
        text.setText("");
        locationText = "";


        //getting the data from the IP Activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            command = extras.getString("command");
            serverIp = extras.getString("serverIp");
        }
        System.out.println("\n\n"+command+"\n\n");
        System.out.println("\n\n"+serverIp+"\n\n");



    }//onCreate

    @Override
    public void onClick(View view) {
        if (view == backButton) {
            Intent myIntent = new Intent(this, IpActivity.class);
            startActivity(myIntent);
        } else {

            //clear Location data
            if (view == clearDataButton){
                text.setText("");
                locationText = "";
                textinfo = "";
                return;
            }


            //get location
            if(view == getDataButton){

                //Check app permissions
                boolean fineLocationAlreadyAccepted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                boolean coarseLocationAlreadyAccepted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

                if (!(fineLocationAlreadyAccepted && coarseLocationAlreadyAccepted)) {
                    //Dialogue to ask the user
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
                    cUpdate("Press again");
                    return;
                }

                //We ask if Location is enabled in settings
                askToEnableLocation();

                //Get location provider
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


                //get location
                askForLocation();

            }



        }


        if(view == addDataButton){

            Intent myIntent = new Intent(this, MainActivity.class);
            myIntent.putExtra("command", command);
            myIntent.putExtra("serverIp", serverIp);
            myIntent.putExtra("location", locationText);
            startActivity(myIntent);

        }




    }


    private void askForLocation() {
        //Required extra check before calling getCurrentLocation()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {return null;}
            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String result = "Long: " + location.getLongitude() + " | Lat: " + location.getLatitude() + " | Unix Epoch Time/Date: " + location.getTime();;
                    cUpdate(result);

                    if(locationText == "") {
                        locationText = result + ", ";
                    }else{
                        locationText = locationText + ", " + result;
                    }

                } else {
                    cUpdate("Error: No location");
                    askToEnableLocation();
                }
            }
        });
    }

    //Helper method
    //If the user disabled GPS location on their phone, we ask them to turn it on
    void askToEnableLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // User has Location enabled
            }
        });
        //If GPS is diabled, we show a dialog
        task.addOnFailureListener(this, new OnFailureListener() {
            int REQUEST_CHECK_SETTINGS = 0x1;
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(DataActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }


    private void cUpdate(String message) {
        System.out.println(message);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                textinfo = "CURRENT LOCATION: " + message + "\n\n" + textinfo;
                text.setText(textinfo);
            }
        });
    }

}//DataActivity
