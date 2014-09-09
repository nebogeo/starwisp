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

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import android.net.wifi.WifiManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import android.net.wifi.WifiConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.net.URLConnection;

import java.net.URL;
import java.net.HttpURLConnection;
import android.os.SystemClock;

public class NetworkManager {

    public enum State {
        IDLE, SCANNING, CONNECTED // what else?
    }

    WifiManager wifi;
	BroadcastReceiver receiver;
    public State state;
    String SSID;
    static class Lock extends Object {}
    static public Lock mLock = new Lock();

    String m_CallbackName;
    StarwispActivity m_Context;
    StarwispBuilder m_Builder;
    private static final String LINE_FEED = "\r\n";

    NetworkManager() {
        state = State.IDLE;
    }

    void Start(String ssid, StarwispActivity c, String name, StarwispBuilder b) {
        Log.i("starwisp","Network startup!");
        m_CallbackName=name;
        m_Context=c;
        m_Builder=b;

        if (state==NetworkManager.State.IDLE) {
            Log.i("starwisp","State is idle, launching scan");

            wifi = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
            state = State.SCANNING;
            SSID = ssid;
            wifi.startScan();
            receiver = new WiFiScanReceiver(SSID, this);
            c.registerReceiver(receiver, new IntentFilter(
                                   WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            // todo - won't work from inside fragments
            m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,"\"Scanning\"");
        }

        if (state==NetworkManager.State.CONNECTED) {
            Log.i("starwisp","State is connected, callback raised");
            m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,"\"Connected\"");
        }
    }

    void Connect() {
        Log.i("starwisp", "Attemping connect to "+SSID);

        List<WifiConfiguration> list = wifi.getConfiguredNetworks();

        Boolean found = false;

        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
                found = true;
                Log.i("starwisp", "Connecting (state=connected)");
                state=State.CONNECTED;
                wifi.disconnect();
                wifi.enableNetwork(i.networkId, true);
                wifi.reconnect();
                Log.i("starwisp", "Connected");
                try {
                    Thread.sleep(2000);
                    Log.i("starwisp","trying post-connection callback");
                    m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,"\"Connected\"");
                } catch (Exception e) {
                    Log.i("starwisp",e.toString());
                    e.printStackTrace();
                }

                break;
            }
        }

        if (!found) {
            Log.i("starwisp", "adding wifi config");
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + SSID + "\"";

//conf.wepKeys[0] = "\"" + networkPass + "\"";
//conf.wepTxKeyIndex = 0;
//conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifi.addNetwork(conf);
        }

    }

    public void StartRequestThread(final String url, final String t, final String callbackname) {
        Runnable runnable = new Runnable() {
	        public void run() {
                if (t.equals("upload")) { // this is a post file thing
                    try {
                        PostFile(url, new File(callbackname)); // callbackname = filename
                    }
                    catch (Exception e)
                    {
                        Log.i("starwisp","Problem uploading file "+callbackname);
                        Log.i("starwisp",e.toString());
                        e.printStackTrace();
                    }
                } else {
                    Request(url, t, callbackname);
                }
	        }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();
    }

    private class ReqMsg {
        ReqMsg(InputStream is, String t, String c) {
            m_Stream=is;
            m_Type=t;
            m_CallbackName=c;
        }
        public InputStream m_Stream;
        public String m_Type;
        public String m_CallbackName;
    }

    private void Request(String u, String type, String CallbackName) {
        try {
            Log.i("starwisp","pinging: "+u);
            URL url = new URL(u);
            HttpURLConnection con = (HttpURLConnection) url
                .openConnection();

            con.setUseCaches(false);
            con.setReadTimeout(100000 /* milliseconds */);
            con.setConnectTimeout(150000 /* milliseconds */);
            con.setRequestMethod("GET");
            con.setDoInput(true);
            // Starts the query
            con.connect();
            m_RequestHandler.sendMessage(
                Message.obtain(m_RequestHandler, 0,
                               new ReqMsg(con.getInputStream(),type,CallbackName)));

        } catch (Exception e) {
            Log.i("starwisp",e.toString());
            e.printStackTrace();
        }
    }

    public List<String> PostFile(String requestURL, File uploadFile)
        throws IOException {

        // creates a unique boundary based on time stamp
        String boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
        httpConn.setRequestProperty("Test", "Bonjour");
        OutputStream outputStream = httpConn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"),
                                             true);

        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
            "Content-Disposition: form-data; name=\"binary\";"
            + "filename=\"" + fileName + "\"")
            .append(LINE_FEED);
        writer.append(
            "Content-Type: "
            + URLConnection.guessContentTypeFromName(fileName))
            .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();


        List<String> response = new ArrayList<String>();

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response;
    }


    private Handler m_RequestHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            ReadStream((ReqMsg)msg.obj);
        }
    };


    private void ReadStream(ReqMsg m) {
        InputStream in = m.m_Stream;
        BufferedReader reader = null;
        try {

            if (m.m_Type.equals("download")) {
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

                // this is storage overwritten on each iteration with bytes
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                // we need to know how may bytes were read to write them to the byteBuffer
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                m_Builder.SaveData(m.m_CallbackName, byteBuffer.toByteArray());
            } else {
                // results in evaluating data read from via http - fix if used from net
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                String all = "";
                while ((line = reader.readLine()) != null) {
                    all+=line+"\n";
                }
                //Log.i("starwisp","got data for "+m.m_CallbackName+"["+all+"]");

                synchronized (mLock)
                {
                    m_Builder.DialogCallback(m_Context,m_Context.m_Name,m.m_CallbackName,all);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }




    private class WiFiScanReceiver extends BroadcastReceiver {
        private static final String TAG = "WiFiScanReceiver";

        public WiFiScanReceiver(String ssid, NetworkManager netm) {
            super();
            SSID=ssid;
            nm = netm;
        }

        public String SSID;
        public NetworkManager nm;

        @Override
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> results = nm.wifi.getScanResults();
            ScanResult bestSignal = null;

            if (nm.state==State.SCANNING) {
                Log.i("starwisp", "Scanning "+nm.state);


                for (ScanResult result : results) {
                    if (result.SSID.equals(SSID)) {
                        Log.i("starwisp", "scan result for..."+SSID);
                        m_Builder.DialogCallback(m_Context,m_Context.m_Name,m_CallbackName,"\"In range\"");
                        nm.Connect();
                        return;
                    }
                }

                if (nm.state==State.SCANNING) {
                    Log.i("starwisp", "REScanning "+nm.state);
                    nm.wifi.startScan();
                }
            }
        }
    }
}
