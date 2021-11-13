package com.example.tesis.ui.ZoneAdmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tesis.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.tesis.ui.authActivity.subZones;


public class addSubZoneToZoneFgrament extends Fragment {
    private ImageButton info_subZone;
    private Button add_subZone;
    private TextView no_SubZone;
    private AutoCompleteTextView autoCompleteTextView;
    private FirebaseFirestore db;
    private String idSubZone, idZone;
    private int selectFloor;
    private List<String> idsSubZone;
    private LinearLayout linearLayout;
    private TableLayout tableLayout;
    private boolean isTableVisible = false;
    private String[] nameSubZone;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.idsSubZone = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();

        Bundle dataObject = getArguments();
        if(dataObject != null) {
            selectFloor = (Integer) dataObject.getSerializable("indexFloor");
            nameSubZone = (String[]) dataObject.getSerializable("nameSubZone");
            idZone = (String) dataObject.getSerializable("idZone");
            getSubZonesBelongBuild();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_sub_zone_to_zone_fgrament, container, false);
        this.info_subZone = root.findViewById(R.id.info_addSubZone_toZone);
        this.add_subZone = root.findViewById(R.id.add_subzone);
        autoCompleteTextView = root.findViewById(R.id.text_add_subZone);
        this.no_SubZone = root.findViewById(R.id.no_subzones);
        this.linearLayout = root.findViewById(R.id.information_table);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getContext(), R.layout.item_dropdown, nameSubZone);
        autoCompleteTextView.setAdapter(arrayAdapter);

        info_subZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInformation();
            }
        });

        add_subZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    addNewSubZone();
                }
                else{
                    Toast.makeText(getContext(), "Existen problemas con el registro.\n Causas: \n1.- Campo vacío.\n2.- SubZona no existe.", Toast.LENGTH_LONG).show();
                }
            }
        });
        return root;
    }

    public void showInformation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog_Alert);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_information_subzonetozone_alert, (ConstraintLayout) getActivity().findViewById(R.id.layout_container_dialog_info));

        ((TextView) view.findViewById(R.id.title_dialog_info)).setText("¿Cómo vincular una subzona?");
        ((Button) view.findViewById(R.id.accept_dialog_info)).setText("Aceptar");
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        view.findViewById(R.id.accept_dialog_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    //Validamos los campos
    public boolean validate() {
        boolean isValidate = false;
        boolean existSubZone = checkSubZoneExist();
        if (!autoCompleteTextView.getText().toString().equals("") && existSubZone) {
            isValidate = true;
        }
        return isValidate;
    }

    //Validamos que la subZona que escribió el usuario exista en la base de datos.
    public boolean checkSubZoneExist(){
        boolean nameExist = false;
        String correctName;
        for(int i=0; i < subZones.size(); i++){
            correctName = subZones.get(i).getName().toLowerCase();
            if(correctName.equals(autoCompleteTextView.getText().toString().trim().toLowerCase())){
                nameExist = true;
                idSubZone = subZones.get(i).getId();
                i = subZones.size();
            }
        }
        return nameExist;
    }

    public void addNewSubZone(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Agregando ...");
        progressDialog.show();

        Map<String, Object> newSubZone= new HashMap<>();
        newSubZone.put("SubZona", idSubZone);
        newSubZone.put("Zona", idZone);
        newSubZone.put("piso", (selectFloor+1));
        db.collection("Pertenece a")
                .add(newSubZone)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(), "Se agregó la subZona Satisfactoriamente", Toast.LENGTH_LONG).show();
                        updateTableAndParameters();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Algo salió mal: "+e, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
    }

    public void updateTableAndParameters(){
        if(isTableVisible){
            addRow(autoCompleteTextView.getText().toString());
        }
        else{
            tableLayout = createTable();
            addRow(autoCompleteTextView.getText().toString());
            this.linearLayout.addView(tableLayout);
        }
        setValues();
    }

    public void addRow(String nameSubZone){
            View v = new View(getContext());
            v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
            v.setBackgroundColor(Color.rgb(51, 51, 51));

            TableRow row = new TableRow(getContext());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.setGravity(Gravity.CENTER);

            TextView textView = new TextView(getContext());

            TableRow.LayoutParams layoutParams;
            layoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.6f);
            layoutParams.weight = 1;
            textView.setLayoutParams(layoutParams);

            textView.setBackgroundResource(R.color.gray);
            textView.setText(nameSubZone);
            textView.setTextSize(15);
            row.addView(textView);
            tableLayout.addView(row);
            tableLayout.addView(v);
    }

    public void setValues(){
        autoCompleteTextView.setText("");
    }

    public void getSubZonesBelongBuild(){
        db.collection("Pertenece a")
                .whereEqualTo("Zona", idZone)
                .whereEqualTo("piso", (selectFloor+1))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot document: task.getResult()){
                            idsSubZone.add(document.getString("SubZona"));
                        }
                        getNameSubZone();
                    }
                });
    }

    public void getNameSubZone(){
        if(idsSubZone.isEmpty()){
            no_SubZone.setVisibility(View.VISIBLE);
        }
        else{
            no_SubZone.setVisibility(View.GONE);
            tableLayout = createTable();
            for(int i=0; i< idsSubZone.size(); i++){
                for(int j=0; j < subZones.size(); j++){
                    if(subZones.get(j).getId().equals(idsSubZone.get(i))){
                        addRow(subZones.get(j).getName());
                        j = subZones.size();
                    }
                }
            }
            this.linearLayout.addView(tableLayout);
        }
    }

    public TableLayout createTable(){
        no_SubZone.setVisibility(View.GONE);
        TableLayout table = new TableLayout(getContext());
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 20, 0, 15);
        table.setLayoutParams(params);

        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.CENTER);

        TextView textView = new TextView(getContext());

        TableRow.LayoutParams layoutParams;
        layoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.6f);
        layoutParams.weight = 1;
        textView.setLayoutParams(layoutParams);

        textView.setBackgroundResource(R.color.third);
        textView.setText("Contenido del edificio en piso "+(selectFloor+1));
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setTextSize(15);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(textView);
        table.addView(row);

        isTableVisible = true;
        return table;
    }
}