package com.example.tesis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.example.tesis.Model.NetworkStateReceiver;
import com.example.tesis.GuestSesion.MainActivity;

public class AlertInternetActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {
    private NetworkStateReceiver networkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_internet);
    }

    @Override
    public void networkAvailable() {
        Intent backMain = new Intent(this, MainActivity.class);
        startActivity(backMain);
        finish();
    }

    @Override
    public void networkUnavailable() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkStateReceiver != null) {
            networkStateReceiver.removeListener(this);
            this.unregisterReceiver(networkStateReceiver);
            networkStateReceiver = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (networkStateReceiver == null) {
            networkStateReceiver = new NetworkStateReceiver();
            networkStateReceiver.addListener(this);
            this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }
}