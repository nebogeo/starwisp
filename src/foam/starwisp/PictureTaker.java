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

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import java.util.List;

class PictureTaker
{
    public Camera mCam;
    public Boolean mTakingPicture;

    public PictureTaker() {
        mTakingPicture=false;
    }

    public void Startup(SurfaceView view) {
        mTakingPicture=false;
        OpenCamera(view);
    }

    public void Shutdown() {
        CloseCamera();
    }

    public List<Size> GetSupportedPictureSizes() {
        mCam = Camera.open();
        if (mCam == null) {
            return null;
        }
        List<Size> list = mCam.getParameters().getSupportedPictureSizes();
        mCam.release();
        mCam = null;
        return list;
    }

    private void OpenCamera(SurfaceView view) {
        try {
            mCam = Camera.open();
            if (mCam == null) {
                return;
            }
            mCam.setPreviewDisplay(view.getHolder());
            mCam.startPreview();
        }
        catch (Exception e) {
            Log.i("starwisp","Problem opening camera! " + e);
            return;
        }
    }

    private void CloseCamera() {
        if (mCam!=null) {
            mCam.stopPreview();
            mCam.release();
            mCam = null;
        }
    }

    public void TakePicture(SurfaceView view, PictureCallback picture)
    {
        if (!mTakingPicture) {
            mTakingPicture=true;
            CloseCamera();
            OpenCamera(view);

            try {
                mCam.takePicture(null, null, picture);
            }
            catch (Exception e) {
                Log.i("starwisp","Problem taking picture: " + e);
            }
        }
        else {
            Log.i("starwisp","Picture already being taken");
        }
    }
}
