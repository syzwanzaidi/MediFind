package com.example.medifind;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    TextView nameView, ageView, genderView, phoneView;
    DatabaseReference database;
    FirebaseUser user;
    FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameView = view.findViewById(R.id.display_name);
        ageView = view.findViewById(R.id.display_age);
        genderView = view.findViewById(R.id.display_gender);
        phoneView = view.findViewById(R.id.display_phone);

        Button editButton = view.findViewById(R.id.btn_edit_profile);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        Button logoutButton = view.findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Go to LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear backstack
            startActivity(intent);
            getActivity().finish();
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            database = FirebaseDatabase.getInstance("https://medifind-62da5-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("Users");

            database.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserProfile profile = snapshot.getValue(UserProfile.class);
                    if (profile != null) {
                        nameView.setText(profile.name);
                        ageView.setText(profile.age);
                        genderView.setText(profile.gender);
                        phoneView.setText(profile.phone);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }
}
