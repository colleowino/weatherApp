package com.cliff.weatherapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

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

        tvCity = findViewById(R.id.tvCity);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvWeather = findViewById(R.id.tvWeather);
        tvDate = findViewById(R.id.tvDate);
        tvTemp_c = findViewById(R.id.tvTemp_c);
        tvTemp_f = findViewById(R.id.tvTemp_f);
        vProgressLayer = findViewById(R.id.progressLayer);

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        // Register the listener with the Location Manager to receive location updates
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0x5) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupListeners();
            }
        }
    }

    private boolean netAndGpsEnabled(){
        boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return (isGPS && isNetwork);
    }

    public void fetchLocationData(Location myLocation){
        requestQueue = Volley.newRequestQueue(this);

        Log.d(TAG, "got new location update");

        lat = Double.toString(myLocation.getLatitude());
        lon = Double.toString(myLocation.getLongitude());
        Log.d(TAG, "DAta: " + myLocation);

        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units=metric&appid=80f3988e0e381340a6afc9c616f390d7";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG,"Data: "+response);
                    try {
                        String locationTemp = response.getJSONObject("main").getString("temp");
                        String country = response.getJSONObject("sys").getString("country");
                        String place = response.getString("name") + ", "+ country;
                        String locationWeather = response.getJSONArray("weather").getJSONObject(0).getString("main");

                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                        tvWeather.setText(locationWeather);
                        tvTemp_f.setText(String.format("%s ℃", locationTemp));
                        Double fahrenheit = Double.parseDouble(locationTemp)*1.8 + 32;
                        tvTemp_c.setText(String.format("%.2f ℉", fahrenheit));
                        tvLatitude.setText(lat);
                        tvLongitude.setText(lon);
                        tvDate.setText(currentDateTimeString);
                        tvCity.setText(place);
                        vProgressLayer.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        //some exception handler code.
                        Log.d(TAG, "error occurred: "+e);
                    }

                },
                error -> {
                    Log.e(TAG, "Something went wrong: "+error);
                    Toast.makeText(getApplicationContext(), "check your internet connection", Toast.LENGTH_LONG).show();
                });

        //add request to queue
        requestQueue.add(jsonObjectRequest);

    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");

        alertDialog.setPositiveButton("Yes", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });

        alertDialog.setNegativeButton("No", (dialog, which) -> {
            dialog.cancel();
            finish();
        });

        alertDialog.show();
    }
}
