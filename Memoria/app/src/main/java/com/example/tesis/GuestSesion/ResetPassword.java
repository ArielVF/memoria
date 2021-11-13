package com.example.tesis.GuestSesion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.tesis.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {
    private EditText email;
    private TextInputLayout validator;
    private FirebaseAuth mAuth;
    private ProgressDialog message;
    private ImageButton back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        email = findViewById(R.id.recuperarcorreosesion);
        mAuth = FirebaseAuth.getInstance();

        back_button = findViewById(R.id.back_button);

        message = new ProgressDialog(this);
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator = (TextInputLayout) findViewById(R.id.layout_recuperar_correo);
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
            }
        });
    }

    public void enviarCorreo(View view){
        if(validar()){
            message.setMessage("Validando información ...");
            message.setCanceledOnTouchOutside(false);
            message.show();
            mAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        AlertDialog.Builder alert = new AlertDialog.Builder(ResetPassword.this);
                        alert.setTitle("Información")
                                .setMessage("Hemos enviado un enlace de recuperación a su correo")
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alert.show();
                    }
                    else{
                        if(task.getException().toString().contains("There is no user record")){
                            Toast.makeText(getApplicationContext(), "El correo ingresado no se encuentra registrado", Toast.LENGTH_LONG).show();
                        }
                        else if(task.getException().toString().contains("formatted")){
                            validator = (TextInputLayout) findViewById(R.id.layout_recuperar_correo);
                            validator.setError("El formato del correo no es correcto");
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Error de conexión con servidor, verifique si está conectado a internet", Toast.LENGTH_LONG).show();
                        }
                    }
                    message.dismiss();
                }
            });
        }
    }

    public boolean validar(){
        if(email.getText().toString().trim().equals("")){
            validator = (TextInputLayout) findViewById(R.id.layout_recuperar_correo);
            validator.setError("Este campo es requerido");
            return false;
        }
        return true;
    }
}