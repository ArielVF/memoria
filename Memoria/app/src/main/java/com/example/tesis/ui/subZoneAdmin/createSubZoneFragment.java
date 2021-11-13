package com.example.tesis.ui.subZoneAdmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tesis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.example.tesis.ui.authActivity.subZones;

public class createSubZoneFragment extends Fragment {
    private FirebaseFirestore db;
    private TextInputEditText name_subzone;
    private Button add_subzone;
    private ImageButton info_subzone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_create_sub_zone, container, false);
        this.name_subzone = root.findViewById(R.id.newsubzone_name);
        this.add_subzone =  root.findViewById(R.id.create_newSubZone);
        this.info_subzone = root.findViewById(R.id.info_addSubZone);

        this.add_subzone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    createNewSubZone();
                }
                else{
                    Toast.makeText(getContext(), "El campo nombre subzona es requerido", Toast.LENGTH_LONG).show();
                }
            }
        });

        this.info_subzone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        return root;
    }

    public boolean validate(){
        return !this.name_subzone.getText().toString().trim().equals("");
    }

    public void createNewSubZone(){
        String name = this.name_subzone.getText().toString().trim();
        if(checkNameNotExist(name.toLowerCase())){
            ProgressDialog message = new ProgressDialog(getContext());
            message.setTitle("Agregan Subzona");
            message.show();
            message.setCanceledOnTouchOutside(false);

            Map<String, Object> event= new HashMap<>();
            event.put("nombre", name);

            db.collection("SubZona")
                    .add(event)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(getContext(), "Se agregó la nueva subzona", Toast.LENGTH_LONG).show();
                            message.dismiss();
                            setValues();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Algo paso: "+e, Toast.LENGTH_LONG).show();
                            message.dismiss();
                        }
                    });
        }else{
            Toast.makeText(getContext(), "Ya existe una subzona llamada "+name+". Intente con otro nombre.", Toast.LENGTH_LONG).show();
        }
    }

    public boolean checkNameNotExist(String realName){
        boolean noExist = true;
        String currentName;
        for (int i=0; i < subZones.size(); i++){
            currentName = subZones.get(i).getName().toLowerCase();
            if(currentName.equals(realName)){
                noExist = false;
                i = subZones.size();
            }
        }
        return noExist;
    }

    public void setValues(){
        this.name_subzone.setText("");
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog_Alert);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_information_alert, (ConstraintLayout) getActivity().findViewById(R.id.layout_container_dialog_info));

        ((TextView) view.findViewById(R.id.title_dialog_info)).setText("Información");
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
}