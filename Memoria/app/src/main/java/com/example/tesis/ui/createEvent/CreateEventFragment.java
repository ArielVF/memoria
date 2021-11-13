package com.example.tesis.ui.createEvent;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.tesis.Model.ImageFilePath;
import com.example.tesis.R;
import com.example.tesis.ui.ZoneAdmin.createZoneFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static com.example.tesis.ui.authActivity.zones;

public class CreateEventFragment extends Fragment {
    private int year, day, month, hour, minutes;
    private Calendar calendar;
    private EditText title, description, start, finish, hour_start, hour_finish;
    private FirebaseFirestore db;
    private ArrayAdapter arrayAdapter;
    private AutoCompleteTextView autoCompleteTextView;
    private Button create, addImage, deleteImage;
    private ImageView imageView;
    private FirebaseAuth mAuth;
    private ProgressDialog message;
    private String[] zone;
    private TextInputLayout validator;
    private final String errorField = "Este campo es requerido";
    private Timestamp startDate, endDate;
    private String realPath = "";

    //Para las imágenes
    private StorageReference storageReference;
    public Bitmap bitmap = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getZones();
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_create_event, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        message = new ProgressDialog(getContext());

        storageReference = FirebaseStorage.getInstance().getReference().child("Foto Eventos");

        autoCompleteTextView = (AutoCompleteTextView) root.findViewById(R.id.event_zone);
        //getData();

        arrayAdapter = new ArrayAdapter(getContext(), R.layout.item_dropdown, zone);
        autoCompleteTextView.setText(arrayAdapter.getItem(0).toString(), false);
        //autoCompleteTextView.setAdapter(arrayAdapter);

        getInputs(root);
        setCalendar();

        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Seleccione Zona");
                builder.setCancelable(false);

                builder.setSingleChoiceItems(arrayAdapter, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        autoCompleteTextView.setText(arrayAdapter.getItem(which).toString());
                        dialog.dismiss();
                    }
                }).setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    uploadPhoto();
                }
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(CreateEventFragment.this)
                        .crop()
                        .start();
            };
        });

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(R.mipmap.no_image_avaible);
                realPath = "";
                addImage.setVisibility(View.VISIBLE);
                deleteImage.setVisibility(View.GONE);
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
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) getActivity().findViewById(R.id.layout_name);
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

    public void getInputs(View root){
        start = (EditText) root.findViewById(R.id.event_start);
        finish = (EditText) root.findViewById(R.id.event_finish);
        hour_start = (EditText) root.findViewById(R.id.evet_hourstart);
        hour_finish = (EditText) root.findViewById(R.id.event_hourfinish);
        create = (Button) root.findViewById(R.id.createevent);
        title = (EditText) root.findViewById(R.id.event_title);
        addImage = (Button) root.findViewById(R.id.add_image_button);
        deleteImage = (Button) root.findViewById(R.id.delete_image_button);
        imageView = (ImageView) root.findViewById(R.id.image_event);
        description = (EditText) root.findViewById(R.id.event_description);
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

    public void getZones(){
        int size = countZonePermanent();
        zone = new String[size];
        int auxCount = 0;
        for (int i=0; i < zones.size(); i++){
            if(zones.get(i).getPermanent()) {
                zone[auxCount] = zones.get(i).getName();
                auxCount++;
            }
        }
    }

    private int countZonePermanent(){
        int count = 0;
        for (int i=0; i < zones.size(); i++){
            if(zones.get(i).getPermanent()){
                count++;
            }
        }
        return count;
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
        },hour+12, minutes, true);
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

    public boolean validate(){
        boolean validate = false;
        //create Validations
        if(title.getText().toString().trim().equals("")){
            validator = (TextInputLayout) getActivity().findViewById(R.id.layout_name);
            validator.setError(errorField);
            showToast("titulo");
        }
        else if(description.getText().toString().trim().equals("")){
            validator = (TextInputLayout) getActivity().findViewById(R.id.layout_description);
            validator.setError(errorField);
            showToast("descripcion");
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
            showToast("fecha termino");
        }
        else if(hour_finish.getText().toString().equals("")){
            validator = (TextInputLayout) getActivity().findViewById(R.id.layout_hourfinish);
            validator.setError(errorField);
            showToast("hora termino");
        }
        else if(checkDateCoherence()){
            showDateError();
        }
        else{
            validate = true;
        }
        return validate;
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

    //Debería ser por geopuntos, no por nombre (CAMBIAAAR)
    public String getIdSector(String n){
        String id = "";
        for (int i=0; i < zones.size(); i++){
            if(zones.get(i).getName().equals(n)){
                id = zones.get(i).getId();
                i = zones.size();
            }
        }
        return id;
    }

    public void createEvent(String photo_data){
        String idUser = mAuth.getCurrentUser().getUid();
        String idZone = getIdSector(autoCompleteTextView.getText().toString());

        Map<String, Object> event= new HashMap<>();
        event.put("creador", idUser);
        event.put("sector", idZone);
        event.put("descripcion", description.getText().toString());
        event.put("nombre", title.getText().toString());
        event.put("fecha inicio", startDate);
        event.put("fecha termino", endDate);
        event.put("foto", photo_data);

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

    public void uploadPhoto(){
        if(!realPath.equals("")){
            message.setMessage("Subiendo foto ...");
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
        }
        else{
            createEvent("sin imagen");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            imageView.setImageURI(uri);
            realPath = ImageFilePath.getPath(getContext(), data.getData());
            deleteImage.setVisibility(View.VISIBLE);
            addImage.setVisibility(View.GONE);
        }
    }

    public void setValues(){
        start.setText("");
        finish.setText("");
        hour_start.setText("");
        hour_finish.setText("");
        title.setText("");
        description.setText("");
        imageView.setImageResource(R.mipmap.no_image_avaible);
        realPath = "";
        autoCompleteTextView.setText(arrayAdapter.getItem(0).toString(), false);
        message.dismiss();
    }

    public void notifyUsers(String nameEvent){
        Log.println(Log.ASSERT, "notificacion", "notificamos a crear");
        RequestQueue myrequest = Volley.newRequestQueue(getContext());
        JSONObject json = new JSONObject();
        JSONObject notification = new JSONObject();
        try{
            Log.println(Log.ASSERT, "notificacion", "creando noti");
            notification.put("titulo", "Hey! Hay un nuevo evento :)");
            notification.put("detalle", "Se agregó el evento "+nameEvent);
            json.put("data", notification);
            json.put("to", "/topics/Eventos");
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
            Log.println(Log.ASSERT, "notificacion", "nos caimos "+e.toString());
            e.printStackTrace();
        }
    }
}