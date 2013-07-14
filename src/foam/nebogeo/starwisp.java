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

/*
public class ActivityManager
{
    public class ActivityDesc
    {
        public String m_Name;
        public Class m_ActClass;
        public String m_Code;
    }

    static public HashMap<String,ActivityDesc> Activities;

    static public void AddActivity(String name, Class actclass, String code)
    {
        Activities.put(name,new ActivityDesc(name,actclass,code));
    }

    static public void StartActivity(Activity src, String name, int requestcode)
    {
        Intent intent = new Intent(src,Activities.get(name).actclass);
        startActivityForResult(intent, requestcode);
    }
}

public class StarwispActivity extends Activity
{
    String

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        ActivityDesc ad=ActivityManager.Activities.get(
        wb.eval(
	}
}

*/

public class starwisp extends Activity
{
    WidgetBuilder m_WidgetBuilder;
    Scheme m_Scheme;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        m_Scheme = new Scheme(this);
        m_WidgetBuilder = new WidgetBuilder(m_Scheme);
        Log.i("starwisp","started, now running starwisp.scm...");
        String json = m_Scheme.eval(m_Scheme.readRawTextFile(this, "starwisp.scm"));
        View root = findViewById(R.id.main);

        // try parse the string to a JSON object
        try {
            m_WidgetBuilder.Build(this, new JSONArray(json), (ViewGroup) root);
            Log.i("starwisp","finished...");

        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }

    }
/*
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	}
*/

}
