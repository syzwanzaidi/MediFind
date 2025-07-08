package com.example.medifind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    TextView nameView, ageView, genderView, phoneView;

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

        // For now: show dummy data
        nameView.setText("Haziq Bin Example");
        ageView.setText("21");
        genderView.setText("Male");
        phoneView.setText("+60123456789");

        return view;
    }
}
