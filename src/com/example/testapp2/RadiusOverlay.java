package com.example.testapp2;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;


public class RadiusOverlay extends Overlay {

	Context mContext;
	float mRadius;
	
	public RadiusOverlay(Context _context, int _radius) {
		mContext = _context;
		mRadius = _radius;
	}
	
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas,  mapView, shadow);
		
		Projection projection = mapView.getProjection();
		Point pt = new Point();
		GeoPoint geo = mapView.getMapCenter();
		
		projection.toPixels(geo ,pt);
		float circleRadius = projection.metersToEquatorPixels(mRadius);
		
		Paint innerCirclePaint = new Paint();
		innerCirclePaint.setColor(Color.GREEN);
		innerCirclePaint.setAlpha(25);
		innerCirclePaint.setAntiAlias(true);
		innerCirclePaint.setStyle(Paint.Style.FILL);
		
		canvas.drawCircle((float)pt.x, (float)pt.y, circleRadius, innerCirclePaint);
	}
}
