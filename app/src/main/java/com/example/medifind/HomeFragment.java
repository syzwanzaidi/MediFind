package com.example.medifind;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    TextView greetingText;
    LinearLayout appointmentContainer;
    FirebaseUser user;
    DatabaseReference userRef, appointmentRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        greetingText = view.findViewById(R.id.greeting_text);
        appointmentContainer = view.findViewById(R.id.appointment_container);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            // Load user's name
            userRef = FirebaseDatabase.getInstance("https://medifind-62da5-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("Users");

            userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserProfile profile = snapshot.getValue(UserProfile.class);
                    if (profile != null) {
                        greetingText.setText("Hi, " + profile.name + "!");
                    } else {
                        greetingText.setText("Hi, User!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to load user info", Toast.LENGTH_SHORT).show();
                }
            });

            // Load appointments
            appointmentRef = FirebaseDatabase.getInstance("https://medifind-62da5-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("Appointments");

            appointmentRef.orderByChild("userId").equalTo(uid)
                    .limitToLast(5)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            appointmentContainer.removeAllViews();

                            boolean found = false;

                            for (DataSnapshot data : snapshot.getChildren()) {
                                String clinicName = data.child("clinicName").getValue(String.class);
                                Long day = data.child("day").getValue(Long.class);
                                Long month = data.child("month").getValue(Long.class);
                                Long year = data.child("year").getValue(Long.class);
                                Long hour = data.child("hour").getValue(Long.class);
                                Long minute = data.child("minute").getValue(Long.class);

                                if (clinicName != null && day != null && month != null && year != null && hour != null && minute != null) {
                                    found = true;

                                    // Create CardView
                                    CardView cardView = new CardView(requireContext());
                                    cardView.setRadius(16);
                                    cardView.setCardElevation(8);
                                    cardView.setUseCompatPadding(true);

                                    LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    );
                                    cardParams.setMargins(0, 0, 0, 24);
                                    cardView.setLayoutParams(cardParams);

                                    // Create content TextView
                                    TextView box = new TextView(getContext());
                                    box.setText(
                                            "Clinic: " + clinicName + "\n" +
                                                    "Date: " + String.format("%02d/%02d/%04d", day, month + 1, year) + "\n" +
                                                    "Time: " + String.format("%02d:%02d", hour, minute)
                                    );
                                    box.setPadding(32, 32, 32, 32);
                                    box.setTextSize(16f);
                                    box.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                                    box.setGravity(Gravity.START);

                                    // Add TextView to CardView
                                    cardView.addView(box);

                                    // Add CardView to container
                                    appointmentContainer.addView(cardView);
                                }
                            }

                            if (!found) {
                                TextView empty = new TextView(getContext());
                                empty.setText("No upcoming appointments.");
                                empty.setPadding(24, 24, 24, 24);
                                empty.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                                appointmentContainer.addView(empty);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            greetingText.setText("Hi, User!");
            TextView notLoggedIn = new TextView(getContext());
            notLoggedIn.setText("Please log in to view your appointments.");
            notLoggedIn.setPadding(24, 24, 24, 24);
            notLoggedIn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            appointmentContainer.addView(notLoggedIn);
        }

        return view;
    }
}
