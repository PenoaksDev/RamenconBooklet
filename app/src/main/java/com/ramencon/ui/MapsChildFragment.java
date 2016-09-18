package com.ramencon.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.penoaks.log.PLog;
import com.ramencon.R;
import com.ramencon.data.models.ModelMap;
import com.ramencon.ui.widget.TouchImageView;

import org.acra.ACRA;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsChildFragment extends Fragment implements ProgressCallback, FutureCallback<Bitmap>
{
	static Map<String, Bitmap> resolvedImages = new HashMap<>();

	public static List<ModelMap> maps;
	private ProgressBar progressBar = null;
	private TouchImageView image;
	private ModelMap map;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		assert getArguments() != null;
		map = maps.get(getArguments().getInt("index"));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_maps_child, container, false);

		image = (TouchImageView) view.findViewById(R.id.maps_image);
		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar_map);
		progressBar.setVisibility(View.VISIBLE);

		image.setMaxZoom(4);
		image.setScaleType(ImageView.ScaleType.MATRIX);

		if (map.image == null)
			image.setImageResource(R.drawable.noimagefound);
		else if (resolvedImages.containsKey(map.id))
		{
			PLog.i("Setting image from cache for " + map.id);
			image.setImageBitmap(resolvedImages.get(map.id));
		}
		else
		{
			PLog.i("Resolving image URI for " + map.id);
			HomeActivity.storageReference.child("images/maps/" + map.image).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>()
			{
				@Override
				public void onComplete(Task<Uri> task)
				{
					if (task.isSuccessful())
					{
						PLog.i("Setting image from resolved uri for " + map.id);
						Ion.with(MapsChildFragment.this).load(task.getResult().toString()).progressHandler(MapsChildFragment.this).asBitmap().setCallback(MapsChildFragment.this);
					}
					else
					{
						PLog.e("Failed to loading image from Google Firebase [images/maps/" + map.image + "]");
						image.setImageResource(R.drawable.errored_white);
					}
				}
			});
		}

		return view;
	}

	@Override
	public void onProgress(long downloaded, long total)
	{
		if (progressBar != null)
		{
			progressBar.setMax((int) total);
			progressBar.setProgress((int) downloaded);
		}
	}

	@Override
	public void onCompleted(Exception e, Bitmap result)
	{
		if (e != null)
		{
			ACRA.getErrorReporter().handleException(new RuntimeException("Recoverable Exception", e));
			Toast.makeText(getContext(), "We had a problem loading the map. The problem was reported to the developer.", Toast.LENGTH_LONG).show();
		}

		if (result != null)
		{
			resolvedImages.put(map.id, result);
			image.setImageBitmap(result);
		}

		PLog.i("Showing map " + map.id);

		progressBar.setProgress(0);
		progressBar.setVisibility(View.GONE);
	}
}
