package com.nihap.lostlink;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;



public class ReportPagerAdapter extends FragmentStateAdapter {
    public ReportPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new LostFragment() : new FoundFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
