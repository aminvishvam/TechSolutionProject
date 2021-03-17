package com.example.techsolutionproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techsolutionproject.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MainActivity extends AppCompatActivity {

    Button btnLogIn, btnRegister;
    RelativeLayout rootLayout;

    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference users;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_main);

//        //Init Firebase
//        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseDatabase.getInstance();
//        }catch (Exception e){
//            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
//        }
            users = db.getReference("Users");
//
//        //Init View
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLogIn = (Button) findViewById(R.id.btnLogIn);
        rootLayout = (RelativeLayout) findViewById(R.id.rootlayout);
//
//
//        Event
        try {
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRegisterDialog();
                }
            });
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
        try {
            btnLogIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoginInDialog();
                }
            });
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void showLoginInDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("LOG IN");
        dialog.setMessage("Please use email to login");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.activity_login, null);

        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);

        dialog.setView(login_layout);

        //Set Button
        dialog.setPositiveButton("LOG IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                //set disable button log in if is processing
                btnLogIn.setEnabled(false);

                //check validations
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (edtPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password too short", Snackbar.LENGTH_SHORT).show();
                    return;
                }


//                final ProgressDialog waitingDialog = new ProgressDialog(MainActivity.this);
//                waitingDialog.show();

                //Login
                mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //waitingDialog.dismiss();
                                startActivity(new Intent(MainActivity.this, Home.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //waitingDialog.dismiss();
                                Snackbar.make(rootLayout, "failed" + e.getMessage(), Snackbar.LENGTH_SHORT).show();

                                //Active button
                                btnLogIn.setEnabled(true);

                            }
                        });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });


        dialog.show();

    }

    private void showRegisterDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        final View register_layout = inflater.inflate(R.layout.activity_register, null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);


         dialog.setView(register_layout);

        //Set Button
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                //check validations
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter phone number", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (edtPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password too short", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                //register new user
                mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //save user to db
                                User user = new User();
                                user.setEmail(edtEmail.getText().toString());
                                user.setPassword(edtPassword.getText().toString());
                                user.setPhone(edtPhone.getText().toString());
                                user.setName(edtName.getText().toString());

                                //use email to key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout, "Registration success", Snackbar.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });


        dialog.show();

    }
}