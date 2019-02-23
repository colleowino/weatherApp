package com.cliff.weatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import android.Manifest;

public class Dashboard extends AppCompatActivity {

    String lat, lon;
    final String TAG = "GPS";

    LocationManager locationManager;
    TextView tvCity, tvLatitude, tvLongitude, tvWeather, tvDate, tvTemp_c, tvTemp_f;
    RelativeLayout vProgressLayer;
    RequestQueue requestQueue;
    public static final Integer MY_PERMISSIONS_REQUEST_LOCATION = 0x5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCity = (TextView) findViewById(R.id.tvCity);
        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        tvWeather = (TextView) findViewById(R.id.tvWeather);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvTemp_c = (TextView) findViewById(R.id.tvTemp_c);
        tvTemp_f = (TextView) findViewById(R.id.tvTemp_f);
        vProgressLayer = (RelativeLayout) findViewById(R.id.progressLayer);

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);


        // Register the listener with the Location Manager to receive location updates
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
        } else {
            setupListeners();
        }

    }

    public void setupListeners() {
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                fetchLocationData(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);

        if (!netAndGpsEnabled()) {
            showSettingsAlert();
        } else {
            Log.d(TAG, "GPS on");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0x5: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupListeners();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private boolean netAndGpsEnabled(){

        boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(!isGPS && !isNetwork){
            return false;
        }
        else{
            return true;
        }
    }

    public void fetchLocationData(Location myLocation){
        requestQueue = Volley.newRequestQueue(this);

        Log.d(TAG, "got new location update");

        lat = Double.toString(myLocation.getLatitude());
        lon = Double.toString(myLocation.getLongitude());
        Log.d(TAG, "DAta: " + myLocation);

        String url = "https://fcc-weather-api.glitch.me/api/current?lat="+lat+"&lon="+lon;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG,"DAta: "+response);
                        try {
                            String locationTemp = response.getJSONObject("main").getString("temp");
                            String country = response.getJSONObject("sys").getString("country");
                            String place = response.getString("name") + ", "+ country;
                            String locationWeather = response.getJSONArray("weather").getJSONObject(0).getString("main");

                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                            tvWeather.setText(locationWeather);
                            tvTemp_c.setText(locationTemp + " \u2103");
                            Double fahrenheit = Double.valueOf(locationTemp)*1.8 + 32;
                            tvTemp_f.setText(fahrenheit + " \u2109");
                            tvLatitude.setText(lat);
                            tvLongitude.setText(lon);
                            tvDate.setText(currentDateTimeString);
                            tvCity.setText(place);
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
                        Toast.makeText(getApplicationContext(), "check your internet connection", Toast.LENGTH_LONG).show();
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
                finish();
            }
        });

        alertDialog.show();
    }



}
