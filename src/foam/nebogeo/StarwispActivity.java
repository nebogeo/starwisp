package foam.nebogeo;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class StarwispActivity extends Activity
{
    public String m_Name;
    public Scheme m_Scheme;
    public WidgetBuilder m_WidgetBuilder;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String json = m_Scheme.eval("(activity-callback 'on-create \""+m_Name+"\" '())");
        View root = findViewById(R.id.main);

        Log.i("starwisp", root.toString());
        Log.i("starwisp", json);

        try {
            m_WidgetBuilder.Build(this, new JSONArray(json), (ViewGroup) root);
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        String ret=m_Scheme.eval("(activity-callback 'on-start \""+m_Name+"\" '())");
        try {
            m_WidgetBuilder.UpdateList(this, new JSONArray(ret));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        String ret=m_Scheme.eval("(activity-callback 'on-resume \""+m_Name+"\" '())");
        try {
            m_WidgetBuilder.UpdateList(this, new JSONArray(ret));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }


    @Override
    public void onPause()
    {
        super.onPause();
        String ret=m_Scheme.eval("(activity-callback 'on-pause \""+m_Name+"\" '())");
        try {
            m_WidgetBuilder.UpdateList(this, new JSONArray(ret));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        String ret=m_Scheme.eval("(activity-callback 'on-stop \""+m_Name+"\" '())");
        try {
            m_WidgetBuilder.UpdateList(this, new JSONArray(ret));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        String ret=m_Scheme.eval("(activity-callback 'on-destroy \""+m_Name+"\" '())");
        try {
            m_WidgetBuilder.UpdateList(this, new JSONArray(ret));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String ret=m_Scheme.eval("(activity-callback 'on-activity-result \""+m_Name+"\" '("+requestCode+" "+resultCode+"))");
        try {
            m_WidgetBuilder.UpdateList(this, new JSONArray(ret));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
	}
}
