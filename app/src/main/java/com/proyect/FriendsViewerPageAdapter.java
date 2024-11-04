package com.proyect;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FriendsViewerPageAdapter extends FragmentStateAdapter
{

    public FriendsViewerPageAdapter(@NonNull FragmentActivity fragmentActivity)
    {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        if(position == 1)
        {
            return new FriendEventListFragment();
        }
        else
        {
            return new FriendInfoPageFragment();
        }
    }

    @Override
    public int getItemCount()
    {
        return 2;
    }
}
