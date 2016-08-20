package com.codepath.timeline.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.codepath.timeline.R;
import com.codepath.timeline.adapters.MomentsAdapter;
import com.codepath.timeline.fragments.DetailDialogFragment;
import com.codepath.timeline.models.Moment;
import com.codepath.timeline.util.MockResponseGenerator;
import com.codepath.timeline.util.view.ItemClickSupport;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimelineActivity extends AppCompatActivity {

  private static final String TAG = TimelineActivity.class.getSimpleName();
  @BindView(R.id.rvMoments)
  RecyclerView rvMoments;

  private List<Moment> mMomentList;
  private MomentsAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_timeline);
    ButterKnife.bind(this);

    initList();
  }

  private void initList(){
    mMomentList = MockResponseGenerator.getInstance().getMomentList();
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

  }

  private void showDetailDialog(int position) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    DetailDialogFragment composeDialogFragment = DetailDialogFragment.newInstance(position);
    composeDialogFragment.show(fragmentManager, "fragment_compose");
  }
}
