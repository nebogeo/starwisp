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

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Surface;
import android.widget.Toast;
import java.io.IOException;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Display;
import android.view.WindowManager;
import android.content.Context;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;

public class CameraPreview extends SurfaceView implements
        SurfaceHolder.Callback {

    private SurfaceHolder mSurfaceHolder;
    public PictureTaker mPictureTaker;
    Context mCtx;

    // Constructor that obtains context and camera
    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, PictureTaker picturetaker) {
        super(context);
        mCtx=context;
        mPictureTaker=picturetaker;
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void TakenPicture() {
        mPictureTaker.TakenPicture();
    }

    public void TakePicture(PictureCallback cb) {
        mPictureTaker.TakePicture(this,cb);
    }

    public void Shutdown() {
        mPictureTaker.Shutdown();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder=surfaceHolder;
        mPictureTaker.Startup(this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
            int width, int height) {
        mSurfaceHolder=surfaceHolder;
        mPictureTaker.mCam.stopPreview();

        Parameters parameters = mPictureTaker.mCam.getParameters();
        List<Size> list = parameters.getSupportedPreviewSizes();

        int minHeight=9999999;
        int minWidth=0;
        for(int i = 0; i<list.size(); i++){
            if(list.get(i).height<minHeight){
                minHeight = list.get(i).height;
                minWidth = list.get(i).width;
            }
        }

        Log.i("starwisp","camera layout: "+minWidth+" "+minHeight);
        parameters.setPreviewSize(minWidth, minHeight);

        list = parameters.getSupportedPictureSizes();
        int maxHeight=0;
        int maxWidth=0;
        for(int i = 0; i<list.size(); i++){
            if(list.get(i).height>maxHeight){
                maxHeight = list.get(i).height;
                maxWidth = list.get(i).width;
            }
        }
        Log.i("starwisp","camera picture size: "+maxWidth+" "+maxHeight);
        parameters.setPictureSize(maxWidth, maxHeight);

        mPictureTaker.mCam.setParameters(parameters);
        mPictureTaker.mCam.startPreview();

/*
        Parameters parameters = mPictureTaker.mCam.getParameters();
        Display display = ((WindowManager)mCtx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0)
        {
            parameters.setPreviewSize(height, width);
            mPictureTaker.mCam.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
            parameters.setPreviewSize(width, height);
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
            parameters.setPreviewSize(height, width);
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
            parameters.setPreviewSize(width, height);
            mPictureTaker.mCam.setDisplayOrientation(180);
        }

        mPictureTaker.mCam.setParameters(parameters);
        mPictureTaker.mCam.startPreview();
*/

    }
}
