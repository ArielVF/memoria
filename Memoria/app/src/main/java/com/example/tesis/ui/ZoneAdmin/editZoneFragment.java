package com.example.tesis.ui.ZoneAdmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.tesis.Model.ImageFilePath;
import com.example.tesis.Model.Zone;
import com.example.tesis.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.tesis.ui.authActivity.subZones;
import static com.example.tesis.ui.authActivity.zoneCurrentEvents;
import static com.example.tesis.ui.authActivity.zoneEventinCurse;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class editZoneFragment extends Fragment implements MapboxMap.OnMapClickListener {
    private TextInputEditText name, description, floor;
    private MapView mapView;
    private MapboxMap map;
    private double lat = 0.0, lon = 0.0, auxLat, auxLon;
    private Button editZone, deleteZone, addSubZone, deleteSubZone;
    private String[] floorSelect;
    private String id, auxFloor, auxName, auxDescription;
    private int option = -1;
    private View root;
    private String[] nameSubZones, photos;
    private FirebaseFirestore db;
    private ImageView image1, image2, image3;
    private Button editImage1, editImage2, editImage3, continue_edit, back_edit;
    private Button deleteImage1, deleteImage2, deleteImage3;
    private int optionUpdate = 0;
    private String[] paths = new String[3];
    private boolean newPhoto;
    private StorageReference storageReference;
    private LinearLayout container1, container2;
    private List<String> photoToChangeAndDelete;

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private CarmenFeature home;
    private CarmenFeature work;
    private FloatingActionButton search;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSubZones();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("Fotos Zonas");
        initializePaths();

        Bundle zoneObject = getArguments();
        if(zoneObject != null) {
            Zone zone = (Zone) zoneObject.getSerializable("zone");
            auxName = zone.getName();
            auxDescription = zone.getDescription();
            photoToChangeAndDelete = new ArrayList<>();
            this.id = zone.getId();
            auxFloor = String.valueOf(zone.getFloor());
            auxLat = zone.getLatitude();
            auxLon = zone.getLongitude();
            this.photos = zone.getPhotos();
            this.lat = auxLat;
            this.lon = auxLon;
        }
    }

    public void initializePaths(){
        paths[0] = "";
        paths[1] = "";
        paths[2] = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_edit_zone, container, false);
        Mapbox.getInstance(getContext().getApplicationContext(), getString(R.string.mapbox_access_token));
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        this.name = view.findViewById(R.id.newzone_name_edit);
        this.description = view.findViewById(R.id.newzone_description_edit);
        this.floor = view.findViewById(R.id.newzone_floor_edit);

        this.editZone = view.findViewById(R.id.edit_zone_button);
        this.deleteZone = view.findViewById(R.id.delete_zone_button);

        this.addSubZone = view.findViewById(R.id.add_subzone);
        this.deleteSubZone = view.findViewById(R.id.delete_subzone);

        this.name.setText(auxName);
        this.description.setText(auxDescription);
        this.floor.setText(auxFloor);

        this.image1 = view.findViewById(R.id.firstImageEdit);
        this.image2 = view.findViewById(R.id.secondImageEdit);
        this.image3 = view.findViewById(R.id.thirdImageEdit);

        this.deleteImage1 = view.findViewById(R.id.firtsPhotoButtonDelete);
        this.deleteImage2 = view.findViewById(R.id.secondPhotoButtonDelete);
        this.deleteImage3 = view.findViewById(R.id.thirdPhotoButtonDelete);

        this.editImage1 = view.findViewById(R.id.firtsPhotoButtonEdit);
        this.editImage2 = view.findViewById(R.id.secondPhotoButtonEdit);
        this.editImage3 = view.findViewById(R.id.thirdPhotoButtonEdit);

        this.continue_edit = view.findViewById(R.id.button_continue_edit_zone);
        this.back_edit = view.findViewById(R.id.back_button_edit_zone);

        this.container1 = view.findViewById(R.id.container);
        this.container2 = view.findViewById(R.id.container2);

        this.search = view.findViewById(R.id.fab_location_search);

        setImage();

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

        continue_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container1.setVisibility(View.GONE);
                container2.setVisibility(View.VISIBLE);
            }
        });

        back_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container1.setVisibility(View.VISIBLE);
                container2.setVisibility(View.GONE);
            }
        });

        this.editImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionUpdate = 1;
                callCameraOrGallery();
            }
        });

        this.editImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionUpdate = 2;
                callCameraOrGallery();
            }
        });

        this.editImage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionUpdate = 3;
                callCameraOrGallery();
            }
        });

        this.deleteImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paths[0] = "sin imagen";
                changeNoImage(0);
            }
        });

        this.deleteImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paths[1] = "sin imagen";
                changeNoImage(1);
            }
        });

        this.deleteImage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paths[2] = "sin imagen";
                changeNoImage(2);
            }
        });

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                setMapboxMap(mapboxMap);
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        addDestinationIconSymbolLayer(style);
                        setDestinationLayer();
                        addUserLocations();
                        setUpSource(style);
                        mapboxMap.addOnMapClickListener(editZoneFragment.this);
                    }
                });
            }
        });

        this.addSubZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveZeroFloor()){
                    selectFloor(0);
                }
                else{
                    Toast.makeText(getContext(), "La zona seleccionada no cuenta con pisos", Toast.LENGTH_LONG).show();
                }
            }
        });

        this.deleteSubZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveZeroFloor()){
                    selectFloor(1);
                }
                else{
                    Toast.makeText(getContext(), "La zona seleccionada no cuenta con pisos", Toast.LENGTH_LONG).show();
                }
            }
        });

        this.editZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    if(existChange()){
                        applyDataZone();
                    }else{
                        Toast.makeText(getContext(), "No existen cambios nuevos para actualizar.", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(getContext(), "Existen campos vaciós, por favor rellene todas las entradas.", Toast.LENGTH_LONG).show();
                }
            }
        });

        this.deleteZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert();
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
        ImagePicker.with(editZoneFragment.this)
                .crop()
                .start();
    }

    /**
     * Rellena los campos de imagen con las fotos
     */
    public void setImage(){
        for(int i=0; i < this.photos.length; i++){
            if(!this.photos[i].trim().equals("sin imagen")){
                changeImage(i, photos[i].trim());
            }
            else{
                changeNoImage(i);
            }
        }
    }

    /**
     * Setea imagen con foto
     */
    public void changeImage(int option, String url){
        switch (option){
            case 0:
                Picasso.get().load(url).into(image1);
                deleteImage1.setVisibility(View.VISIBLE);
                editImage1.setVisibility(View.GONE);
                break;
            case 1:
                Picasso.get().load(url).into(image2);
                deleteImage2.setVisibility(View.VISIBLE);
                editImage2.setVisibility(View.GONE);
                break;
            default:
                Picasso.get().load(url).into(image3);
                deleteImage3.setVisibility(View.VISIBLE);
                editImage3.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Setea una imagen sin foto
     */
    public void changeNoImage(int option){
        switch (option){
            case 0:
                image1.setImageResource(R.mipmap.no_image_avaible);
                deleteImage1.setVisibility(View.GONE);
                editImage1.setVisibility(View.VISIBLE);
                break;
            case 1:
                image2.setImageResource(R.mipmap.no_image_avaible);
                deleteImage2.setVisibility(View.GONE);
                editImage2.setVisibility(View.VISIBLE);
                break;
            default:
                image3.setImageResource(R.mipmap.no_image_avaible);
                deleteImage3.setVisibility(View.GONE);
                editImage3.setVisibility(View.VISIBLE);
                break;
        }
    }

    public boolean existChange(){
        String currentName = name.getText().toString();
        String currentDescription = description.getText().toString();
        String currentfloor = floor.getText().toString();
        newPhoto = existNewPhoto();

        return !currentName.equals(auxName) ||
                !currentDescription.equals(auxDescription) ||
                !currentfloor.equals(auxFloor) ||
                auxLat!=lat || auxLon!=lon || newPhoto;
    }

    /**
     * Valida si existen cambios en las fotos
     * @return booleano que confirma si hay cambios en las fotos.
     */
    public boolean existNewPhoto(){
       boolean changes = false;
       for(int i=0; i < paths.length; i++){
           if(!paths[i].equals("")){
               changes = true;
               i = paths.length;
           }
       }
       return changes;
    }

    public void applyDataZone(){
        int currentFloor = Integer.valueOf(floor.getText().toString());
        int newAuxFloor = Integer.valueOf(auxFloor);
        if(currentFloor < newAuxFloor){
            showMessageAboutDeleteAFloor();
        }
        else{
            havechangeinphotos();
        }
    }

    public void havechangeinphotos(){
        if(newPhoto){
            checkProcessPhoto();
        }
        else{
            setPaths();
            editSelectZone();
        }
    }

    public boolean allNewPhotos(){
        boolean allNew = true;
        for(int i=0; i < paths.length; i++){
            if(!photos[i].trim().equals("sin imagen")){
                allNew = false;
                i = paths[i].length();
            }
        }
        return allNew;
    }

    public void checkProcessPhoto(){
        if(!allNewPhotos()){
            searchForDelete();
        }else{
            uploadPhoto();
        }
    }

    public void searchForDelete(){
        for(int i=0; i < paths.length; i++){
            if(!paths[i].trim().equals("") && !photos[i].trim().equals("sin imagen")){
                photoToChangeAndDelete.add(photos[i].trim());
            }
        }
        deletePastPhotos();
    }

    public void deletePastPhotos(){
        for(int i=0; i < photoToChangeAndDelete.size(); i++){

        }
    }

    public void uploadPhoto(){
        organizePathsAndPhotos();
    }

    public void organizePathsAndPhotos(){
        String auxPa;
        String auxPh;
        int lastIndex = paths.length-1;
        for(int i=0; i<paths.length; i++){
            if(!paths[i].trim().equals("")){
                auxPa = paths[lastIndex].trim();
                auxPh = photos[lastIndex].trim();

                paths[lastIndex] = paths[i];
                photos[lastIndex] = photos[i];

                paths[i] = auxPa;
                photos[i] = auxPh;

                i = paths.length;
            }
        }
    }

    /**
     * Setemos los paths para mantener datos de fotos.
     */
    public void setPaths(){
        for (int i=0; i < paths.length; i++){
            paths[i] = photos[i].trim();
            Log.println(Log.ASSERT, "LA", paths[i]);
        }
    }

    public void editSelectZone(){
        String newName = name.getText().toString().trim();
        String newDescription = description.getText().toString().trim();
        Integer newFloor = Integer.valueOf(floor.getText().toString());
        GeoPoint newPoint = new GeoPoint(lat, lon);

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Editando datos");
        progressDialog.setCancelable(false);
        progressDialog.show();
        List<String> dataPhoto = Arrays.asList(paths[0], paths[1], paths[2]);

        db.collection("Zona").document(id)
                .update("nombre", newName,
                "descripcion", newDescription,
                "cantidad piso", newFloor,
                "coordenada", newPoint, "foto", dataPhoto).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    storeNewData();
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Datos actualizados correctamente.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getContext(), "Ocurrió un error, vuelva a intentar actualizar los datos", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void storeNewData(){
        auxName = name.getText().toString();
        auxDescription = description.getText().toString();
        auxFloor = floor.getText().toString();
        auxLat = lat;
        auxLon = lon;
        initializePaths();
    }
    
    public void showMessageAboutDeleteAFloor(){
        AlertDialog.Builder alertdialog =  new AlertDialog.Builder(getContext());
        alertdialog.setTitle("Advertencia");
        alertdialog.setMessage(R.string.delete_floor_in_zone);
        alertdialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteFloor();
            }
        });
        alertdialog.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertdialog.setCancelable(false);
        alertdialog.show();
    }

    public void deleteFloor(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Borrando subzonas ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        int wishFloor = Integer.valueOf(floor.getText().toString());
        int actualFloor = Integer.valueOf(auxFloor);
        db.collection("Pertenece a")
                .whereEqualTo("Zona", this.id)
                .whereGreaterThan("piso", wishFloor)
                .whereLessThanOrEqualTo("piso", actualFloor)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        WriteBatch writeBatch = db.batch();
                        if(task.isSuccessful()){
                            if(task.getResult().isEmpty()){
                                progressDialog.dismiss();
                                havechangeinphotos();
                            }
                            else{
                                for(DocumentSnapshot document: task.getResult()){
                                    writeBatch.delete(document.getReference());
                                }
                                writeBatch.commit()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                havechangeinphotos();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Ocurrió un error, favor vuelva a intentarlo", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Ocurrió un error, favor vuelva a intentarlo", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public boolean validate(){
        return !name.getText().toString().trim().equals("") &&
                !description.getText().toString().trim().equals("") &&
                !floor.getText().toString().trim().equals("");
    }

    public void showAlert(){
        AlertDialog.Builder alertdialog =  new AlertDialog.Builder(getContext());
        alertdialog.setTitle("Advertencia");
        alertdialog.setMessage(R.string.delete_zone_message);
        alertdialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(checkIfExistOnEvent()){
                    Toast.makeText(getContext(), "La zona no puede ser eliminada, porque tiene eventos próximos a ocurrir en ella.", Toast.LENGTH_LONG).show();
                }else{
                    deletePhotos();
                }
            }
        });
        alertdialog.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertdialog.setCancelable(false);
        alertdialog.show();
    }

    public boolean checkIfExistOnEvent(){
        boolean exist = false;
        if(zoneEventinCurse.contains(id) || zoneCurrentEvents.contains(id)){
            exist = true;
        }
        return exist;
    }

    public boolean noHavePhoto(){
        boolean havePhoto = false;
        int lastIndex = photos.length-1;
        for (int i=0; i < photos.length; i++){
            if(!photos[i].trim().equals("sin imagen")){
                havePhoto = true;
                String link = photos[i];
                photos[i] = photos[lastIndex];
                photos[lastIndex] = link;

                i = photos.length;
            }
        }
        return havePhoto;
    }

    public void deletePhotos(){
        if(noHavePhoto()){
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Eliminando fotos");
            progressDialog.setCancelable(false);
            progressDialog.show();
            for(int i=0; i < photos.length; i++){
                if(!photos[i].trim().equals("sin imagen")){
                    int index = i;
                    StorageReference currentPhotoInBd = FirebaseStorage.getInstance().getReferenceFromUrl(photos[i].trim());
                    currentPhotoInBd.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if(index+1 == photos.length){
                                progressDialog.dismiss();
                                deleteSubzonesBelongThisZone();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }else{
            deleteSubzonesBelongThisZone();
        }
    }

    public void deleteSubzonesBelongThisZone(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Eliminando enlaces con subzonas");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("Pertenece a")
                .whereEqualTo("Zona", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        WriteBatch writeBatch = db.batch();
                        if(task.isSuccessful()){
                            if(task.getResult().isEmpty()){
                                deleteCompleteZone(progressDialog);
                            }
                            else{
                                for(DocumentSnapshot document: task.getResult()){
                                    writeBatch.delete(document.getReference());
                                }
                                writeBatch.commit()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                deleteCompleteZone(progressDialog);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Ocurrió un error, favor vuelva a intentarlo", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                        else{
                            Toast.makeText(getContext(), "Ocurrió un error, favor vuelva a intentarlo", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void deleteCompleteZone(ProgressDialog progressDialog){
        progressDialog.setTitle("Eliminando Zona");
        db.collection("Zona")
                .document(id)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Zona eliminada completamente.", Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Ocurrió un error, favor vuelva a intentarlo", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void setDestinationLayer(){
        Point destinationPoint = Point.fromLngLat(this.lon, this.lat);
        GeoJsonSource source = map.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }
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

    public void setMapboxMap(MapboxMap mapboxMap){
        this.map = mapboxMap;
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

    public void selectFloor(final int selection){
        int n_floor = Integer.valueOf(auxFloor);
        this.floorSelect = new String[n_floor];
        for (int i=0; i < n_floor; i++){
            floorSelect[i] = "piso "+(i+1);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccione piso");
        builder.setCancelable(false);

        builder.setSingleChoiceItems(floorSelect, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                option = which;
            }
        }).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getOption(selection);
            }
        }).setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void getOption(int selection){
        if(option != -1){
            Bundle bundle = new Bundle();
            bundle.putSerializable("indexFloor", option);
            bundle.putSerializable("idZone", id);
            bundle.putSerializable("nameSubZone", nameSubZones);
            if(selection == 0){
                Navigation.findNavController(root).navigate(R.id.addSubZoneToZoneFgrament, bundle);
            }
            else{
                Navigation.findNavController(root).navigate(R.id.deleteSubZoneInZoneFragment, bundle);
            }
        }
    }

    public boolean haveZeroFloor(){
        return !this.auxFloor.equals("0");
    }

    public void getSubZones(){
        nameSubZones = new String[subZones.size()];
        for (int i=0; i<subZones.size(); i++){
            nameSubZones[i] = subZones.get(i).getName();
        }
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
            image1.setImageURI(uri);
            editImage1.setVisibility(View.GONE);
            deleteImage1.setVisibility(View.VISIBLE);
            paths[0] = ImageFilePath.getPath(getContext(), data.getData());
        }
        else if(optionUpdate == 2){
            image2.setImageURI(uri);
            editImage2.setVisibility(View.GONE);
            deleteImage2.setVisibility(View.VISIBLE);
            paths[1] = ImageFilePath.getPath(getContext(), data.getData());
        }
        else{
            image3.setImageURI(uri);
            editImage3.setVisibility(View.GONE);
            deleteImage3.setVisibility(View.VISIBLE);
            paths[2] = ImageFilePath.getPath(getContext(), data.getData());
        }
    }
}