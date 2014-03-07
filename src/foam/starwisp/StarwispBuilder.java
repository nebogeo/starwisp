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

import java.util.ArrayList;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.media.MediaPlayer;
import android.os.Vibrator;

// removed due to various aggravating factors
//import android.support.v7.widget.GridLayout;
//import android.widget.GridLayout;

import android.util.Log;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.*;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.FrameLayout;
//import android.widget.GridLayout.Spec;
import android.widget.ScrollView;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.webkit.WebView;
import android.widget.Toast;
import android.widget.Space;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.View;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.view.View.OnDragListener;
import android.view.View.DragShadowBuilder;
import android.view.DragEvent;
import android.text.TextWatcher;
import android.text.Html;
import android.text.Editable;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.widget.DatePicker;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.Camera;
import java.io.FileNotFoundException;
import android.net.Uri;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.List;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.TypedValue;
import android.os.Handler;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.graphics.Matrix;

import android.content.ClipDescription;
import android.content.ClipData;
import android.content.ClipData.Item;

import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import java.util.Calendar;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class StarwispBuilder
{
    Scheme m_Scheme;
    NetworkManager m_NetworkManager;
	LocationManager m_LocationManager;
    DorisLocationListener m_GPS;
    Handler m_Handler;

    public StarwispBuilder(Scheme scm) {
        m_Scheme = scm;
        m_NetworkManager = new NetworkManager();
        m_Handler = new Handler();
    }

    public int BuildOrientation(String p) {
        if (p.equals("vertical")) return LinearLayout.VERTICAL;
        if (p.equals("horizontal")) return LinearLayout.HORIZONTAL;
        return LinearLayout.VERTICAL;
    }

    public int BuildLayoutGravity(String p) {
        if (p.equals("centre")) return Gravity.CENTER;
        if (p.equals("left")) return Gravity.LEFT;
        if (p.equals("right")) return Gravity.RIGHT;
        if (p.equals("fill")) return Gravity.FILL;
        return Gravity.LEFT;
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
            float weight = (float)arr.getDouble(3);
            LinearLayout.LayoutParams lp;
            if (weight == -1) {
                lp = new LinearLayout.LayoutParams(BuildLayoutParam(arr.getString(1)),
                                                   BuildLayoutParam(arr.getString(2)));
            } else {
                lp = new LinearLayout.LayoutParams(BuildLayoutParam(arr.getString(1)),
                                                   BuildLayoutParam(arr.getString(2)),
                                                   weight);
            }
            lp.gravity=BuildLayoutGravity(arr.getString(4));
            int margin=arr.getInt(5);
            lp.setMargins(margin,margin,margin,margin);
            return lp;
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
            return null;
        }
    }

    public void DialogCallback(StarwispActivity ctx, String ctxname, String name, String args)
    {
        try {
            String ret=m_Scheme.eval("(dialog-callback \""+name+"\" '("+args+"))");
            UpdateList(ctx, ctxname, new JSONArray(ret));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }


    private void Callback(StarwispActivity ctx, String ctxname, int wid)
    {
        try {
            String ret=m_Scheme.eval("(widget-callback \""+ctxname+"\" "+wid+" '())");
            UpdateList(ctx, ctxname, new JSONArray(ret));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    private void CallbackArgs(StarwispActivity ctx, String ctxname, int wid, String args)
    {
        try {
            String ret=m_Scheme.eval("(widget-callback \""+ctxname+"\" "+wid+" '("+args+"))");
            UpdateList(ctx, ctxname, new JSONArray(ret));
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }


    public static void email(Context context, String emailTo, String emailCC,
                             String subject, String emailText, List<String> filePaths)
    {
        //need to "send multiple" to get more than one attachment
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
        new String[]{emailTo});
        emailIntent.putExtra(android.content.Intent.EXTRA_CC,
                             new String[]{emailCC});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        ArrayList<String> extra_text = new ArrayList<String>();
        extra_text.add(emailText);
        emailIntent.putStringArrayListExtra(Intent.EXTRA_TEXT, extra_text);
        //emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);

        //has to be an ArrayList
        ArrayList<Uri> uris = new ArrayList<Uri>();
        //convert from paths to Android friendly Parcelable Uri's
        for (String file : filePaths)
        {
            File fileIn = new File(file);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    public static void photo(StarwispActivity ctx, String path, int code) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(ctx.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = new File(path);

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                           Uri.fromFile(photoFile));
                ctx.startActivityForResult(takePictureIntent, code);
            } else {
                Log.i("starwisp","Could not open photo file");
            }
        }
    }

    public void Build(final StarwispActivity ctx, final String ctxname, JSONArray arr, ViewGroup parent) {

        try {
            String type = arr.getString(0);

            Log.i("starwisp","building started "+type);

            if (type.equals("build-fragment")) {
                String name = arr.getString(1);
                int ID = arr.getInt(2);
                Fragment fragment = ActivityManager.GetFragment(name);
                LinearLayout inner = new LinearLayout(ctx);
                inner.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                inner.setId(ID);
                FragmentTransaction fragmentTransaction = ctx.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(ID,fragment);
                fragmentTransaction.commit();
                parent.addView(inner);
                return;
            }

            if (type.equals("linear-layout")) {
                StarwispLinearLayout.Build(this,ctx,ctxname,arr,parent);
                return;
            }

            if (type.equals("relative-layout")) {
                StarwispRelativeLayout.Build(this,ctx,ctxname,arr,parent);
                return;
            }

            if (type.equals("draggable")) {
                final LinearLayout v = new LinearLayout(ctx);
                final int id=arr.getInt(1);
                v.setPadding(20,20,20,20);
                v.setId(id);
                v.setOrientation(BuildOrientation(arr.getString(2)));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                v.setClickable(true);
                v.setFocusable(true);

                JSONArray col = arr.getJSONArray(4);
                v.setBackgroundResource(R.drawable.draggable);

                GradientDrawable drawable = (GradientDrawable) v.getBackground();
                final int colour=Color.argb(col.getInt(3), col.getInt(0), col.getInt(1), col.getInt(2));
                drawable.setColor(colour);


                parent.addView(v);
                JSONArray children = arr.getJSONArray(5);
                for (int i=0; i<children.length(); i++) {
                    Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
                }

                // Sets a long click listener for the ImageView using an anonymous listener object that
                // implements the OnLongClickListener interface
/*                v.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View vv) {
                        if (id!=99) {
                            ClipData dragData = ClipData.newPlainText("simple text", ""+id);
                            View.DragShadowBuilder myShadow = new MyDragShadowBuilder(v);
                            Log.i("starwisp","start drag id "+vv.getId());
                            v.startDrag(dragData, myShadow, null, 0);
                            v.setVisibility(View.GONE);
                            return true;
                        }
                        return false;
                    }
                });
*/
                // Sets a long click listener for the ImageView using an anonymous listener object that
                // implements the OnLongClickListener interface
                v.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View vv, MotionEvent event) {
                        if (event.getAction()==MotionEvent.ACTION_DOWN && id!=99) {
//                            ClipData dragData = ClipData.newPlainText("simple text", ""+id);

                            ClipData dragData = new ClipData(
                                new ClipDescription(null,new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN }),
                                new ClipData.Item(""+id));

                            View.DragShadowBuilder myShadow = new MyDragShadowBuilder(v);
                            Log.i("starwisp","start drag id "+vv.getId());
                            v.startDrag(dragData, myShadow, null, 0);
                            v.setVisibility(View.GONE);
                            return true;
                        }
                        return false;
                    }
                });




                v.setOnDragListener(new View.OnDragListener() {
                    public boolean onDrag(View vv, DragEvent event) {
                        final int action = event.getAction();
                        switch(action) {
                        case DragEvent.ACTION_DRAG_STARTED:
                            if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                                // returns true to indicate that the View can accept the dragged data.
                                return true;
                            } else {
                                // Returns false. During the current drag and drop operation, this View will
                                // not receive events again until ACTION_DRAG_ENDED is sent.
                                return false;
                            }
                        case DragEvent.ACTION_DRAG_ENTERED: {
                            return true;
                        }
                        case DragEvent.ACTION_DRAG_LOCATION: return true;
                        case DragEvent.ACTION_DRAG_EXITED: {
                            return true;
                        }
                        case DragEvent.ACTION_DROP: {
                            ClipData.Item item = event.getClipData().getItemAt(0);
                            String dragData = item.getText().toString();
                            Log.i("starwisp","Dragged view is " + dragData);
                            int otherid = Integer.parseInt(dragData);
                            View otherw=ctx.findViewById(otherid);
                            Log.i("starwisp","removing from parent "+((View)otherw.getParent()).getId());

                            // check we are not adding to ourself
                            if (id!=otherid) {
                                ((ViewManager)otherw.getParent()).removeView(otherw);
                                Log.i("starwisp","adding to " + id);
                                v.addView(otherw);
                            }
                            otherw.setVisibility(View.VISIBLE);
                            return true;
                        }
                        case DragEvent.ACTION_DRAG_ENDED: {
                            if (event.getResult()) {
                            } else {
                            };
                            return true;
                        }
                            // An unknown action type was received.
                        default:
                            Log.e("starwisp","Unknown action type received by OnDragListener.");
                            break;
                        };
                        return true;
                    }});
                return;
            }

            if (type.equals("frame-layout")) {
                FrameLayout v = new FrameLayout(ctx);
                v.setId(arr.getInt(1));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));
                parent.addView(v);
                JSONArray children = arr.getJSONArray(3);
                for (int i=0; i<children.length(); i++) {
                    Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
                }
                return;
            }

            /*
            if (type.equals("grid-layout")) {
                GridLayout v = new GridLayout(ctx);
                v.setId(arr.getInt(1));
                v.setRowCount(arr.getInt(2));
                //v.setColumnCount(arr.getInt(2));
                v.setOrientation(BuildOrientation(arr.getString(3)));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));

                parent.addView(v);
                JSONArray children = arr.getJSONArray(5);
                for (int i=0; i<children.length(); i++) {
                    Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
                }

                return;
            }
            */

            if (type.equals("scroll-view")) {
                HorizontalScrollView v = new HorizontalScrollView(ctx);
                v.setId(arr.getInt(1));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));
                parent.addView(v);
                JSONArray children = arr.getJSONArray(3);
                for (int i=0; i<children.length(); i++) {
                    Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
                }
                return;
            }

            if (type.equals("scroll-view-vert")) {
                ScrollView v = new ScrollView(ctx);
                v.setId(arr.getInt(1));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));
                parent.addView(v);
                JSONArray children = arr.getJSONArray(3);
                for (int i=0; i<children.length(); i++) {
                    Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
                }
                return;
            }


            if (type.equals("view-pager")) {
                ViewPager v = new ViewPager(ctx);
                v.setId(arr.getInt(1));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));
                v.setOffscreenPageLimit(3);
                final JSONArray items = arr.getJSONArray(3);

                v.setAdapter(new FragmentPagerAdapter(ctx.getSupportFragmentManager()) {

                    @Override
                    public int getCount() {
                        return items.length();
                    }

                    @Override
                    public Fragment getItem(int position) {
                        try {
                            String fragname = items.getString(position);
                            return ActivityManager.GetFragment(fragname);
                        } catch (JSONException e) {
                            Log.e("starwisp", "Error parsing data " + e.toString());
                        }
                        return null;
                    }
                });
                parent.addView(v);
                return;
            }

            if (type.equals("space")) {
                // Space v = new Space(ctx); (class not found runtime error??)
                TextView v = new TextView(ctx);
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));
                parent.addView(v);
            }


            if (type.equals("image-view")) {
                ImageView v = new ImageView(ctx);
                v.setId(arr.getInt(1));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));

                String image = arr.getString(2);

                if (image.startsWith("/")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(image);
                    v.setImageBitmap(bitmap);
                } else {
                    int id = ctx.getResources().getIdentifier(image,"drawable", ctx.getPackageName());
                    v.setImageResource(id);
                }

                parent.addView(v);
            }

            if (type.equals("text-view")) {
                TextView v = new TextView(ctx);
                v.setId(arr.getInt(1));
                v.setText(Html.fromHtml(arr.getString(2)));
                v.setTextSize(arr.getInt(3));
                v.setMovementMethod(LinkMovementMethod.getInstance());
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));
                v.setClickable(false);
                v.setEnabled(false);

                v.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View vv, MotionEvent event) {
                        return false;
                    }
                });

                if (arr.length()>5) {
                    if (arr.getString(5).equals("left")) {
                        v.setGravity(Gravity.LEFT);
                    } else {
                        if (arr.getString(5).equals("fill")) {
                            v.setGravity(Gravity.FILL);
                        } else {
                            v.setGravity(Gravity.CENTER);
                        }
                    }
                } else {
                    v.setGravity(Gravity.LEFT);
                }
                v.setTypeface(((StarwispActivity)ctx).m_Typeface);
                parent.addView(v);
            }

            if (type.equals("debug-text-view")) {
                TextView v = (TextView)ctx.getLayoutInflater().inflate(R.layout.debug_text, null);
//                v.setBackgroundResource(R.color.black);
                v.setId(arr.getInt(1));
//                v.setText(Html.fromHtml(arr.getString(2)));
//                v.setTextColor(R.color.white);
//                v.setTextSize(arr.getInt(3));
//                v.setMovementMethod(LinkMovementMethod.getInstance());
//                v.setMaxLines(10);
//                v.setVerticalScrollBarEnabled(true);
//                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));
                //v.setMovementMethod(new ScrollingMovementMethod());

                /*
                if (arr.length()>5) {
                    if (arr.getString(5).equals("left")) {
                        v.setGravity(Gravity.LEFT);
                    } else {
                        if (arr.getString(5).equals("fill")) {
                            v.setGravity(Gravity.FILL);
                        } else {
                            v.setGravity(Gravity.CENTER);
                        }
                    }
                } else {
                    v.setGravity(Gravity.LEFT);
                }
                v.setTypeface(((StarwispActivity)ctx).m_Typeface);*/
                parent.addView(v);
            }


            if (type.equals("web-view")) {
                WebView v = new WebView(ctx);
                v.setId(arr.getInt(1));
                v.setVerticalScrollBarEnabled(false);
                v.loadData(arr.getString(2), "text/html", "utf-8");
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                parent.addView(v);
            }


            if (type.equals("edit-text")) {
                final EditText v = new EditText(ctx);
                v.setId(arr.getInt(1));
                v.setText(arr.getString(2));
                v.setTextSize(arr.getInt(3));

                String inputtype = arr.getString(4);
                if (inputtype.equals("text")) {
                    //v.setInputType(InputType.TYPE_CLASS_TEXT);
                } else if (inputtype.equals("numeric")) {
                    v.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else if (inputtype.equals("email")) {
                    v.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
                }

                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(5)));
                v.setTypeface(((StarwispActivity)ctx).m_Typeface);
                final String fn = arr.getString(5);
                //v.setSingleLine(true);

                v.addTextChangedListener(new TextWatcher() {
                     public void afterTextChanged(Editable s) {
                         CallbackArgs(ctx,ctxname,v.getId(),"\""+s.toString()+"\"");
                     }
                     public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                     public void onTextChanged(CharSequence s, int start, int before, int count) {}
                 });

                parent.addView(v);
            }

            if (type.equals("button")) {
                Button v = new Button(ctx);
                v.setId(arr.getInt(1));
                v.setText(arr.getString(2));
                v.setTextSize(arr.getInt(3));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));
                v.setTypeface(((StarwispActivity)ctx).m_Typeface);
                final String fn = arr.getString(5);
                v.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Callback(ctx,ctxname,v.getId());
                    }
                });

                parent.addView(v);
            }

            if (type.equals("toggle-button")) {
                ToggleButton v = new ToggleButton(ctx);
                if (arr.getString(5).equals("fancy")) {
                    v = (ToggleButton)ctx.getLayoutInflater().inflate(R.layout.toggle_button_fancy, null);
                }

                if (arr.getString(5).equals("yes")) {
                    v = (ToggleButton)ctx.getLayoutInflater().inflate(R.layout.toggle_button_yes, null);
                }

                if (arr.getString(5).equals("maybe")) {
                    v = (ToggleButton)ctx.getLayoutInflater().inflate(R.layout.toggle_button_maybe, null);
                }

                if (arr.getString(5).equals("no")) {
                    v = (ToggleButton)ctx.getLayoutInflater().inflate(R.layout.toggle_button_no, null);
                }

                v.setId(arr.getInt(1));
                v.setText(arr.getString(2));
                v.setTextSize(arr.getInt(3));
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));
                v.setTypeface(((StarwispActivity)ctx).m_Typeface);
                final String fn = arr.getString(6);
                v.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String arg="#f";
                        if (((ToggleButton) v).isChecked()) arg="#t";
                        CallbackArgs(ctx,ctxname,v.getId(),arg);
                    }
                });
                parent.addView(v);
            }


            if (type.equals("seek-bar")) {
                SeekBar v = new SeekBar(ctx);
                v.setId(arr.getInt(1));
                v.setMax(arr.getInt(2));
                v.setProgress(arr.getInt(2)/2);
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                final String fn = arr.getString(4);

                v.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar v, int a, boolean s) {
                        CallbackArgs(ctx,ctxname,v.getId(),Integer.toString(a));
                    }
                    public void onStartTrackingTouch(SeekBar v) {}
                    public void onStopTrackingTouch(SeekBar v) {}
                });
                parent.addView(v);
            }

            if (type.equals("spinner")) {
                Spinner v = new Spinner(ctx);
                final int wid = arr.getInt(1);
                v.setId(wid);
                final JSONArray items = arr.getJSONArray(2);
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                ArrayList<String> spinnerArray = new ArrayList<String>();

                for (int i=0; i<items.length(); i++) {
                    spinnerArray.add(items.getString(i));
                }

                ArrayAdapter spinnerArrayAdapter =
                    new ArrayAdapter<String>(ctx,
                                             R.layout.spinner_item,
                                             spinnerArray) {
                    public View getView(int position, View convertView,ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        ((TextView) v).setTypeface(((StarwispActivity)ctx).m_Typeface);
                        return v;
                    }
                };

                spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_layout);

                v.setAdapter(spinnerArrayAdapter);
                v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
                        try {
                            CallbackArgs(ctx,ctxname,wid,"\""+items.getString(pos)+"\"");
                        } catch (JSONException e) {
                            Log.e("starwisp", "Error parsing data " + e.toString());
                        }
                    }
                    public void onNothingSelected(AdapterView<?> v) {}
                });

                parent.addView(v);
            }

            if (type.equals("nomadic")) {
                final int wid = arr.getInt(1);
                NomadicSurfaceView v = new NomadicSurfaceView(ctx,wid);
                v.setId(wid);
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));
                Log.e("starwisp", "built the thing");
                parent.addView(v);
                Log.e("starwisp", "addit to the view");
            }

            if (type.equals("canvas")) {
                StarwispCanvas v = new StarwispCanvas(ctx);
                final int wid = arr.getInt(1);
                v.setId(wid);
                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));
                v.SetDrawList(arr.getJSONArray(3));
                parent.addView(v);
            }

            if (type.equals("camera-preview")) {
                PictureTaker pt = new PictureTaker();
                CameraPreview v = new CameraPreview(ctx,pt);
                final int wid = arr.getInt(1);
                v.setId(wid);


                //              LinearLayout.LayoutParams lp =
                //  new LinearLayout.LayoutParams(minWidth, minHeight, 1);

                v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));

