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

    public SensorHandler(StarwispActivity c, StarwispBuilder b) {
        m_Context=c;
        m_Builder=b;
        m_SensorManager = (SensorManager)c.getSystemService(Context.SENSOR_SERVICE);
        m_Sensors =  m_SensorManager.getSensorList(Sensor.TYPE_ALL);

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

    public void onSensorChanged(SensorEvent event) {
        String name = event.sensor.getName();
        String type = String.valueOf(event.sensor.getType());
        String args = "(\""+name+"\" "+type+" "+
            String.valueOf(event.accuracy)+" "+
            String.valueOf(event.timestamp)+" ";

        for (int i=0; i<event.values.length; i++) {
            args+=String.valueOf(event.values[i])+" ";
        }
        args +=")";

        m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,args);
    }
}
