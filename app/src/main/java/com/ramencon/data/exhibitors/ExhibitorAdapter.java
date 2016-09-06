package com.ramencon.data.exhibitors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ramencon.R;
import com.ramencon.models.ModelExhibitor;

import java.util.List;

public class ExhibitorAdapter extends BaseAdapter
{
	private LayoutInflater inflater = null;
	private Context context;

	private List<ModelExhibitor> list;

	public ExhibitorAdapter(Context context, List<ModelExhibitor> list)
	{
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;

		this.list = list;
	}

	@Override
	public int getCount()
	{
		return list.size();
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
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View listItemView = inflater.inflate(R.layout.exhibitor_listitem, null);



		return listItemView;
	}
}
