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

import com.example.tesis.R;
import com.example.tesis.ui.authActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText pass;
    private TextInputLayout validator;
    private FirebaseAuth mAuth;
    private ProgressDialog message;
    private ImageButton back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.correosesion);
        pass = findViewById(R.id.passsesion);

        back_button = findViewById(R.id.back_button);

        message = new ProgressDialog(this);

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) findViewById(R.id.layout_correoinstitucional);
                validator.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                validator = (TextInputLayout) findViewById(R.id.layout_contrasenia);
                validator.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

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

    public void registerRoute(View view){
        Intent registerView = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerView);
    }

    public void recuperarContrasenia(View view){
        Intent resetView = new Intent(LoginActivity.this, ResetPassword.class);
        startActivity(resetView);
    }

    public void authroute(View view){
        if(validarCampos()){
            message.setMessage("Iniciando sesión ...");
            message.setCanceledOnTouchOutside(false);
            message.show();
            mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if(user.isEmailVerified()){
                                    Intent authView = new Intent(LoginActivity.this, authActivity.class);
                                    Toast.makeText(getApplicationContext(), "Sesión iniciada correctamente", Toast.LENGTH_SHORT).show();
                                    startActivity(authView);
                                    finish();
                                    MainActivity.fa.finish();
                                    message.dismiss();
                                }
                                else{
                                    //Toast.makeText(getApplicationContext(), "En espera de validación del correo", Toast.LENGTH_SHORT).show();
                                    askToUser();
                                }
                            } else {
                                if(task.getException().toString().contains("There is no user record")){
                                    Toast.makeText(getApplicationContext(), "El correo ingresado no se encuentra registrado", Toast.LENGTH_LONG).show();
                                    validator = (TextInputLayout) findViewById(R.id.layout_correoinstitucional);
                                    validator.setError("Correo no registrado");
                                }
                                else if(task.getException().toString().contains("formatted")){
                                    validator = (TextInputLayout) findViewById(R.id.layout_correoinstitucional);
                                    validator.setError("El formato del correo no es correcto");
                                }
                                else if(task.getException().toString().contains("password is invalid")){
                                    Toast.makeText(getApplicationContext(), "Hay datos que no son correctos", Toast.LENGTH_LONG).show();
                                    validator = (TextInputLayout) findViewById(R.id.layout_contrasenia);
                                    validator.setError("Contraseña no válida");
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "Error de conexión, verifique si está conectado a internet", Toast.LENGTH_LONG).show();
                                }
                            }
                            message.dismiss();
                        }
                    });
        }
    }

    public void askToUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Alerta");
        builder.setMessage("Se ha detectado que su email no se encuentra validado. Revise en su correo electrónico para confirmar su registro.\n" +
                "Si usted no ha recibido ningún correo pulse reenviar, de lo contrario solo pulse cancelar.");
        builder.setPositiveButton("Reenviar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Se ha reenviado el correo", Toast.LENGTH_LONG).show();
                mAuth.getCurrentUser().reload();
                mAuth.getCurrentUser().sendEmailVerification();
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public boolean validarCampos(){
        if(email.getText().toString().isEmpty()){
            validator = (TextInputLayout) findViewById(R.id.layout_correoinstitucional);
            validator.setError("Este campo es requerido");
            return false;
        }
        if(pass.getText().toString().isEmpty()){
            validator = (TextInputLayout) findViewById(R.id.layout_contrasenia);
            validator.setError("Este campo es requerido");
            return false;
        }
        return true;
    }
}