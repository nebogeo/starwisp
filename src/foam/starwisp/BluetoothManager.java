// Starwisp Copyright (C) 2015 Foam Kernow
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

import android.content.Context;
import java.io.InputStream;
import java.io.OutputStream;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import java.util.Set;
import android.util.Log;
import java.io.IOException;

class BluetoothManager {

    private OutputStream outputStream;
    private InputStream inStream;

    static class Lock extends Object {}
    static public Lock mLock = new Lock();
    String m_CallbackName;
    StarwispActivity m_Context;
    StarwispBuilder m_Builder;
    boolean m_Connected;

    BluetoothManager() {
    }

    public void Start(StarwispActivity c, String name, StarwispBuilder b) {
        Log.i("starwisp","Bluetooth startup!");
        m_CallbackName=name;
        m_Context=c;
        m_Builder=b;

        try {
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();

                if(bondedDevices.size() > 0){
                    BluetoothDevice[] devices = (BluetoothDevice[]) bondedDevices.toArray();
                    BluetoothDevice device = devices[0];
                    ParcelUuid[] uuids = device.getUuids();
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                    socket.connect();
                    outputStream = socket.getOutputStream();
                    inStream = socket.getInputStream();
                    m_Connected = true;
                    StartReadThread();
                }

                Log.e("starwisp", "No appropriate paired devices.");
            }else{
                Log.e("starwisp", "Bluetooth is disabled.");
            }
        }
        } catch (Exception e) {
            Log.i("starwisp",e.toString());
            e.printStackTrace();
        }
    }

    public void Write(String s) {
        try {
        if (m_Connected) {
            Log.i("starwisp", "Writing bluetooth: "+s);
            outputStream.write(s.getBytes());
        }
        } catch (Exception e) {
            Log.i("starwisp",e.toString());
            e.printStackTrace();
        }
    }

    public void StartReadThread() {
        Runnable runnable = new Runnable() {
	        public void run() {

                final int BUFFER_SIZE = 1024;
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytes = 0;
                int b = BUFFER_SIZE;

                while (true) {
                    Log.i("starwisp", "Polling bluetooth");

                    try {
                        bytes = inStream.read(buffer, bytes, BUFFER_SIZE - bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String data = new String(buffer);

                    Log.i("starwisp","bluetooth recieved:"+data);

                    synchronized (mLock) {
                        m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,data);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        Log.i("starwisp",e.toString());
                        e.printStackTrace();
                    }

                }
            }
            };
        Thread mythread = new Thread(runnable);
        mythread.start();
    }


};
