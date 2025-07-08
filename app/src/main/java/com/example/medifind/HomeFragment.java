package com.example.medifind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    TextView greetingText, appointmentsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        greetingText = view.findViewById(R.id.greeting_text);
        appointmentsText = view.findViewById(R.id.appointments_text);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "User";
        greetingText.setText("Hi, " + name + "!");

        // Replace with your upcoming appointments logic
        appointmentsText.setText("No upcoming appointments.");

        return view;
    }
}
