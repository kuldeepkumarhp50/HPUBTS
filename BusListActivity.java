package com.hpubts50.hpubustrackerserver;

import com.hpubts50.hpubustrackerserver.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BusListActivity extends ListActivity implements OnClickListener {
	private String busname;
	private int busposition;
	Button btn_next;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_list);

		// Initialize views
		InitializeView();

		// Setting List Adapter for custom list
		setListAdapter(new MyBusListAdapter(this, android.R.layout.simple_list_item_single_choice, R.id.txtlist_name, getResources().getStringArray(R.array.HPU_Buses)));
	}

	private void InitializeView() {
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_next.setOnClickListener(this);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		// super.onListItemClick(l, v, position, id);
		busname = (String) getListAdapter().getItem(position);
		busposition = position;
		btn_next.setEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bus_list, menu);
		return true;
	}

	// Custom Adapter inner class for custom list

	private class MyBusListAdapter extends ArrayAdapter<String> {

		public MyBusListAdapter(Context context, int resource, int textViewResourceId, String[] strings) {
			super(context, resource, textViewResourceId, strings);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.e("MYTAG", "one");
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(R.layout.bus_item, parent, false);
			String[] items = getResources().getStringArray(R.array.HPU_Buses);
			Log.e("MYTAG", "two");

			ImageView imglist_icon = (ImageView) row.findViewById(R.id.imglist_icon);
			TextView txtlist_name = (TextView) row.findViewById(R.id.txtlist_name);
			// RadioButton radio_list = (RadioButton)
			// row.findViewById(R.id.radio_list);
			Log.e("MYTAG", "three");

			txtlist_name.setText(items[position]);
			Log.e("MYTAG", "four");

			if (items[position].equals("Airavat")) {
				imglist_icon.setImageResource(R.drawable.airavat);
			} else if (items[position].equals("Alakananda")) {
				imglist_icon.setImageResource(R.drawable.alaknanda);
			} else if (items[position].equals("Alakananda")) {
				imglist_icon.setImageResource(R.drawable.alaknanda);
			} else if (items[position].equals("Chaitanya")) {
				imglist_icon.setImageResource(R.drawable.chaitanya);
			} else if (items[position].equals("Garud")) {
				imglist_icon.setImageResource(R.drawable.garud);
			} else if (items[position].equals("Nandi")) {
				imglist_icon.setImageResource(R.drawable.nandi);
			} else if (items[position].equals("Neela")) {
				imglist_icon.setImageResource(R.drawable.neela);
			} else if (items[position].equals("Pushpak")) {
				imglist_icon.setImageResource(R.drawable.pushpak);
			}
			Log.e("MYTAG", "five");

			return row;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			Intent imain = new Intent(BusListActivity.this, MainActivity.class);
			Bundle BusInfo = new Bundle();
			BusInfo.putString("BusName", busname);
			BusInfo.putInt("BusPosition", busposition);
			imain.putExtras(BusInfo);
			startActivity(imain);
			finish();
			break;

		}

	}
}
