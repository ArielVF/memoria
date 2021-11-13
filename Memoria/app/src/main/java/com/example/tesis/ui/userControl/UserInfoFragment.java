package com.example.tesis.ui.userControl;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tesis.Model.User;
import com.example.tesis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserInfoFragment extends Fragment {
    private EditText name, lastname, rut, role, email;
    private int positionRole;
    private Button edit, disable, enable;
    private String auxRole, roleSelection, auxName, auxLastName, id;
    private FirebaseFirestore db;
    private String[] roles = {"Estudiante", "Creador"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_user_info, container, false);
        this.name = root.findViewById(R.id.info_name);
        this.lastname = root.findViewById(R.id.info_lastname);
        this.email = root.findViewById(R.id.info_email);
        this.rut = root.findViewById(R.id.info_rut);
        this.role = root.findViewById(R.id.info_role);

        this.enable = root.findViewById(R.id.active_user);
        this.edit = root.findViewById(R.id.edit_user);
        this.disable = root.findViewById(R.id.delete_user);

        this.db = FirebaseFirestore.getInstance();

        Bundle userObject = getArguments();
        if(userObject != null){
            User user = (User) userObject.getSerializable("user");

            if(user.isEnable()){
                disable.setVisibility(View.VISIBLE);
            }
            else{
                enable.setVisibility(View.VISIBLE);
            }

            id = user.getId();
            name.setText(user.getName());
            lastname.setText(user.getLastname());
            email.setText(user.getEmail());
            rut.setText(user.formatRut());
            role.setText(user.getRol());

            auxName = this.name.getText().toString();
            auxLastName = this.lastname.getText().toString();
            auxRole = this.role.getText().toString();

            roleSelection = auxRole;
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserInfo();
            }
        });

        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDisableDialog();
            }
        });

        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEnableDialog();
            }
        });

        role.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Seleccione rol");
                builder.setCancelable(false);
                int element = getIndexElement();

                builder.setSingleChoiceItems(roles, element, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        positionRole = which;
                    }
                }).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        role.setText(roles[positionRole].toLowerCase());
                        roleSelection = roles[positionRole];
                        dialog.dismiss();
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
        });
        return root;
    }

    public int getIndexElement(){
        int index = 0;
        for (int i=0; i < roles.length; i++){
            if(roleSelection.toLowerCase().equals(roles[i].toLowerCase())){
                index = i;
                i = roles.length;
            }
        }
        return index;
    }

    public void setUserInfo(){
        String actualName = this.name.getText().toString();
        String actualLastname = this.lastname.getText().toString();
        String actualRole = this.role.getText().toString();

        if(auxName.equals(actualName) && auxLastName.equals(actualLastname) && auxRole.equals(actualRole)){
            Toast.makeText(getContext(), "No existen cambios para actualizar", Toast.LENGTH_LONG).show();
        }
        else{
            if(validateFields(actualName, actualLastname)){
                ProgressDialog dialog = new ProgressDialog(getContext());
                dialog.setTitle("Actualizando usuario ...");
                dialog.setCancelable(false);
                dialog.show();
                db.collection("Usuarios").document(id)
                        .update("nombre", actualName, "apellido", actualLastname, "rol", actualRole)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
                            }
                        });
                dialog.dismiss();
                auxName = actualName;
                auxLastName = actualLastname;
                auxRole = actualRole;
            }
        }
    }

    private boolean validateFields(String n, String ln) {
        boolean itsOk = false;
        if(n.trim().equals("")){
            Toast.makeText(getContext(), "El campo nombre no puede estar vacío", Toast.LENGTH_LONG).show();
        }
        else if(ln.trim().equals("")){
            Toast.makeText(getContext(), "El campo apellido no puede estar vacío", Toast.LENGTH_LONG).show();
        }
        else{
            itsOk = true;
        }
        return itsOk;
    }

    public void showDisableDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog_Alert);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_delete, (ConstraintLayout) getActivity().findViewById(R.id.layout_container_dialog));

        ((TextView) view.findViewById(R.id.title_dialog_delete)).setText("Deshabilitar Usuario");
        ((TextView) view.findViewById(R.id.text_dialog_delete)).setText("Con esta acción el usuario "+auxName+" no podrá acceder a la aplicación.\n¿Seguro que desea continuar?");
        ((Button) view.findViewById(R.id.cancel_dialog_delete)).setText("Cancelar");
        ((Button) view.findViewById(R.id.acept_dialog_delete)).setText("Aceptar");

        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.cancel_dialog_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
                alertDialog.dismiss();
            }
        });

        view.findViewById(R.id.acept_dialog_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableOrdisableUser(false, "deshabilitado");
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void showEnableDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog_Alert);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_accept, (ConstraintLayout) getActivity().findViewById(R.id.layout_container_dialog_accept));

        ((TextView) view.findViewById(R.id.title_dialog_accept)).setText("Habilitar Usuario");
        ((TextView) view.findViewById(R.id.text_dialog_accept)).setText("Con esta acción el usuario "+auxName+" podrá acceder a la aplicación.\n¿Seguro que desea continuar?");
        ((Button) view.findViewById(R.id.cancel_dialog_accept)).setText("Cancelar");
        ((Button) view.findViewById(R.id.accept_dialog_accept)).setText("Aceptar");

        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.cancel_dialog_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
                alertDialog.dismiss();
            }
        });

        view.findViewById(R.id.accept_dialog_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableOrdisableUser(true, "habilitado");
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void enableOrdisableUser(boolean option, String value){
        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setTitle("Aplicando cambios ...");
        dialog.setCancelable(false);
        dialog.show();
        db.collection("Usuarios").document(id)
                .update("habilitado", option)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        getActivity().onBackPressed();
                        Toast.makeText(getContext(), "Usuario "+value, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Ocurrió un error, por favor intente nuevamente", Toast.LENGTH_LONG).show();
                    }
                });
    }
}