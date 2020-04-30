package fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.R;

import views.ProjectCardRecyclerViewAdapter;
import views.ProjectCardViewHolder;

import static com.hypersphere.what.Utils.getProjects;

public class ProfileFragment extends Fragment {


	private RecyclerView recyclerView;

	private ProjectCardViewHolder.ProjectPreviewClickListener projectPreviewClickListener;

	public ProfileFragment(ProjectCardViewHolder.ProjectPreviewClickListener listener) {
		projectPreviewClickListener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profile, container, false);

		recyclerView = view.findViewById(R.id.profile_recycler_view);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		//GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
		recyclerView.setLayoutManager(linearLayoutManager);

		ProjectCardRecyclerViewAdapter adapter = new ProjectCardRecyclerViewAdapter(getProjects(0), projectPreviewClickListener);
		recyclerView.setAdapter(adapter);

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getContext(), "clk", Toast.LENGTH_SHORT).show();
			}
		});

		return view;
	}

}
