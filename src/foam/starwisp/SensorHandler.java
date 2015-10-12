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

import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import java.util.List;
import android.content.Context;
import android.util.Log;

class SensorHandler implements SensorEventListener  {
    String m_CallbackName;
    StarwispActivity m_Context;
    StarwispBuilder m_Builder;

    private final SensorManager m_SensorManager;
    private final List<Sensor> m_Sensors;

    // need to keep track of these to determine the orientation
    float[] m_Gravity;
    float[] m_Geomagnetic;
    float[] m_Orientation;

    float m_Jerk;

    public SensorHandler(StarwispActivity c, StarwispBuilder b) {
        m_Context=c;
        m_Builder=b;
        m_SensorManager = (SensorManager)c.getSystemService(Context.SENSOR_SERVICE);
        m_Sensors =  m_SensorManager.getSensorList(Sensor.TYPE_ALL);
        m_Orientation = new float[3];
        m_Jerk=0;

        //mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void GetSensors(StarwispActivity c, String cbname, StarwispBuilder b) {
        String args = "(";
        for (int i=0; i<m_Sensors.size(); i++) {
            String name = m_Sensors.get(i).getName();
            Log.i("starwisp",name);
            String type = String.valueOf(m_Sensors.get(i).getType());
            args += "(\""+name+"\" "+type+") ";
        }
        args+=")";
        b.DialogCallback(c,c.m_Name,cbname,args);
    }

    public void StartSensors(StarwispActivity c, String cbname, StarwispBuilder b, List<Integer> requested) {
        m_Context=c;
        m_Builder=b;
        m_CallbackName = cbname;

        // clear any existing listeners
        StopSensors();

        for (int i=0; i<m_Sensors.size(); i++) {
            Sensor sensor = m_Sensors.get(i);
            int sensor_type = sensor.getType();
            boolean found = false;

            for (int j=0; j<requested.size(); j++) {
                if (requested.get(j)==sensor_type) found=true;
            }

            if (found) {
                Log.i("starwisp","starting sensor "+sensor.getName());
                // todo choose which are started up...
                m_SensorManager.registerListener(this, m_Sensors.get(i), SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    public void StopSensors() {
        m_SensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public boolean updateOrientation(SensorEvent event) {
        // calculate orientation in the new manner
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            m_Gravity = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            m_Geomagnetic = event.values;
            if (m_Gravity != null && m_Geomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, m_Gravity, m_Geomagnetic);
                if (success) {
                    SensorManager.getOrientation(R, m_Orientation);
                }
                return true;
            }
        }
        return false;
    }

    public String buildSensorString(String name, String type, SensorEvent event, float[] values) {
        String args = "(\""+name+"\" "+type+" "+
            String.valueOf(event.accuracy)+" "+
            String.valueOf(event.timestamp)+" ";

        for (int i=0; i<values.length; i++) {
            args+=String.valueOf(values[i])+" ";
        }
        args +=")";
        return args;
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            // this sensor is depreciated
            return;
        }

        String args;
        // check for calculated orientation...
        if (updateOrientation(event)) {
            // piggyback on type_orientation for the time being
            args = buildSensorString("calculated orientation",
                                     String.valueOf(Sensor.TYPE_ORIENTATION),
                                     event,
                                     m_Orientation);

            m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,args);
        }

        // build it normally from the sensor data
        args = buildSensorString(event.sensor.getName(),
                                 String.valueOf(event.sensor.getType()),
                                 event,
                                 event.values);

        m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,args);
    }
}
