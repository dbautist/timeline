package com.codepath.timeline.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.codepath.timeline.R;
import com.codepath.timeline.adapters.MomentsHeaderAdapter;
import com.codepath.timeline.fragments.DetailDialogFragment;
import com.codepath.timeline.models.Moment;
import com.codepath.timeline.network.TimelineClient;
import com.codepath.timeline.util.AppConstants;
import com.codepath.timeline.view.ItemClickSupport;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimelineActivity extends AppCompatActivity {
    // TimelineActivity calls showDetailDialog() to generate DetailDialogFragment
    // DetailDialogFragment creates R.layout.fragment_moment_detail and ScreenSlidePagerAdapter

    static int SOURCE_MODE = 1;
    // 0: R.drawable.image_test2
    // 1: getIntent().getStringExtra("imageUrl")

    private static final String TAG = TimelineActivity.class.getSimpleName();
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsing_toolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rvMoments)
    RecyclerView rvMoments;
    @BindView(R.id.ivAutoPlay)
    ImageView ivAutoPlay;
    @BindView(R.id.addBtn)
    FloatingActionButton add;

    private List<Moment> mMomentList;
    private MomentsHeaderAdapter mAdapter;
    private int ADD_MOMENT_REQUEST_CODE = 6;
    private String storyObjectId;
    private String storyTitle;
    private String storyBackgroundImageUrl;
    private ScaleGestureDetector mScaleGestureDetector;
    private int pinch_zoom_index;
    GridLayoutManager gridLayoutManagerChat;
    LinearLayoutManager linearLayoutManagerDefault;
    GridLayoutManager gridLayoutManagerTwoColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get story info from intent
        // NOTE: Can't pass 'Story' since it's not Parcelable/Serializable
        storyObjectId = getIntent().getStringExtra(AppConstants.OBJECT_ID);
        storyTitle = getIntent().getStringExtra(AppConstants.STORY_TITLE);
        storyBackgroundImageUrl = getIntent().getStringExtra(AppConstants.STORY_BACKGROUND_IMAGE_URL);

        updateStoryInfo();
        initList();
    }

    private void updateStoryInfo() {
        // extract from the intent
        // load the image url for the background of the story into the image view
        if (SOURCE_MODE == 0) {
            collapsing_toolbar.setTitle("Baby Matthew Smith");
            collapsing_toolbar.setCollapsedTitleTextColor(Color.WHITE);
            Glide.with(this)
                    .load(R.drawable.image_test2)
                    .centerCrop()
                    .into(ivAutoPlay);
        } else if (SOURCE_MODE == 1) {
            collapsing_toolbar.setTitle(storyTitle);
            collapsing_toolbar.setCollapsedTitleTextColor(Color.WHITE);
            Glide.with(this)
                    .load(storyBackgroundImageUrl)
                    .centerCrop()
                    .into(ivAutoPlay);
        }
    }

    private void initList() {
        mMomentList = new ArrayList<>();
        mAdapter = new MomentsHeaderAdapter(this, mMomentList);

        gridLayoutManagerChat = new GridLayoutManager(this, 1); // 1
        linearLayoutManagerDefault = new GridLayoutManager(this, 1); // 2
        gridLayoutManagerTwoColumns = new GridLayoutManager(this, 2); // 3

        rvMoments.setLayoutManager(linearLayoutManagerDefault);
        pinch_zoom_index = 2;

        rvMoments.setAdapter(mAdapter);

        // Add the sticky headers decoration
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(mAdapter);
        rvMoments.addItemDecoration(headersDecor);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                headersDecor.invalidateHeaders();
            }
        });

        ItemClickSupport.addTo(rvMoments).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        showDetailDialog(position);
                    }
                });

        getMomentList();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Todo: add a new moment
                Intent intent = new Intent(TimelineActivity.this, NewMomentActivity.class);
                startActivityForResult(intent, ADD_MOMENT_REQUEST_CODE);
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(
                this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                // TODO: tune pinch zoom for demo
                if (detector.getCurrentSpan() > 200 && detector.getTimeDelta() > 200) {
                // if (true) {
                    if (detector.getCurrentSpan() - detector.getPreviousSpan() < -1) {
                        if (pinch_zoom_index == 1) {
                            rvMoments.setLayoutManager(linearLayoutManagerDefault);
                            pinch_zoom_index = 2;
                            return true;
                        } else if (pinch_zoom_index == 2) {
                            rvMoments.setLayoutManager(gridLayoutManagerTwoColumns);
                            pinch_zoom_index = 3;
                            return true;
                        }
                    } else if(detector.getCurrentSpan() - detector.getPreviousSpan() > 1) {
                        if (pinch_zoom_index == 3) {
                            rvMoments.setLayoutManager(linearLayoutManagerDefault);
                            pinch_zoom_index = 2;
                            return true;
                        } else if (pinch_zoom_index == 2) {
                            rvMoments.setLayoutManager(gridLayoutManagerChat);
                            pinch_zoom_index = 1;
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        rvMoments.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleGestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    private void getMomentList() {
        TimelineClient.getInstance().getMomentList(storyObjectId, new TimelineClient.TimelineClientGetMomentListListener() {
            @Override
            public void onGetMomentList(List<Moment> itemList) {
                mMomentList.addAll(itemList);
                mAdapter.notifyItemRangeInserted(0, mMomentList.size());
            }
        });
    }

    private void showDetailDialog(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DetailDialogFragment composeDialogFragment = DetailDialogFragment.newInstance(storyObjectId, position);
        composeDialogFragment.show(fragmentManager, "fragment_compose");
    }

    @OnClick(R.id.ivAutoPlay)
    public void onAutoPlay(View view) {
        // TEMPORARY PLACEHOLDER
        Intent intent = new Intent(TimelineActivity.this, AutoPlayActivity.class);
        intent.putExtra(AppConstants.OBJECT_ID, storyObjectId);
        intent.putExtra(AppConstants.STORY_TITLE, storyTitle);
        intent.putExtra(AppConstants.STORY_BACKGROUND_IMAGE_URL, storyBackgroundImageUrl);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == ADD_MOMENT_REQUEST_CODE && resultCode == 1) {
            // Get the URI that points to the selected contact
            Moment moment = Parcels.unwrap(data.getParcelableExtra("moment"));
            Log.d("DEBUG", moment.toString());
//            Snackbar.make(findViewById(android.R.id.content), moment.toString(), Snackbar.LENGTH_SHORT).show();
            if (moment != null) {
                addMoment(moment);
            }
        }
    }

    private void addMoment(Moment moment){
        // add to top
        // mMomentList.add(0, moment);

        mMomentList.add(moment);
        mAdapter.notifyItemInserted(0);

        // smooth scroll to bottom for now
        rvMoments.smoothScrollToPosition(mMomentList.size());
    }
}
