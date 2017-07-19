package com.afterlogic.aurora.drive.presentation.modules.main.v2.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.afterlogic.aurora.drive.presentation.modules._baseFiles.v2.view.FileTypesPagerAdapter;

/**
 * Created by aleksandrcikin on 04.07.17.
 * mail: mail@sunnydaydev.me
 */

class MainFileTypesPagerAdapter extends FileTypesPagerAdapter {

    MainFileTypesPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    protected Fragment getFilesListFragment(String type) {
        return MainFileListFragment.newInstance(type);
    }
}
