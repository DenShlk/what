package views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.ProjectEntry;
import com.hypersphere.what.R;

import java.util.List;

public class ProjectCardRecyclerViewAdapter extends RecyclerView.Adapter<ProjectCardViewHolder> {

	private List<ProjectEntry> projectList;
	private ProjectCardViewHolder.ProjectPreviewClickListener itemClickListener;

	public ProjectCardRecyclerViewAdapter(List<ProjectEntry> projectList, ProjectCardViewHolder.ProjectPreviewClickListener itemClickListener) {
		this.projectList = projectList;
		this.itemClickListener = itemClickListener;
	}

	@NonNull
	@Override
	public ProjectCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_card, parent, false);
		return new ProjectCardViewHolder(layoutView);
	}

	@Override
	public void onBindViewHolder(@NonNull ProjectCardViewHolder holder, int position) {
		holder.setProject(projectList.get(position));
		holder.setListener(itemClickListener);
	}

	@Override
	public int getItemCount() {
		return projectList.size();
	}
}
