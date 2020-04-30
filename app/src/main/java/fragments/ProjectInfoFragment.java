package fragments;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hypersphere.what.CloudManager;
import com.hypersphere.what.ProjectEntry;
import com.hypersphere.what.R;
import com.hypersphere.what.Utils;

import views.GalleryRecyclerAdapter;


public class ProjectInfoFragment extends Fragment {

	View mView;

	private TextView infoTitle, infoMoney, infoDescription;
	private RecyclerView infoGallery;

	private GalleryRecyclerAdapter infoGalleryAdapter;

	public ProjectInfoFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		mView =inflater.inflate(R.layout.fragment_project_info, container, false);

		infoTitle = mView.findViewById(R.id.info_title_text);
		infoMoney = mView.findViewById(R.id.info_money_text);
		infoDescription = mView.findViewById(R.id.info_description);
		infoGallery = mView.findViewById(R.id.info_image_recycler);

		infoGallery.setHasFixedSize(true);
		infoGallery.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
		infoGalleryAdapter = new GalleryRecyclerAdapter(getActivity());
		infoGallery.setAdapter(infoGalleryAdapter);

		return mView;
	}

	public void fillInfo(ProjectEntry project){
		infoTitle.setText(project.title);
		infoDescription.setText(project.description);
		infoMoney.setText(Utils.getProgressString(project.donationsCollected, project.donationsGoal));

		infoGalleryAdapter.clear();
		for(String src : project.images){
			CloudManager.loadImage(src, new CloudManager.OnDownloadListener<Bitmap>() {
				@Override
				public void onComplete(Bitmap data) {
					infoGalleryAdapter.addImage(data);
				}
			});
		}
	}

}
