package com.example.tesis.GuestSesion;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.tesis.GuestSesion.LoginActivity;
import com.example.tesis.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText name, lastname, rut, rutdv, email, pass, verifyPass;
    private TextInputLayout validator;
    private ProgressDialog message;
    private FirebaseFirestore db;
    private String role;
    private ImageButton back_button;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.editName);
        lastname = findViewById(R.id.editApellido);
        email = findViewById(R.id.editCorreo);
        rut = findViewById(R.id.editRut);
        rutdv = findViewById(R.id.editDV);
        pass = findViewById(R.id.editPassword);
        verifyPass = findViewById(R.id.verifyPassword);
        role = "";

        back_button = findViewById(R.id.back_button);

        message = new ProgressDialog(this);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) findViewById(R.id.layout_name);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lastname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) findViewById(R.id.layout_apellido);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) findViewById(R.id.layout_correo);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        rut.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) findViewById(R.id.layout_rut);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        rutdv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) findViewById(R.id.layout_dv);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) findViewById(R.id.layout_pass);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        verifyPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) findViewById(R.id.layout_verifypass);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
    }
        
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    public boolean checkFields(){
        if(name.getText().toString().trim().equals("")){
            validator = (TextInputLayout) findViewById(R.id.layout_name);
            validator.setError("Este campo es requerido");
            return false;
        }
        if(lastname.getText().toString().trim().equals("")){
            validator = (TextInputLayout) findViewById(R.id.layout_apellido);
            validator.setError("Este campo es requerido");
            return false;
        }
        if(rut.getText().toString().trim().equals("")){
            validator = (TextInputLayout) findViewById(R.id.layout_rut);
            validator.setError("Este campo es requerido");
            return false;
        }
        else{
            if(rutdv.getText().toString().trim().equals("")){
                validator = (TextInputLayout) findViewById(R.id.layout_dv);
                validator.setError("completar");
                return false;
            }
        }
        if(email.getText().toString().trim().equals("")){
            validator = (TextInputLayout) findViewById(R.id.layout_correo);
            validator.setError("Este campo es requerido");
            return false;
        }
        if(pass.getText().toString().trim().equals("")){
            validator = (TextInputLayout) findViewById(R.id.layout_pass);
            validator.setError("Este campo es requerido");
            return false;
        }
        else if(pass.getText().length() < 8){
            validator = (TextInputLayout) findViewById(R.id.layout_pass);
            validator.setError("La contraseña debe contener al menos 8 caracteres");
            return false;
        }
        else{
            if(verifyPass.getText().toString().trim().equals("")){
                validator = (TextInputLayout) findViewById(R.id.layout_verifypass);
                validator.setError("Este campo es requerido");
                return false;
            }
            else if(!(pass.getText().toString().equals(verifyPass.getText().toString()))) {
                validator = (TextInputLayout) findViewById(R.id.layout_verifypass);
                validator.setError("Las contraseñas no coinciden");
                return false;
            }
        }
        return true;
    }

    public void addUser(View view){
        if(checkFields()) {
            String finalRut;
            finalRut = rut.getText().toString() + "-" + rutdv.getText().toString();

            if(checkRut(finalRut.trim())){
                if(checkEmail(email.getText().toString().trim())){
                    message.setMessage("Creando registro ...");
                    message.setCanceledOnTouchOutside(false);
                    message.show();

                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("nombre", name.getText().toString());
                                        user.put("apellido", lastname.getText().toString());
                                        user.put("rut", finalRut);
                                        user.put("correo", email.getText().toString());
                                        user.put("rol", role);
                                        user.put("habilitado", true);

                                        db.collection("Usuarios").document(mAuth.getCurrentUser().getUid())
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                                        currentUser.sendEmailVerification()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            setFields();
                                                                            message.dismiss();
                                                                        }
                                                                        AlertDialog.Builder alert = new AlertDialog.Builder(RegisterActivity.this);
                                                                        alert.setTitle("Información")
                                                                                .setMessage("¡Usuario registrado correctamente!\nHemos enviado un enlace de confirmación a su correo.")
                                                                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        dialog.dismiss();
                                                                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                                                                    }
                                                                                }).setCancelable(false);
                                                                        alert.show();
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getApplicationContext(), "Algo paso: "+e, Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }
                                    else {
                                        if(task.getException().toString().contains("is already in use")){
                                            Toast.makeText(getApplicationContext(), "El correo ingresado ya tiene una cuenta registrada", Toast.LENGTH_LONG).show();
                                            validator = (TextInputLayout) findViewById(R.id.layout_correo);
                                            validator.setError("Correo ya registrado");
                                        }
                                        else if(task.getException().toString().contains("badly formatted")){
                                            validator = (TextInputLayout) findViewById(R.id.layout_correo);
                                            validator.setError("El correo no tiene un formato correcto");
                                        }
                                    }
                                    message.dismiss();
                                }
                            });
                }
            }
            else{
                Toast.makeText(getApplicationContext(), "Hay problemas con el registro, favor revisar los campos", Toast.LENGTH_LONG).show();
                validator = (TextInputLayout) findViewById(R.id.layout_rut);
                validator.setError("Ingrese un rut válido");
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Hay problemas con el registro, favor revisar los campos", Toast.LENGTH_LONG).show();
        }
    }

    public boolean checkRut(String rut) {
        boolean validate = false;
        try {
            rut = rut.toUpperCase();
            rut = rut.replace(" ", "");

            String rutAndDv[] = rut.split("-");
            String auxRut = rutAndDv[0];
            String dv = rutAndDv[1];
            int constant = 2;
            int acumulate = 0;
            int digit;

            Log.println(Log.ASSERT, "PRUEBAS RUT : ", auxRut+" "+dv);
            Log.println(Log.ASSERT, "PRUEBAS RUT : ", String.valueOf(auxRut.length()));

            for (int i = auxRut.length()-1; i >= 0; i--) {
                digit = Integer.parseInt(String.valueOf(auxRut.charAt(i)));
                Log.println(Log.ASSERT, "PRUEBAS RUT digit * i: ", String.valueOf(digit)+" "+auxRut.charAt(i));

                acumulate += digit * constant;
                constant++;

                Log.println(Log.ASSERT, "PRUEBAS RUT Acumulate: ", String.valueOf(acumulate));

                if (constant == 8) {
                    constant = 2;
                }
            }

            int auxDv = (acumulate % 11);
            auxDv = 11-auxDv;

            Log.println(Log.ASSERT, "PRUEBAS RUT auxDV: ", String.valueOf(auxDv));

            if (auxDv > 9) {
                if (auxDv == 10 && dv.equals("K")) {
                    validate = true;
                } else if (auxDv == 11 && dv.equals("0")) {
                    validate = true;
                }
            }
            else{
                if(dv.equals(String.valueOf(auxDv))){
                    validate = true;
                }
            }
        } catch (Exception e) {
            Log.println(Log.ASSERT, "ESTOY ACA", "HOLA: "+e);
            return false;
        }
        return validate;
    }

    public boolean checkEmail(String mail){
        boolean validate = false;
        Pattern pattern = Pattern
                .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

        Matcher mather = pattern.matcher(mail);
        if(mather.find() == true){
            String auxMail[] = mail.split("@");
            String extensionMail = auxMail[1];
            if(extensionMail.equals("alumnos.utalca.cl")){
                role = "estudiante";
                validate = true;
            }
            else if(extensionMail.equals("utalca.cl")){
                role = "creador";
                validate = true;
            }
            else{
                validator = (TextInputLayout) findViewById(R.id.layout_correo);
                validator.setError("Debe ser correo institucional");
            }
        }
        else{
            validator = (TextInputLayout) findViewById(R.id.layout_correo);
            validator.setError("Formato incorrecto");
        }
        return validate;
    }

    public void setFields(){
        name.setText("");
        lastname.setText("");
        email.setText("");
        rut.setText("");
        rutdv.setText("");
        pass.setText("");
        verifyPass.setText("");
    }
}