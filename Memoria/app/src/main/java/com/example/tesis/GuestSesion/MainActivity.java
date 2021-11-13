package com.example.tesis.GuestSesion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/* Classes needed to initialize the map */
import com.example.tesis.AlertInternetActivity;
import com.example.tesis.Model.NetworkStateReceiver;
import com.example.tesis.Model.SubZone;
import com.example.tesis.Model.Zone;
import com.example.tesis.FullArActivity;
import com.example.tesis.R;
import com.example.tesis.SingleElementARActivity;
import com.example.tesis.ui.authActivity;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
//Set language Mapbox
import com.mapbox.mapboxsdk.style.layers.Layer;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
// classes needed to add the location component
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
// classes needed to add a marker
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
// classes to calculate a route
import com.mapbox.navigator.NavigationStatus;
import com.mapbox.services.android.navigation.ui.v5.MapOfflineOptions;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
// classes needed to launch navigation UI
import android.view.View;
import android.widget.Button;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.v5.navigation.NavigationService;
import com.mapbox.services.android.navigation.v5.navigation.metrics.NavigationMetricListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener, NetworkStateReceiver.NetworkStateReceiverListener {
    private MapboxMap mapboxMap;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    private Button button;
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private FirebaseAuth mAuth;
    private RelativeLayout test;
    private TextView title, description, time_navigation;
    private ImageView img;
    private Locale spanish = new Locale("es", "ES"); //set spanish instruction
    private FirebaseFirestore db;
    private List<Feature> symbolLayerIconFeatureList;
    private List<Feature> auxsymbolLayerIconFeatureList;
    public static List<Zone> zonesMain;
    public static List<SubZone> subZonesMain;
    private String zoneName;
    private FloatingActionsMenu float_menu;
    private AutoCompleteTextView autoCompleteTextView;
    private Button search, erase;
    private ArrayList<QueryDocumentSnapshot> documentsFound;
    private String[] allZones;
    private NetworkStateReceiver networkStateReceiver;
    private boolean offline = false;
    private ProgressDialog progressDialog;
    private ArrayList<DocumentSnapshot> documentSnapshotsFound;
    private String navigation_element = DirectionsCriteria.PROFILE_WALKING;
    private boolean isSearch = false;
    public static MainActivity fa;
    private com.google.android.material.floatingactionbutton.FloatingActionButton car_navigation, walk_navigation, bike_navigation,
            car_navigation_p, walk_navigation_p, bike_navigation_p;
    private Point auxOrigin, auxDestination;
    public static boolean navigation_on = false;
    private LocationManager lm;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fa = this;
        checkUser();
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        db = FirebaseFirestore.getInstance();
        symbolLayerIconFeatureList = new ArrayList<>();
        auxsymbolLayerIconFeatureList = new ArrayList<>();

        documentsFound = new ArrayList<>();
        documentSnapshotsFound = new ArrayList<>();

        zonesMain = new ArrayList<>();
        subZonesMain = new ArrayList<>();

        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        float_menu = findViewById(R.id.float_menu);
        search = findViewById(R.id.search_button);
        erase = findViewById(R.id.search_erase);

        test = (RelativeLayout) findViewById(R.id.customMarker);
        title = (TextView) findViewById(R.id.title_marker) ;
        description = (TextView) findViewById(R.id.description_marker);
        img= (ImageView) findViewById(R.id.ImgMarker);
        test.setVisibility(View.GONE);

        car_navigation = findViewById(R.id.car_button);
        bike_navigation = findViewById(R.id.bike_button);
        walk_navigation = findViewById(R.id.walk_button);

        car_navigation_p = findViewById(R.id.car_button_pressed);
        bike_navigation_p = findViewById(R.id.bike_button_pressed);
        walk_navigation_p = findViewById(R.id.walk_button_pressed);

        time_navigation = findViewById(R.id.time_navigation);

        button = findViewById(R.id.startButton);

        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        lm.addGpsStatusListener(new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    alertGps();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean simulateRoute = false;
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(simulateRoute)
                        .build();
                    // Call this method with Context from within an Activity
                NavigationLauncher.startNavigation(MainActivity.this, options);
                navigation_on = true;
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = autoCompleteTextView.getText().toString().trim();
                String cadenaNormalize = Normalizer.normalize(data, Normalizer.Form.NFD);
                data = cadenaNormalize.replaceAll("[^\\p{ASCII}]", "");
                searchZone(data);
            }
        });

        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                documentsFound.clear();
                documentSnapshotsFound.clear();
                erase.setVisibility(View.GONE);
                search.setVisibility(View.VISIBLE);
                autoCompleteTextView.setText("");
                autoCompleteTextView.setEnabled(true);
                isSearch = false;
                updateMapbox();
            }
        });

        car_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation_element = DirectionsCriteria.PROFILE_DRIVING;
                car_navigation_p.setVisibility(View.VISIBLE);
                car_navigation.setVisibility(View.GONE);

                walk_navigation.setVisibility(View.VISIBLE);
                bike_navigation.setVisibility(View.VISIBLE);
                walk_navigation_p.setVisibility(View.GONE);
                bike_navigation_p.setVisibility(View.GONE);

                getRoute(auxOrigin, auxDestination);
            }
        });

        bike_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation_element = DirectionsCriteria.PROFILE_CYCLING;

                bike_navigation_p.setVisibility(View.VISIBLE);
                bike_navigation.setVisibility(View.GONE);

                walk_navigation.setVisibility(View.VISIBLE);
                car_navigation.setVisibility(View.VISIBLE);
                walk_navigation_p.setVisibility(View.GONE);
                car_navigation_p.setVisibility(View.GONE);

                getRoute(auxOrigin, auxDestination);
            }
        });

        walk_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation_element = DirectionsCriteria.PROFILE_WALKING;

                walk_navigation_p.setVisibility(View.VISIBLE);
                walk_navigation.setVisibility(View.GONE);

                car_navigation.setVisibility(View.VISIBLE);
                bike_navigation.setVisibility(View.VISIBLE);
                car_navigation_p.setVisibility(View.GONE);
                bike_navigation_p.setVisibility(View.GONE);

                getRoute(auxOrigin, auxDestination);
            }
        });
    }

    public void my_location(View view){
        float_menu.collapse();
        if(locationComponent!=null){
            setCameraPosition(locationComponent.getLastKnownLocation().getLatitude(),
                    locationComponent.getLastKnownLocation().getLongitude(),
                    12, 5);
        }
        else{
            Toast.makeText(getApplicationContext(), "Debe brindar permisos de localización para usar esta funcionalidad", Toast.LENGTH_LONG).show();
        }
    }

    private void upadateGeoPoints(){
        symbolLayerIconFeatureList.clear();
        for (int i = 0; i < zonesMain.size(); i++){
            Feature singleFeature = Feature.fromGeometry(Point.fromLngLat(zonesMain.get(i).getLongitude(), zonesMain.get(i).getLatitude()));
            singleFeature.addStringProperty("title", zonesMain.get(i).getName());
            singleFeature.addStringProperty("description", zonesMain.get(i).getDescription());
            singleFeature.addStringProperty("url_photo", zonesMain.get(i).getLastPhotoIndex());
            symbolLayerIconFeatureList.add(singleFeature);
        }
    }

    public void arZone(View v){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            showSnackBar("Permisos de cámara denegados. Otorge los permisos para utilizar esta funcioanlidad.");
        }else {
            Intent arActivity = new Intent(MainActivity.this, SingleElementARActivity.class);
            arActivity.putExtra("isGuestSesion", "si");
            arActivity.putExtra("zoneTitle", zoneName);
            startActivity(arActivity);
            finish();
        }
    }

    public void arTotal(View v){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            showSnackBar("Permisos de cámara denegados. Otorge los permisos para utilizar esta funcioanlidad.");
        }else {
            Intent arActivity = new Intent(MainActivity.this, FullArActivity.class);
            arActivity.putExtra("isGuestSesion", "si");
            startActivity(arActivity);
            finish();
        }
    }

    public void checkUser(){
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            if(mAuth.getCurrentUser().isEmailVerified()){
                startActivity(new Intent(getApplicationContext(), authActivity.class));
                finish();
            }
        }
    }

    public void updateMapbox(){
        if(!isSearch){
            upadateGeoPoints();
        }
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.removeSource("marker-source");
                style.removeLayer("marker-layer");
                style.removeImage("my-marker-image");
                style.addSource(new GeoJsonSource("marker-source",
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));

                style.addImage("my-marker-image", BitmapFactory.decodeResource(
                        MainActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));
                style.addLayer(new SymbolLayer("marker-layer", "marker-source")
                        .withProperties(PropertyFactory.iconImage("my-marker-image"),
                                iconAllowOverlap(true),
                                iconOffset(new Float[]{0f, -9f})));
                //enableLocationComponent(style);
                addDestinationIconSymbolLayer(style);
            }
        });
    }

    public void getNameZone(){
        allZones = new String[zonesMain.size()+ subZonesMain.size()];
        for(int i = 0; i < zonesMain.size(); i++){
            allZones[i] = zonesMain.get(i).getName();
        }
        int auxCount = zonesMain.size();
        for(int i = 0; i < subZonesMain.size(); i++){
            allZones[auxCount] = subZonesMain.get(i).getName();
            auxCount++;
        }
        autoCompleteTextView = findViewById(R.id.searched);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this, R.layout.item_dropdown, allZones);
        autoCompleteTextView.setAdapter(arrayAdapter);
    }

    public void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.root_layout), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Aceptar", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public void searchZone(String data){
        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog.setTitle("Buscando zona");
        progressDialog.setCancelable(false);

        documentsFound.clear();
        db.collection("Zona")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name_zone = Normalizer.normalize(document.get("nombre").toString(), Normalizer.Form.NFD);
                                name_zone = name_zone.replaceAll("[^\\p{ASCII}]", "");
                                if(name_zone.toLowerCase().equals(data.trim().toLowerCase())){
                                    documentsFound.add(document);
                                }
                            }
                            if(documentsFound.size() != 0){
                                progressDialog.dismiss();
                                showSnackBar("Resultado exitoso. Seleccione la zona para ver más información.");
                                showZone();
                            }else{
                                searchSubZone(data);
                            }
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Algo salió mal, vuelva a intentar la búsqueda"+task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void searchSubZone(String data){
        db.collection("SubZona")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name_zone = Normalizer.normalize(document.get("nombre").toString(), Normalizer.Form.NFD);
                                name_zone = name_zone.replaceAll("[^\\p{ASCII}]", "");
                                if(name_zone.toLowerCase().equals(data.trim().toLowerCase())){
                                    documentsFound.add(document);
                                }
                            }
                            if(documentsFound.size() != 0){
                                getZonesHaveSubZone();
                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Sin resultados favorables para la zona buscada.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Algo salió mal, vuelva a intentar la búsqueda", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void getZonesHaveSubZone(){
        db.collection("Pertenece a")
                .whereEqualTo("SubZona", documentsFound.get(0).getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().isEmpty()){
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "El sitio no está vínculado a ningún edificio.", Toast.LENGTH_LONG).show();
                        }
                        else{
                            documentsFound.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                documentsFound.add(document);
                            }
                            if(documentsFound.size() > 1){
                                showSnackBar("Resultado exitoso. Hay más de una zona que contiene el sitio buscado.");
                            }else{
                                showSnackBar("El sitio buscado está en el piso "+documentsFound.get(0).get("piso")+" del edificio en pantalla.");
                            }
                            storeNameZone();
                        }
                    }
                });
    }

    public void storeNameZone(){
        for (int i=0; i < documentsFound.size(); i++) {
            int storeIndex = i;
            db.collection("Zona").document(documentsFound.get(i).getString("Zona")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    documentSnapshotsFound.add(task.getResult());
                    if(documentSnapshotsFound.size() == documentsFound.size()){
                        documentsFound.clear();
                        showZone();
                    }
                }
            });
        }
    }

    public void showZone(){
        erase.setVisibility(View.VISIBLE);
        search.setVisibility(View.GONE);
        autoCompleteTextView.setEnabled(false);
        if(documentsFound.isEmpty()){
            setSymbolLayerSnapshot(documentSnapshotsFound);
        }
        else{
            setSymbolLayerQuery(documentsFound);
        }
        isSearch = true;
        updateMapbox();
        progressDialog.dismiss();
    }

    public void setSymbolLayerQuery(ArrayList<QueryDocumentSnapshot> documentsFound){
        symbolLayerIconFeatureList.clear();
        for(int i=0; i < documentsFound.size(); i++){
            Feature singleFeature = Feature.fromGeometry(Point.fromLngLat(documentsFound.get(i).getGeoPoint("coordenada").getLongitude(), documentsFound.get(i).getGeoPoint("coordenada").getLatitude()));
            singleFeature.addStringProperty("title", documentsFound.get(i).getString("nombre"));
            singleFeature.addStringProperty("description", documentsFound.get(i).getString("descripcion"));
            String photos = documentsFound.get(i).get("foto").toString();
            String url = getLinkPhoto(photos);
            singleFeature.addStringProperty("url_photo", url);
            symbolLayerIconFeatureList.add(singleFeature);
        }
        double lat = documentsFound.get(0).getGeoPoint("coordenada").getLatitude();
        double lon = documentsFound.get(0).getGeoPoint("coordenada").getLongitude();
        setValuetoSetCamera(documentsFound.size(), lat, lon);
    }

    public void setSymbolLayerSnapshot(ArrayList<DocumentSnapshot> documentsFound){
        symbolLayerIconFeatureList.clear();
        for(int i=0; i < documentsFound.size(); i++){
            Feature singleFeature = Feature.fromGeometry(Point.fromLngLat(documentsFound.get(i).getGeoPoint("coordenada").getLongitude(), documentsFound.get(i).getGeoPoint("coordenada").getLatitude()));
            singleFeature.addStringProperty("title", documentsFound.get(i).getString("nombre"));
            singleFeature.addStringProperty("description", documentsFound.get(i).getString("descripcion"));
            String photos = documentsFound.get(i).get("foto").toString();
            String url = getLinkPhoto(photos);
            singleFeature.addStringProperty("url_photo", url);
            symbolLayerIconFeatureList.add(singleFeature);
        }
        double lat = documentsFound.get(0).getGeoPoint("coordenada").getLatitude();
        double lon = documentsFound.get(0).getGeoPoint("coordenada").getLongitude();
        setValuetoSetCamera(documentsFound.size(), lat, lon);
    }

    /**
     * Método para obtener la foto de la zona o zonas que se están buscando.
     * @return la dirección de la imagen para cargarla en el marcador personalizado
     */
    public String getLinkPhoto(String photos){
        photos = photos.replace("["," ");
        photos = photos.replace( "]", " ");
        String auxPhotos[] = photos.split(",");
        return auxPhotos[auxPhotos.length-1].trim();
    }

    public void setValuetoSetCamera(int size, double lat, double lon){
        if(size > 1){
            setCameraPosition(lat, lon, 10, 5);
        }
        else{
            setCameraPosition(lat, lon, 17, 5);
        }
    }

    public void setCameraPosition(double lat, double lon, int zoom, int tilt){
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(lat, lon)) // Sets the new camera position
                .zoom(zoom) // Sets the zoom
                .tilt(tilt) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 1500);
    }

    private void readData(Style style){
        db.collection("Zona")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getBoolean("zona fija")){
                                    Feature singleFeature = Feature.fromGeometry(Point.fromLngLat(document.getGeoPoint("coordenada").getLongitude(), document.getGeoPoint("coordenada").getLatitude()));
                                    singleFeature.addStringProperty("title", document.getString("nombre"));
                                    singleFeature.addStringProperty("description", document.getString("descripcion"));
                                    String photos = document.get("foto").toString();
                                    String url = getLinkPhoto(photos).trim();
                                    singleFeature.addStringProperty("url_photo", url);

                                    symbolLayerIconFeatureList.add(singleFeature);

                                    Zone zone = new Zone(document.getId(), document.getString("nombre"),
                                            document.getString("descripcion"), document.getGeoPoint("coordenada").getLongitude(),
                                            document.getGeoPoint("coordenada").getLatitude(),
                                            Integer.valueOf(document.get("cantidad piso").toString()), document.getBoolean("zona fija"));
                                    zone.convertPhotoToList(document.get("foto").toString());  //Inicializamos variable foto
                                    zonesMain.add(zone);
                                }
                            }
                            Layer settlementLabelLayer = style.getLayer("settlement-label");
                            if (settlementLabelLayer != null) {
                                settlementLabelLayer.setProperties(textField("{name_es}"));
                            }
                            style.addSource(new GeoJsonSource("marker-source",
                                    FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));
                            style.addImage("my-marker-image", BitmapFactory.decodeResource(
                                    MainActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));
                            style.addLayer(new SymbolLayer("marker-layer", "marker-source")
                                    .withProperties(PropertyFactory.iconImage("my-marker-image"),
                                            iconAllowOverlap(true),
                                            iconOffset(new Float[]{0f, -9f})));

                            getSubZoneData();
                        } else {
                            Toast.makeText(getApplicationContext(), "Algo salio mal", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void getSubZoneData(){
        db.collection("SubZona").orderBy("nombre", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        subZonesMain.clear();
                        if (e != null) {
                            return;
                        }
                        for (QueryDocumentSnapshot document : value) {
                            String id = document.getId();
                            String name = document.getString("nombre");
                            SubZone subZone = new SubZone(id, name);
                            subZonesMain.add(subZone);
                        }
                        getNameZone();
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded(){
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                readData(style);
                enableLocationComponent(style);
                addDestinationIconSymbolLayer(style);
                mapboxMap.addOnMapClickListener(MainActivity.this);
            }
        });
    }

    public void closeMarker(View view){
        navigationMapRoute.updateRouteVisibilityTo(false);
        quitAnimation();
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.map_marker_light));

        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                //iconOffset(new Float[]{0f, -9f}),
                PropertyFactory.iconSize(0.5f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        float_menu.collapse();
        handleClickIcon(mapboxMap.getProjection().toScreenLocation(point), point);
        return true;
    }

    public void setZoneName(String title){
        this.zoneName = title;
    }

    public void showCustomMarker(String t, String d, String url) {
        setZoneName(t); //Seteamos el nombre de la zona seleccionada
        title.setText(t);
        description.setText(d);
        if(!url.trim().equals("sin imagen")){
            Picasso.get().load(url.trim()).into(img);
        }else{
            img.setImageResource(R.mipmap.sin_imagen);
        }
        if(test.getVisibility() == View.GONE){
            startAnimationMarker();
        }
    }

    private void startAnimationMarker(){
        AnimationSet set = new AnimationSet(true);
        Animation animation = null;
        //desde la esquina inferior derecha a la superior izquierda
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);

        //desde la esquina superior izquierda a la esquina inferior derecha
        //animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);

        //duración en milisegundos
        animation.setDuration(300);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.25f);

        test.setLayoutAnimation(controller);
        test.startAnimation(animation);
        test.setVisibility(View.VISIBLE);
    }

    public void show_information(View view){
        float_menu.collapseImmediately();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Información");
        builder.setMessage(R.string.how_to_search);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNeutralButton("Ver más", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showAllZone();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

    private void handleClickIcon(PointF screenPoint, LatLng point) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, "marker-layer");
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        if (!features.isEmpty()) {
            Feature selectedFeature = features.get(0);
            //seteamos posición de cámara para seguir al marcador
            Point auxPoint = (Point) selectedFeature.geometry();
            //Seteamos el destino al punto seleccionado
            destinationPoint = Point.fromLngLat(auxPoint.longitude(), auxPoint.latitude());
            //seteamos posición de cámara para seguir al marcador
            setCameraPosition(auxPoint.latitude(), auxPoint.longitude(), 17, 5);

            String title = selectedFeature.getStringProperty("title");
            String description = selectedFeature.getStringProperty("description");
            String url_photo = selectedFeature.getStringProperty("url_photo");
            //selectedIcon(point)
            showCustomMarker(title, description, url_photo);

            //Monstramos ruta
            Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                    locationComponent.getLastKnownLocation().getLatitude());

            GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
            if (source != null) {
                source.setGeoJson(Feature.fromGeometry(destinationPoint));
            }
            auxOrigin = originPoint;
            auxDestination = destinationPoint;
            getRoute(originPoint, destinationPoint);
        }
        else{
            GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
            if (source != null) {
                source.setGeoJson(Feature.fromGeometry(destinationPoint));
            }
            if(test.getVisibility() == View.VISIBLE){
                quitAnimation();
            }
            if (navigationMapRoute != null) {
                navigationMapRoute.updateRouteVisibilityTo(false);
            }
        }
    }

    public void showAllZone(){
        db.collection("Zona").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                String allZones= "";
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getBoolean("zona fija")){
                        allZones += "- "+document.get("nombre").toString()+"\n";
                    }
                }
                AlertDialog.Builder allZonesMessage = new AlertDialog.Builder(MainActivity.this);
                allZonesMessage.setTitle("Zonas");
                allZonesMessage.setMessage(allZones);
                allZonesMessage.setCancelable(false);
                allZonesMessage.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                allZonesMessage.show();
            }
        });
    }

    private void quitAnimation(){
        AnimationSet set = new AnimationSet(true);
        Animation animation = null;

        //desde la esquina superior izquierda a la esquina inferior derecha
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);

        //duración en milisegundos
        animation.setDuration(300);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.25f);

        test.setLayoutAnimation(controller);
        test.startAnimation(animation);
        test.setVisibility(View.GONE);
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .profile(navigation_element)
                .voiceUnits(DirectionsCriteria.METRIC)
                .language(spanish)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null) {
                            button.setEnabled(false);
                            time_navigation.setText("Sin ruta disponible para este medio.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            button.setEnabled(false);
                            time_navigation.setText("Sin ruta disponible para este medio.");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);
                        showTimeToArrival(currentRoute.duration()/60);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.updateRouteArrowVisibilityTo(false);
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    public void showTimeToArrival(Double time){
        button.setEnabled(true);
        int hour = (int) (time/60);
        if(hour > 24){
            int day = hour / 24;
            if(day > 1){
                time_navigation.setText("Tiempo de demora: "+day+" días aprox.");
            }else{
                time_navigation.setText("Tiempo de demora: "+day+" día aprox.");
            }
        }else{
            int minutes = (int) (time%60);
            if(hour > 0){
                if(minutes < 10) {
                    time_navigation.setText("Tiempo de demora: "+hour+":0"+minutes+" Hrs.");
                }else{
                    time_navigation.setText("Tiempo de demora: "+hour+":"+minutes+" Hrs.");
                }
            }else{
                if(minutes <= 1) {
                    time_navigation.setText("Tiempo demora: " + minutes + " min.");
                }
                else if(minutes < 10){
                    time_navigation.setText("Tiempo demora: 0" + minutes + " min.");
                }else{
                    time_navigation.setText("Tiempo demora: "+minutes+" min.");
                }
            }
        }
    }



    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager((PermissionsListener) this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    public void loginRoute(View view){
        float_menu.collapse();
        Intent loginView = new Intent(this, LoginActivity.class);
        startActivity(loginView);
    }

    public Zone getInfoOfZone(){
        Zone zone = null;
        for (int i = 0; i < zonesMain.size(); i++){
            if(zonesMain.get(i).getName().equals(zoneName)){
                zone = zonesMain.get(i);
                break;
            }
        }
        return zone;
    }

    public void alertGps(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("GPS Apagado");
        alertDialog.setMessage(R.string.gps_off);
        alertDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    dialog.dismiss();
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Habilite el GPS", Toast.LENGTH_LONG).show();
                    alertGps();
                }
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void moreView(View view){
        Zone zone = getInfoOfZone();
        Bundle bundle = new Bundle();
        bundle.putSerializable("dataZone", zone);
        Intent infoZoneView = new Intent(this, InfoZone.class);
        infoZoneView.putExtra("data", bundle);
        startActivity(infoZoneView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        if(offline){
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigation_on = false;
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            alertGps();
        }
        if (networkStateReceiver == null) {
            networkStateReceiver = new NetworkStateReceiver();
            networkStateReceiver.addListener(this);
            this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }else{
            this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkStateReceiver != null) {
            networkStateReceiver.removeListener(this);
            this.unregisterReceiver(networkStateReceiver);
            networkStateReceiver = null;
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void networkAvailable() {
        //Nothing to do
        offline = false;
    }

    @Override
    public void networkUnavailable() {
        if(!navigation_on){
            Intent alertInternetView = new Intent(this, AlertInternetActivity.class);
            startActivity(alertInternetView);
            offline = true;
            this.finish();
        }
    }
}














