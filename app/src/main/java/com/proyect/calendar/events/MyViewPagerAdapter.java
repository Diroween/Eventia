package com.proyect.calendar.events;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyViewPagerAdapter extends FragmentStateAdapter {
    public MyViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PastEventsFragment();
            case 1:
                return new PresentEventsFragment();
            case 2:
                return new FutureEventsFragment();
            case 3:
                return new PeriodicEventsFragment();
            default:
                return new PastEventsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
