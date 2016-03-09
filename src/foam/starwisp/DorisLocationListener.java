// Starwisp Copyright (C) 2013 Dave Griffiths
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package foam.starwisp;

import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class DorisLocationListener implements LocationListener {

	protected static final int ONE_MINUTE = 60 * 1000;
	protected static final int FIVE_MINUTES = 5 * ONE_MINUTE;
	protected static final int ACCURACY_THRESHOLD = 30; // in meters
	protected MapView mapView;
	protected MapController mapController;
	protected LocationManager locationManager;
	protected Location currentLocation;
    String m_CallbackName;
    StarwispActivity m_Context;
    StarwispBuilder m_Builder;
    int m_MinMSecs;
    int m_MinMetres;

    public DorisLocationListener(LocationManager lm) {
		locationManager = lm;
    }

    public void Start(StarwispActivity c, String name, StarwispBuilder b, int min_millisecs, int min_metres) {
        m_CallbackName=name;
        m_Context=c;
        m_Builder=b;
        m_MinMSecs = min_millisecs;
        m_MinMetres = min_metres;
        setDeviceLocation();
    }

	protected void locationChanged(double latitude, double longitude) {
        m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,"("+String.valueOf(latitude)+" "+String.valueOf(longitude)+")");
	}

	protected void setDeviceLocation() {

		Location lastNetLocation = null;
		Location lastGpsLocation = null;

		boolean netAvailable = locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		boolean gpsAvailable = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!netAvailable && !gpsAvailable) {

            Log.i("DORIS","NO GPS or Net");

/*			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.location_disabled))
					.setMessage(getString(R.string.location_reenable))
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									startActivity(new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
								}
							})
					.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
                                }).create().show();*/
		}
		if (netAvailable) {
			lastNetLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (gpsAvailable) {
			lastGpsLocation = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		setBestLocation(lastNetLocation, lastGpsLocation);
		// If chosen location is more than a minute old, start querying
		// network/GPS
        if (netAvailable) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, m_MinMSecs, m_MinMetres, this);
			}
        if (gpsAvailable) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, m_MinMSecs, m_MinMetres, this);
        }
	}

	public void startLocating() {
        setDeviceLocation();
    }

	public void stopLocating() {
		if (locationManager != null) {
			try {
				locationManager.removeUpdates(this);
			} catch (Exception ex) {
				Log.e(getClass().getSimpleName(), "stopLocating", ex);
			}
			locationManager = null;
		}
	}



	/**
	 * Convert latitude and longitude to a GeoPoint
	 *
	 * @param latitude
	 *            Latitude
	 * @param longitude
	 *            Lingitude
	 * @return GeoPoint
	 */
	protected GeoPoint getPoint(double latitude, double longitude) {
		return (new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6)));
	}

	protected void setBestLocation(Location location1, Location location2) {
		if (location1 != null && location2 != null) {
			boolean location1Newer = location1.getTime() - location2.getTime() > FIVE_MINUTES;
			boolean location2Newer = location2.getTime() - location1.getTime() > FIVE_MINUTES;
			boolean location1MoreAccurate = location1.getAccuracy() < location2
					.getAccuracy();
			boolean location2MoreAccurate = location2.getAccuracy() < location1
					.getAccuracy();
			if (location1Newer || location1MoreAccurate) {
				locationChanged(location1.getLatitude(),
						location1.getLongitude());
			} else if (location2Newer || location2MoreAccurate) {
				locationChanged(location2.getLatitude(),
						location2.getLongitude());
			}
		} else if (location1 != null) {
			locationChanged(location1.getLatitude(), location1.getLongitude());
		} else if (location2 != null) {
			locationChanged(location2.getLatitude(), location2.getLongitude());
		}
	}

	public void onLocationChanged(Location location) {
		if (location != null) {
            currentLocation = location;
			locationChanged(location.getLatitude(), location.getLongitude());
/*			if (location.hasAccuracy()
					&& location.getAccuracy() < ACCURACY_THRESHOLD) {
				// accuracy is within ACCURACY_THRESHOLD, de-activate location
				// detection
				stopLocating();
                }*/
		}
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
