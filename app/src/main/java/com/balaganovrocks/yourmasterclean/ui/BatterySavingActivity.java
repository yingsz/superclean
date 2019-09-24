package com.balaganovrocks.yourmasterclean.ui;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balaganovrocks.yourmasterclean.R;
import com.balaganovrocks.yourmasterclean.adapter.BatterySavingAdapter;
import com.balaganovrocks.yourmasterclean.adapter.ClearMemoryAdapter;
import com.balaganovrocks.yourmasterclean.base.BaseSwipeBackActivity;
import com.balaganovrocks.yourmasterclean.bean.AppProcessInfo;
import com.balaganovrocks.yourmasterclean.model.StorageSize;
import com.balaganovrocks.yourmasterclean.service.CoreService;
import com.balaganovrocks.yourmasterclean.utils.StorageUtil;
import com.balaganovrocks.yourmasterclean.utils.SystemBarTintManager;
import com.balaganovrocks.yourmasterclean.utils.T;
import com.balaganovrocks.yourmasterclean.utils.UIElementsHelper;
import com.balaganovrocks.yourmasterclean.widget.textcounter.CounterView;
import com.balaganovrocks.yourmasterclean.widget.textcounter.formatters.DecimalFormatter;
import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.john.waveview.WaveView;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;

public class BatterySavingActivity extends BaseSwipeBackActivity implements OnDismissCallback, CoreService.OnPeocessActionListener {

    ActionBar ab;

    @InjectView(R.id.listview)
    ListView mListView;

   // @InjectView(R.id.wave_view)
   // WaveView mwaveView;


    @InjectView(R.id.header)
    RelativeLayout header;
    List<AppProcessInfo> mAppProcessInfos = new ArrayList<>();
    BatterySavingAdapter mBatterySavingAdapter;

    @InjectView(R.id.textCounter)
    CounterView textCounter;
    @InjectView(R.id.sufix)
    TextView sufix;
    public long Allmemory;

    @InjectView(R.id.bottom_lin)
    LinearLayout bottom_lin;

    @InjectView(R.id.progressBar)
    View mProgressBar;
    @InjectView(R.id.progressBarText)
    TextView mProgressBarText;

    @InjectView(R.id.clear_button)
    Button clearButton;
    private static final int INITIAL_DELAY_MILLIS = 300;
    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter;
    private static final String TAG = "myLogs";
    private CoreService mCoreService;
    private InterstitialAd mInterstitialAd;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.ProcessServiceBinder) service).getService();
            mCoreService.setOnActionListener(BatterySavingActivity.this);
            mCoreService.scanRunProcess();
            //  updateStorageUsage();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService.setOnActionListener(null);
            mCoreService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_saving);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //  applyKitKatTranslucency();
        mBatterySavingAdapter = new BatterySavingAdapter(mContext, mAppProcessInfos);
        mListView.setAdapter(mBatterySavingAdapter);
        bindService(new Intent(mContext, CoreService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
        int footerHeight = mContext.getResources().getDimensionPixelSize(R.dimen.footer_height);
        mListView.setOnScrollListener(new QuickReturnListViewOnScrollListener(QuickReturnType.FOOTER, null, 0, bottom_lin, footerHeight));
        textCounter.setAutoFormat(false);
        textCounter.setFormatter(new DecimalFormatter());
        textCounter.setAutoStart(false);
        textCounter.setIncrement(5f); // the amount the number increments at each time interval
        textCounter.setTimeInterval(50); // the time interval (ms) at which the text changes
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4369038195513432/2986510514");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Apply KitKat specific translucency.
     */
    private void applyKitKatTranslucency() {

        // KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager mTintManager = new SystemBarTintManager(this);
            mTintManager.setStatusBarTintEnabled(true);
            mTintManager.setNavigationBarTintEnabled(true);
            // mTintManager.setTintColor(0xF00099CC);

            mTintManager.setTintDrawable(UIElementsHelper
                    .getGeneralActionBarBackground(this));

            getActionBar().setBackgroundDrawable(
                    UIElementsHelper.getGeneralActionBarBackground(this));

        }

    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] ints) {

    }

    @Override
    public void onScanStarted(Context context) {
        mProgressBarText.setText(R.string.scanning);
        showProgressBar(true);
    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {
        mProgressBarText.setText(getString(R.string.scanning_m_of_n, current, max));
    }

    @Override
    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {
        mAppProcessInfos.clear();

        Allmemory = 0;
        for (AppProcessInfo appInfo : apps) {
          if (!appInfo.isSystem) {
              mAppProcessInfos.add(appInfo);
              Allmemory += appInfo.memory;
          }
        }


       //refeshTextCounter();

        mBatterySavingAdapter.notifyDataSetChanged();
        showProgressBar(false);


        if (apps.size() > 0) {
            header.setVisibility(View.VISIBLE);
            bottom_lin.setVisibility(View.VISIBLE);


        } else {
            header.setVisibility(View.GONE);
            bottom_lin.setVisibility(View.GONE);
        }
        //   mClearMemoryAdapter = new ClearMemoryAdapter(mContext,
        //           apps);  mClearMemoryAdapter = new ClearMemoryAdapter(mContext,
        //           apps);
        //   swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(mClearMemoryAdapter, MemoryCleanActivity.this));
        //   swingBottomInAnimationAdapter.setAbsListView(mListView);
        //   assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        //   swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);
//
        //   mListView.setAdapter(swingBottomInAnimationAdapter);
        //   clearMem.setText("200M");


    }

  //  private void refeshTextCounter() {
  //      mwaveView.setProgress(20);
  //      StorageSize mStorageSize = StorageUtil.convertStorageSize(Allmemory);
  //      textCounter.setStartValue(0f);
  //      textCounter.setEndValue(mStorageSize.value);
  //      sufix.setText(mStorageSize.suffix);
  //      //  textCounter.setSuffix(mStorageSize.suffix);
  //      textCounter.start();
  //  }

    @Override
    public void onCleanStarted(Context context) {

    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {

    }


    @OnClick(R.id.clear_button)
    public void onClickClear() {
        long killAppmemory = 0;


        for (int i = mAppProcessInfos.size() - 1; i >= 0; i--) {
            if (mAppProcessInfos.get(i).checked) {
                killAppmemory += mAppProcessInfos.get(i).memory;
                mCoreService.killBackgroundProcesses(mAppProcessInfos.get(i).processName);
                mAppProcessInfos.remove(mAppProcessInfos.get(i));
                mBatterySavingAdapter.notifyDataSetChanged();
            }
        }
        Allmemory = Allmemory - killAppmemory;
        T.showLong(mContext, R.string.saving_battery);
       // if (Allmemory > 0) {
       //     refeshTextCounter();
       // }
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG,"onAdLoaded");
// Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d(TAG,"onAdFailedToLoad");
// Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                Log.d(TAG,"onAdOpened");

                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(TAG,"onAdLeftApplication");
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG,"onAdClosed");
                Intent intent = new Intent(getBaseContext(), ResultActivity.class);
                startActivityForResult(intent, 0); // do something
// Code to be executed when when the interstitial ad is closed.
            }
        });

    }


    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                    mContext, android.R.anim.fade_out));
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
