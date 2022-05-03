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
import android.location.LocationListener;
import android.location.LocationManager;
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

public class FullArActivity extends AppCompatActivity {
    private ArchitectView architectView;
    private final int LOCATION_REQUEST_CODE = 1001;
    private static final int CAMERA_REQUEST_CODE = 200;
    private Button backHome;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude, longitude, altitude;
    private float acc;
    private JSONArray dataZones = new JSONArray();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_ar);
        String isGuestSesion = getIntent().getStringExtra("isGuestSesion");
        if(isGuestSesion != null){
            passZonesGuestToJson();
        }else{
            passZonesToJson();
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000); //Cada 3 segundos
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        this.backHome = this.findViewById(R.id.back_home);
        architectView = (ArchitectView) this.findViewById(R.id.architectView);
        final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
        config.setLicenseKey("your_license");
        config.setCameraResolution(CameraSettings.CameraResolution.AUTO);
        config.setCameraPosition(CameraSettings.CameraPosition.BACK);
        try {
            architectView.onCreate(config);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ERROR EN CARGA DE AR", Toast.LENGTH_LONG).show();
        }
    }

    public void passZonesGuestToJson(){
        for (int i = 0; i < zonesMain.size(); i++) {
            if(zonesMain.get(i).getPermanent()){
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
                } catch (JSONException e) { }
            }
        }
    }

    public void passZonesToJson() {
        for (int i = 0; i < zones.size(); i++) {
            if(zones.get(i).getPermanent()){
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
                } catch (JSONException e) { }
            }
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
                        apiException.startResolutionForResult(FullArActivity.this, 1001);
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
                this.architectView.load("demo29/index.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        backHome.setOnClickListener(new View.OnClickListener() {
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
                    this.architectView.load("demo29/index.html");
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
