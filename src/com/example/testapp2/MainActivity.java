package com.example.testapp2;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.*;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends MapActivity implements OnClickListener, OnSeekBarChangeListener {

	RadiusOverlay mRadiusOverlay;
	TestItemizedOverlay itemizedOverlay;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
        seekBar.setOnSeekBarChangeListener(this);
        
        ((TextView) findViewById(R.id.tvRadius)).setText("Radius: " + seekBar.getProgress());
        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(this);
        
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location == null) {
        	location = new Location(LocationManager.GPS_PROVIDER);
        	location.setLatitude(38.954444);
        	location.setLongitude(-77.346389);
        }
        GeoPoint center = new GeoPoint((int)(location.getLatitude() * 1E6),(int)(location.getLongitude() * 1E6));
        
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        MapController controller = mapView.getController();
        controller.setCenter(center);
        //controller.setCenter(new GeoPoint(20894722,-156470000));
        controller.setZoom(14);
        
        mRadiusOverlay = new RadiusOverlay(this, 1500);
        mapView.getOverlays().add(mRadiusOverlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public void onClick(View view) {
		switch(view.getId()) {
			case R.id.button1:
				Button button = (Button)view;
				button.setEnabled(false);
				
				MapView mapView = (MapView) findViewById(R.id.mapview);
				if(itemizedOverlay != null) {
					itemizedOverlay.clearOverlays();
				}
				mapView.postInvalidate();
			    
				class GetPageTask extends AsyncTask<String, Void, String> {
					
					private String readStream(InputStream in) {
						StringBuilder response = new StringBuilder("");
						try {
							int i;
							while((i = in.read()) != -1) {
								response.append((char)i);
							}
						} catch(IOException e) {
							e.printStackTrace();
						}
						return response.toString();
					}
					
					protected String doInBackground(String... strs) {
						URL url;
						HttpURLConnection urlConnection;
						try {
							url = new URL(strs[0]);
							urlConnection = (HttpURLConnection) url.openConnection();
						} catch(Exception e) {
							e.printStackTrace();
							
							return e.getMessage();
						}
						
						String str = "Stream not read";
						try {
							InputStream in = urlConnection.getInputStream();
							str = readStream(in);
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
						     urlConnection.disconnect();
						}
						return str;
					}

					@Override
					protected void onPostExecute(String result) {
						//AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
						//dialog.setTitle("Page Response");
						//dialog.setMessage(result);
						//dialog.show();
						
						JSONObject json = new JSONObject();
						try {
							json = new JSONObject(result);
						} catch(Exception e) {
							// Page did not return JSON response
							return;
						}
						
						JSONArray hotspots = new JSONArray();
						JSONObject metadata = new JSONObject();
						try {
							hotspots = json.getJSONArray("hotspots");
							metadata = json.getJSONObject("meta");
							
							AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
							dialog.setTitle("Showing " + hotspots.length() + " of " + metadata.getInt("count"));
							dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
								 public void onClick(DialogInterface dialog, int id) {
									 dialog.cancel();
								 }
							  });
							//dialog.setMessage("Total Records: " + metadata.getInt("count") + "\nUnique Species: " + metadata.getInt("unique"));
							dialog.show();
							
						} catch(Exception e) {
							// Invalid JSON response
							return;
						}
						
						
						
						MapView mapView = (MapView) findViewById(R.id.mapview);
						        
					    List<Overlay> mapOverlays = mapView.getOverlays();
					    Drawable drawable = MainActivity.this.getResources().getDrawable(R.drawable.red_dot);
				        itemizedOverlay = new TestItemizedOverlay(drawable, MainActivity.this);
						
					    int length = hotspots.length();
					    if(length < 1) {
					    	// no results were returned
					    	return;
					    }
					    try {
					    	for(int i = 0; i < length; i++) {
								JSONObject hotspot = hotspots.getJSONObject(i);
								JSONObject location = hotspot.getJSONObject("loc");
								JSONObject text = hotspot.getJSONObject("text");
								GeoPoint point = new GeoPoint((int)(location.getDouble("lat") * 1E6),(int)(location.getDouble("lon") * 1E6));
						        OverlayItem overlayitem = new OverlayItem(point, text.getString("title"), text.getString("description"));
						        
						        itemizedOverlay.addOverlay(overlayitem);
							}
							
							mapOverlays.add(itemizedOverlay);
							mapView.postInvalidate();
							Button button = (Button)findViewById(R.id.button1);
							button.setEnabled(true);
					    } catch(JSONException e) {
					    	AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
							dialog.setTitle("Error");
							dialog.setMessage(e.getMessage());
							dialog.show();
					    }
					}
					
					
				}
				
				GeoPoint point = ((MapView)findViewById(R.id.mapview)).getMapCenter();
				
				int radius = ((SeekBar)findViewById(R.id.seekBar1)).getProgress();
				
				String request = "http://iscdata.hipacdata.org/test.php?userId=1&radius=" + radius + "&lon=" + (point.getLongitudeE6() / 1E6) + "&lat=" + (point.getLatitudeE6() / 1E6);
				
				//AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				//dialog.setTitle("Data Request");
				//dialog.setMessage(request);
				//dialog.show();
				
				new GetPageTask().execute(request);
				
				break;
		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		TextView textView = (TextView) findViewById(R.id.tvRadius);
		textView.setText("Radius: " + progress);
		
		MapView mapView = ((MapView) findViewById(R.id.mapview));
		GeoPoint center = mapView.getMapCenter();
		
		mRadiusOverlay.mRadius = progress;
		
		mapView.postInvalidate();
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	
		
}
