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
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;

public class ActivityManager
{
    static private HashMap<String,Class> m_Activities;

    static {
        m_Activities = new HashMap<String,Class>();
    }

    static public void Register(String name, Class actclass)
    {
        Log.i("starwisp","adding "+name+" to activity registry");
        m_Activities.put(name,actclass);
    }

    static public void StartActivity(Activity src, String name, int requestcode)
    {
        Class ActClass = m_Activities.get(name);
        if (ActClass == null)
        {
            Log.i("starwisp","activity "+name+" not found in registry");
        }
        else
        {
            Intent intent = new Intent(src,ActClass);
            src.startActivityForResult(intent, requestcode);
        }
    }
}
