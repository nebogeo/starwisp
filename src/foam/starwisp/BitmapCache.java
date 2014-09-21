// Starwisp Copyright (C) 2014 Dave Griffiths
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
import android.graphics.Bitmap;
import java.util.HashMap;
import android.media.ExifInterface;
import android.graphics.Matrix;
import android.graphics.BitmapFactory;
import java.io.IOException;
import android.util.Log;
import android.util.LruCache;
import java.io.FileOutputStream;

public class BitmapCache
{
    static LruCache<String, Bitmap> m_Cache;

    static {
        setup();
    }

    static void setup() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 10;

        m_Cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

    }

    static void ProcessInPlace(String Filename)
    {
        Log.i("starwisp-cache","processing bitmap: "+Filename);
        Bitmap bitmap = BitmapFactory.decodeFile(Filename);
        if (bitmap!=null) {
            // do the rotation here, so the photo shows the right way round
            try {
                ExifInterface exif = new ExifInterface(Filename);
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
                bitmap = Bitmap.createScaledBitmap(bitmap, 480, 640, false);

                FileOutputStream out = new FileOutputStream(Filename);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            }
            catch (IOException e) {
                Log.i("starwisp-cache","problem processing bitmap: "+Filename);
            }
        }
    }

    static Bitmap Load(String Filename)
    {
        Bitmap ret = m_Cache.get(Filename);

        if (ret != null)
        {
            Log.i("starwisp-cache","returning cached bitmap: "+Filename);
            return ret;
        }
        else
        {
            Log.i("starwisp-cache","making new bitmap: "+Filename);
            Bitmap bitmap = BitmapFactory.decodeFile(Filename);
            if (bitmap!=null) {
                // do the rotation here, so the photo shows the right way round
                try {
                    ExifInterface exif = new ExifInterface(Filename);
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

                    Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                    if (bitmap2 != bitmap){
                        Log.i("starwisp-cache","recycling bitmap");
                        bitmap.recycle();
                    }

                    //bitmap = Bitmap.createScaledBitmap(bitmap, 480, 640, false);
                    Bitmap newbitmap = Bitmap.createScaledBitmap(bitmap2, 240, 320, false);
                    //Bitmap newbitmap = Bitmap.createScaledBitmap(bitmap2, 120, 160, false);
                    if (newbitmap != bitmap2){
                        Log.i("starwisp-cache","recycling bitmap2");
                        bitmap2.recycle();
                    }

                    Log.i("starwisp-cache","success making new bitmap: "+Filename);

                    m_Cache.put(Filename,newbitmap);
                    return newbitmap;
                }
                catch (IOException e) {
                    Log.i("starwisp-cache","error making new bitmap: "+Filename);
                }
                return null;
            }
            //Log.i("starwisp-cache","error loading bitmap: "+Filename);
            return null;
        }
    }

}
