package com.example.tesis.ui.perfil;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.tesis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private TextInputEditText name, lastname;
    private Button editUser;
    private FirebaseFirestore db;
    private FirebaseAuth mauth;
    private String currentName, currentLastname;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mauth = FirebaseAuth.getInstance();
        getCurrentUser();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
        name = root.findViewById(R.id.edit_user_name);
        lastname = root.findViewById(R.id.edit_lastname_user);
        editUser = root.findViewById(R.id.edit_user_data);
        editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    editCurrentUserData();
                }
                else{
                    Toast.makeText(getContext(), "Existen problemas con el registro.\nCausas:\n1.- Campos vacíos\n2.- No existen cambios para actualizar", Toast.LENGTH_LONG).show();
                }
            }
        });
        return root;
    }

    public boolean validate(){
        return (!name.getText().toString().trim().equals("") && !lastname.getText().toString().trim().equals(""))
                && (!name.getText().toString().equals(currentName) || !lastname.getText().toString().equals(currentLastname));
    }

    public void editCurrentUserData(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Actualizando datos");
        progressDialog.setCancelable(false);
        progressDialog.show();
        String newName = name.getText().toString();
        String newLastName = lastname.getText().toString();
        db.collection("Usuarios")
                .document(mauth.getUid())
                .update("nombre", newName, "apellido", newLastName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setValues();
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Cambios aplicados satisfactoriamente.", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Surgió un problema durante la actualización. Intente nuevamente.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setValues(){
        currentName = name.getText().toString();
        currentLastname = lastname.getText().toString();
    }

    public void getCurrentUser() {
        db.collection("Usuarios")
                .document(mauth.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        currentName = snapshot.getString("nombre");
                        currentLastname = snapshot.getString("apellido");
                        setEditTextInput();
                    }
                });
    }

    public void setEditTextInput(){
        name.setText(currentName);
        lastname.setText(currentLastname);
    }
}