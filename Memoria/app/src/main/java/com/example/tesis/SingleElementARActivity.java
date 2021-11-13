package com.example.tesis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tesis.GuestSesion.MainActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.camera.CameraSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.example.tesis.GuestSesion.MainActivity.zonesMain;
import static com.example.tesis.ui.authActivity.zones;

public class SingleElementARActivity extends AppCompatActivity {
    private ArchitectView architectView;
    private static final int CAMERA_REQUEST_CODE = 200;
    private final int LOCATION_REQUEST_CODE = 1001;
    private Button backhome;
    private double latitude, longitude, altitude;
    private float acc;
    private String zoneName;
    private JSONArray dataZones = new JSONArray();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_element_a_r);
        this.zoneName = getIntent().getStringExtra("zoneTitle");
        String isGuestSesion = getIntent().getStringExtra("isGuestSesion");
        if(isGuestSesion != null){
            passZonesGuestToJson();
        }else {
            getDataZone();
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000); //Cada 3 segundos
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        backhome = this.findViewById(R.id.single_back_home);
        architectView = (ArchitectView) this.findViewById(R.id.architectView);
        final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
        config.setLicenseKey("rsBv+8AkzEcMJ+h3l34E3bsMgJiixzpkZzK5EkZoK4fQ8Nx+MuOaYIIRxKUlFXJUcbGM2vKQ3LuMPdeAjHo/fVCUQ8O1m8RwpLFyrXWvdksHcsEDwHcEdoZ2Tet0FefBPOldqvTSKfJoNn2ufV4Ehko/7rKbSNtEmDXch3u0YSxTYWx0ZWRfXwiufaefxL9uqff7WZGE/03pl0ykHB+8YOePRn5CBFAvOdeViVM4gm+4eiRFv9TverBfQkKo8COryWEiHYLa1J44iY+DbSZICwMe2ksSHT0LKLdujUNgaHtcZd4IDOYeUiRpEcV4f96EKmLyuCBU+2PFkX2cCtRwirhO0iXzSH23UfEIm/VFSdFrT0OKd60CdMfqCSqvoMkpZ7gjVDmZv13b1UDenT75dBJgJZGcGsTRNx9k6RwZTV5b2FsdvHxP2T9eo8EeTvZ6a83tB12P74/TnDqe+8xUi52IUN1bE1nIRzDldDwPoOeswdaOL+xM9c2TXrLC0WkREiHiC6MNIaH+7ePgBjX4LtbNY8aq1NMLRMCs69yfcbKE8hlqcvwJvO++coMWl8/Ra5+7h3KkQd7j1AtYyQO4lXGPPdT73JuFO4ncS7Nwz2CLnewkz04hxH56saHg6EJRK188Gaz78FQxBkLAQXbr6s8KmEaOpxe+qOdw6eI3lXQYlIYRyIjj0f8PYyjJluNcJf76q8R3ArczKuMPn6bUQn1kwxiU+ieInLxUOdf3uavkHxgHxqzyCF97XsT3mG9Icdv6FxnnFTa4D0VNaegZRYDJXqySfrof4qW44eYJVLMfcIU0WSNlu21yIiGZ5npUzMsKddOfkXzbsEhyILGPW1j3EF1yTG68hmTaR5H/o9I=");
        config.setCameraResolution(CameraSettings.CameraResolution.AUTO);
        config.setCameraPosition(CameraSettings.CameraPosition.BACK);
        try {
            architectView.onCreate(config);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ERROR EN CARGA DE AR", Toast.LENGTH_LONG).show();
        }
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if(locationResult != null){
                currentLocation = locationResult.getLastLocation();
                architectView.setLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getAltitude(), currentLocation.getAccuracy());
            }
        }
    };

    public void passZonesGuestToJson(){
        for (int i=0; i < zonesMain.size(); i++){
            if(zonesMain.get(i).getName().trim().equals(zoneName.trim())){
                JSONObject zone = new JSONObject();
                try {
                    zone.put("id", zonesMain.get(i).getId());
                    zone.put("name", zonesMain.get(i).getName());
                    zone.put("description", zonesMain.get(i).getDescription());
                    zone.put("latitude", zonesMain.get(i).getLatitude());
                    zone.put("longitude", zonesMain.get(i).getLongitude());
                    zone.put("altitude", 250.0);
                    zone.put("floor", zonesMain.get(i).getFloor());
                    zone.put("photo", zonesMain.get(i).getLastPhotoIndex().trim());
                    dataZones.put(zone);
                    i = zonesMain.size();
                } catch (JSONException e) {
                }
            }
        }
    }

    public void checkSettings() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(SingleElementARActivity.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    public void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void getDataZone(){
        for (int i=0; i < zones.size(); i++){
            if(zones.get(i).getName().trim().equals(zoneName.trim())){
                JSONObject zone = new JSONObject();
                try {
                    zone.put("id", zones.get(i).getId());
                    zone.put("name", zones.get(i).getName());
                    zone.put("description", zones.get(i).getDescription());
                    zone.put("latitude", zones.get(i).getLatitude());
                    zone.put("longitude", zones.get(i).getLongitude());
                    zone.put("altitude", 250.0);
                    zone.put("floor", zones.get(i).getFloor());
                    zone.put("photo", zones.get(i).getLastPhotoIndex().trim());
                    dataZones.put(zone);
                    i = zones.size();
                } catch (JSONException e) { }
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, CAMERA_REQUEST_CODE);
        } else {
            this.architectView.onPostCreate();
            try {
                this.architectView.callJavascript("getData(" + dataZones + ")");
                this.architectView.load("singleAR/index.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        backhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent arActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(arActivity);
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.architectView.onPostCreate();
                try {
                    this.architectView.setLocation( longitude, altitude, acc);
                    this.architectView.load("singleAR/index.html");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No hay permisos de cámara", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkSettings();
        } else {
            askLocationPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        startLocationUpdates();
        this.architectView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.architectView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        this.architectView.onDestroy();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    altitude = location.getAltitude();
                    acc = location.getAccuracy();
                    architectView.setLocation(latitude, longitude, altitude, acc);
                }
                else{
                    Log.println(Log.ASSERT, "LOCATION:", "null location");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.println(Log.ASSERT, "ERROR LOCATION:", e.toString());
            }
        });

    }

    private void askLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }else{
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

}