//                v.setLayoutParams(lp);
                parent.addView(v);
            }

            if (type.equals("button-grid")) {
                LinearLayout horiz = new LinearLayout(ctx);
                final int id = arr.getInt(1);
                final String buttontype = arr.getString(2);
                horiz.setId(id);
                horiz.setOrientation(LinearLayout.HORIZONTAL);
                parent.addView(horiz);
                int height = arr.getInt(3);
                int textsize = arr.getInt(4);
                LinearLayout.LayoutParams lp = BuildLayoutParams(arr.getJSONArray(5));
                JSONArray buttons = arr.getJSONArray(6);
                int count = buttons.length();
                int vertcount = 0;
                LinearLayout vert = null;

                for (int i=0; i<count; i++) {
                    JSONArray button = buttons.getJSONArray(i);

                    if (vertcount==0) {
                        vert = new LinearLayout(ctx);
                        vert.setId(0);
                        vert.setOrientation(LinearLayout.VERTICAL);
                        horiz.addView(vert);
                    }
                    vertcount=(vertcount+1)%height;

                    if (buttontype.equals("button")) {
                        Button b = new Button(ctx);
                        b.setId(button.getInt(0));
                        b.setText(button.getString(1));
                        b.setTextSize(textsize);
                        b.setLayoutParams(lp);
                        b.setTypeface(((StarwispActivity)ctx).m_Typeface);
                        final String fn = arr.getString(6);
                        b.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                CallbackArgs(ctx,ctxname,id,""+v.getId()+" #t");
                            }
                        });
                        vert.addView(b);
                    }
                    else if (buttontype.equals("toggle")) {
                        ToggleButton b = new ToggleButton(ctx);
                        b.setId(button.getInt(0));
                        b.setText(button.getString(1));
                        b.setTextSize(textsize);
                        b.setLayoutParams(lp);
                        b.setTypeface(((StarwispActivity)ctx).m_Typeface);
                        final String fn = arr.getString(6);
                        b.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                String arg="#f";
                                if (((ToggleButton) v).isChecked()) arg="#t";
                                CallbackArgs(ctx,ctxname,id,""+v.getId()+" "+arg);
                            }
                        });
                        vert.addView(b);
                    }
                }
            }



        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing ["+arr.toString()+"] " + e.toString());
        }

        //Log.i("starwisp","building ended");

    }

    public void UpdateList(FragmentActivity ctx, String ctxname, JSONArray arr) {
        try {
            for (int i=0; i<arr.length(); i++) {
                Update((StarwispActivity)ctx,ctxname,new JSONArray(arr.getString(i)));
            }
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    public void Update(final StarwispActivity ctx, final String ctxname, JSONArray arr) {
        try {

            String type = arr.getString(0);
            final Integer id = arr.getInt(1);
            String token = arr.getString(2);

            Log.i("starwisp", "Update: "+type+" "+id+" "+token);

            // non widget commands
            if (token.equals("toast")) {
                Toast msg = Toast.makeText(ctx.getBaseContext(),arr.getString(3),Toast.LENGTH_SHORT);
                msg.show();
                return;
            }

            if (token.equals("play-sound")) {
                String name = arr.getString(3);

                if (name.equals("ping")) {
                    MediaPlayer mp = MediaPlayer.create(ctx, R.raw.ping);
                    mp.start();
                }
                if (name.equals("active")) {
                    MediaPlayer mp = MediaPlayer.create(ctx, R.raw.active);
                    mp.start();
                }
            }

            if (token.equals("vibrate")) {
                Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(arr.getInt(3));
            }


            if (type.equals("replace-fragment")) {
                int ID = arr.getInt(1);
                String name = arr.getString(2);
                Fragment fragment = ActivityManager.GetFragment(name);
                FragmentTransaction ft = ctx.getSupportFragmentManager().beginTransaction();

                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                //ft.setCustomAnimations(
                //    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                //    R.animator.card_flip_left_in, R.animator.card_flip_left_out);
                ft.replace(ID, fragment);
                //ft.addToBackStack(null);
                ft.commit();
                return;
            }

            if (token.equals("dialog-fragment")) {
                FragmentManager fm = ctx.getSupportFragmentManager();
                final int ID = arr.getInt(3);
                final JSONArray lp = arr.getJSONArray(4);
                final String name = arr.getString(5);

                final Dialog dialog = new Dialog(ctx);
                dialog.setTitle("Title...");

                LinearLayout inner = new LinearLayout(ctx);
                inner.setId(ID);
                inner.setLayoutParams(BuildLayoutParams(lp));

                dialog.setContentView(inner);

//                Fragment fragment = ActivityManager.GetFragment(name);
//                FragmentTransaction fragmentTransaction = ctx.getSupportFragmentManager().beginTransaction();
//                fragmentTransaction.add(ID,fragment);
//                fragmentTransaction.commit();

                dialog.show();



/*                DialogFragment df = new DialogFragment() {
                    @Override
                    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                             Bundle savedInstanceState) {
                        LinearLayout inner = new LinearLayout(ctx);
                        inner.setId(ID);
                        inner.setLayoutParams(BuildLayoutParams(lp));

                        return inner;
                    }

                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        Dialog ret = super.onCreateDialog(savedInstanceState);
                        Log.i("starwisp","MAKINGDAMNFRAGMENT");

                        Fragment fragment = ActivityManager.GetFragment(name);
                        FragmentTransaction fragmentTransaction = ctx.getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.add(1,fragment);
                        fragmentTransaction.commit();
                        return ret;
                    }
                };
                df.show(ctx.getFragmentManager(), "foo");
*/
            }

            if (token.equals("time-picker-dialog")) {

                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // Create a new instance of TimePickerDialog and return it
                TimePickerDialog d=new TimePickerDialog(ctx, null, hour, minute, true);
                d.show();
                return;
            };

            if (token.equals("make-directory")) {
                File file = new File(((StarwispActivity)ctx).m_AppDir+arr.getString(3));
                file.mkdirs();
                return;
            }

            if (token.equals("list-files")) {
                final String name = arr.getString(3);
                File file = new File(((StarwispActivity)ctx).m_AppDir+arr.getString(5));
                // todo, should probably call callback with empty list
                if (file != null) {
                    File list[] = file.listFiles();

                    if (list != null) {
                        String code="(";
                        for( int i=0; i< list.length; i++)
                        {
                            code+=" \""+list[i].getName()+"\"";
                        }
                        code+=")";

                        DialogCallback(ctx, ctxname, name, code);
                    }
                }
                return;
            }

            if (token.equals("gps-start")) {
                final String name = arr.getString(3);

                if (m_LocationManager == null) {
                    m_LocationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
                    m_GPS = new DorisLocationListener(m_LocationManager);
                }

                m_GPS.Start((StarwispActivity)ctx,name,this);
                return;
            }

            if (token.equals("walk-draggable")) {
                final String name = arr.getString(3);
                int iid = arr.getInt(5);
                DialogCallback(ctx,ctxname,name,
                               WalkDraggable(ctx,name,ctxname,iid));
                return;
            }


            if (token.equals("delayed")) {
                final String name = arr.getString(3);
                final int d = arr.getInt(5);
                Runnable timerThread = new Runnable() {
                    public void run() {
                        DialogCallback(ctx, ctxname, name, "");
                    }
                };
                m_Handler.removeCallbacksAndMessages(null);
                m_Handler.postDelayed(timerThread, d);
                return;
            }

            if (token.equals("network-connect")) {
                final String name = arr.getString(3);
                final String ssid = arr.getString(5);
                m_NetworkManager.Start(ssid,(StarwispActivity)ctx,name,this);
                return;
            }

            if (token.equals("http-request")) {
                if (m_NetworkManager.state==NetworkManager.State.CONNECTED) {
                    Log.i("starwisp","attempting http request");
                    final String name = arr.getString(3);
                    final String url = arr.getString(5);
                    m_NetworkManager.StartRequestThread(url,"normal",name);
                }
                return;
            }

            if (token.equals("http-download")) {
                if (m_NetworkManager.state==NetworkManager.State.CONNECTED) {
                    Log.i("starwisp","attempting http dl request");
                    final String filename = arr.getString(4);
                    final String url = arr.getString(5);
                    m_NetworkManager.StartRequestThread(url,"download",filename);
                }
                return;
            }

            if (token.equals("take-photo")) {
                photo(ctx,arr.getString(3),arr.getInt(4));
            }

            if (token.equals("send-mail")) {
                final String to[] = new String[1];
                to[0]=arr.getString(3);
                final String subject = arr.getString(4);
                final String body = arr.getString(5);

                JSONArray attach = arr.getJSONArray(6);
                ArrayList<String> paths = new ArrayList<String>();
                for (int a=0; a<attach.length(); a++)
                {
                    Log.i("starwisp",attach.getString(a));
                    paths.add(attach.getString(a));
                }

                email(ctx, to[0], "", subject, body, paths);
            }

            if (token.equals("date-picker-dialog")) {
                final Calendar c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);
                int year = c.get(Calendar.YEAR);

                final String name = arr.getString(3);

                // Create a new instance of TimePickerDialog and return it
                DatePickerDialog d=new DatePickerDialog(
                    ctx,
                    new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker view, int year, int month, int day) {
                            DialogCallback(ctx, ctxname, name, day+" "+month+" "+year);
                        }
                    }, year, month, day);
                d.show();
                return;
            };

            if (token.equals("alert-dialog")) {

                final String name = arr.getString(3);
                final String msg = arr.getString(5);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int result = 0;
                        if (which==DialogInterface.BUTTON_POSITIVE) result=1;
                        DialogCallback(ctx, ctxname, name, ""+result);
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setMessage(msg).setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

                return;
            }



            if (token.equals("start-activity")) {
                ActivityManager.StartActivity(ctx,arr.getString(3),arr.getInt(4),arr.getString(5));
                return;
            }

            if (token.equals("start-activity-goto")) {
                ActivityManager.StartActivityGoto(ctx,arr.getString(3),arr.getString(4));
                return;
            }

            if (token.equals("finish-activity")) {
                ctx.setResult(arr.getInt(3));
                ctx.finish();
                return;
            }

///////////////////////////////////////////////////////////

            // now try and find the widget
            View vv=ctx.findViewById(id);
            if (vv==null)
            {
                Log.i("starwisp", "Can't find widget : "+id);
                return;
            }

            // tokens that work on everything
            if (token.equals("hide")) {
                vv.setVisibility(View.GONE);
                return;
            }

            if (token.equals("show")) {
                vv.setVisibility(View.VISIBLE);
                return;
            }

            // tokens that work on everything
            if (token.equals("set-enabled")) {
                vv.setEnabled(arr.getInt(3)==1);
                return;
            }


            // special cases

            if (type.equals("linear-layout")) {
                StarwispLinearLayout.Update(this,(LinearLayout)vv,token,ctx,ctxname,arr);
                return;
            }

            if (type.equals("draggable")) {
                LinearLayout v = (LinearLayout)vv;
                if (token.equals("contents")) {
//                    v.removeAllViews();
                    JSONArray children = arr.getJSONArray(3);
                    for (int i=0; i<children.length(); i++) {
                        Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
                    }
                }
            }

            if (type.equals("button-grid")) {
                LinearLayout horiz = (LinearLayout)vv;
                if (token.equals("grid-buttons")) {
                    horiz.removeAllViews();

                    JSONArray params = arr.getJSONArray(3);
                    String buttontype = params.getString(0);
                    int height = params.getInt(1);
                    int textsize = params.getInt(2);
                    LinearLayout.LayoutParams lp = BuildLayoutParams(params.getJSONArray(3));
                    final JSONArray buttons = params.getJSONArray(4);
                    final int count = buttons.length();
                    int vertcount = 0;
                    LinearLayout vert = null;

                    for (int i=0; i<count; i++) {
                        JSONArray button = buttons.getJSONArray(i);

                        if (vertcount==0) {
                            vert = new LinearLayout(ctx);
                            vert.setId(0);
                            vert.setOrientation(LinearLayout.VERTICAL);
                            horiz.addView(vert);
                        }
                        vertcount=(vertcount+1)%height;

                        if (buttontype.equals("button")) {
                            Button b = new Button(ctx);
                            b.setId(button.getInt(0));
                            b.setText(button.getString(1));
                            b.setTextSize(textsize);
                            b.setLayoutParams(lp);
                            b.setTypeface(((StarwispActivity)ctx).m_Typeface);
                            final String fn = params.getString(5);
                            b.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    CallbackArgs(ctx,ctxname,id,""+v.getId()+" #t");
                                }
                            });
                            vert.addView(b);
                        }
                        else if (buttontype.equals("toggle")) {
                            ToggleButton b = new ToggleButton(ctx);
                            b.setId(button.getInt(0));
                            b.setText(button.getString(1));
                            b.setTextSize(textsize);
                            b.setLayoutParams(lp);
                            b.setTypeface(((StarwispActivity)ctx).m_Typeface);
                            final String fn = params.getString(5);
                            b.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    String arg="#f";
                                    if (((ToggleButton) v).isChecked()) arg="#t";
                                    CallbackArgs(ctx,ctxname,id,""+v.getId()+" "+arg);
                                }
                            });
                            vert.addView(b);
                        }
                        else if (buttontype.equals("single")) {
                            ToggleButton b = new ToggleButton(ctx);
                            b.setId(button.getInt(0));
                            b.setText(button.getString(1));
                            b.setTextSize(textsize);
                            b.setLayoutParams(lp);
                            b.setTypeface(((StarwispActivity)ctx).m_Typeface);
                            final String fn = params.getString(5);
                            b.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    try {
                                        for (int i=0; i<count; i++) {
                                            JSONArray button = buttons.getJSONArray(i);
                                            int bid = button.getInt(0);
                                            if (bid!=v.getId()) {
                                                ToggleButton tb=(ToggleButton)ctx.findViewById(bid);
                                                tb.setChecked(false);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        Log.e("starwisp", "Error parsing data " + e.toString());
                                    }

                                    CallbackArgs(ctx,ctxname,id,""+v.getId()+" #t");
                                }
                            });
                            vert.addView(b);
                        }


                    }
                }
            }




