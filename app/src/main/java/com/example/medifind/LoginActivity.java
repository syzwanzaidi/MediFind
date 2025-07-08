package com.example.medifind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    EditText e1, e2;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        e1 = findViewById(R.id.editText);   // Email
        e2 = findViewById(R.id.editText2);  // Password

        mAuth = FirebaseAuth.getInstance();
    }

    public void loginUser(View v){
        String email = e1.getText().toString().trim();
        String password = e2.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(getApplicationContext(), "Blank not allowed", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();

                                String uid = mAuth.getCurrentUser().getUid();
                                DatabaseReference userRef = FirebaseDatabase
                                        .getInstance("https://medifind-62da5-default-rtdb.asia-southeast1.firebasedatabase.app")
                                        .getReference("Users").child(uid);

                                userRef.get().addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful() && profileTask.getResult().exists()) {
                                        // Profile exists → Go to Home
                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                        startActivity(intent);
                                    } else {
                                        // No profile → Go to ProfileActivity
                                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                        startActivity(intent);
                                    }
                                    finish();
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "User could not be login", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
