package com.example.tesis.ui.ZoneAdmin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.tesis.Model.ImageFilePath;
import com.example.tesis.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static com.example.tesis.ui.authActivity.zones;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class createZoneFragment extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener{
    private View root;
    private MapView mapView;
    private MapboxMap map;
    private double lat = 0.0, lon = 0.0;
    private LinearLayout linearLayout;
    private TextInputEditText floor, name, description;
    private Button create_zone, add1, add2, add3, continue_button, back_button;
    private FirebaseFirestore db;
    private ImageView photo1, photo2, photo3;
    private int optionUpdate;
    private LinearLayout container1, container2;
    private String[] paths = new String[3];
    private StorageReference storageReference;

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private CarmenFeature home;
    private CarmenFeature work;
    private FloatingActionButton search;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_create_zone, container, false);
        Mapbox.getInstance(getContext().getApplicationContext(), getString(R.string.mapbox_access_token));
        initPaths();
        storageReference = FirebaseStorage.getInstance().getReference().child("Fotos Zonas");
        return root;
    }

    public void initPaths(){
        this.paths[0] = "";
        this.paths[1] = "";
        this.paths[2] = "";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        this.linearLayout = view.findViewById(R.id.content_scroll);
        this.name = view.findViewById(R.id.newzone_name);
        this.description = view.findViewById(R.id.newzone_description);
        this.floor = view.findViewById(R.id.newzone_floor);
        this.create_zone = view.findViewById(R.id.create_zone);

        this.add1 = view.findViewById(R.id.firtsPhotoButton);
        this.add2 = view.findViewById(R.id.secondPhotoButton);
        this.add3 = view.findViewById(R.id.thirdPhotoButton);

        this.photo1 = view.findViewById(R.id.firstImage);
        this.photo2 = view.findViewById(R.id.secondImage);
        this.photo3 = view.findViewById(R.id.thirdImage);

        this.continue_button = view.findViewById(R.id.continue_createzone);
        this.back_button = view.findViewById(R.id.back_button_create_zone);

        this.container1 = view.findViewById(R.id.container);
        this.container2 = view.findViewById(R.id.container2);

        this.search = view.findViewById(R.id.fab_location_search);

        db = FirebaseFirestore.getInstance();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .addInjectedFeature(home)
                                .addInjectedFeature(work)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(getActivity());
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });

        continue_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mapHaveIcon()){
                    container2.setVisibility(View.VISIBLE);
                    container1.setVisibility(View.GONE);
                }else{
                    Toast.makeText(getContext(), "Seleccione la ubicación de la zona en el mapa", Toast.LENGTH_LONG).show();
                }
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container2.setVisibility(View.GONE);
                container1.setVisibility(View.VISIBLE);
            }
        });

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                setMapboxMap(mapboxMap);
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        addUserLocations();
                        setUpSource(style);

                        addDestinationIconSymbolLayer(style);
                        mapboxMap.addOnMapClickListener(createZoneFragment.this);
                    }
                });
            }
        });

        create_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

        this.add1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionUpdate = 1;
                callCameraOrGallery();
            }
        });

        this.add2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionUpdate = 2;
                callCameraOrGallery();
            }
        });

        this.add3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionUpdate = 3;
                callCameraOrGallery();
            }
        });
    }

    private void addUserLocations() {
        home = CarmenFeature.builder().text("Universidad de Talca")
                .geometry(Point.fromLngLat(-122.3964485, 37.7912561))
                .placeName("Facultad de ingeniería, Curicó")
                .id("mapbox-sf")
                .properties(new JsonObject())
                .build();

        work = CarmenFeature.builder().text("Universidad de Talca")
                .placeName("Campus Talca")
                .geometry(Point.fromLngLat(-77.0338348, 38.899750))
                .id("mapbox-dc")
                .properties(new JsonObject())
                .build();
    }

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    public void callCameraOrGallery(){
        ImagePicker.with(createZoneFragment.this)
                .crop()
                .start();
    }

    public void setMapboxMap(MapboxMap mapboxMap){
        this.map = mapboxMap;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        GeoJsonSource source = map.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }
        this.lat = point.getLatitude();
        this.lon = point.getLongitude();
        return true;
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));

        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                //iconOffset(new Float[]{0f, -9f}),
                PropertyFactory.iconSize(0.8f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    public boolean mapHaveIcon(){
        return this.lat != 0.0 && this.lon != 0.0;
    }

    public void validate(){
        String auxName = name.getText().toString().trim();
        String auxDescription = description.getText().toString().trim();
        String auxFloor = floor.getText().toString();
        if(auxName.equals("") || auxDescription.trim().equals("") || auxFloor.trim().equals("")){
            Toast.makeText(getContext(), "Existen campos vacíos, favor revisar", Toast.LENGTH_LONG).show();
        }
        else if(checkIsRepeatParameter(auxName)){
            Toast.makeText(getContext(), "Ya existe un sector con ese nombre o ubicado en el mismo sector", Toast.LENGTH_LONG).show();
        }
        else{
            uploadPhoto(auxName, auxDescription, Integer.valueOf(auxFloor));
        }
    }

    public void addZone(String n, String d, int f){
        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setTitle("Creando nueva Zona");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        GeoPoint geoPoint = new GeoPoint(this.lat, this.lon);
        Map<String, Object> zone = new HashMap<>();
        zone.put("nombre", n);
        zone.put("descripcion", d);
        zone.put("cantidad piso", f);
        zone.put("coordenada", geoPoint);
        zone.put("zona fija", true);
        zone.put("foto", Arrays.asList(paths[0], paths[1], paths[2]));

        db.collection("Zona").add(zone)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText( getContext(), "nueva zona agregada", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    setValues();
                }
            }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText( getContext(), "Algo salió mal, intente nuevamente", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

    public void uploadPhoto(String n, String d, int f){
        orderPaths();
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Procesando imágenes");
        progressDialog.setCancelable(false);
        progressDialog.show();
        for(int i=0; i<paths.length; i++) {
            if (!paths[i].equals("")) {
                int index = i;
                try {
                    File url = new File(paths[i]);
                    Bitmap bitmap = new Compressor(getContext())
                            .setMaxWidth(640)
                            .setMaxHeight(480)
                            .setQuality(90)
                            .compressToBitmap(url);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                    final byte[] byte_bitmap = byteArrayOutputStream.toByteArray();

                    StorageReference image = storageReference.child(name.getText().toString()+" "+floor.getText().toString()+i+".jpg"); //Nombre de la imagen
                    UploadTask uploadTask = image.putBytes(byte_bitmap);

                    Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw Objects.requireNonNull(task.getException());
                            }
                            return image.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            paths[index] = task.getResult().toString();
                            if(index+1 == paths.length){
                                progressDialog.dismiss();
                                addZone(n, d, f);
                            }
                        }
                    });
                } catch (IOException e) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Ocurrió un error mientras se subia la imagen, actualice la página e intente nuevamente", Toast.LENGTH_LONG).show();
                }
            } else {
                paths[i] = "sin imagen";
                if(i+1 == paths.length){
                    progressDialog.dismiss();
                    addZone(n, d, f);
                }
            }
        }
    }

    //Revisamos si existe alguna zona con un nombre igual al sitio nuevo o ubicado en el mismo sector.
    public boolean checkIsRepeatParameter(String auxName){
        boolean isRepeat=false;
        GeoPoint selectPoint = new GeoPoint(this.lat, this.lon);
        String currentName;
        GeoPoint currentgeo;
        for (int i=0; i < zones.size(); i++){
            currentName = zones.get(i).getName().trim().toLowerCase();
            currentgeo = new GeoPoint(zones.get(i).getLatitude(), zones.get(i).getLongitude());
            if(currentName.equals(auxName.toLowerCase()) || currentgeo.equals(selectPoint)){
                isRepeat = true;
                i = zones.size();
            }
        }
        return isRepeat;
    }

    public void setValues(){
        this.name.setText("");
        this.description.setText("");
        this.floor.setText("");

        this.photo1.setImageResource(R.mipmap.no_image_avaible);
        this.photo2.setImageResource(R.mipmap.no_image_avaible);
        this.photo3.setImageResource(R.mipmap.no_image_avaible);

        this.lat = 0.0;
        this.lon = 0.0;

        container1.setVisibility(View.VISIBLE);
        container2.setVisibility(View.GONE);

        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                addDestinationIconSymbolLayer(style);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon

            if (map != null) {
                Style style = map.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    // Move map camera to the selected location
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(12)
                                    .build()), 1500);
                }
            }
        }else{
            if (resultCode == Activity.RESULT_OK) {
                updatePhotoImageView(data.getData(), data);
            }
            else if(resultCode == ImagePicker.RESULT_ERROR){
                Toast.makeText(getContext(), "Problemas con obtener la imagen", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void updatePhotoImageView(Uri uri, Intent data){
        if(optionUpdate == 1){
            photo1.setImageURI(uri);
            paths[0] = ImageFilePath.getPath(getContext(), data.getData());
        }
        else if(optionUpdate == 2){
            photo2.setImageURI(uri);
            paths[1] = ImageFilePath.getPath(getContext(), data.getData());
        }
        else{
            photo3.setImageURI(uri);
            paths[2] = ImageFilePath.getPath(getContext(), data.getData());
        }
    }

    /*En caso de existir un elemento con un path distinto a null o vacío, se deja en último lugar
    para que este haga la llamada a crear zona y así evitar que la url no se cargue.*/
    public void orderPaths(){
        int lastindex = paths.length-1;
        String aux;
        for(int i=0; i < paths.length; i++){
            if(!paths[i].equals("")){
                aux = paths[lastindex];
                paths[lastindex] = paths[i];
                paths[i] = aux;
                i = paths.length;
            }
        }
    }
}