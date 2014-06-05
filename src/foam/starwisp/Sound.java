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

import android.os.Bundle;
import android.os.Environment;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import java.io.IOException;

class SoundManager {

    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;

    void Shutdown() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    void StartRecording(String filename) {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(filename);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        Log.i("starwisp","starting to record:"+filename);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("startwisp", "sound record: prepare() failed");
        }

        mRecorder.start();
    }

    void StopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    void StartPlaying(String filename) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(filename);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("starwisp", "sound play: prepare() failed");
        }
    }

    void StopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

};
