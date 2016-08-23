package com.codepath.timeline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.codepath.timeline.R;
import com.codepath.timeline.adapters.MomentsAdapter;
import com.codepath.timeline.fragments.DetailDialogFragment;
import com.codepath.timeline.models.Moment;
import com.codepath.timeline.models.Story;
import com.codepath.timeline.network.TimelineClient;
import com.codepath.timeline.util.view.ItemClickSupport;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimelineActivity extends AppCompatActivity {
    // TimelineActivity calls showDetailDialog() to generate DetailDialogFragment
    // DetailDialogFragment creates R.layout.fragment_moment_detail and ScreenSlidePagerAdapter

    private static final String TAG = TimelineActivity.class.getSimpleName();
    @BindView(R.id.rvMoments)
    RecyclerView rvMoments;
    @BindView(R.id.ivAutoPlay)
    ImageView ivAutoPlay;

    private List<Moment> mMomentList;
    private MomentsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);

        initList();
    }

    private void initList() {
        mMomentList = new ArrayList<>();
        mAdapter = new MomentsAdapter(this, mMomentList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvMoments.setLayoutManager(linearLayoutManager);
        rvMoments.setAdapter(mAdapter);

        ItemClickSupport.addTo(rvMoments).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        showDetailDialog(position);
                    }
                });

        // Todo: Use the story extracted from the intent
        // extract from the intent
        Story story = (Story) Parcels.unwrap(getIntent().getParcelableExtra("story"));
        Log.d("DEBUG", story.toString());

        // load the image url for the background of the story into the image view
        // Todo: Change drawable to the image loaded from the intent
        String imageUrl = getIntent().getStringExtra("imageUrl");
        Glide.with(this)
                .load(R.drawable.image_test2)
                .into(ivAutoPlay);

        getMomentList();
    }

    // TODO: Change the momentId when making network request
    private void getMomentList() {
        mMomentList.addAll(TimelineClient.getInstance().getMomentsList(this, -1));
        mAdapter.notifyItemRangeInserted(0, mMomentList.size());
    }

    private void showDetailDialog(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DetailDialogFragment composeDialogFragment = DetailDialogFragment.newInstance(mMomentList, position);
        composeDialogFragment.show(fragmentManager, "fragment_compose");
    }

    @OnClick(R.id.ivAutoPlay)
    public void onAutoPlay(View view) {
        // TEMPORARY PLACEHOLDER
        Intent intent = new Intent(TimelineActivity.this, AutoPlayActivity.class);
        startActivity(intent);
    }
}
