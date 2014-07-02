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

import android.widget.RelativeLayout;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.view.ViewGroup;
import android.graphics.Color;
import android.util.Log;

public class StarwispRelativeLayout
{

    public static int BuildLayoutParam(String p) {
        if (p.equals("fill-parent")) return RelativeLayout.LayoutParams.FILL_PARENT;
        if (p.equals("match-parent")) return RelativeLayout.LayoutParams.MATCH_PARENT;
        if (p.equals("wrap-content")) return RelativeLayout.LayoutParams.WRAP_CONTENT;
        try {
            return Integer.parseInt(p);
        } catch (NumberFormatException e) {
            Log.i("starwisp", "Layout error with ["+p+"]");
            // send error message
            return RelativeLayout.LayoutParams.WRAP_CONTENT;
        }
    }

    public static void AddRule(RelativeLayout.LayoutParams params, JSONArray rule) {
        try {
            String token = rule.getString(0);

            if (token.equals("parent-top")) {
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP); return;
            }
            if (token.equals("parent-bottom")) {
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM); return;
            }
            if (token.equals("right-of")) {
                int id = rule.getInt(1);
                params.addRule(RelativeLayout.RIGHT_OF, id);
                return;
            }
            if (token.equals("left-of")) {
                int id = rule.getInt(1);
                params.addRule(RelativeLayout.LEFT_OF, id);
                return;
            }
            if (token.equals("above")) {
                int id = rule.getInt(1);
                params.addRule(RelativeLayout.ABOVE, id);
                return;
            }
            if (token.equals("below")) {
                int id = rule.getInt(1);
                params.addRule(RelativeLayout.BELOW, id);
                return;
            }
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data StarwispRelativeLayout " + e.toString());
            return;
        }
    }

    public static void AddRules(RelativeLayout.LayoutParams params, JSONArray rules) {
        try {
            for (int i=0; i<rules.length(); i++) {
                AddRule(params,rules.getJSONArray(i));
            }
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data in StarwispRelativeLayout " + e.toString());
            return;
        }
    }

    public static RelativeLayout.LayoutParams BuildRelativeLayoutParams(JSONArray arr) {
        try {
            RelativeLayout.LayoutParams lp =
                new RelativeLayout.LayoutParams(BuildLayoutParam(arr.getString(1)),
                                                BuildLayoutParam(arr.getString(2)));
            JSONArray margin=arr.getJSONArray(3);
            lp.setMargins(margin.getInt(0),margin.getInt(1),
                          margin.getInt(2),margin.getInt(3));
            AddRules(lp,arr.getJSONArray(4));
            return lp;
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data in StarwispRelativeLayout " + e.toString());
            return null;
        }
    }

    // (id (w h margin (rules)) colour children)

    public static void Build(StarwispBuilder b, final StarwispActivity ctx, final String ctxname, JSONArray arr, ViewGroup parent) {
        try {
            RelativeLayout v = new RelativeLayout(ctx);
            v.setId(arr.getInt(1));
            v.setLayoutParams(BuildRelativeLayoutParams(arr.getJSONArray(2)));
            v.setPadding(2,2,2,2);
            Log.i("starwisp","SETTING BG COLOUR");
            JSONArray col = arr.getJSONArray(3);
            v.setBackgroundColor(Color.argb(col.getInt(3), col.getInt(0), col.getInt(1), col.getInt(2)));
            parent.addView(v);
            JSONArray children = arr.getJSONArray(4);
            for (int i=0; i<children.length(); i++) {
                b.Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
            }
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data in StarwispRelativeLayout " + e.toString());
        }
    }

    public static void Update(StarwispBuilder b, RelativeLayout v, String token, final StarwispActivity ctx, final String ctxname, JSONArray arr) {
        try {
            if (token.equals("contents")) {
                v.removeAllViews();
                JSONArray children = arr.getJSONArray(3);
                for (int i=0; i<children.length(); i++) {
                    b.Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
                }
            }
            if (token.equals("contents-add")) {
                Log.i("starwisp","adding to relative layout...");
                JSONArray children = arr.getJSONArray(3);
                for (int i=0; i<children.length(); i++) {
                    b.Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
                }
            }
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data in StarwispLinearLayout " + e.toString());
        }
    }
}
