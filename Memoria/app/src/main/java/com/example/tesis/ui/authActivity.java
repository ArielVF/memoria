package com.example.tesis.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tesis.AlertActivityView;
import com.example.tesis.AlertInternetActivity;
import com.example.tesis.Model.Event;
import com.example.tesis.Model.NetworkStateReceiver;
import com.example.tesis.Model.SubZone;
import com.example.tesis.Model.Zone;
import com.example.tesis.GuestSesion.MainActivity;
import com.example.tesis.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class authActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener{
    private FirebaseAuth mAuth;
    private AppBarConfiguration mAppBarConfiguration;
    private ProgressDialog message;
    private TextView data1;
    private TextView data2;
    private FirebaseFirestore db;
    private NavigationView navigationView;

    public static List<Zone> zones;
    //public static List<User> users;
    public static List<Event> activeEvents;
    public static List<String> zoneCurrentEvents;
    public static List<String> zoneEventinCurse;
    public static List<SubZone> subZones;
    public static boolean onRute = false;

    private NetworkStateReceiver networkStateReceiver;

    private ScheduledExecutorService scheduler;

    private Button button;

    private boolean offline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isHourManual()){
            Intent alertView = new Intent(this, AlertActivityView.class);
            startActivity(alertView);
        }
        else{
            setContentView(R.layout.activity_auth);
            this.db = FirebaseFirestore.getInstance(); //Get Bd
            this.mAuth = FirebaseAuth.getInstance(); //Get AuthBD

            this.zones = new ArrayList<>();
            this.subZones = new ArrayList<>();
            //this.users = new ArrayList<>();
            this.activeEvents = new ArrayList<>();
            this.zoneCurrentEvents = new ArrayList<>();
            this.zoneEventinCurse = new ArrayList<>();

            getAuthUserData();
            getZoneData();
            //getAllUserData();
            getActiveEvents();
            getsubZones();
            suscribeToTopic();

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            this.navigationView = findViewById(R.id.nav_view);

           // button = (Button) findViewById(R.id.closeSesion);

            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.selectTypeEvent,
                    R.id.userControl, R.id.myEvents, R.id.zoneFragment, R.id.subZoneFragment, R.id.allEventsFragment)
                    .setDrawerLayout(drawer)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.content_main);
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        }
    }

    public void getAuthUserData(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        data1 = (TextView) headerView.findViewById(R.id.nombreusuario);
        data2 = (TextView) headerView.findViewById(R.id.correousuario);

        db.collection("Usuarios")
                .whereEqualTo("correo",  mAuth.getCurrentUser().getEmail())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        for (QueryDocumentSnapshot document : value) {
                            data1.setText(document.getString("nombre")+" "+document.getString("apellido"));
                            data2.setText(document.getString("correo"));
                            setNavBar(document.getString("rol"));
                        }
                    }
                });
    }

    /*
    public void getAllUserData(){
        db.collection("Usuarios").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                users.clear();
                if (e != null) {
                    return;
                }
                for (QueryDocumentSnapshot document : value) {
                    User user = new User(document.getId(), document.getString("nombre"), document.getString("apellido"),
                            document.getString("correo"), document.getString("rol"), document.getString("rut"), document.getBoolean("habilitado"));
                    users.add(user);
                }
            }
        });
    }*/

    public void getZoneData(){
        db.collection("Zona").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                zones.clear();
                List<String> temporalZones = new ArrayList<>();
                if (e != null) {
                    return;
                }
                for (QueryDocumentSnapshot document : value) {
                    Zone zone = new Zone(document.getId(), document.getString("nombre"),
                            document.getString("descripcion"), document.getGeoPoint("coordenada").getLongitude(),
                            document.getGeoPoint("coordenada").getLatitude(),
                            Integer.valueOf(document.get("cantidad piso").toString()), document.getBoolean("zona fija"));
                    zone.convertPhotoToList(document.get("foto").toString());  //Inicializamos variable foto
                    zones.add(zone);
                    if(!zone.getPermanent()){
                        temporalZones.add(zone.getId());
                    }
                }
            }
        });
    }

    public void setNavBar(String rol){
        if(rol.equals("creador")){
            this.navigationView.getMenu().findItem(R.id.myEvents).setVisible(true);
            this.navigationView.getMenu().findItem(R.id.selectTypeEvent).setVisible(true);
            this.navigationView.getMenu().findItem(R.id.zoneFragment).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.subZoneFragment).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.allEventsFragment).setVisible(false);
        }
        else if(rol.equals("administrador")){
            this.navigationView.getMenu().findItem(R.id.myEvents).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.selectTypeEvent).setVisible(true);
            this.navigationView.getMenu().findItem(R.id.userControl).setVisible(true);
            this.navigationView.getMenu().findItem(R.id.zoneFragment).setVisible(true);
            this.navigationView.getMenu().findItem(R.id.subZoneFragment).setVisible(true);
            this.navigationView.getMenu().findItem(R.id.allEventsFragment).setVisible(true);
        }
        else{
            this.navigationView.getMenu().findItem(R.id.myEvents).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.selectTypeEvent).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.userControl).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.zoneFragment).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.subZoneFragment).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.allEventsFragment).setVisible(false);
        }
        this.navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.auth, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void logout(View view){
        mAuth.signOut();
        message = new ProgressDialog(this);

        message.setMessage("Cerrando sesión ...");
        message.setCanceledOnTouchOutside(false);
        message.show();

        Toast.makeText(getApplicationContext(), "Sesión cerrada", Toast.LENGTH_LONG).show();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
        message.dismiss();
    }

    public void suscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic("Eventos").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), "Usuario suscrito a Eventos", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getActiveEvents(){
        Date currentDate = new Date();
        //Instanciamos bd por si se realiza la llamada desde mapFragment
        if(db == null){
            db = FirebaseFirestore.getInstance();
        }
        db.collection("Eventos").orderBy("fecha inicio", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                activeEvents.clear();
                zoneEventinCurse.clear();
                zoneCurrentEvents.clear();
                for (QueryDocumentSnapshot document : value) {
                    Timestamp start = document.getTimestamp("fecha inicio");
                    Timestamp finish = document.getTimestamp("fecha termino");
                    try{
                        if(currentDate.compareTo(finish.toDate()) <= 0){
                            Event event = new Event(document.getId(), document.getString("creador"), document.getString("sector"),
                                    document.getString("nombre"), document.getString("descripcion"),
                                    document.getTimestamp("fecha inicio"), document.getTimestamp("fecha termino"), document.getString("foto"));
                            activeEvents.add(event);
                            String sector = event.getZone();
                            if(start.toDate().compareTo(currentDate) > 0){
                                if(!zoneCurrentEvents.contains(sector)){
                                    zoneCurrentEvents.add(sector);
                                }
                            }
                            else{
                                if(!zoneEventinCurse.contains(sector)){
                                    zoneEventinCurse.add(sector);
                                }
                            }
                        }
                    }catch (Exception ex){}
                }
            }
        });
    }

    public void getsubZones(){
        db.collection("SubZona").orderBy("nombre", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        subZones.clear();
                        if (e != null) {
                            return;
                        }
                        for (QueryDocumentSnapshot document : value) {
                            String id = document.getId();
                            String name = document.getString("nombre");
                            SubZone subZone = new SubZone(id, name);
                            subZones.add(subZone);
                        }

                    }
                });
    }

    private boolean isHourManual(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(getApplicationContext().getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            //Menor a Android 4.2
            return android.provider.Settings.System.getInt(getApplicationContext().getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0) == 1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRute = false;
        if(!isHourManual()) {
            Intent alertView = new Intent(this, AlertActivityView.class);
            startActivity(alertView);
            finish();
        }
        if (networkStateReceiver == null) {
            networkStateReceiver = new NetworkStateReceiver();
            networkStateReceiver.addListener(this);
            this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }else{
            this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }
        userBlocked();
    }

    public void userBlocked(){
        unsuscribeTopic();
        db.collection("Usuarios").document(mAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.getBoolean("habilitado")){
                    out_user();
                    Toast.makeText(getApplicationContext(), "Usuario bloqueado, contacte a su jefe de carrera.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void out_user(){
        mAuth.signOut();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    public void unsuscribeTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic("Eventos").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }        });
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
    protected void onStart() {
        super.onStart();
        if(offline){
            finish();
        }
    }

    @Override
    public void networkAvailable() {
        //Nothing to do
        offline = false;
    }

    @Override
    public void networkUnavailable() {
        if(!onRute){
            Intent alertInternetView = new Intent(this, AlertInternetActivity.class);
            startActivity(alertInternetView);
            offline = true;
        }
    }
}