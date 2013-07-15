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
        ActivityManager.Register("two",TestActivity.class);
    };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.main);

        // build static things
        m_Scheme = new Scheme(this);
        m_WidgetBuilder = new WidgetBuilder(m_Scheme);
        m_Name = "main";

        Log.i("starwisp","started, now running starwisp.scm...");
        m_Scheme.eval(m_Scheme.readRawTextFile(this, "starwisp.scm"));

        super.onCreate(savedInstanceState);
    }
}
