package com.codepath.timeline.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.view.View;

import com.codepath.timeline.R;
import com.codepath.timeline.fragments.AutoPlayFragment;
import com.codepath.timeline.models.Moment;
import com.codepath.timeline.network.TimelineClient;
import com.codepath.timeline.util.AppConstants;
import com.qslll.library.ExpandingPagerFactory;
import com.qslll.library.ExpandingViewPagerAdapter;
import com.qslll.library.fragments.ExpandingFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/*
      Expanding view for displaying list of moments: https://github.com/qs-lll/ExpandingPager
      Automatic scroll used for auto-playing list of moments: https://github.com/Trinea/android-auto-scroll-view-pager
 */
public class AutoPlayActivity extends AppCompatActivity
        implements ExpandingFragment.OnExpandingClickListener {

    private static final String TAG = AutoPlayActivity.class.getSimpleName();

    @BindView(R.id.vpMoment)
    AutoScrollViewPager vpMoment;

    private List<Moment> mMomentList;
    private Moment mMoment;
    private AutoPlayPagerAdapter mPagerAdapter;
    private String storyObjectId;
    private String storyTitle;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_play);
        ButterKnife.bind(this);

        // get story info from intent
        // NOTE: Can't pass 'Story' since it's not Parcelable/Serializable
        storyObjectId = getIntent().getStringExtra(AppConstants.OBJECT_ID);
        storyTitle = getIntent().getStringExtra(AppConstants.STORY_TITLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(storyTitle);
        getMomentList();
    }

    private void initList() {
        mPagerAdapter = new AutoPlayPagerAdapter(getSupportFragmentManager());
        vpMoment.setAdapter(mPagerAdapter);

        ExpandingPagerFactory.setupViewPager(vpMoment);
        vpMoment.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ExpandingFragment expandingFragment = ExpandingPagerFactory.getCurrentFragment(vpMoment);
                if (expandingFragment != null && expandingFragment.isOpenend()) {
                    expandingFragment.close();
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // initialize viewpager for automatically scrolling through the list of moments
        vpMoment.setInterval(5000);
        vpMoment.startAutoScroll();
        vpMoment.setCurrentItem(0);
    }

    private void getMomentList() {
        TimelineClient.getInstance().getMomentList(storyObjectId, new TimelineClient.TimelineClientGetMomentListListener() {
            @Override
            public void onGetMomentList(List<Moment> itemList) {
                mMomentList = new ArrayList<>();
                mMomentList.addAll(itemList);
                initList();
            }

            @Override
            public void onGetMomentChatList(List<Moment> itemList) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!ExpandingPagerFactory.onBackPressed(vpMoment)) {
            super.onBackPressed();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Explode slideTransition = new Explode();
        getWindow().setReenterTransition(slideTransition);
        getWindow().setExitTransition(slideTransition);
    }

    @SuppressWarnings("unchecked")
    private void startInfoActivity(View view, Moment moment) {
        Activity activity = this;
        ActivityCompat.startActivity(activity,
                AutoPlayInfoActivity.newInstance(activity, moment),
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        new Pair<>(view, getString(R.string.transition_image)))
                        .toBundle());
    }

    @Override
    public void onExpandingClick(View v) {

        // when clicked second time, just close the expanding fragment
        ExpandingFragment expandingFragment = ExpandingPagerFactory.getCurrentFragment(vpMoment);
        if (expandingFragment != null && expandingFragment.isOpenend()) {
            expandingFragment.close();
        }
        // Todo: uncomment if need to transition to a detailed view of a moment on the second click,
        // Todo: otherwise delete this code
//        View view = v.findViewById(R.id.ivMedia);
//        Moment moment = mMomentList.get(vpMoment.getCurrentItem());
//        startInfoActivity(view, moment);
    }

    private class AutoPlayPagerAdapter extends ExpandingViewPagerAdapter {
        public AutoPlayPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mMoment = mMomentList.get(position);
            return AutoPlayFragment.newInstance(mMoment.getObjectId());
        }

        @Override
        public int getCount() {
            return mMomentList.size();
        }
    }
}
