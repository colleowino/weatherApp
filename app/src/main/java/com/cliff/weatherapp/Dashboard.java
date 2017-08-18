package com.cliff.weatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    boolean isGPS = false;
    boolean isNetwork = false;
    String lat, lon;
    final String TAG = "GPS";

    LocationManager locationManager;
    TextView tvLatitude, tvLongitude, tvWeather, tvDate, tvTemp_c, tvTemp_f;
    RelativeLayout vProgressLayer;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        tvWeather = (TextView) findViewById(R.id.tvWeather);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvTemp_c = (TextView) findViewById(R.id.tvTemp_c);
        tvTemp_f = (TextView) findViewById(R.id.tvTemp_f);
        vProgressLayer = (RelativeLayout) findViewById(R.id.progressLayer);

        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        requestQueue = Volley.newRequestQueue(this);

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
            fetchLocationData();
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

    public void fetchLocationData(){
        String url = "https://fcc-weather-api.glitch.me/api/current?lat="+lat+"&lon="+lon;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG,"DAta: "+response);

                        try {
                            String locationTemp = response.getJSONObject("main").getString("temp");
                            String locationWeather = response.getJSONArray("weather").getJSONObject(0).getString("main");

                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                            tvWeather.setText(locationWeather);
                            tvTemp_c.setText(locationTemp + " \u2103");
                            Double fahrenheit = Double.valueOf(locationTemp)*1.8 + 32;
                            tvTemp_f.setText(fahrenheit + " \u2109");
                            tvLatitude.setText(lat);
                            tvLongitude.setText(lon);
                            tvDate.setText(currentDateTimeString);
                            vProgressLayer.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            //some exception handler code.
                            Log.d(TAG, "error occured: "+e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Something went wrong: "+error);
                    }
                });

        //add request to queue
        requestQueue.add(jsonObjectRequest);

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
