package com.example.medifind;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ReminderFragment extends Fragment {

    EditText etClinicName;
    Button btnDate, btnTime, btnSave;
    int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    DatabaseReference databaseReference;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Ask for POST_NOTIFICATIONS permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        View view = inflater.inflate(R.layout.activity_appointment_reminder, container, false);

        etClinicName = view.findViewById(R.id.etClinicName);
        btnDate = view.findViewById(R.id.btnDate);
        btnTime = view.findViewById(R.id.btnTime);
        btnSave = view.findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase
                .getInstance("https://medifind-62da5-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

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
            btnDate.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
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

        if (clinicName.isEmpty() || selectedYear == 0 || selectedHour == 0) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
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
                        scheduleNotification();
                        Toast.makeText(requireContext(), "Appointment saved!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save appointment", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scheduleNotification() {
        String clinicName = etClinicName.getText().toString().trim();
        String dateText = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
        String timeText = String.format("%02d:%02d", selectedHour, selectedMinute);

        Intent intent = new Intent(requireContext(), ReminderBroadcast.class);
        intent.putExtra("clinicName", clinicName);
        intent.putExtra("date", dateText);
        intent.putExtra("time", timeText);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(), 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        // Set appointment time
        Calendar appointmentTime = Calendar.getInstance();
        appointmentTime.set(Calendar.YEAR, selectedYear);
        appointmentTime.set(Calendar.MONTH, selectedMonth);
        appointmentTime.set(Calendar.DAY_OF_MONTH, selectedDay);
        appointmentTime.set(Calendar.HOUR_OF_DAY, selectedHour);
        appointmentTime.set(Calendar.MINUTE, selectedMinute);
        appointmentTime.set(Calendar.SECOND, 0);
        appointmentTime.set(Calendar.MILLISECOND, 0);

        // Schedule reminder 10 seconds before the appointment time
        long reminderTimeMillis = appointmentTime.getTimeInMillis() - (10 * 1000); // 10 seconds before

        if (reminderTimeMillis < System.currentTimeMillis()) {
            Toast.makeText(requireContext(), "Reminder time already passed, Please recheck your appointment time.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
        }

        Toast.makeText(requireContext(), "Reminder set for " + timeText, Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "appointment_channel",
                    "Appointment Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = requireContext().getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
