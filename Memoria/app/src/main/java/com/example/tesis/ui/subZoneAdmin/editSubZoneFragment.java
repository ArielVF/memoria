package com.example.tesis.ui.subZoneAdmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.tesis.Model.SubZone;
import com.example.tesis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class editSubZoneFragment extends Fragment {
    private String name, id;
    private TextInputEditText edit_name;
    private Button edit, delete;
    private FirebaseFirestore db;
    private ProgressDialog progressDialogDelete;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        progressDialogDelete = new ProgressDialog(getContext());
        progressDialogDelete.setTitle("Eliminando subzona");
        progressDialogDelete.setCancelable(false);

        Bundle subZoneObject = getArguments();
        if(subZoneObject != null){
            SubZone subZone = (SubZone) subZoneObject.getSerializable("subZone");
            id = subZone.getId();
            name = subZone.getName();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_edit_sub_zone, container, false);
        edit = root.findViewById(R.id.edit_new_SubZone);
        delete = root.findViewById(R.id.delete_new_subzone);
        edit_name = root.findViewById(R.id.newsubzone_edit_name);

        edit_name.setText(name);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyEdit();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage();
            }
        });
        return root;
    }

    public void applyEdit(){
        if(validate()){
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Actualizando datos");
            progressDialog.setCancelable(false);
            progressDialog.show();

            String newName = this.edit_name.getText().toString().trim();
            db.collection("SubZona").document(id)
                    .update("nombre", newName)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            setValue();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });
        }
        else{
            Toast.makeText(getContext(), "Existen problemas para actualizar el registro: \n1- El campo está vació.\n2- No existen cambios en el nombre.", Toast.LENGTH_LONG).show();
        }
    }

    public void showMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Eliminar subzona");
        builder.setMessage(R.string.delete_subzone_info);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialogDelete.show();
                deleteZoneLink();
            }
        }).setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public boolean validate(){
        return !this.edit_name.getText().toString().trim().equals(name) && !this.edit_name.getText().toString().trim().equals("");
    }

    public void setValue(){
        name = this.edit_name.getText().toString();
    }

    public void applyDelete(){
        db.collection("SubZona").document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Elemento eliminado", Toast.LENGTH_LONG).show();
                        progressDialogDelete.dismiss();
                        getActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialogDelete.dismiss();
                        Toast.makeText(getContext(), "Ocurrió un problema, vuelva a intentarlo", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void deleteZoneLink(){
        db.collection("Pertenece a").whereEqualTo("SubZona", id)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                WriteBatch writeBatch = db.batch();
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot: snapshotList){
                    writeBatch.delete(snapshot.getReference());
                }
                writeBatch.commit()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                applyDelete();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialogDelete.dismiss();
                        Toast.makeText(getContext(), "Ocurrió un problema, vuelva a intentarlo", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialogDelete.dismiss();
                Toast.makeText(getContext(), "Ocurrió un problema, vuelva a intentarlo", Toast.LENGTH_LONG).show();
            }
        });
    }
}