package com.example.tesis.Model;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.tesis.R;

import java.util.ArrayList;
import java.util.List;

public class NetworkStateReceiver extends BroadcastReceiver {
    // Listeners list
    protected List<NetworkStateReceiverListener> listeners;

    // Connection flag
    protected Boolean connected;

    /**
     * Public constructor
     */
    public NetworkStateReceiver() {
        listeners = new ArrayList<NetworkStateReceiverListener>();
        connected = null;
    }

    /**
     *
     * @param context  Context - Application context
     * @param intent  Intent - Manages application actions on network state changes
     */
    public void onReceive(Context context, Intent intent) {
        if(intent == null || intent.getExtras() == null) return;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();

        if((ni != null) && ni.isConnected()) {
            connected = true;
        } else {
            connected = false;
        }

        mNotifyStateToAll();
    }

    /**
     * Notify the state to all needed methods
     */
    private void mNotifyStateToAll() {
        for(NetworkStateReceiverListener listener : listeners)
            mNotifyState(listener);
    }

    /**
     * Notify the network state
     * @param listener  NetworkStateReceiverListener - receives network state change
     */
    private void mNotifyState(NetworkStateReceiverListener listener) {
        if(connected == null || listener == null) return;

        if(connected == true) {
            listener.networkAvailable();
        } else {
            listener.networkUnavailable();
        }
    }

    /**
     * Add listener once it is needed
     * @param l  NetworkStateReceiverListener - receives network state change
     */
    public void addListener(NetworkStateReceiverListener l) {
        listeners.add(l);
        mNotifyState(l);
    }

    /**
     * Remove the listener once it is not needed anymore
     * @param l  NetworkStateReceiverListener - receives network state change
     */
    public void removeListener(NetworkStateReceiverListener l) {
        listeners.remove(l);
    }

    /**
     * Set interface to communicate with Main methods
     */
    public interface NetworkStateReceiverListener {
        public void networkAvailable();
        public void networkUnavailable();
    }
}

