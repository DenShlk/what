package com.hypersphere.what.views;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;
import com.hypersphere.what.R;

import java.util.ArrayList;
import java.util.List;

public class TimePointAdapter extends RecyclerView.Adapter<TimePointAdapter.TimePointHolder> {

    private RecyclerView.Adapter mainAdapter;
    private List<TimePointState> points = new ArrayList<>();
    private List<TimePointHolder> holders = new ArrayList<>();
    private int itemWidth = 0;
    private double centerX;
    private TimepointCallback callback;

    public void setMainAdapter(RecyclerView.Adapter mainAdapter) {
        this.mainAdapter = mainAdapter;

        for (int i = 0; i < 4; i++) {
            points.add(TimePointState.STATE_EMPTY);
        }
        notifyItemRangeInserted(0, 4);
    }

    public void setCallback(TimepointCallback callback){
        this.callback = callback;
    }

    @NonNull
    @Override
    public TimePointHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timeline_item, parent, false);


        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.width = (int) (parent.getMeasuredWidth() * 0.2);
        itemView.setLayoutParams(layoutParams);

        itemWidth = layoutParams.width;
        centerX = (int) (itemWidth * 2.5);

        TimePointHolder holder = new TimePointHolder(itemView, TimePointState.values()[viewType]);
        holders.add(holder);
        return holder;
    }

    public int getContentWidth(){
        return itemWidth * getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return points.get(position).ordinal();
    }

    @Override
    public void onBindViewHolder(@NonNull TimePointHolder holder, int position) {
        if (position >= 2 && position < points.size() - 2)
            holder.setLineType(TimelineView.getTimeLineViewType(position - 2, getItemCount() - 4));
    }

    public void newItem(TimePointState state) {
        points.add(points.size() - 2, state);
        notifyItemInserted(points.size() - 3);
    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    public enum TimePointState {
        STATE_EMPTY,
        STATE_COMPLETE,
        STATE_FUTURE,
    }

    public void centerMoved(int scroll){
        centerX = itemWidth * 2.5 + scroll;
        Log.d("center", scroll+" "+centerX);
        for (int i = 0; i < holders.size(); i++) {
            double x = itemWidth * 0.5 + itemWidth * i;
            double d = Math.min(Math.abs(x - centerX) / itemWidth, 1);
            holders.get(i).setCenterDistance(d);
        }
    }

    public class TimePointHolder extends RecyclerView.ViewHolder {

        TimelineView timelineView;
        int position;

        public void setPosition(int position) {
            this.position = position;
        }

        public TimePointHolder(@NonNull View itemView, TimePointState state) {
            super(itemView);

            timelineView = itemView.findViewById(R.id.timeline_view);
            switch (state) {
                case STATE_EMPTY:
                    timelineView.setVisibility(View.INVISIBLE);
                    break;
                case STATE_FUTURE:
                    timelineView.setMarker(timelineView.getContext().getDrawable(R.drawable.ic_circle_in_circle_24dp));
                    timelineView.setMarkerColor(timelineView.getContext().getColor(R.color.colorNotComplete));
                    timelineView.setLineStyle(TimelineView.LineStyle.DASHED);
                    timelineView.setMarkerColor(Color.GRAY);
                    break;
                case STATE_COMPLETE:
                    timelineView.setMarker(timelineView.getContext().getDrawable(R.drawable.ic_check_circle_black_24dp));
                    timelineView.setMarkerColor(timelineView.getContext().getColor(R.color.colorComplete));
                    break;
            }
            timelineView.setMarkerSize((int) timelineView.getResources().getDimension(R.dimen.marker_normal));

            timelineView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(callback!=null)
                        callback.moveTo(position);
                }
            });
        }

        void setLineType(int type) {
            timelineView.initLine(type);
        }

        public void setCenterDistance(double d){
            float max = timelineView.getResources().getDimension(R.dimen.marker_focused);
            float min = timelineView.getResources().getDimension(R.dimen.marker_normal);
            timelineView.setMarkerSize((int) (max - d * (max - min)));
        }
    }

    public interface TimepointCallback {
        public void moveTo(int position);
    }
}
