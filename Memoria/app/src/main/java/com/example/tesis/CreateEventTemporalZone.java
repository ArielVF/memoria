package com.example.tesis;

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
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.tesis.Model.ImageFilePath;
import com.example.tesis.ui.myEvents.MyEventInfoFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class CreateEventTemporalZone extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener{
    private MapView mapView;
    private MapboxMap map;
    private Calendar calendar;
    private int year, day, month, hour, minutes;
    private double lat = 0.0, lon = 0.0;
    private Button nextStep, backStep, create_event, addImage, deleteImage;
    private LinearLayout layout_one, layout_second;
    private TextInputLayout validator;
    private ImageView imageView;
    private TextInputEditText zone_name;
    private EditText title, description, start, finish, hour_start, hour_finish;
    private String realPath = "";
    private final String errorField = "Este campo es requerido";
    private Timestamp startDate, endDate;
    private FirebaseFirestore db;
    private ProgressDialog message;
    private StorageReference storageReference;
    public Bitmap bitmap = null;
    private FirebaseAuth mAuth;
    private String zoneReference;

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
        message = new ProgressDialog(getContext());
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_create_event_temporal_zone, container, false);

        nextStep = root.findViewById(R.id.next_step);
        backStep = root.findViewById(R.id.back_step);
        layout_one = root.findViewById(R.id.layout_one);
        layout_second = root.findViewById(R.id.layout_second);
        create_event = root.findViewById(R.id.createevent_t);

        zone_name = root.findViewById(R.id.newzone_name_t);

        start = root.findViewById(R.id.event_start_t);
        finish = root.findViewById(R.id.event_finish_t);
        hour_start = root.findViewById(R.id.evet_hourstart_t);
        hour_finish = root.findViewById(R.id.event_hourfinish_t);
        title = root.findViewById(R.id.event_title_t);
        addImage = root.findViewById(R.id.add_image_button_t);
        imageView = root.findViewById(R.id.image_event_t);
        description = root.findViewById(R.id.event_description_t);
        deleteImage = root.findViewById(R.id.delete_image_button_t);

        search = root.findViewById(R.id.fab_location_search);

        setCalendar();

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

        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate_step_one()){
                    layout_one.setVisibility(View.GONE);
                    layout_second.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getContext(), "Faltan datos para continuar:\n" +
                            "-Seleccione un punto en el mapa", Toast.LENGTH_LONG).show();
                }
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(CreateEventTemporalZone.this)
                        .crop()
                        .start();
            }
        });

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage.setVisibility(View.VISIBLE);
                deleteImage.setVisibility(View.GONE);

                imageView.setImageResource(R.mipmap.no_image_avaible);
                realPath = "";
            }
        });

        backStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_one.setVisibility(View.VISIBLE);
                layout_second.setVisibility(View.GONE);
            }
        });

        create_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate_step_two()){
                    createTemporalZoneEvent();
                }
            }
        });

        start.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    showCalendar(0);
                }
                return false;
            }
        });

        finish.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    showCalendar(1);
                }
                return false;
            }
        });

        hour_start.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    showHour(0);
                }
                return false;
            }
        });

        hour_finish.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    showHour(1);
                }
                return false;
            }
        });

        //Set validator
        zone_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) getActivity().findViewById(R.id.layout_name_zone);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) getActivity().findViewById(R.id.layout_name_t);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) getActivity().findViewById(R.id.layout_description);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        start.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) getActivity().findViewById(R.id.layout_start);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        finish.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) getActivity().findViewById(R.id.layout_finish);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        hour_start.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) getActivity().findViewById(R.id.layout_hour);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        hour_finish.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) getActivity().findViewById(R.id.layout_hourfinish);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return root;
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

                        addUserLocations();
                        // Create an empty GeoJSON source using the empty feature collection
                        setUpSource(style);

                        addDestinationIconSymbolLayer(style);
                        mapboxMap.addOnMapClickListener(CreateEventTemporalZone.this);
                    }
                });
            }
        });
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
        Log.println(Log.ASSERT, "coord:", lat+" "+lon);
        return false;
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

    public boolean validate_step_one(){
        return lat != 0.0 && lon != 0.0;
    }

    public boolean validate_step_two(){
        boolean isValid = false;
        if(zone_name.getText().toString().trim().equals("")){
            validator = (TextInputLayout) getActivity().findViewById(R.id.layout_name_zone);
            validator.setError(errorField);
            showToast("nombre zona");
        }
        if(title.getText().toString().trim().equals("")){
            validator = (TextInputLayout) getActivity().findViewById(R.id.layout_name_t);
            validator.setError(errorField);
            showToast("título");
        }
        else if(description.getText().toString().trim().equals("")){
            validator = (TextInputLayout) getActivity().findViewById(R.id.layout_description);
            validator.setError(errorField);
            showToast("descripción");
        }
        else if(start.getText().toString().equals("")){
            validator = (TextInputLayout) getActivity().findViewById(R.id.layout_start);
            validator.setError(errorField);
            showToast("fecha inicio");
        }
        else if(hour_start.getText().toString().equals("")){
            validator = (TextInputLayout) getActivity().findViewById(R.id.layout_hour);
            validator.setError(errorField);
            showToast("hora inicio");
        }
        else if(finish.getText().toString().equals("")){
            validator = (TextInputLayout) getActivity().findViewById(R.id.layout_finish);
            validator.setError(errorField);
            showToast("fecha término");
        }
        else if(hour_finish.getText().toString().equals("")){
            validator = (TextInputLayout) getActivity().findViewById(R.id.layout_hourfinish);
            validator.setError(errorField);
            showToast("hora término");
        }
        else if(checkDateCoherence()){
            showDateError();
        }
        else{
            isValid = true;
        }
        return isValid;
    }

    public void showToast(String value){
        Toast.makeText(getContext(), "Existen problemas, favor revise el campo "+value+", debe completarlo para continuar.", Toast.LENGTH_LONG).show();
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

    public boolean checkDateCoherence(){
        boolean isNovalid = true;
        SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Date today = new Date(); //get date today
        String auxStart = start.getText().toString()+" "+hour_start.getText().toString();
        String auxFinish = finish.getText().toString()+" "+hour_finish.getText().toString();
        try {
            Date dates=formatter.parse(auxStart);
            Date datef=formatter.parse(auxFinish);
            if(dates.compareTo(datef) <= 0){
                if(today.compareTo(dates) <= 0){
                    getDateInTimeStamp(dates, datef);
                    isNovalid = false;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return isNovalid;
    }

    private void getDateInTimeStamp(Date dates, Date datef) {
        startDate = new Timestamp(dates);
        endDate = new Timestamp(datef);
    }

    public void setCalendar(){
        calendar = Calendar.getInstance();
        start.setInputType(InputType.TYPE_NULL);
        finish.setInputType(InputType.TYPE_NULL);
        hour_start.setInputType(InputType.TYPE_NULL);
        hour_finish.setInputType(InputType.TYPE_NULL);

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        hour = calendar.get(Calendar.HOUR);
        minutes = calendar.get(Calendar.MINUTE);
    }

    public void showHour(int option){
        TimePickerDialog timerPickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if(option == 1){
                    hour_finish.setText(String.format("%02d:%02d", hourOfDay, minute));
                }
                else{
                    hour_start.setText(String.format("%02d:%02d", hourOfDay, minute));
                }
            }
        }, hour+12, minutes, true);
        timerPickerDialog.show();
    }

    public void showCalendar(int option){
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if(option == 1){
                    finish.setText(String.format("%02d/%02d/%04d", dayOfMonth, month+1,year));
                }
                else {
                    start.setText(String.format("%02d/%02d/%04d", dayOfMonth, month+1,year));
                }
            }
        },year, month, day);
        datePickerDialog.show();
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
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
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
        }else{
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                imageView.setImageURI(uri);
                realPath = ImageFilePath.getPath(getContext(), data.getData());

                deleteImage.setVisibility(View.VISIBLE);
                addImage.setVisibility(View.GONE);
            }
        }
    }

    public void createTemporalZoneEvent(){
        ProgressDialog dialog = new ProgressDialog(getContext());
        message.setTitle("Agregando zona temporal");
        message.setCanceledOnTouchOutside(false);
        message.show();

        GeoPoint geoPoint = new GeoPoint(this.lat, this.lon);
        Map<String, Object> zone = new HashMap<>();
        zone.put("nombre", zone_name.getText().toString().trim());
        zone.put("descripcion", "Esta zona es temporal. Una vez finalizado el evento que se realizará en este sector desaparecerá del mapa.");
        zone.put("cantidad piso", 0);
        zone.put("coordenada", geoPoint);
        zone.put("zona fija", false);
        zone.put("foto", Arrays.asList("sin imagen", "sin imagen", "sin imagen"));

        db.collection("Zona").add(zone)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText( getContext(), "zona agregada", Toast.LENGTH_LONG).show();
                        message.dismiss();
                        zoneReference = documentReference.getId();
                        uploadPhoto();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText( getContext(), "Algo salió mal, intente nuevamente", Toast.LENGTH_LONG).show();
                message.dismiss();
            }
        });
    }

    public void uploadPhoto(){
        if(!realPath.equals("")){
            message.setMessage("Subiendo foto del evento...");
            message.setCanceledOnTouchOutside(false);
            message.show();
            try {
                File url = new File(realPath);
                bitmap = new Compressor(getContext())
                        .setMaxWidth(640)
                        .setMaxHeight(480)
                        .setQuality(50)
                        .compressToBitmap(url);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                final byte[] byte_bitmap = byteArrayOutputStream.toByteArray();

                StorageReference image = storageReference.child(title.getText().toString()+"_"+hour_start.getText().toString()+".jpg"); //Nombre de la imagen
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
                        createEvent(task.getResult().toString());
                    }
                });
            } catch (IOException e) {
                message.dismiss();
                Toast.makeText(getContext(), "Ocurrió un error mientras se subia la imagen, intente nuevamente", Toast.LENGTH_LONG).show();
            }
        }else{
            createEvent("sin imagen");
        }
    }

    public void createEvent(String value){
        String idUser = mAuth.getCurrentUser().getUid();
        String idZone = zoneReference;

        Map<String, Object> event= new HashMap<>();
        event.put("creador", idUser);
        event.put("sector", idZone);
        event.put("descripcion", description.getText().toString());
        event.put("nombre", title.getText().toString());
        event.put("fecha inicio", startDate);
        event.put("fecha termino", endDate);
        event.put("foto", value);

        message.setMessage("Creando evento ...");
        message.setCanceledOnTouchOutside(false);
        message.show();

        db.collection("Eventos")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(), "Se ha creado el evento", Toast.LENGTH_LONG).show();message.dismiss();
                        notifyUsers(title.getText().toString());
                        setValues();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Algo paso: "+e, Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void setValues(){
        layout_one.setVisibility(View.VISIBLE);
        layout_second.setVisibility(View.GONE);
        realPath = "";
        zone_name.setText("");
        start.setText("");
        finish.setText("");
        hour_start.setText("");
        hour_finish.setText("");
        title.setText("");
        addImage.setText("");
        imageView.setImageResource(R.mipmap.no_image_avaible);
        description.setText("");
        lat = 0.0;
        lon = 0.0;

        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                addDestinationIconSymbolLayer(style);
            }
        });

    }

    public void notifyUsers(String nameEvent){
        RequestQueue myrequest = Volley.newRequestQueue(getContext());
        JSONObject json = new JSONObject();
        try{
            json.put("to", "/topics/"+"Evento");
            JSONObject notification = new JSONObject();
            notification.put("titulo", "Se agregó el evento "+nameEvent);
            notification.put("detalle", "Atención: El evento ocurrirá en una nueva zona.");
            json.put("data", notification);
            String URL = "https://fcm.googleapis.com/fcm/send";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, json, null, null){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=AAAAWMf6eP0:APA91bGc3ATeF5sII-UnJZSzG9GB14u33eagN_jnvSPKiOHcGlSeS5jwYtkhV5toj2z9BP7EWiBVF0jO6wEj-jsa0trB2xcPasmlagiVzZxL3FeICUmzAFuKa_H7eV5KkdOislr7mpbR");
                    return header;
                }
            };
            myrequest.add(request);
        }catch (JSONException e){
            e.printStackTrace();
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
}