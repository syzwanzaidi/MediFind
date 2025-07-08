package com.example.medifind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    EditText nameField, ageField, phoneField;
    Spinner genderSpinner;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        nameField = findViewById(R.id.name);
        ageField = findViewById(R.id.age);
        phoneField = findViewById(R.id.phone);
        genderSpinner = findViewById(R.id.gender);

        // Set up gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Firebase database ref
        database = FirebaseDatabase.getInstance("https://medifind-62da5-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users");

    }

    public void saveProfile(View view) {
        String name = nameField.getText().toString().trim();
        String age = ageField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();

        Log.d("SAVE_DEBUG", "Name: " + name);
        Log.d("SAVE_DEBUG", "Age: " + age);
        Log.d("SAVE_DEBUG", "Phone: " + phone);
        Log.d("SAVE_DEBUG", "Gender: " + gender);

        if (user == null) {
            Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show();
            Log.e("SAVE_DEBUG", "User is null");
            return;
        }

        // Go to HomeActivity [FOR TESTING ONLY]
        startActivity(new Intent(this, HomeActivity.class));

        String uid = user.getUid();
        Log.d("SAVE_DEBUG", "User UID: " + uid);

        UserProfile profile = new UserProfile(name, age, gender, phone);

        database.child(uid).setValue(profile)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show();
                        Log.d("SAVE_DEBUG", "Saved successfully to Firebase");
//                        // Go to HomeActivity
//                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save!", Toast.LENGTH_SHORT).show();
                        Log.e("SAVE_DEBUG", "Save failed");
                    }
                }
                )
                .addOnFailureListener(e -> {
                    Log.e("SAVE_DEBUG", "Save error: ", e);
                });
    }

}