/*
            if (type.equals("grid-layout")) {
                GridLayout v = (GridLayout)vv;
                if (token.equals("contents")) {
                    v.removeAllViews();
                    JSONArray children = arr.getJSONArray(3);
                    for (int i=0; i<children.length(); i++) {
                        Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
                    }
                }
            }
*/
            if (type.equals("view-pager")) {
                ViewPager v = (ViewPager)vv;
                if (token.equals("switch")) {
                    v.setCurrentItem(arr.getInt(3));
                }
                if (token.equals("pages")) {
                    final JSONArray items = arr.getJSONArray(3);
                    v.setAdapter(new FragmentPagerAdapter(ctx.getSupportFragmentManager()) {
                        @Override
                        public int getCount() {
                            return items.length();
                        }

                        @Override
                        public Fragment getItem(int position) {
                            try {
                                String fragname = items.getString(position);
                                return ActivityManager.GetFragment(fragname);
                            } catch (JSONException e) {
                                Log.e("starwisp", "Error parsing data " + e.toString());
                            }
                            return null;
                        }
                    });
                }
            }

            if (type.equals("image-view")) {
                ImageView v = (ImageView)vv;
                if (token.equals("image")) {
                    int iid = ctx.getResources().getIdentifier(arr.getString(3),
                                                               "drawable", ctx.getPackageName());
                    v.setImageResource(iid);
                }
                if (token.equals("external-image")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(arr.getString(3));
                    // do the rotation here, so the photo shows the right way round
                    try {
                        ExifInterface exif = new ExifInterface(arr.getString(3));
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(0);

                        if (orientation == 6) {
                            matrix.postRotate(90);
                        }
                        else if (orientation == 3) {
                            matrix.postRotate(180);
                        }
                        else if (orientation == 8) {
                            matrix.postRotate(270);
                        }

                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        v.setImageBitmap(bitmap);
                    }
                    catch (IOException e) {
                    }
                }
                return;
            }

            if (type.equals("text-view") || type.equals("debug-text-view")) {
                TextView v = (TextView)vv;
                if (token.equals("text")) {
                    if (type.equals("debug-text-view")) {
                        //v.setMovementMethod(new ScrollingMovementMethod());
                    }
                    v.setText(arr.getString(3));
                }
                return;
            }

            if (type.equals("edit-text")) {
                EditText v = (EditText)vv;
                if (token.equals("text")) {
                    v.setText(arr.getString(3));
                }
                if (token.equals("request-focus")) {
                    v.requestFocus();
                    InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                }
                return;
            }


            if (type.equals("button")) {
                Button v = (Button)vv;
                if (token.equals("text")) {
                    v.setText(arr.getString(3));
                }

                if (token.equals("listener")) {
                    final String fn = arr.getString(3);
                    v.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            m_Scheme.eval("("+fn+")");
                        }
                    });
                }
                return;
            }

            if (type.equals("toggle-button")) {
                ToggleButton v = (ToggleButton)vv;
                if (token.equals("text")) {
                    v.setText(arr.getString(3));
                    return;
                }

                if (token.equals("checked")) {
                    if (arr.getInt(3)==0) v.setChecked(false);
                    else v.setChecked(true);
                    return;
                }

                if (token.equals("listener")) {
                    final String fn = arr.getString(3);
                    v.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            m_Scheme.eval("("+fn+")");
                        }
                    });
                }
                return;
            }


            if (type.equals("canvas")) {
                StarwispCanvas v = (StarwispCanvas)vv;
                if (token.equals("drawlist")) {
                    v.SetDrawList(arr.getJSONArray(3));
                }
                return;
            }

            if (type.equals("camera-preview")) {
                final CameraPreview v = (CameraPreview)vv;

                if (token.equals("take-picture")) {
                    final String path = ((StarwispActivity)ctx).m_AppDir+arr.getString(3);

                    v.TakePicture(
                        new PictureCallback() {
                            public void onPictureTaken(byte[] data, Camera camera) {
                                String datetime = getDateTime();
                                String filename = path+datetime + ".jpg";
                                SaveData(filename,data);
                                v.Shutdown();
                                ctx.finish();
                            }
                        });
                }

                if (token.equals("shutdown")) {
                    v.Shutdown();
                }

                return;
            }

            if (type.equals("seek-bar")) {
                SeekBar v = new SeekBar(ctx);
                if (token.equals("max")) {
                    // android seekbar bug workaround
                    int p=v.getProgress();
                    v.setMax(0);
                    v.setProgress(0);
                    v.setMax(arr.getInt(3));
                    v.setProgress(1000);

                    // not working.... :(
                }
            }


            if (type.equals("spinner")) {
                Spinner v = (Spinner)vv;

                if (token.equals("selection")) {
                    v.setSelection(arr.getInt(3));
                }

                if (token.equals("array")) {
                    final JSONArray items = arr.getJSONArray(3);
                    ArrayList<String> spinnerArray = new ArrayList<String>();

                    for (int i=0; i<items.length(); i++) {
                        spinnerArray.add(items.getString(i));
                    }

                    ArrayAdapter spinnerArrayAdapter =
                        new ArrayAdapter<String>(ctx,
                                                 android.R.layout.simple_spinner_item,
                                                 spinnerArray) {
                        public View getView(int position, View convertView,ViewGroup parent) {
                            View v = super.getView(position, convertView, parent);
                            ((TextView) v).setTypeface(((StarwispActivity)ctx).m_Typeface);
                            return v;
                        }
                    };

                    v.setAdapter(spinnerArrayAdapter);

                    final int wid = id;
                    // need to update for new values
                    v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
                            try {
                                CallbackArgs(ctx,ctxname,wid,"\""+items.getString(pos)+"\"");
                            } catch (JSONException e) {
                                Log.e("starwisp", "Error parsing data " + e.toString());
                            }
                        }
                        public void onNothingSelected(AdapterView<?> v) {}
                    });

                }
                return;
            }

        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data " + e.toString());
        }
    }

    public String WalkDraggable(StarwispActivity ctx, String name, String ctxname, int id) {
        View v=ctx.findViewById(id);
        Class c = v.getClass();
        String ret="(";
        if (c == LinearLayout.class) {
            LinearLayout l = (LinearLayout)v;

            ret+=m_Scheme.eval("(widget-callback \""+ctxname+"\" "+id+" '())")+" ";

            for (int i = 0; i < l.getChildCount(); i++) {
                View cv = l.getChildAt(i);
                Class cc = cv.getClass();
                if (cc == LinearLayout.class) {
                    ret+=WalkDraggable(ctx, name, ctxname, cv.getId());
                }
            }
        }
        ret+=")";

        return ret;
    }

    static public void SaveData(String path, byte[] data) {
        try {
            File file = new File(path);

            if (file == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        } catch (Exception e) {
        }
    }

	public static String getDateTime() {
		DateFormat df = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(new Date());
	}


  private static class MyDragShadowBuilder extends View.DragShadowBuilder {
    // The drag shadow image, defined as a drawable thing
    private static Drawable shadow;
        public MyDragShadowBuilder(View v) {
            super(v);
            // Creates a draggable image that will fill the Canvas provided by the system.
            shadow = new ColorDrawable(Color.BLUE);
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            int width, height;
            width = getView().getWidth()/2;
            height = getView().getHeight()/2;
            shadow.setBounds(0, 0, width, height);
            size.set(width, height);
            touch.set(width / 2, height / 2);
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {
            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }
    }
}
