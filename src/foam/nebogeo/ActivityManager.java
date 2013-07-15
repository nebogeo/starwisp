package foam.nebogeo;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;

public class ActivityManager
{
    static public HashMap<String,Class> Activities;

    static public void Register(String name, Class actclass)
    {
        Activities.put(name,actclass);
    }

    static public void StartActivity(Activity src, String name, int requestcode)
    {
        Intent intent = new Intent(src,Activities.get(name));
        startActivityForResult(intent, requestcode);
    }
}
