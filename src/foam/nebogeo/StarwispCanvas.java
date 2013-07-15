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

package foam.nebogeo;

import android.content.Context;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONArray;

class StarwispCanvas extends SurfaceView implements SurfaceHolder.Callback {

    StarwispCanvasThread m_Thread;
    JSONArray m_Drawlist;

    public StarwispCanvas(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public void SetDrawList(JSONArray s) { m_Drawlist=s; }

    public Paint getPaint(Canvas canvas, JSONArray prim)
    {
        try {
            Paint myPaint = new Paint();
            JSONArray c = prim.getJSONArray(1);
            myPaint.setColor(Color.rgb(c.getInt(0), c.getInt(1), c.getInt(2)));
            myPaint.setStrokeWidth(prim.getInt(2));
            return myPaint;
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
            return null;
        }
    }

    public void DrawLine(Canvas canvas, JSONArray prim) {
        try {
            JSONArray v = prim.getJSONArray(3);
            canvas.drawLine(v.getInt(0),
                            v.getInt(1),
                            v.getInt(2),
                            v.getInt(3),
                            getPaint(canvas,prim));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        try {
            for (int i=0; i<m_Drawlist.length(); i++) {
                JSONArray prim = m_Drawlist.getJSONArray(i);
                if (prim.getString(0).equals("line")) {
                    DrawLine(canvas, prim);
                }
            }
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height) {


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setWillNotDraw(false); //Allows us to use invalidate() to call onDraw()

        m_Thread = new StarwispCanvasThread(getHolder(), this); //Start the thread that
        m_Thread.setRunning(true);                     //will make calls to
        m_Thread.start();                              //onDraw()
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            m_Thread.setRunning(false);                //Tells thread to stop
            m_Thread.join();                           //Removes thread from mem.
        } catch (InterruptedException e) {}
    }
}
