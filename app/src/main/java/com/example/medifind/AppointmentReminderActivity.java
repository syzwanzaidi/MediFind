package com.example.medifind;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AppointmentReminderActivity extends AppCompatActivity {

    EditText etClinicName;
    Button btnDate, btnTime, btnSave;
    int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    DatabaseReference databaseReference;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_reminder);

        etClinicName = findViewById(R.id.etClinicName);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        btnSave = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Appointments");

        createNotificationChannel();

        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveAppointment());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = day;
            btnDate.setText(day + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            selectedHour = hour;
            selectedMinute = minute;
            btnTime.setText(String.format("%02d:%02d", hour, minute));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void saveAppointment() {
        String clinicName = etClinicName.getText().toString().trim();

        if (clinicName.isEmpty() || selectedYear == 0 || selectedHour == 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Appointment Saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save appointment", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scheduleNotification() {
        Intent intent = new Intent(this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);

        long appointmentTime = calendar.getTimeInMillis();
        long reminderTime = appointmentTime - (60 * 60 * 1000); // 1 hour before

        if (reminderTime > System.currentTimeMillis()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "appointment_channel",
                    "Appointment Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
