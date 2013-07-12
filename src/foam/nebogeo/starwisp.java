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
import android.view.KeyEvent;
import android.text.TextWatcher;
import android.text.Editable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;



public class starwisp extends Activity
{
    static class Lock extends Object {}
    static public Lock mLock = new Lock();
    private static native void nativeInit();
    private static native void nativeDone();
    private static native String nativeEval(String code);

    static {
        System.loadLibrary("starwisp-core");
    }

    public int BuildOrientation(String p) {
        if (p.equals("vertical")) return LinearLayout.VERTICAL;
        if (p.equals("horizontal")) return LinearLayout.HORIZONTAL;
        return LinearLayout.VERTICAL;
    }

    public int BuildLayoutParam(String p) {
        if (p.equals("fill-parent")) return LayoutParams.FILL_PARENT;
        if (p.equals("match-parent")) return LayoutParams.MATCH_PARENT;
        if (p.equals("wrap-content")) return LayoutParams.WRAP_CONTENT;
        try {
            return Integer.parseInt(p);
        } catch (NumberFormatException e) {
            Log.i("starwisp", "Layout error with ["+p+"]");
            // send error message
            return LayoutParams.WRAP_CONTENT;
        }
    }

    public LinearLayout.LayoutParams BuildLayoutParams(JSONArray arr) {
        try {
            return new LinearLayout.LayoutParams(BuildLayoutParam(arr.getString(1)),
                                    BuildLayoutParam(arr.getString(2)),
                                    (float)arr.getDouble(3));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
            return null;
        }
    }

    public void Build(final Context ctx, JSONArray arr, ViewGroup parent) {
        try {
            String type = arr.getString(0);

            Log.i("starwisp","Building: "+type);

            if (type.equals("linear-layout")) {
                LinearLayout v = new LinearLayout(ctx);
                v.setId(arr.getInt(1));
                v.setOrientation(BuildOrientation(arr.getString(2)));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                Log.i("starwisp","Linear layout, "+arr.getJSONArray(4).length()+" children");
                parent.addView(v);
                JSONArray children = arr.getJSONArray(4);
                for (int i=0; i<children.length(); i++) {
                    Build(ctx,new JSONArray(children.getString(i)), v);
                }
                return;
            }

            if (type.equals("text-view")) {
                TextView v = new TextView(ctx);
                v.setId(arr.getInt(1));
                v.setText(arr.getString(2));
                v.setTextSize(arr.getInt(3));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));
                parent.addView(v);
            }

            if (type.equals("edit-text")) {
                final EditText v = new EditText(ctx);
                v.setId(arr.getInt(1));
                v.setText(arr.getString(2));
                v.setTextSize(arr.getInt(3));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));
                final String fn = arr.getString(5);
                v.setSingleLine(true);
