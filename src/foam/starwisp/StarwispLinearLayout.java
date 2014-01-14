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

import android.widget.LinearLayout;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.view.ViewGroup;
import android.graphics.Color;
import android.util.Log;

public class StarwispLinearLayout
{
    public static void Build(StarwispBuilder b, final StarwispActivity ctx, final String ctxname, JSONArray arr, ViewGroup parent) {
        try {
            LinearLayout v = new LinearLayout(ctx);
            v.setId(arr.getInt(1));
            v.setOrientation(b.BuildOrientation(arr.getString(2)));
            v.setLayoutParams(b.BuildLayoutParams(arr.getJSONArray(3)));
            //v.setPadding(2,2,2,2);
            JSONArray col = arr.getJSONArray(4);
            v.setBackgroundColor(Color.argb(col.getInt(3), col.getInt(0), col.getInt(1), col.getInt(2)));
            parent.addView(v);
            JSONArray children = arr.getJSONArray(5);
            for (int i=0; i<children.length(); i++) {
                b.Build(ctx,ctxname,new JSONArray(children.getString(i)), v);
            }
        } catch (JSONException e) {
            Log.e("starwisp", "Error parsing data in StarwispLinearLayout " + e.toString());
        }
    }

    public static void Update(StarwispBuilder b, LinearLayout v, String token, final StarwispActivity ctx, final String ctxname, JSONArray arr) {
        try {
            if (token.equals("contents")) {
                v.removeAllViews();
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
