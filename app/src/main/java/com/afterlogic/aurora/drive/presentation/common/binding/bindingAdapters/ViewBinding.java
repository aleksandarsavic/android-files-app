package com.afterlogic.aurora.drive.presentation.common.binding.bindingAdapters;

import android.databinding.BindingAdapter;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;

import com.afterlogic.aurora.drive.R;
import com.afterlogic.aurora.drive.core.common.logging.MyLog;
import com.afterlogic.aurora.drive.presentation.common.binding.itemsAdapter.ViewsViewModelBindAdapter;

import java.util.List;

/**
 * Created by sashka on 13.09.16.<p/>
 * mail: sunnyday.development@gmail.com
 */
@SuppressWarnings("unused")
public class ViewBinding {

    private static final String TAG = ViewBinding.class.getSimpleName();

    @BindingAdapter("bind:backgroundColor")
    public static void setBackgroundColor(View view, int color){
        if (color != -1){
            view.setBackgroundColor(color);
        }
    }

    @BindingAdapter("bind:onClick")
    public static void setOnClick(View view, View.OnClickListener onClickListener){
        view.setClickable(true);
        view.setOnClickListener(onClickListener);
    }

    @BindingAdapter("bind:onLongClick")
    public static void setOnLongClick(View view, View.OnLongClickListener onClickListener){
        view.setClickable(true);
        view.setOnLongClickListener(onClickListener);
    }

    @BindingAdapter("bind:alpha")
    public static void setAlpha(View view, float alpha){
        ViewCompat.setAlpha(view, alpha);
    }

    @BindingAdapter({"bind:layout_behavior"})
    public static void setLayoutBehavior(View view, String behaviorClassName){
        if (view.getParent() instanceof CoordinatorLayout){
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            if (behaviorClassName != null) {
                try {
                    Object behavior = Class.forName(behaviorClassName).newInstance();
                    if (behavior != null && behavior instanceof CoordinatorLayout.Behavior) {
                        lp.setBehavior((CoordinatorLayout.Behavior) behavior);
                    }
                } catch (InstantiationException e) {
                    MyLog.majorException(TAG, e);
                } catch (IllegalAccessException e) {
                    MyLog.majorException(TAG, e);
                } catch (ClassNotFoundException e) {
                    MyLog.majorException(TAG, e);
                }
            } else {
                lp.setBehavior(null);
            }
        }
    }


    @BindingAdapter({"bind:childView"})
    public static void setChildView(ViewGroup viewGroup, @Nullable View child){
        setChildView(viewGroup, child, 0);
    }

    @BindingAdapter({"bind:childView", "bind:childViewIndex"})
    public static void setChildView(ViewGroup viewGroup, @Nullable  View child, int index){
        for (int i = 0; i < viewGroup.getChildCount(); i++){
            View view = viewGroup.getChildAt(i);
            if (view.getTag(R.id.bind_view_child) != null){
                viewGroup.removeView(view);
                break;
            }
        }

        if (child != null) {
            child.setTag(R.id.bind_view_child, (byte) 1);
            viewGroup.addView(child, index);
        }
    }

    @BindingAdapter({"bind:childViewAdapter", "bind:items"})
    public static <VM> void setChildViewsAdapter(ViewGroup container, ViewsViewModelBindAdapter<VM> adapter, List<VM> list){
        adapter.setItems(list);
        adapter.onAttach(container);
        container.setTag(R.id.bind_view_child_adapter, adapter);
    }
}