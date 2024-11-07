package com.proyect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String WORK_TAG = "my_periodic_work";
    private static final int INTERVAL_MINUTES = 1;
    private static final int PERIOD_SECONDS = 5;
    ImageView ivCalendar;
    ImageView ivToday;
    ImageView ivNotes;
    ArrayList<String> arrayToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalendarFragment.init(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_main);

        setSupportActionBar(toolbar);

        ivCalendar = findViewById(R.id.btn_events);
        ivToday = findViewById(R.id.btn_today);
        ivNotes = findViewById(R.id.btn_notas);

        ivCalendar.setBackgroundResource(R.drawable.btn_calendar_colored80x80);
        ivToday.setBackgroundResource(R.drawable.btn_today_nocolor80x80);
        ivNotes.setBackgroundResource(R.drawable.btn_notes_nocolor80x80);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CalendarFragment calendarFragment = new CalendarFragment();
        fragmentTransaction.add(R.id.ll_fragments_main, calendarFragment);
        fragmentTransaction.commit();

        ivCalendar.setOnClickListener(this);
        ivToday.setOnClickListener(this);
        ivNotes.setOnClickListener(this);

        arrayToday = new ArrayList<String>();

        WorkManager workManager = WorkManager.getInstance(this);

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                UploadWorker.class,
                INTERVAL_MINUTES,
                TimeUnit.MINUTES,
                PERIOD_SECONDS,
                TimeUnit.SECONDS)
                .build();

        workManager.enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request);

        workManager.getWorkInfoByIdLiveData(request.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Log.v("MainActivity", "Periodic work finished: " + workInfo.getState());
                    }
                });


    }


    @Override
    public void onClick(View view) {
        Fragment fragment;

        Bundle bundle = new Bundle();

        bundle.putStringArrayList("arrayToday", arrayToday);

        if (view.getId() == R.id.btn_today) {
            fragment = new TodayFragment();

            ivCalendar.setBackgroundResource(R.drawable.btn_calendar_nocolor80x80);
            ivToday.setBackgroundResource(R.drawable.btn_today_colored80x80);
            ivNotes.setBackgroundResource(R.drawable.btn_notes_nocolor80x80);

        } else if (view.getId() == R.id.btn_notas) {
            fragment = new NotesFragment();

            ivCalendar.setBackgroundResource(R.drawable.btn_calendar_nocolor80x80);
            ivToday.setBackgroundResource(R.drawable.btn_today_nocolor80x80);
            ivNotes.setBackgroundResource(R.drawable.btn_notes_colored80x80);
        } else {
            fragment = new CalendarFragment();

            ivCalendar.setBackgroundResource(R.drawable.btn_calendar_colored80x80);
            ivToday.setBackgroundResource(R.drawable.btn_today_nocolor80x80);
            ivNotes.setBackgroundResource(R.drawable.btn_notes_nocolor80x80);
        }

        fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.ll_fragments_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent i;

        if (item.getItemId() == R.id.itemConfig) {

            i = new Intent(getApplicationContext(), Settings.class);

            startActivity(i);
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}