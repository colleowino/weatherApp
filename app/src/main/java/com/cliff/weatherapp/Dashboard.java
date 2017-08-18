package com.cliff.weatherapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.*;
import android.util.Log;
import java.util.List;

import android.app.AlertDialog;
import android.provider.Settings;
import android.content.DialogInterface;
import android.content.Intent;

public class Dashboard extends AppCompatActivity {

    boolean isGPS = false;
    boolean isNetwork = false;
    String lat, lon;
    final String TAG = "GPS";

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPS && !isNetwork) {
            Log.d(TAG, "Connection off");
            showSettingsAlert();
            //getLastLocation();
        } else {
            Log.d(TAG, "Connection on");
            // check permissions
            // get location
            Location myLocation = getLastKnownLocation();
            lat = Double.toString(myLocation.getLatitude());
            lon = Double.toString(myLocation.getLongitude());
            Log.d(TAG,"DAta: "+myLocation);
        }
    }

    private Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;

        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }

        return bestLocation;
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }



}
