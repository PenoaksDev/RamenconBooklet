package com.ramencon.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.ramencon.data.DataLoadingFragment;
import com.penoaks.fragments.PersistentFragment;
import com.penoaks.log.PLog;
import com.penoaks.sepher.ConfigurationSection;
import com.ramencon.R;
import com.ramencon.data.maps.MapsDataReceiver;
import com.ramencon.data.models.ModelMap;
import com.ramencon.ui.widget.TouchImageView;

import org.lucasr.twowayview.TwoWayView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsFragment extends DataLoadingFragment implements PersistentFragment
{
	private static MapsFragment instance = null;

	public static MapsFragment instance()
	{
		if (instance == null)
			instance = new MapsFragment();
		return instance;
	}

	public MapsDataReceiver receiver = new MapsDataReceiver();
	private ViewPager mViewPager;
	private TwoWayView mTwoWayView;
	private Bundle savedState = null;
	private int selectedPosition = 0;

	public MapsFragment()
	{
		instance = this;
		setReceiver(receiver);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getActivity().setTitle("Maps and Locations");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_maps, container, false);
	}

	@Override
	public void onDataReceived(ConfigurationSection data, boolean isUpdate)
	{
		if (isUpdate)
			return;

		View root = getView();

		if (savedState != null)
			selectedPosition = savedState.getInt("selectedPosition", 0);

		mTwoWayView = (TwoWayView) root.findViewById(R.id.maps_listview);
		mTwoWayView.setAdapter(new PagedListAdapater());

		mViewPager = (ViewPager) root.findViewById(R.id.maps_pager);
		mViewPager.setAdapter(new ViewPagerAdapter(getFragmentManager()));
		mViewPager.setCurrentItem(selectedPosition);
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{

			}

			@Override
			public void onPageSelected(int position)
			{
				for (int i = 0; i < mTwoWayView.getChildCount(); i++)
					mTwoWayView.getChildAt(i).setBackgroundColor(0x00000000);
				if (mTwoWayView.getChildAt(position) != null)
					mTwoWayView.getChildAt(position).setBackgroundColor(0xFFff4081);
				if (mViewPager.getChildAt(position) != null)
					((TouchImageView) mViewPager.getChildAt(position).findViewById(R.id.maps_image)).resetZoom();
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{

			}
		});
	}

	@Override
	public void saveState(Bundle bundle)
	{
		if (getView() == null)
			return;

		bundle.putInt("selectedPosition", mViewPager.getCurrentItem());
	}

	@Override
	public void loadState(Bundle bundle)
	{
		this.savedState = bundle;
	}

	public class ViewPagerAdapter extends FragmentStatePagerAdapter
	{
		public ViewPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			MapChildFragment.maps = receiver.maps;

			Bundle bundle = new Bundle();
			bundle.putInt("index", position);

			MapChildFragment frag = new MapChildFragment();
			frag.setArguments(bundle);
			return frag;
		}

		@Override
		public int getCount()
		{
			return receiver.maps.size();
		}
	}

	public static class MapChildFragment extends Fragment implements ProgressCallback, FutureCallback<ImageView>
	{
		public static List<ModelMap> maps;
		private Map<String, String> resolvedImages = new HashMap<>();
		private ProgressBar progressBar = null;

		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
		{
			View view = inflater.inflate(R.layout.fragment_maps_child, container, false);

			assert getArguments() != null;

			final TouchImageView image = (TouchImageView) view.findViewById(R.id.maps_image);
			progressBar = (ProgressBar) view.findViewById(R.id.progress_bar_map);
			progressBar.setVisibility(View.VISIBLE);

			final ModelMap map = maps.get(getArguments().getInt("index"));

			if (map.image == null)
				image.setImageResource(R.drawable.noimagefound);
			else if (resolvedImages.containsKey(map.id))
				Ion.with(this).load(resolvedImages.get(map.id)).progressHandler(this).intoImageView(image).setCallback(this);
				// Picasso.with(getContext()).load(resolvedImages.get(map.id)).placeholder(R.drawable.image_loading).into(image);
			else
				HomeActivity.storageReference.child("images/maps/" + map.image).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>()
				{
					@Override
					public void onComplete(@NonNull Task<Uri> task)
					{
						if (task.isSuccessful())
						{
							resolvedImages.put(map.id, task.getResult().toString());
							Ion.with(getContext()).load(task.getResult().toString()).progressHandler(MapChildFragment.this).intoImageView(image).setCallback(MapChildFragment.this);
							// Picasso.with(getContext()).load(task.getResult()).placeholder(R.drawable.image_loading).into(image);
						}
						else
						{
							PLog.e("Failed to loading image from Google Firebase [images/maps/" + map.image + "]");
							image.setImageResource(R.drawable.errored_white);
						}
					}
				});

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

			PLog.i("Downloaded " + downloaded + " of " + total);
		}

		@Override
		public void onCompleted(Exception e, ImageView result)
		{
			progressBar.setProgress(0);
			progressBar.setVisibility(View.GONE);
		}
	}

	public class PagedListAdapater extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return receiver.maps.size();
		}

		@Override
		public Object getItem(int position)
		{
			return position;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			TextView tv = new TextView(getContext());
			tv.setText(receiver.maps.get(position).title);
			tv.setPadding(18, 6, 18, 6);
			tv.setTextColor(0xffffffff);
			tv.setGravity(Gravity.CENTER);
			tv.setLayoutParams(new TwoWayView.LayoutParams(ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT));

			if (position == selectedPosition)
				tv.setBackgroundColor(0xFFff4081);

			tv.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					ViewGroup parent = (ViewGroup) v.getParent();
					for (int i = 0; i < parent.getChildCount(); i++)
						parent.getChildAt(i).setBackgroundColor(0x00000000);

					v.setBackgroundColor(0xFFff4081);

					mViewPager.setCurrentItem(position);

					if (mViewPager.getChildAt(position) != null)
						((TouchImageView) mViewPager.getChildAt(position).findViewById(R.id.maps_image)).resetZoom();
				}
			});

			tv.setOnLongClickListener(new View.OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					savedState = new Bundle();
					saveState(savedState);

					MapsFragment.this.refreshData();

					Toast.makeText(getContext(), "Maps Refreshed", Toast.LENGTH_LONG).show();

					return true;
				}
			});

			return tv;
		}
	}
}
