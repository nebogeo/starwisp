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
