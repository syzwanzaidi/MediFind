package com.example.medifind;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AppointmentFragment extends Fragment {

    EditText etClinicName;
    Button btnDate, btnTime, btnSave;
    int selectedYear = 0, selectedMonth = 0, selectedDay = 0, selectedHour = -1, selectedMinute = -1;

    DatabaseReference databaseReference;
    FirebaseAuth mAuth;

    public AppointmentFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment, container, false);

        etClinicName = view.findViewById(R.id.etClinicName);
        btnDate = view.findViewById(R.id.btnDate);
        btnTime = view.findViewById(R.id.btnTime);
        btnSave = view.findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Appointments");

        createNotificationChannel();

        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveAppointment());

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = day;
            btnDate.setText(day + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, hour, minute) -> {
            selectedHour = hour;
            selectedMinute = minute;
            btnTime.setText(String.format("%02d:%02d", hour, minute));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void saveAppointment() {
        String clinicName = etClinicName.getText().toString().trim();

        if (clinicName.isEmpty() || selectedYear == 0 || selectedHour == -1) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String appointmentId = databaseReference.push().getKey();

        Map<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("clinicName", clinicName);
        appointmentData.put("year", selectedYear);
        appointmentData.put("month", selectedMonth);
        appointmentData.put("day", selectedDay);
        appointmentData.put("hour", selectedHour);
        appointmentData.put("minute", selectedMinute);
        appointmentData.put("userId", userId);

        databaseReference.child(appointmentId).setValue(appointmentData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        scheduleReminderWithWorkManager(clinicName);
                        Toast.makeText(requireContext(), "Appointment Saved!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save appointment", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scheduleReminderWithWorkManager(String clinicName) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);

        long appointmentTime = calendar.getTimeInMillis();
        long reminderTime = appointmentTime - (60 * 60 * 1000); // 1 hour before
        long delay = reminderTime - System.currentTimeMillis();

        if (delay <= 0) {
            Toast.makeText(requireContext(), "Reminder time is in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        Data data = new Data.Builder()
                .putString("clinicName", clinicName)
                .build();

        OneTimeWorkRequest reminderRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(requireContext()).enqueue(reminderRequest);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "appointment_channel",
                    "Appointment Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = requireContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
