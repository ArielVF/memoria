package com.example.tesis.ui.myEvents;

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

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;
import com.example.tesis.Model.Event;
import com.example.tesis.Model.ImageFilePath;
import com.example.tesis.R;
import com.example.tesis.ui.createEvent.CreateEventFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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

public class MyEventInfoFragment extends Fragment {
    private TextView title, description, startDate, endDate, startHour, endHour;
    private AutoCompleteTextView zoneName;
    private String id, auxTitle, auxDescription, auxZonename, auxStartDate, auxEndDate, auxHourStart, auxHourEnd, url;
    private Button edit, delete, change_image, delete_image;
    private int year, day, month, hour, minutes;
    private Calendar calendar;
    private ImageView my_image_event;
    private FirebaseFirestore db;
    private ArrayAdapter arrayAdapter;
    private String zonesName[];
    private String realPath = "";
    private StorageReference storageReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getZones();
        startCalendar();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("Foto Eventos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_event_info, container, false);
        // Inflate the layout for this fragment
        this.title = root.findViewById(R.id.event_edit_title);
        this.description = root.findViewById(R.id.event_edit_description);
        this.zoneName = root.findViewById(R.id.event_edit_zone);
        this.startDate = root.findViewById(R.id.event_edit_start);
        this.startHour = root.findViewById(R.id.evet_edit_hourstart);
        this.endDate = root.findViewById(R.id.event_edit_finish);
        this.endHour = root.findViewById(R.id.event_edit_hourfinish);
        this.my_image_event = root.findViewById(R.id.image_my_event);
        this.delete_image = root.findViewById(R.id.delete_image);

        this.edit = root.findViewById(R.id.edit_event_button);
        this.delete = root.findViewById(R.id.delete_event_button);
        this.change_image = root.findViewById(R.id.change_image);

        Bundle eventObject = getArguments();
        if(eventObject != null){
            Event event = (Event) eventObject.getSerializable("event");
            setViewfields(event);
            saveinfo();
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editEvent(id);
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
                ImagePicker.with(MyEventInfoFragment.this)
                        .crop()
                        .start();
            }
        });

        delete_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_image.setVisibility(View.VISIBLE);
                delete_image.setVisibility(View.GONE);
                realPath = "sin imagen";
                my_image_event.setImageResource(R.mipmap.no_image_avaible);
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
        zoneName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Seleccione Zona");
                builder.setCancelable(false);

                builder.setSingleChoiceItems(arrayAdapter, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        zoneName.setText(arrayAdapter.getItem(which).toString());
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
        return root;
    }

    public void startCalendar(){
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR);
        minutes = calendar.get(Calendar.MINUTE);
    }

    public void setViewfields(Event event) {
        id = event.getId();
        title.setText(event.getTitle());
        description.setText(event.getDescription());
        int indexItem = getItemindex(event.getZone());
        arrayAdapter = new ArrayAdapter(getContext(), R.layout.item_dropdown, zonesName);
        zoneName.setText(arrayAdapter.getItem(indexItem).toString(), false);
        startDate.setText(event.getDateStart());
        startHour.setText(event.getHourStart());
        endDate.setText(event.getDateEnd());
        endHour.setText(event.getHourEnd());
        url = event.getUrl();
        if(!url.equals("sin imagen")){
            change_image.setVisibility(View.GONE);
            delete_image.setVisibility(View.VISIBLE);
            Picasso.get().load(url).into(my_image_event);
        }
    }

    public int getItemindex(String nameOfzone){
        int indexItem = 0;
        for(int i=0; i < zonesName.length; i++){
            if(nameOfzone.equals(zonesName[i])){
                indexItem = i;
                i = zonesName.length;
            }
        }
        return indexItem;
    }

    public void getZones(){
        zonesName = new String[zones.size()];
        for (int i=0; i < zones.size(); i++){
            zonesName[i] = zones.get(i).getName();
        }
    }

    public void saveinfo(){
        auxTitle = title.getText().toString();
        auxDescription = description.getText().toString();
        auxZonename = zoneName.getText().toString();
        auxStartDate = startDate.getText().toString();
        auxHourStart = startHour.getText().toString();
        auxEndDate = endDate.getText().toString();
        auxHourEnd = endHour.getText().toString();
        realPath = "";
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

    public void editEvent(String id){
        if(auxTitle.equals(title.getText().toString()) && auxDescription.equals(description.getText().toString())
        && auxZonename.equals(zoneName.getText().toString()) && auxStartDate.equals(startDate.getText().toString()) &&
                auxEndDate.equals(endDate.getText().toString()) && auxHourStart.equals(startHour.getText().toString()) &&
                auxHourEnd.equals(endHour.getText().toString()) && realPath.equals("")){
            Toast.makeText(getContext(), "No existen cambios para actualizar", Toast.LENGTH_LONG).show();
        }
        else if(title.getText().toString().trim().equals("") || description.getText().toString().trim().equals("")){
            Toast.makeText(getContext(), "Complete todos los campos", Toast.LENGTH_LONG).show();
        }
        else if(checkIsPastEvent()){
            Toast.makeText(getContext(), "No se puede modificar un evento activo o caducado.", Toast.LENGTH_LONG).show();
        }
        else if(validateNewDate()){
            showDateError();
        }
        else{
            if(realPath.equals("")){
                updateEvent();
            }
            else{
                deletePastPhoto();
            }
        }
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

    public void updateEvent(){
        String tempId = getIdZone(zoneName.getText().toString());
        Timestamp dateConvertStart = convertTimeStamp(startDate.getText().toString()+" "+startHour.getText().toString());
        Timestamp dateConvertEnd =  convertTimeStamp(endDate.getText().toString()+" "+endHour.getText().toString());
        db.collection("Eventos").document(id)
                .update("nombre", title.getText().toString().trim(), "descripcion", description.getText().toString().trim(),
                        "sector", tempId, "fecha inicio", dateConvertStart,
                        "fecha termino", dateConvertEnd, "foto", url)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
                        saveinfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
                    }
                });
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
                        updateEvent();
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

    public void updateURL(String newURL){
        this.url = newURL;
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
            Date startDate = formatter.parse(convertDate(auxStartDate, auxHourStart));
            if(startDate.compareTo(currentDate) <= 0){
                past = true;
            }
        }catch (Exception e){}
        return past;
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
                deleteEvent();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
            }
        });
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
                deleteEvent();
            }
        }
        else{
            Toast.makeText(getContext(), "El evento se encuentra activo, no puede ser eliminado.", Toast.LENGTH_LONG).show();
        }
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
            Date endDate = formatter.parse(convertDate(auxEndDate, auxHourEnd));
            Date startDate = formatter.parse(convertDate(auxStartDate,auxHourStart));
            if(startDate.compareTo(now) < 0 && endDate.compareTo(now) > 0){
                isNoActive = false;
            }
        }
        catch (Exception e){}
        return isNoActive;
    }

    public String getIdZone(String tempName){
        for (int i=0; i < zones.size(); i++){
            if(zones.get(i).getName().equals(tempName)){
                tempName = zones.get(i).getId();
                i = zones.size();
            }
        }
        return tempName;
    }

    public String convertDate(String dateC, String hourC){
        return dateC+" "+hourC;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            my_image_event.setImageURI(uri);
            realPath = ImageFilePath.getPath(getContext(), data.getData());
            delete_image.setVisibility(View.VISIBLE);
            change_image.setVisibility(View.GONE);
        }
    }
}