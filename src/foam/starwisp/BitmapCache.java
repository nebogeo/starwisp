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

public class BitmapCache
{
    static HashMap<String,Bitmap> m_Cache;

    static {
        m_Cache = new HashMap<String,Bitmap>();
    }

    static Bitmap Load(String Filename)
    {
        if (m_Cache.containsKey(Filename))
        {
            Log.i("starwisp","returning cached bitmap...");
            return m_Cache.get(Filename);
        }
        else
        {
            Bitmap bitmap = BitmapFactory.decodeFile(Filename);
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

                m_Cache.put(Filename,bitmap);
                return bitmap;

            }
            catch (IOException e) {
            }

        }
        return null;
    }

}
