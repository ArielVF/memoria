package com.example.tesis.ui.myEvents;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.tesis.Model.Event;
import com.example.tesis.Model.ImageFilePath;
import com.example.tesis.Model.Zone;
import com.example.tesis.R;
import com.example.tesis.ui.ZoneAdmin.createZoneFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static com.example.tesis.ui.authActivity.zones;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class EditMyEventZoneTemporal extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener{
    private TextView title, description, startDate, endDate, startHour, endHour;
    private String id, auxZoneName, auxTitle, auxDescription, auxStartDate, auxEndDate, auxStartHour, auxEndHour, url;
    private ImageView my_image_event;
    private Zone zoneData;
    private MapView mapView;
    private MapboxMap map;
    private double lat = 0.0, lon = 0.0, auxLat, auxLon;
    private TextInputEditText name_zone_edit;
    private LinearLayout first, second;
    private Button next_step, second_step, edit, delete, change_image, delete_image;
    private int year, day, month, hour, minutes;
    private Calendar calendar;
    private String realPath = "";
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private boolean changeZone=false, changeEvent=false;

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private CarmenFeature home;
    private CarmenFeature work;
    private FloatingActionButton search;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext().getApplicationContext(), getString(R.string.mapbox_access_token));
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("Foto Eventos");
        Bundle eventObject = getArguments();
        if(eventObject != null){
            Event event = (Event) eventObject.getSerializable("event");
            auxZoneName = event.getZone().trim();
            getZone(auxZoneName);
            setAuxValues(event);
        }
        startCalendar();
        this.lat = zoneData.getLatitude();
        this.lon = zoneData.getLongitude();
        auxLat = lat;
        auxLon = lon;
    }

    public void setAuxValues(Event event){
        id = event.getId();
        auxTitle = event.getTitle();
        auxDescription = event.getDescription();
        auxStartDate = event.getDateStart();
        auxStartHour = event.getHourStart();
        auxEndDate = event.getDateEnd();
        auxEndHour = event.getHourEnd();
        url = event.getUrl();
    }

    public void startCalendar(){
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR);
        minutes = calendar.get(Calendar.MINUTE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_my_event_zone_temporal, container, false);
        name_zone_edit = root.findViewById(R.id.newzone_name_t_edit);

        title = root.findViewById(R.id.event_edit_title);
        description = root.findViewById(R.id.event_edit_description);
        startDate = root.findViewById(R.id.event_edit_start);
        startHour = root.findViewById(R.id.evet_edit_hourstart);
        endDate = root.findViewById(R.id.event_edit_finish);
        endHour = root.findViewById(R.id.event_edit_hourfinish);
        my_image_event = root.findViewById(R.id.image_my_event);
        change_image = root.findViewById(R.id.change_image);
        delete_image = root.findViewById(R.id.delete_image);

        edit = root.findViewById(R.id.edit_event_button);
        delete = root.findViewById(R.id.delete_event_button);

        first = root.findViewById(R.id.layout_edit_one);
        second = root.findViewById(R.id.layout_edit_second);
        next_step = root.findViewById(R.id.next_step_edit);
        second_step = root.findViewById(R.id.back_step_edit);

        search = root.findViewById(R.id.fab_location_search);

        setValues();

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

        next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidFirstStep()){
                    first.setVisibility(View.GONE);
                    second.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getContext(), "Existen problemas, seleccione una zona en el mapa.", Toast.LENGTH_LONG).show();
                }
            }
        });

        second_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first.setVisibility(View.VISIBLE);
                second.setVisibility(View.GONE);
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(changes()){
                    if(isValidSecondStep()){
                        if(realPath.equals("")){
                            updateZone();
                        }
                        else{
                            deletePastPhoto();
                        }
                    }
                }else{
                    Toast.makeText(getContext(), "No existen cambios para actualizar.", Toast.LENGTH_LONG).show();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        change_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(EditMyEventZoneTemporal.this)
                        .crop()
                        .start();
            }
        });

        delete_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_image.setVisibility(View.VISIBLE);
                delete_image.setVisibility(View.GONE);
                my_image_event.setImageResource(R.mipmap.no_image_avaible);
                realPath = "sin imagen";
            }
        });

        startDate.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    showCalendar(0);
                }
                return false;
            }
        });
        endDate.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    showCalendar(1);
                }
                return false;
            }
        });
        startHour.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    showHour(0);
                }
                return false;
            }
        });
        endHour.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    showHour(1);
                }
                return false;
            }
        });
        return root;
    }

    public void showDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog_Alert);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_delete, (ConstraintLayout) getActivity().findViewById(R.id.layout_container_dialog));

            ((TextView) view.findViewById(R.id.title_dialog_delete)).setText("Eliminar evento");
            ((TextView) view.findViewById(R.id.text_dialog_delete)).setText("Con esta acción el evento "+auxTitle+" será borrado.\n¿Seguro que desea continuar?");
            ((Button) view.findViewById(R.id.cancel_dialog_delete)).setText("Cancelar");
            ((Button) view.findViewById(R.id.acept_dialog_delete)).setText("Aceptar");
            builder.setView(view);
            AlertDialog alertDialog = builder.create();

            view.findViewById(R.id.cancel_dialog_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            view.findViewById(R.id.acept_dialog_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    checkElementsEvent();
                }
            });
            alertDialog.show();
        }

    /**
     * Revisa si el evento a eliminar tiene alguna foto asociada para borrarla.
     */
    public void checkElementsEvent(){
        if(checkIsNoActiveEvent()){
            if(!url.equals("sin imagen")){
                deletePhoto();
            }
            else{
                deleteZone();
            }
        }
        else{
            Toast.makeText(getContext(), "El evento se encuentra activo, no puede ser eliminado.", Toast.LENGTH_LONG).show();
        }
    }

    public void deletePhoto(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Procesando ...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        StorageReference currentPhotoInBd = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        currentPhotoInBd.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                deleteZone();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void deleteZone(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Borrando zona temporal...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        db.collection("Zona").document(zoneData.getId().trim()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                deleteEvent();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void deleteEvent(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Eliminando Evento...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        db.collection("Eventos").document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        getActivity().onBackPressed();
                        Toast.makeText(getContext(), "El Evento ha sido eliminado", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean checkIsNoActiveEvent() {
        boolean isNoActive = true;
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try{
            Date endDate = formatter.parse(convertDate(auxEndDate, auxEndHour));
            Date startDate = formatter.parse(convertDate(auxStartDate,auxStartHour));
            if(startDate.compareTo(now) < 0 && endDate.compareTo(now) > 0){
                isNoActive = false;
            }
        }
        catch (Exception e){}
        return isNoActive;
    }

    public void showCalendar(int option){
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if(option == 1){
                    endDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, month+1,year));
                }
                else {
                    startDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, month+1,year));
                }
            }
        },year, month, day);
        datePickerDialog.show();
    }

    public void showHour(int option){
        TimePickerDialog timerPickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if(option == 1){
                    endHour.setText(String.format("%02d:%02d", hourOfDay, minute));
                }
                else{
                    startHour.setText(String.format("%02d:%02d", hourOfDay, minute));
                }
            }
        },hour+12, minutes, true);
        timerPickerDialog.show();
    }

    public Timestamp convertTimeStamp(String date){
        Timestamp newDate = new Timestamp(new Date()); //Instancia con la fecha de hoy.
        SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try{
            Date dates=formatter.parse(date);
            newDate = new Timestamp(dates);
        }catch (Exception e){}
        return newDate;
    }

    public void updateZone(){
        if(changeZone){
            ProgressDialog message = new ProgressDialog(getContext());
            message.setMessage("Actualizando zona...");
            message.setCanceledOnTouchOutside(false);
            message.show();
            GeoPoint geoPoint = new GeoPoint(this.lat, this.lon);
            db.collection("Zona").document(zoneData.getId()).update("nombre", name_zone_edit.getText().toString().trim(),
            "coordenada", geoPoint).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    message.dismiss();
                    Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
                    updateEvent();
                }
            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            message.dismiss();
                            Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
                        }
                    });
        }else{
            updateEvent();
        }
    }

    public void updateEvent(){
        if(changeEvent){
            ProgressDialog message = new ProgressDialog(getContext());
            message.setMessage("Actualizando evento ...");
            message.setCanceledOnTouchOutside(false);
            message.show();
            Timestamp dateConvertStart = convertTimeStamp(startDate.getText().toString()+" "+startHour.getText().toString());
            Timestamp dateConvertEnd =  convertTimeStamp(endDate.getText().toString()+" "+endHour.getText().toString());
            db.collection("Eventos").document(id)
                    .update("nombre", title.getText().toString().trim(), "descripcion", description.getText().toString().trim(),
                            "fecha inicio", dateConvertStart, "fecha termino", dateConvertEnd, "foto", url)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
                            message.dismiss();
                            saveinfo();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            message.dismiss();
                            Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
                        }
                    });
        }else {
            saveinfo();
        }
    }

    public void saveinfo(){
        auxZoneName = name_zone_edit.getText().toString().trim();
        auxTitle = title.getText().toString().trim();
        auxDescription = description.getText().toString().trim();
        auxStartHour = startHour.getText().toString().trim();
        auxStartDate = startDate.getText().toString().trim();
        auxEndDate = endDate.getText().toString().trim();
        auxEndHour = endHour.getText().toString().trim();
        realPath = "";
        changeEvent = false;
        changeZone = false;
        auxLat = lat;
        auxLon = lon;
    }

    public boolean changes(){
        boolean existChanges = true;
        if(auxTitle.equals(title.getText().toString()) && auxDescription.equals(description.getText().toString())
                && auxZoneName.equals(name_zone_edit.getText().toString()) && auxStartDate.equals(startDate.getText().toString()) &&
                auxEndDate.equals(endDate.getText().toString()) && auxStartHour.equals(startHour.getText().toString()) &&
                auxEndHour.equals(endHour.getText().toString()) && realPath.equals("") && (lat == auxLat && lon == auxLon)){
            existChanges = false;
        }
        if(existChanges){
            if(!auxTitle.equals(title.getText().toString()) || !auxDescription.equals(description.getText().toString())
                    || !auxStartDate.equals(startDate.getText().toString()) ||
                    !auxEndDate.equals(endDate.getText().toString()) || !auxStartHour.equals(startHour.getText().toString()) ||
                    !auxEndHour.equals(endHour.getText().toString()) || !realPath.equals("")) {
                changeEvent = true;
            }
            if(!auxZoneName.equals(name_zone_edit.getText().toString()) || lat != auxLat || lon != auxLon){
                changeZone = true;
            }
        }
        return existChanges;
    }

    public boolean isValidSecondStep(){
        boolean isValid = true;
        if(name_zone_edit.getText().toString().trim().equals("")){
            isValid = false;
            Toast.makeText(getContext(), "Por favor, ingrese el nombre de la zona", Toast.LENGTH_LONG).show();
        }
        if(name_zone_edit.getText().toString().trim().equals("") || title.getText().toString().trim().equals("") || description.getText().toString().trim().equals("")){
            isValid = false;
            Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_LONG).show();
        }
        else if(checkIsPastEvent()){
            isValid = false;
            Toast.makeText(getContext(), "No se puede modificar un evento activo o caducado.", Toast.LENGTH_LONG).show();
        }
        else if(validateNewDate()){
            showDateError();
            isValid = false;
        }
        return isValid;
    }

    public String convertDate(String dateC, String hourC){
        return dateC+" "+hourC;
    }

    public boolean validateNewDate(){
        boolean isNovalid = true;
        SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String auxStart = startDate.getText().toString()+" "+startHour.getText().toString();
        String auxFinish = endDate.getText().toString()+" "+endHour.getText().toString();
        try {
            Date dates=formatter.parse(auxStart);
            Date datef=formatter.parse(auxFinish);
            Date today = new Date(); //get date today
            if(dates.compareTo(datef) <= 0){
                if(today.compareTo(dates) <= 0){
                    isNovalid = false;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return isNovalid;
    }

    public void showDateError(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Error")
                .setMessage("Existen problemas con la fecha y hora.\n" +
                        "1- Asegurese que la fecha y hora inicial sea mayor o igual que la de hoy.\n" +
                        "2- La fecha inicial debe ser menor o igual que la de termino.")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false);
        alert.show();
    }

    public boolean checkIsPastEvent(){
        boolean past = false;
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try{
            Date startDate = formatter.parse(convertDate(auxStartDate, auxStartHour));
            if(startDate.compareTo(currentDate) <= 0){
                past = true;
            }
        }catch (Exception e){}
        return past;
    }

    public boolean isValidFirstStep(){
        return lat != 0.0 && lon != 0.0;
    }

    public void setValues(){
        name_zone_edit.setText(zoneData.getName());
        title.setText(auxTitle);
        description.setText(auxDescription);
        startDate.setText(auxStartDate);
        startHour.setText(auxStartHour);
        endDate.setText(auxEndDate);
        endHour.setText(auxEndHour);
        if(!url.equals("sin imagen")){
            Picasso.get().load(url).into(my_image_event);
            change_image.setVisibility(View.GONE);
            delete_image.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                setMapboxMap(mapboxMap);
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        addDestinationIconSymbolLayer(style);
                        setCameraPosition();
                        setDestinationLayer();
                        addUserLocations();
                        setUpSource(style);
                        mapboxMap.addOnMapClickListener(EditMyEventZoneTemporal.this);
                    }
                });
            }
        });
    }

    public void setCameraPosition(){
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(lat, lon)) // Sets the new camera position
                .zoom(8) // Sets the zoom
                .tilt(5) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder

        map.setCameraPosition(position);
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
        return false;
    }

    public void setDestinationLayer(){
        Point destinationPoint = Point.fromLngLat(this.lon, this.lat);
        GeoJsonSource source = map.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) { }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                PropertyFactory.iconSize(0.8f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    public void getZone(String zoneName){
        for(int i=0; i< zones.size();i++){
            if(zones.get(i).getName().equals(zoneName)){
                zoneData = zones.get(i);
                i = zones.size();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                                    .zoom(14)
                                    .build()), 1500);
                }
            }
        }
        else{
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                my_image_event.setImageURI(uri);
                realPath = ImageFilePath.getPath(getContext(), data.getData());

                change_image.setVisibility(View.GONE);
                delete_image.setVisibility(View.VISIBLE);
            }
        }
    }

    public void deletePastPhoto(){
        if(!url.equals("sin imagen")){
            StorageReference currentPhotoInBd = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            currentPhotoInBd.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    saveNewPhoto();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
                }
            });
        }
        else{
            saveNewPhoto();
        }
    }

    public void saveNewPhoto(){
        if(!realPath.equals("sin imagen")){
            ProgressDialog message = new ProgressDialog(getContext());
            message.setMessage("Subiendo foto ...");
            message.setCanceledOnTouchOutside(false);
            message.show();
            try {
                File file = new File(realPath);
                Bitmap bitmap = new Compressor(getContext())
                        .setMaxWidth(640)
                        .setMaxHeight(480)
                        .setQuality(50)
                        .compressToBitmap(file);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                final byte[] byte_bitmap = byteArrayOutputStream.toByteArray();

                StorageReference image = storageReference.child(title.getText().toString()+"_"+startHour.getText().toString()+".jpg"); //Nombre de la imagen
                UploadTask uploadTask = image.putBytes(byte_bitmap);

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            message.dismiss();
                            Toast.makeText(getContext(), "Ocurrió un error mientras se subia la imagen, intente nuevamente", Toast.LENGTH_LONG).show();
                            throw Objects.requireNonNull(task.getException());
                        }
                        return image.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        message.dismiss();
                        updateURL(task.getResult().toString());
                        updateZone();
                    }
                });
            } catch (IOException e) {
                message.dismiss();
                Toast.makeText(getContext(), "Ocurrió un error mientras se subia la imagen, intente nuevamente", Toast.LENGTH_LONG).show();
            }
        }else{
            url = realPath;
            updateEvent();
        }
    }

    private void addUserLocations() {
        home = CarmenFeature.builder().text("Universidad de Talca")
                .geometry(Point.fromLngLat(-71.22969342561356, -35.00275968283705))
                .placeName("Facultad de ingeniería, Curicó")
                .id("ut-LN")
                .properties(new JsonObject())
                .build();

        work = CarmenFeature.builder().text("Universidad de Talca")
                .placeName("Campus Talca")
                .geometry(Point.fromLngLat(-71.63557620359465, -35.40495166631072))
                .id("ut-TL")
                .properties(new JsonObject())
                .build();
    }

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    public void updateURL(String newURL){
        this.url = newURL;
    }
}