package com.example.tesis.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tesis.GuestSesion.LoginActivity;
import com.example.tesis.Model.Zone;
import com.example.tesis.FullArActivity;
import com.example.tesis.R;
import com.example.tesis.SingleElementARActivity;
import com.example.tesis.ui.authActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.squareup.picasso.Picasso;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.tesis.ui.authActivity.onRute;
import static com.example.tesis.ui.authActivity.subZones;
import static com.example.tesis.ui.authActivity.zoneCurrentEvents;
import static com.example.tesis.ui.authActivity.zoneEventinCurse;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

import static com.example.tesis.ui.authActivity.zones;

public class MapFragment extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    private authActivity activity;
    private MapView mapView;

    public static MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    private FirebaseFirestore db;
    private ArrayList symbolLayerIconFeatureList;

    private LocationComponent locationComponent;

    private Button startNavigation, showDetail, ar_button, search, erase;
    private ImageButton close;
    private DirectionsRoute currentRoute;
    private Locale spanish = new Locale("es", "Es"); //set spanish instruction
    private NavigationMapRoute navigationMapRoute;
    private String zoneName;
    private RelativeLayout customMarker;
    private ImageView img;
    private TextView title, description, time_navigation;
    private String[] dataZone = new String[2];

    private FloatingActionsMenu float_menu;
    private FloatingActionButton update, info, reality_augmented, my_location;
    private View root;

    private AutoCompleteTextView autoCompleteTextView;
    private Style style;
    private ArrayList<QueryDocumentSnapshot> documentsFound;
    private ArrayList<DocumentSnapshot> documentSnapshotsFound;
    private boolean isSearch = false;
    private ProgressDialog progressDialog;
    private String navigation_element = DirectionsCriteria.PROFILE_WALKING;

    private com.google.android.material.floatingactionbutton.FloatingActionButton car_navigation, walk_navigation, bike_navigation,
            car_navigation_p, walk_navigation_p, bike_navigation_p;

    private Point auxOrigin, auxDestination;
    private LocationManager lm;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (authActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Buscando sector...");
        progressDialog.setCancelable(false);
        lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(getContext().getApplicationContext(), getString(R.string.mapbox_access_token));
        root = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();
        symbolLayerIconFeatureList = new ArrayList<>();
        return root;
    }

    public void getallZones() {
        int numberOfZones = zones.size() + subZones.size();
        String[] allZones = new String[numberOfZones];
        for (int i = 0; i < zones.size(); i++) {
            if (zones.get(i).getPermanent()) {
                allZones[i] = zones.get(i).getName();
            } else {
                if (zoneCurrentEvents.contains(zones.get(i).getId()) || zoneEventinCurse.contains(zones.get(i).getId())) {
                    allZones[i] = zones.get(i).getName();
                }
            }
        }
        int auxCount = zones.size();
        for (int i = 0; i < subZones.size(); i++) {
            allZones[auxCount] = subZones.get(i).getName();
            auxCount++;
        }

        for (int i = 0; i < allZones.length; i++) {
            if (allZones[i] == null) {
                allZones[i] = "";
            }
        }
        autoCompleteTextView = getActivity().findViewById(R.id.searched);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getContext(), R.layout.item_dropdown, allZones);
        autoCompleteTextView.setAdapter(arrayAdapter);
    }

    private void setMapbox(MapboxMap m) {
        this.mapboxMap = m;
    }

    private void upadateGeoPoints() {
        symbolLayerIconFeatureList.clear();
        for (int i = 0; i < zones.size(); i++) {
            Feature singleFeature = Feature.fromGeometry(Point.fromLngLat(zones.get(i).getLongitude(), zones.get(i).getLatitude()));
            singleFeature.addStringProperty("title", zones.get(i).getName());
            singleFeature.addStringProperty("description", zones.get(i).getDescription());
            singleFeature.addStringProperty("url_photo", zones.get(i).getLastPhotoIndex());
            if (zoneEventinCurse.contains(zones.get(i).getId())) {
                singleFeature.addStringProperty("evento", "ahora");
            } else if (zoneCurrentEvents.contains(zones.get(i).getId())) {
                singleFeature.addStringProperty("evento", "si");
            } else {
                singleFeature.addStringProperty("evento", "no");
            }
            if (!zones.get(i).getPermanent()) {
                if (zoneCurrentEvents.contains(zones.get(i).getId()) || zoneEventinCurse.contains(zones.get(i).getId())) {
                    symbolLayerIconFeatureList.add(singleFeature);
                }
            } else {
                symbolLayerIconFeatureList.add(singleFeature);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        documentsFound = new ArrayList<>();
        documentSnapshotsFound = new ArrayList<>();
        customMarker = (RelativeLayout) getActivity().findViewById(R.id.customMarker);

        float_menu = (FloatingActionsMenu) getActivity().findViewById(R.id.menu_float);
        info = (FloatingActionButton) getActivity().findViewById(R.id.button_info_map);
        update = (FloatingActionButton) getActivity().findViewById(R.id.button_update_map);
        my_location = (FloatingActionButton) getActivity().findViewById(R.id.button_my_location);
        reality_augmented = (FloatingActionButton) getActivity().findViewById(R.id.button_augmented_reality);
        startNavigation = getActivity().findViewById(R.id.startButton);

        img = (ImageView) getActivity().findViewById(R.id.ImgMarker);
        title = (TextView) getActivity().findViewById(R.id.title_marker);
        description = (TextView) getActivity().findViewById(R.id.description_marker);
        close = (ImageButton) getActivity().findViewById(R.id.close);
        showDetail = (Button) getActivity().findViewById(R.id.show_more);
        ar_button = (Button) getActivity().findViewById(R.id.ar_button);

        search = (Button) getActivity().findViewById(R.id.search_button);
        erase = (Button) getActivity().findViewById(R.id.search_erase);

        car_navigation = getActivity().findViewById(R.id.car_button);
        bike_navigation = getActivity().findViewById(R.id.bike_button);
        walk_navigation = getActivity().findViewById(R.id.walk_button);

        car_navigation_p = getActivity().findViewById(R.id.car_button_pressed);
        bike_navigation_p = getActivity().findViewById(R.id.bike_button_pressed);
        walk_navigation_p = getActivity().findViewById(R.id.walk_button_pressed);

        time_navigation = getActivity().findViewById(R.id.time_navigation);

        //MapView large
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        lm.addGpsStatusListener(new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    alertGps();
                }
            }
        });

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                setMapbox(mapboxMap);
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        LocalizationPlugin localizationPlugin = new LocalizationPlugin(mapView, mapboxMap, style);
                        upadateGeoPoints();
                        Layer settlementLabelLayer = style.getLayer("settlement-label");
                        if (settlementLabelLayer != null) {
                            settlementLabelLayer.setProperties(textField("{name_es}"));
                        }

                        style.addSource(new GeoJsonSource("marker-source",
                                FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));

                        style.addImage("my-marker-image", BitmapFactory.decodeResource(
                                getActivity().getResources(), R.mipmap.red_marker));

                        style.addImage("my-marker-image2", BitmapFactory.decodeResource(
                                getActivity().getResources(), R.mipmap.blue_marker));

                        style.addImage("my-marker-image3", BitmapFactory.decodeResource(
                                getActivity().getResources(), R.mipmap.gray_marker));

                        style.addLayer(new SymbolLayer("marker-layer", "marker-source")
                                        .withProperties(PropertyFactory.iconImage(match(
                                                get("evento"), literal("si"),
                                                stop("si", "my-marker-image3"),
                                                stop("no", "my-marker-image"),
                                                stop("ahora", "my-marker-image2"))),
                                                iconAllowOverlap(true),
                                                iconOffset(new Float[]{0f, -9f})));

                        addDestinationIconSymbolLayer(style);
                        enableLocationComponent(style);
                        getallZones();
                        mapboxMap.addOnMapClickListener(MapFragment.this);
                    }
                });
            }
        });

        startNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean simulateRoute = false;
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(simulateRoute)
                        .build();
                    // Call this method with Context from within an Activity
                NavigationLauncher.startNavigation(getActivity(), options);
                onRute = true;
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMarker();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog dialog = new ProgressDialog(getContext());
                dialog.setTitle("Actualizando mapa ...");
                dialog.setCancelable(false);
                dialog.show();
                float_menu.collapseImmediately();

                authActivity a = new authActivity();
                a.getActiveEvents();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateMap();
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Mapa actualizado", Toast.LENGTH_LONG).show();
                    }
                }, 5000);
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float_menu.collapseImmediately();
                showInformationMarker();
            }
        });

        showDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailZone();
            }
        });

        reality_augmented.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    showSnackBar("Permisos de cámara denegados. Otorge los permisos para utilizar esta funcioanlidad.");
                }else {
                    Intent arActivity = new Intent(getActivity(), FullArActivity.class);
                    startActivity(arActivity);
                    getActivity().finish();
                }
            }
        });

        ar_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    showSnackBar("Permisos de cámara denegados. Otorge los permisos para utilizar esta funcioanlidad.");
                }else {
                    Intent arActivity = new Intent(getActivity(), SingleElementARActivity.class);
                    arActivity.putExtra("zoneTitle", zoneName);
                    startActivity(arActivity);
                    getActivity().finish();
                }
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = autoCompleteTextView.getText().toString().trim();
                String name_zone = Normalizer.normalize(data, Normalizer.Form.NFD);
                data = name_zone.replaceAll("[^\\p{ASCII}]", "");
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
                updateMap();
            }
        });

        my_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float_menu.collapse();
                if(locationComponent!=null){
                    setCameraPosition(locationComponent.getLastKnownLocation().getLatitude(),
                            locationComponent.getLastKnownLocation().getLongitude(),
                            12, 5);
                }
                else{
                    Toast.makeText(getContext(), "Debe brindar permisos de localización para usar esta funcionalidad", Toast.LENGTH_LONG).show();
                }
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

    public void alertGps(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("GPS Apagado");
        alertDialog.setMessage(R.string.gps_off);
        alertDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    dialog.dismiss();
                }else{
                    Toast.makeText(getApplicationContext(), "Habilite el GPS", Toast.LENGTH_LONG).show();
                    alertGps();
                }
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(root.findViewById(R.id.root_layout_fragment), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Aceptar", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public void searchZone(String data){
        progressDialog.show();
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
        updateMap();
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
            if(zoneEventinCurse.contains(documentsFound.get(i).getId())){
                singleFeature.addStringProperty("evento", "ahora");
            }
            else if(zoneCurrentEvents.contains(documentsFound.get(i).getId())){
                singleFeature.addStringProperty("evento", "si");
            }
            else{
                singleFeature.addStringProperty("evento", "no");
            }
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
            setCameraPosition(lat, lon, 15, 5);
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

    public void setSymbolLayerSnapshot(ArrayList<DocumentSnapshot> documentsFound){
        symbolLayerIconFeatureList.clear();
        for(int i=0; i < documentsFound.size(); i++){
            Feature singleFeature = Feature.fromGeometry(Point.fromLngLat(documentsFound.get(i).getGeoPoint("coordenada").getLongitude(), documentsFound.get(i).getGeoPoint("coordenada").getLatitude()));
            singleFeature.addStringProperty("title", documentsFound.get(i).getString("nombre"));
            singleFeature.addStringProperty("description", documentsFound.get(i).getString("descripcion"));
            String photos = documentsFound.get(i).get("foto").toString();
            String url = getLinkPhoto(photos);
            singleFeature.addStringProperty("url_photo", url);
            if(zoneEventinCurse.contains(documentsFound.get(i).getId())){
                singleFeature.addStringProperty("evento", "ahora");
            }
            else if(zoneCurrentEvents.contains(documentsFound.get(i).getId())){
                singleFeature.addStringProperty("evento", "si");
            }
            else{
                singleFeature.addStringProperty("evento", "no");
            }
            symbolLayerIconFeatureList.add(singleFeature);
        }
        double lat = documentsFound.get(0).getGeoPoint("coordenada").getLatitude();
        double lon = documentsFound.get(0).getGeoPoint("coordenada").getLongitude();
        setValuetoSetCamera(documentsFound.size(), lat, lon);
    }

    public Zone getInfoOfZone(){
        Zone zone = null;
        for (int i=0; i < zones.size(); i++){
            if(zones.get(i).getName().equals(dataZone[0])){
                zone = zones.get(i);
                break;
            }
        }
        return zone;
    }

    public void showDetailZone(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Cargando datos ...");
        progressDialog.setCancelable(false);
        Zone zone = getInfoOfZone();
        Bundle bundle = new Bundle();
        bundle.putSerializable("dataZone", zone);
        bundle.putSerializable("isActiveEvent", dataZone[1]);
        Navigation.findNavController(root).navigate(R.id.infoZoneFragment, bundle);
        navigationMapRoute = null; //Cuando se ingresa a ver detalles de una zona, la navegación tiene un bug que al volver de la actividad de detalles ya no muestra la ruta. Esto lo soluciona.
        progressDialog.dismiss();
    }

    public void showInformationMarker(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog_Alert);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialogmarker_information, (ConstraintLayout) getActivity().findViewById(R.id.layout_container_dialog_info));

        ((TextView) view.findViewById(R.id.title_dialog_info)).setText("Ayuda");
        ((Button) view.findViewById(R.id.accept_dialog_info)).setText("Aceptar");
        ((Button) view.findViewById(R.id.show_more_dialog_info)).setText("Ver más");
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        view.findViewById(R.id.accept_dialog_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        view.findViewById(R.id.show_more_dialog_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                showAllZones();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void showAllZones(){
        db.collection("Zona").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                String allZones= "";
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getBoolean("zona fija")){
                        allZones += "- "+document.get("nombre").toString()+"\n";
                    }
                }
                AlertDialog.Builder allZonesMessage = new AlertDialog.Builder(getContext());
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

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        if(navigationMapRoute == null){
            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
        }
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


    public void closeMarker(){
        navigationMapRoute.updateRouteVisibilityTo(false);
        quitAnimationMarker();
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getApplicationContext())) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(getApplicationContext(), loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            //locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            //permissionsManager = new PermissionsManager((PermissionsListener) this);
            //permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(getApplicationContext(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(getContext())
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
                            startNavigation.setEnabled(false);
                            time_navigation.setText("Sin ruta disponible para este medio.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            startNavigation.setEnabled(false);
                            time_navigation.setText("Sin ruta disponible para este medio.");
                            return;
                        }
                        currentRoute = response.body().routes().get(0);
                        showTimeToArrival(currentRoute.duration()/60);
                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.updateRouteArrowVisibilityTo(false);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.println(Log.WARN, "Problema","Error: " + throwable.getMessage());
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(getContext(), "aca", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            alertGps();
        }
        else if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
            alertDialog.setTitle("Permisos desactivados");
            alertDialog.setMessage("Se ha detectado que los permisos de ubicación han sido desactivados.\n" +
                    "Esta herramienta necesita de estos permisos para funcionar correctamente.\n" +
                    "Reinicie o activelos manualmente en las configuraciones del dispositivo para esta aplicación.");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    getActivity().finish();
                }
            });
            alertDialog.show();
        }
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //mapView.onSaveInstanceState(outState);
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        float_menu.collapse();
        handleClickIcon(mapboxMap.getProjection().toScreenLocation(point), point);
        return true;
    }

    private void handleClickIcon(PointF screenPoint, LatLng point) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, "marker-layer");
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        if (!features.isEmpty()) {
            Feature selectedFeature = features.get(0);
            Point auxPoint = (Point) selectedFeature.geometry();
            //Seteamos el destino al punto seleccionado
            destinationPoint = Point.fromLngLat(auxPoint.longitude(), auxPoint.latitude());
            //seteamos posición de cámara para seguir al marcador
            setCameraPosition(auxPoint.latitude(), auxPoint.longitude(), 17, 5);

            dataZone[0] = selectedFeature.getStringProperty("title");
            String description = selectedFeature.getStringProperty("description");
            String url_photo = selectedFeature.getStringProperty("url_photo");
            dataZone[1] = selectedFeature.getStringProperty("evento");
            //selectedIcon(point)
            showCustomMarker(dataZone[0], description, url_photo);
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
            if(customMarker.getVisibility() == View.VISIBLE){
                quitAnimationMarker();
            }
            if (navigationMapRoute != null) {
                navigationMapRoute.updateRouteVisibilityTo(false);
            }
        }
    }

    public void showTimeToArrival(Double time){
        startNavigation.setEnabled(true);
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
        if(customMarker.getVisibility() == View.GONE){
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

        customMarker.setLayoutAnimation(controller);
        customMarker.startAnimation(animation);
        customMarker.setVisibility(View.VISIBLE);
    }

    private void quitAnimationMarker(){
        AnimationSet set = new AnimationSet(true);
        Animation animation = null;
        //desde la esquina superior izquierda a la esquina inferior derecha
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        //duración en milisegundos
        animation.setDuration(300);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.25f);
        customMarker.setLayoutAnimation(controller);
        customMarker.startAnimation(animation);
        customMarker.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
    }

    private void updateMap(){
        if(!isSearch){
            upadateGeoPoints();
        }
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.removeSource("marker-source");
                style.removeLayer("marker-layer");
                style.removeImage("my-marker-image");
                style.removeImage("my-marker-image2");
                style.removeImage("my-marker-image3");

                style.addSource(new GeoJsonSource("marker-source",
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));

                style.addImage("my-marker-image", BitmapFactory.decodeResource(
                        getActivity().getResources(), R.mipmap.red_marker));

                style.addImage("my-marker-image2", BitmapFactory.decodeResource(
                        getActivity().getResources(), R.mipmap.blue_marker));

                style.addImage("my-marker-image3", BitmapFactory.decodeResource(
                        getActivity().getResources(), R.mipmap.gray_marker));

                style.addLayer(new SymbolLayer("marker-layer", "marker-source")
                        .withProperties(PropertyFactory.iconImage(match(
                                get("evento"), literal("si"),
                                stop("si", "my-marker-image3"),
                                stop("no", "my-marker-image"),
                                stop("ahora", "my-marker-image2"))),
                                iconAllowOverlap(true),
                                iconOffset(new Float[]{0f, -9f})));

                //enableLocationComponent(style);
                addDestinationIconSymbolLayer(style);
            }
        });
    }
}