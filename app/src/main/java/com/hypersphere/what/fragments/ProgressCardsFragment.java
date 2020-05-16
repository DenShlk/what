package com.hypersphere.what.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.R;
import com.hypersphere.what.views.ProgressStepAdapter;
import com.hypersphere.what.views.TimePointAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProgressCardsFragment extends Fragment {

	int timelineRecyclerScroll = 0;
	int contentRecyclerScroll = 0;
	RecyclerView lastDraggedRecycler = null;
	View mView;

	public ProgressCardsFragment() {

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.fragment_progress_cards, container, false);

		final RecyclerView recycler = mView.findViewById(R.id.recycler_view);
		recycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
		final ProgressStepAdapter adapter = new ProgressStepAdapter();
		recycler.setAdapter(adapter);
		final PagerSnapHelper snapHelper = new PagerSnapHelper();
		snapHelper.attachToRecyclerView(recycler);

		final RecyclerView timelineView = mView.findViewById(R.id.timeline_recycler);
		timelineView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
		final TimePointAdapter timelineAdapter = new TimePointAdapter();
		adapter.attachTimelineAdapter(timelineAdapter);
		timelineView.setAdapter(timelineAdapter);
		LinearSnapHelper centerHelper = new LinearSnapHelper();
		centerHelper.attachToRecyclerView(timelineView);

		recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
					lastDraggedRecycler = recyclerView;
					// timelineView.smoothScrollBy(timelineView.getWidth() / 5 * direction, 0, new DecelerateInterpolator());
				}
			}

			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				contentRecyclerScroll+=dx;
				if(lastDraggedRecycler == recyclerView) {
					double k = 1.0 * (timelineAdapter.getContentWidth() - timelineView.getWidth()) / (adapter.getContentWidth() - recyclerView.getWidth());
					timelineView.scrollBy((int) (contentRecyclerScroll * k - timelineRecyclerScroll), 0);
				}
			}
		});

		timelineView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				Log.d("scrollstate", String.valueOf(newState));
				lastDraggedRecycler = recyclerView;
			}

			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				timelineRecyclerScroll+=dx;
				timelineAdapter.centerMoved(timelineRecyclerScroll);

				if(lastDraggedRecycler == recyclerView) {
					double k = 1.0 * (timelineAdapter.getContentWidth() - timelineView.getWidth()) / (adapter.getContentWidth() - recyclerView.getWidth());
					recycler.scrollBy((int) (timelineRecyclerScroll / k - contentRecyclerScroll), 0);
				}


			}
		});

		for (int i = 0; i < 10; i++) {
			adapter.addCard();
		}

		return mView;
	}

}
