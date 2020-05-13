package com.hypersphere.what.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.R;

import java.util.ArrayList;
import java.util.List;

public class ProgressStepAdapter extends RecyclerView.Adapter<ProgressStepAdapter.ProgressStepHolder> {
    List<Integer> cards = new ArrayList<>();
    TimePointAdapter timeAdapter;
    private int itemWidth = 0;

    public void attachTimelineAdapter(TimePointAdapter adapter){
        timeAdapter = adapter;
        adapter.setMainAdapter(ProgressStepAdapter.this);
    }

    @NonNull
    @Override
    public ProgressStepHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.progress_step_card, parent, false);

        itemWidth = parent.getMeasuredWidth();

        return new ProgressStepHolder(itemView);
    }

    public int getContentWidth(){
        return itemWidth * getItemCount();
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressStepHolder holder, int position) {
        holder.setNumber(position);
    }

    public void addCard(){
        cards.add(cards.size() + 1);
        notifyItemInserted(cards.size() - 1);
        if(cards.size() < 4)
            timeAdapter.newItem(TimePointAdapter.TimePointState.STATE_COMPLETE);
        else
            timeAdapter.newItem(TimePointAdapter.TimePointState.STATE_FUTURE);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public class ProgressStepHolder extends RecyclerView.ViewHolder {

        TextView content;
        public ProgressStepHolder(@NonNull View itemView) {
            super(itemView);

            content = itemView.findViewById(R.id.card_text);
        }

        void setNumber(int n){
            content.setText("Card " + n);
        }
    }
}
