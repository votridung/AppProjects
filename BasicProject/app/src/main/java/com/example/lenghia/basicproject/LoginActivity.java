package com.example.lenghia.basicproject;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.lenghia.basicproject.Common.Common;
import com.example.lenghia.basicproject.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.security.MessageDigest;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by LE NGHIA on 3/29/2018.
 */

public class LoginActivity extends AppCompatActivity {

    Button btnSignIn, btnRegister;
    RelativeLayout rootLayout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    User user;
    private String userId;

    public static final String info_user = "Information_User";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_login);

        //
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(info_user);


        btnRegister = findViewById(R.id.btnRegister);
        btnSignIn = findViewById(R.id.btnSignIn);
        rootLayout = findViewById(R.id.rootLayout);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });

        //printKeyHash();
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.lenghia.basicproject",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRegisterDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage(" Please use email to register ! ");

        //layout register
        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register, null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                //check validation
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, " Please enter your email address !", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                    Snackbar.make(rootLayout, " Please enter your phone number !", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, " Please enter your password !", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if ((edtPassword.getText().toString().length()) < 6) {
                    Snackbar.make(rootLayout, " Password too short !", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                //register new user
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //save user into db
                                user = new User();
                                user.setEmail(edtEmail.getText().toString());
                                user.setPassword(edtPassword.getText().toString());
                                user.setName(edtName.getText().toString());
                                user.setPhone(edtPhone.getText().toString());

                                //Using email is key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout, " Register successfully !", Snackbar.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootLayout, " Failed ! " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout, " Failed ! " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    private void showLoginDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage(" Please use email to sign in ! ");

        //layout register
        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login, null);

        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);


        dialog.setView(login_layout);
        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                btnSignIn.setEnabled(false);

                //check validation
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, " Please enter your email address !", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, " Please enter your password !", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if ((edtPassword.getText().toString().length()) < 6) {
                    Snackbar.make(rootLayout, " Password too short !", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                final AlertDialog waitingDialog = new SpotsDialog(LoginActivity.this);
                waitingDialog.show();


                //Login
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingDialog.dismiss();

                                users.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                            user = new User();
                                            user.setName(postSnapshot.getValue(User.class).getName());
                                            user.setEmail(postSnapshot.getValue(User.class).getEmail());

                                            //Log.d("show setName", user.getName());
                                        }
                                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                        intent.putExtra("name",user.getName());
                                        intent.putExtra("email",user.getEmail());
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d("error", databaseError.toString());
                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout, " Failed ! " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        btnSignIn.setEnabled(true);
                    }
                });

            }
        });


        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });


        dialog.show();
    }

}
