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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.tesis.ui.authActivity.subZones;

public class deleteSubZoneInZoneFragment extends Fragment {
    private int selectFloor;
    private String[] nameSubZone;
    private String idZone;
    private FirebaseFirestore db;
    private List<String> idsSubZone;
    private TableLayout tableLayout;
    private AutoCompleteTextView autoCompleteTextView;
    private Button delete;
    private TextView nosubzonecontent;
    private LinearLayout linearLayout;
    private ProgressDialog progressDialog;
    private ImageButton delete_info;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        idsSubZone = new ArrayList<>();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Borrando subzona de este piso...");
        progressDialog.setCancelable(false);
        Bundle dataObject = getArguments();
        if(dataObject != null) {
            selectFloor = (Integer) dataObject.getSerializable("indexFloor");
            nameSubZone = (String[]) dataObject.getSerializable("nameSubZone");
            idZone = (String) dataObject.getSerializable("idZone");
            getSubZonesBelongBuild();
        }
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
            nosubzonecontent.setVisibility(View.VISIBLE);
        }
        else{
            nosubzonecontent.setVisibility(View.GONE);
            tableLayout = createTable();
            for(int i=0; i< idsSubZone.size(); i++){
                for(int j=0; j < subZones.size(); j++){
                    if(subZones.get(j).getId().equals(idsSubZone.get(i))){
                        addRow(subZones.get(j).getName(), i);
                        j = subZones.size();
                    }
                }
            }
            this.linearLayout.addView(tableLayout);
        }
    }

    public void addRow(String nameSubZone, int index) {
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
            tableLayout.addView(row, (index+1));
            tableLayout.addView(v);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_delete_sub_zone_in_zone, container, false);
        delete = root.findViewById(R.id.delete_link_subzone);
        autoCompleteTextView = root.findViewById(R.id.text_delete_subZone);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getContext(), R.layout.item_dropdown, nameSubZone);
        autoCompleteTextView.setAdapter(arrayAdapter);
        nosubzonecontent = root.findViewById(R.id.no_subzones_link_delete);
        linearLayout = root.findViewById(R.id.information_table_delete);
        delete_info = root.findViewById(R.id.info_deleteSubZone_toZone);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idsSubZone.isEmpty()){
                    Toast.makeText(getContext(), "Actualmente este piso no tiene subzonas vinculadas.", Toast.LENGTH_LONG).show();
                }
                else{
                    checkIsEmpty();
                }
            }
        });

        delete_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogInformation();
            }
        });
        return root;
    }

    public void showDialogInformation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog_Alert);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_information_subzonetozone_delete, (ConstraintLayout) getActivity().findViewById(R.id.layout_container_dialog_info));

        ((TextView) view.findViewById(R.id.title_dialog_info)).setText("¿Cómo desvincular una subzona?");
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

    public void checkIsEmpty(){
        if(validate()){
            getSubzoneId();
        }
        else{
            Toast.makeText(getContext(), "El campo eliminar subzona es requerido.", Toast.LENGTH_LONG).show();
        }
    }

    public void getSubzoneId(){
        String subzoneName = autoCompleteTextView.getText().toString().trim();
        db.collection("SubZona").whereEqualTo("nombre", subzoneName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().isEmpty()){
                    Toast.makeText(getContext(), "La subzona ingresada no existe.\nLa tabla muestra las subzonas que tiene asociadas este piso, respete mayúsculas.", Toast.LENGTH_LONG).show();
                }
                else{
                    for(QueryDocumentSnapshot document: task.getResult()){
                        String currentName = document.getString("nombre");
                        if(currentName.equals(subzoneName)){
                            checkLink(document.getId());
                        }
                    }
                }
            }
        });
    }

    public void checkLink(String id){
        boolean haveLink = false;
        int storeIndex = 0;
        for(int i=0; i < idsSubZone.size(); i++){
            if(idsSubZone.get(i).equals(id)){
                haveLink = true;
                storeIndex = i;
                i = idsSubZone.size();
            }
        }
        if(haveLink){
            progressDialog.show();
            getSubzoneLinkToDelete(id, storeIndex);
        }
        else{
            Toast.makeText(getContext(), "La subzona indicada no forma parte de este edificio.\nLa tabla muestra las subzonas que tiene asociadas este piso, respete mayúsculas.", Toast.LENGTH_LONG).show();
        }
    }

    public void getSubzoneLinkToDelete(String id, int index){
        db.collection("Pertenece a")
                .whereEqualTo("SubZona", id)
                .whereEqualTo("Zona", idZone)
                .whereEqualTo("piso", (selectFloor+1))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            deleteSubzone(task.getResult().getDocuments().get(0).getId(), index);
                        }
                        else{
                            Toast.makeText(getContext(), "Ocurrió un error durante el proceso, intente nuevamente.", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }

                    }
                });
    }

    public void deleteSubzone(String idLink, int index){
        db.collection("Pertenece a").document(idLink).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Se ha eliminado la subzona de este piso", Toast.LENGTH_LONG).show();
                    updateTableandParameters(index);
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Ocurrió un error durante el proceso, intente nuevamente.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateTableandParameters(int index){
        tableLayout.removeViewAt(index+1);
        idsSubZone.remove(index);
        autoCompleteTextView.setText("");
        checkIsTableEmpty();
    }

    public void checkIsTableEmpty(){
        if(idsSubZone.isEmpty()){
            tableLayout.removeViewAt(0);
            nosubzonecontent.setVisibility(View.VISIBLE);
        }
    }

    public boolean validate(){
        return !autoCompleteTextView.getText().toString().trim().equals("");
    }

    public TableLayout createTable(){
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
        table.addView(row, 0);
        return table;
    }
}