package com.ramencon.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.penoaks.helpers.DataLoadingFragment;
import com.penoaks.helpers.PersistentFragment;
import com.ramencon.R;
import com.ramencon.data.maps.MapsDataReceiver;
import com.ramencon.data.models.ModelMap;
import com.ramencon.ui.widget.TouchImageView;
import com.squareup.picasso.Picasso;

import org.lucasr.twowayview.TwoWayView;

import java.util.List;

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
	public View onPopulateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_maps, container, false);
	}

	@Override
	public void onDataLoaded(DataSnapshot dataSnapshot)
	{
		View root = getView();

		mTwoWayView = (TwoWayView) root.findViewById(R.id.maps_listview);
		mTwoWayView.setAdapter(new PagedListAdapater());

		mViewPager = (ViewPager) root.findViewById(R.id.maps_pager);
		mViewPager.setAdapter(new ViewPagerAdapter(getFragmentManager()));
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

				mTwoWayView.getChildAt(position).setBackgroundColor(0xFFff4081);

				if (mViewPager.getChildAt(position) != null)
					((TouchImageView) mViewPager.getChildAt(position).findViewById(R.id.maps_image)).resetZoom();
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{

			}
		});

		if (savedState != null)
		{
			mViewPager.onRestoreInstanceState( savedState.getParcelable("mViewPager") );
			mTwoWayView.onRestoreInstanceState( savedState.getParcelable("mTwoWayView") );
		}
	}

	@Override
	public void saveState(Bundle bundle)
	{
		if (getView() == null)
			return;

		bundle.putParcelable("mViewPager", mViewPager.onSaveInstanceState());
		bundle.putParcelable("mTwoWayView", mTwoWayView.onSaveInstanceState());
	}

	@Override
	public void loadState(Bundle bundle)
	{
		this.savedState = bundle;
	}

	@Override
	public void onCancelled(DatabaseError databaseError)
	{
		throw databaseError.toException();
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

		@Override
		public CharSequence getPageTitle(int position)
		{
			return "TAB " + (position + 1);
		}
	}

	public static class MapChildFragment extends Fragment
	{
		public static List<ModelMap> maps;

		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
		{
			View view = inflater.inflate(R.layout.fragment_maps_child, container, false);

			assert getArguments() != null;

			final TouchImageView image = (TouchImageView) view.findViewById(R.id.maps_image);

			HomeActivity.storageReference.child("images/maps/" + maps.get(getArguments().getInt("index")).image).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>()
			{
				@Override
				public void onComplete(@NonNull Task<Uri> task)
				{
					if (task.isSuccessful())
						Picasso.with(getContext()).load(task.getResult()).into(image);
					else
						image.setImageResource(R.drawable.error);
				}
			});

			return view;
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