/*                v.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        v.setFocusable(true);
                        v.requestFocus();

                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    }
                });
*/
                v.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View a, int keyCode, KeyEvent event) {
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            try {
                                String ret=eval("("+fn+" \""+v.getText()+"\")");
                                UpdateList(ctx, new JSONArray(ret));
                            } catch (JSONException e) {
                                Log.e("starwisp", "Error parsing data " + e.toString());
                            }
                        }
                        return false;
                    }
                });

                parent.addView(v);


            }

            if (type.equals("button")) {
                Button v = new Button(ctx);
                v.setId(arr.getInt(1));
                v.setText(arr.getString(2));
                v.setTextSize(arr.getInt(3));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));
                final String fn = arr.getString(5);
                v.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            String ret=eval("("+fn+")");
                            UpdateList(ctx, new JSONArray(ret));
                        } catch (JSONException e) {
                            Log.e("starwisp", "Error parsing data " + e.toString());
                        }
                    }
                });
                parent.addView(v);
            }

            if (type.equals("seek-bar")) {
                SeekBar v = new SeekBar(ctx);
                v.setId(arr.getInt(1));
                v.setMax(arr.getInt(2));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                final String fn = arr.getString(4);

                v.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar v, int a, boolean s) {
                        try {
                            String ret=eval("("+fn+" "+Integer.toString(a)+")");
                            UpdateList(ctx, new JSONArray(ret));
                        } catch (JSONException e) {
                            Log.e("starwisp", "Error parsing data " + e.toString());
                        }
                    }
                    public void onStartTrackingTouch(SeekBar v) {}
                    public void onStopTrackingTouch(SeekBar v) {}
                });
                parent.addView(v);
            }

            if (type.equals("spinner")) {
                Spinner v = new Spinner(this);
                v.setId(arr.getInt(1));
                final JSONArray items = arr.getJSONArray(2);
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                ArrayList<String> spinnerArray = new ArrayList<String>();
                final String fn = arr.getString(4);

                for (int i=0; i<items.length(); i++) {
                    spinnerArray.add(items.getString(i));
                }

                ArrayAdapter spinnerArrayAdapter =
                    new ArrayAdapter<String>(this,
                                             android.R.layout.simple_spinner_item,
                                             spinnerArray);
                v.setAdapter(spinnerArrayAdapter);

                v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
                        try {
                            String ret=eval("("+fn+" \""+items.getString(pos)+"\")");
                            UpdateList(ctx, new JSONArray(ret));
                        } catch (JSONException e) {
                            Log.e("starwisp", "Error parsing data " + e.toString());
                        }
                    }
                    public void onNothingSelected(AdapterView<?> v) {}
                });

                parent.addView(v);
            }


        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    public void UpdateList(Context ctx, JSONArray arr) {
        try {
            for (int i=0; i<arr.length(); i++) {
                Update(ctx,new JSONArray(arr.getString(i)));
            }
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    public void Update(Context ctx, JSONArray arr) {
        try {

            String type = arr.getString(0);
            Integer id = arr.getInt(1);
            String token = arr.getString(2);

            // tokens that work on everything
            if (token.equals("hide")) {
                View v=findViewById(id);
                v.setVisibility(View.GONE);
            }

            if (token.equals("show")) {
                View v=findViewById(id);
                v.setVisibility(View.VISIBLE);
            }

            if (token.equals("toast")) {
                Toast msg = Toast.makeText(getBaseContext(),arr.getString(3),Toast.LENGTH_SHORT);
                msg.show();
            }

            // special cases
            if (type.equals("text-view")) {
                TextView v = (TextView)findViewById(id);
                if (token.equals("text")) {
                    v.setText(arr.getString(3));
                }
            }

            if (type.equals("edit-text")) {
                EditText v = (EditText)findViewById(id);
                if (token.equals("text")) {
                    v.setText(arr.getString(3));
                }
            }


            if (type.equals("button")) {
                Button v = (Button)findViewById(id);
                if (token.equals("text")) {
                    v.setText(arr.getString(3));
                }

                if (token.equals("listener")) {
                    final String fn = arr.getString(3);
                    v.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            eval("("+fn+")");
                        }
                    });
                }
            }
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i("starwisp","starting up...");
        nativeInit();
        Log.i("starwisp","started, now running init.scm...");
        eval(readRawTextFile(this, "init.scm"));
        eval(readRawTextFile(this, "lib.scm"));
        Log.i("starwisp","started, now running starwisp.scm...");
        String json = eval(readRawTextFile(this, "starwisp.scm"));

        View root = findViewById(R.id.main);
        View debug = findViewById(R.id.debug);

        TextView valueTV = new TextView(this);
        valueTV.setText("Testing "+json);
        valueTV.setId(999);
        valueTV.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
        ((LinearLayout) debug).addView(valueTV);

        // try parse the string to a JSON object
        try {
            Build(this, new JSONArray(json), (ViewGroup) root);
            Log.i("starwisp","finished...");

        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }

    }

    private String eval(String code) {
        synchronized (mLock)
        {
            return nativeEval(code);
        }
    }

    public static String readRawTextFile(Context ctx, String fn)
    {
        BufferedReader inRd=null;
        try
        {
            StringBuffer inLine = new StringBuffer();
            inRd =
                new BufferedReader(new InputStreamReader
                                   (ctx.getAssets().open(fn)));
            String text;
            while ((text = inRd.readLine()) != null) {
                inLine.append(text);
                inLine.append("\n");
            }

            return inLine.toString();
        }
        catch (IOException e)
        {
            return "";
        }
        finally
        {
            try { inRd.close(); }
            catch (IOException e) { return ""; }
        }
    }

}
