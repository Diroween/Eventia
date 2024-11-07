package com.proyect;

import static com.proyect.Constants.TO;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.proyect.utils.ReminderWorker;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;             // Permite almacenar fechas en formato yyyy-mm-dd
import java.time.LocalDateTime;
import java.time.LocalTime;             // Permite almacenar horas en formato hh:mm:ss:nn
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;

public class CalendarFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static View customLayout;
    //Creamos un CalendarView
    public static CalendarView calendar;
    public static TextView tvHora;
    public static TextView tvFecha;
    public static EditText etTitulo;
    public static LocalTime horaSelec = LocalTime.of(0, 0);
    public static LocalDate fechaSelec = LocalDate.now();
    public static String str;

    private static String title;
    private static String message;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static Context sContext;


    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private static void notificarEvento() {
        ApiUtils.getClients().sendNotification(new PushNotification(new NotificationData(title, message), TO))
                .enqueue(new Callback<PushNotification>() {
                    @Override
                    public void onResponse(Call<PushNotification> call, Response<PushNotification> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(calendar.getContext(), "NEW NOTIFICATION SENT", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(calendar.getContext(), response.toString(), Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<PushNotification> call, Throwable throwable) {
                        Toast.makeText(calendar.getContext(), throwable.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        calendar = view.findViewById(R.id.cv_calendar);

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {

                customLayout = LayoutInflater.from(getActivity()).inflate(R.layout.event_type_picker, calendar, false);
                calendar.addView(customLayout);

                Button button1 = view.findViewById(R.id.btnCancelar);
                Button button2 = view.findViewById(R.id.btnAceptar);
                Button button3 = view.findViewById(R.id.btnTimePicker);

                etTitulo = view.findViewById(R.id.etTitulo);
                tvFecha = view.findViewById(R.id.tvFecha);
                tvHora = view.findViewById(R.id.tvHora);

                button1.setOnClickListener(new MyOnClickListener());
                button2.setOnClickListener(new MyOnClickListener());
                button3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        openDialog();
                    }
                });

                TextView tView2 = view.findViewById(R.id.tvFecha);


                LocalDate fechaPrueba = LocalDate.now();
                boolean leap = fechaPrueba.isLeapYear();
                LocalDate fechaFutura = fechaPrueba.with(Month.MAY);

                // Sumamos +1 para el valor mes
                fechaSelec = LocalDate.of(i, i1 + 1, i2);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                tView2.setText(fechaSelec.format(formatter));

                LocalTime horaActual = LocalTime.now();



                Toast.makeText(calendar.getContext(), "" + ZonedDateTime.now().toString(), Toast.LENGTH_LONG).show();


            }
        });
    }

    private void openDialog() {

        TimePickerDialog dialog = new TimePickerDialog(this.getContext(), R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {

                horaSelec = LocalTime.of(i, i1);

                String horasMinutos = String.format("%02d:%02d", horaSelec.getHour(), horaSelec.getMinute());

                tvHora.setText(horasMinutos);



            }
        }, LocalTime.now().plusHours(1).getHour(), 00, true);
        dialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    private void msg() {
        FirebaseMessaging.getInstance()
                .subscribeToTopic("All");


    }

    private static class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (customLayout.getVisibility() == View.VISIBLE) {
                if (v.getId() == R.id.btnCancelar) {
                    calendar.removeView(customLayout);
                } else if (v.getId() == R.id.btnAceptar) {

                    title = etTitulo.getText().toString();
                    message = tvFecha.getText().toString() + " " + tvHora.getText().toString();

                    LocalDateTime ahora = LocalDateTime.now();
                    LocalDateTime ldt = LocalDateTime.of(fechaSelec, horaSelec);

                    ZonedDateTime now = ZonedDateTime.now();
                    ZoneOffset offset = now.getOffset();

                    createWorkRequest(etTitulo.getText().toString(), ldt.toEpochSecond(offset) - ahora.toEpochSecond(offset));

                    Toast.makeText(calendar.getContext(), "" + ZonedDateTime.now().toString() , Toast.LENGTH_LONG).show();


                    notificarEvento();

                    calendar.removeView(customLayout);
                }
            }

        }

    }

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }
    public static void createWorkRequest(String message, long timeDelayInSeconds) {
        OneTimeWorkRequest myWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(timeDelayInSeconds, TimeUnit.SECONDS)
                .setInputData(new Data.Builder()
                        .putString("title", "Reminder")
                        .putString("message", message)
                        .build())
                .build();

        WorkManager.getInstance(sContext).enqueue(myWorkRequest);
    }


}
