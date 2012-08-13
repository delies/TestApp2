package com.example.testapp2;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;



public class TestItemizedOverlay extends ItemizedOverlay {


	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	
	public TestItemizedOverlay(Drawable defaultMarker) {
		super(boundCenter(defaultMarker));
	}

	public TestItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenter(defaultMarker));
		  mContext = context;
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	void clearOverlays() {
		if(!mOverlays.isEmpty()) {
			mOverlays.clear();
		}
		
	}
	
	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  TextView tv = (TextView) ((Activity)mContext).findViewById(R.layout.alert_title);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
		 public void onClick(DialogInterface dialog, int id) {
			 dialog.cancel();
		 }
	  });
	  dialog.show();
	  return true;
	}
	
}
