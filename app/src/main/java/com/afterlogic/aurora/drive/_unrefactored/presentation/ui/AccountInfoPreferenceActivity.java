package com.afterlogic.aurora.drive._unrefactored.presentation.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afterlogic.aurora.drive.R;
import com.afterlogic.aurora.drive._unrefactored.data.common.ApiProvider;
import com.afterlogic.aurora.drive._unrefactored.presentation.ui.common.base.BaseActivity;
import com.afterlogic.aurora.drive.application.App;
import com.afterlogic.aurora.drive.model.AuroraSession;

/**
 * Created by sashka on 16.01.17.<p/>
 * mail: sunnyday.development@gmail.com
 */

public class AccountInfoPreferenceActivity extends BaseActivity {

    protected ApiProvider mApiProvider = new ApiProvider();

    private TextView mLogin;
    private TextView mHost;

    @Override
    public int getActivityLayout() {
        return R.layout.activity_account_info;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogin = (TextView) findViewById(R.id.email);
        mHost = (TextView) findViewById(R.id.domain);

        ((App) getApplication()).modulesFactory().inject(mApiProvider);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AuroraSession session = mApiProvider.getSessionManager().getAuroraSession();
        if (session != null){
            mLogin.setText(session.getLogin());
            mHost.setText(session.getDomain().toString());
        } else {
            finishOnEmptySession();
        }
    }

    @Override
    public void initActionBar(ActionBar ab) {
        super.initActionBar(ab);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void finishOnEmptySession(){
        Toast.makeText(this, R.string.prompt_account_info_not_authorized, Toast.LENGTH_LONG).show();
        finish();
    }
}
