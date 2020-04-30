package fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.R;
import com.hypersphere.what.Utils;

import views.ProjectCardRecyclerViewAdapter;
import views.ProjectCardViewHolder;


public class HomeFragment extends Fragment {

	RecyclerView recyclerView;

	ProjectCardViewHolder.ProjectPreviewClickListener projectPreviewClickListener;

	public HomeFragment(ProjectCardViewHolder.ProjectPreviewClickListener listener) {
		projectPreviewClickListener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, container, false);

		recyclerView = view.findViewById(R.id.home_recycler_view);
		recyclerView.hasFixedSize();
		GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
		recyclerView.setLayoutManager(gridLayoutManager);

		ProjectCardRecyclerViewAdapter adapter = new ProjectCardRecyclerViewAdapter(Utils.getProjects(100), projectPreviewClickListener);
		recyclerView.setAdapter(adapter);

		return view;
	}
}
