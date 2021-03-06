package com.afterlogic.aurora.drive.presentation.modules.baseFiles.view;

import android.content.Context;
import androidx.databinding.ObservableList;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.afterlogic.aurora.drive.model.Storage;
import com.afterlogic.aurora.drive.presentation.common.binding.itemsAdapter.ItemsAdapter;
import com.afterlogic.aurora.drive.presentation.common.binding.itemsAdapter.SimpleOnObservableListChangedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by sashka on 03.02.17.<p/>
 * mail: sunnyday.development@gmail.com
 */

public class FilesPagerAdapter extends FragmentStatePagerAdapter implements ItemsAdapter<Storage>{

    private final FilesContentProvider mFilesContentProvider;

    private List<Storage> mStorages;

    private BaseFilesListFragment mPrimaryFragment = null;

    private SparseArray<UUID> mFilesModuleIds = new SparseArray<>();

    private int mDataStateIndex = 0;

    private ObservableList.OnListChangedCallback<ObservableList<Storage>> mOnListChangedCallback = new SimpleOnObservableListChangedListener<>(
            this::notifyDataSetChanged
    );

    private Context mContext;

    public FilesPagerAdapter(FragmentManager fm, FilesContentProvider filesContentProvider, Context ctx) {
        super(fm);
        mFilesContentProvider = filesContentProvider;
        mContext = ctx;
    }

    @Override
    public void setItems(List<Storage> items) {
        if (mStorages == items) return;

        if (mStorages != null && mStorages instanceof ObservableList){
            ObservableList<Storage> observable = (ObservableList<Storage>) mStorages;
            observable.removeOnListChangedCallback(mOnListChangedCallback);
        }

        if (items != null) {
            /*
            TODO reverse pages
            if (RtlUtil.isRtl(mContext)) {
                mStorages = new ArrayList<>();
                Stream.of(items).forEach(it -> mStorages.add(0, it));
            } else {
                mStorages = new ArrayList<>(items);
            }*/

            mStorages = new ArrayList<>(items);
        } else {
            mStorages = null;
        }

        if (mStorages != null && mStorages instanceof ObservableList){
            ObservableList<Storage> observable = (ObservableList<Storage>) mStorages;
            observable.addOnListChangedCallback(mOnListChangedCallback);
        }
        notifyDataSetChanged();
    }

    @Override
    public BaseFilesListFragment getItem(int position) {
        BaseFilesListFragment fragment = mFilesContentProvider.get(mStorages.get(position).getType());
        fragment.setArgsType(mStorages.get(position).getType());
        fragment.setModuleUuid(mFilesModuleIds.get(position));

        int currentDataStateIndex = mDataStateIndex;
        fragment.setFirstCreateInterceptor(view -> {
            if (currentDataStateIndex != mDataStateIndex) return;
            mFilesModuleIds.put(position, view.getModuleUuid());
        });

        return fragment;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mPrimaryFragment = (BaseFilesListFragment) object;
    }

    @Override
    public int getCount() {
        return mStorages == null ? 0 : mStorages.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mStorages.get(position).getCaption();
    }

    @Nullable
    public BaseFilesListFragment getPrimaryFragment() {
        return mPrimaryFragment;
    }

    @Override
    public void notifyDataSetChanged() {
        mDataStateIndex++;
        mFilesModuleIds.clear();
        super.notifyDataSetChanged();
    }

    interface FilesContentProvider{
        BaseFilesListFragment get(String type);
    }
}
