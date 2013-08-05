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

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.graphics.Color;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.View;
import android.view.Gravity;
import android.view.KeyEvent;
import android.text.TextWatcher;
import android.text.Editable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;



public class starwisp extends StarwispActivity
{
    static {
        // register all activities here
        ActivityManager.Register("main",starwisp.class);
        ActivityManager.Register("calc",CalcActivity.class);
        ActivityManager.Register("newfield",NewFieldActivity.class);
        ActivityManager.Register("field",FieldActivity.class);
        ActivityManager.Register("fieldhistory",FieldHistoryActivity.class);
        ActivityManager.Register("fieldcalc",FieldCalcActivity.class);
        ActivityManager.Register("camera",CameraActivity.class);
    };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.main);

        // build static things
        m_Scheme = new Scheme(this);
        m_Builder = new StarwispBuilder(m_Scheme);
        m_Name = "main";

        Log.i("starwisp","started, now running starwisp.scm...");
        m_Scheme.eval(m_Scheme.readRawTextFile(this, "starwisp.scm"));

        super.onCreate(savedInstanceState);
    }
}
