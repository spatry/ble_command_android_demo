/*
 * Copyright (C) 2015, Zentri, Inc. All Rights Reserved.
 *
 * The Zentri BLE Android Libraries and Zentri BLE example applications are provided free of charge
 * by Zentri. The combined source code, and all derivatives, are licensed by Zentri SOLELY for use
 * with devices manufactured by Zentri, or devices approved by Zentri.
 *
 * Use of this software on any other devices or hardware platforms is strictly prohibited.
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.zentri.zentri_ble_command_demo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.zentri.zentri_ble_command.BLECallbacks;
import com.zentri.zentri_ble_command.Command;
import com.zentri.zentri_ble_command.ErrorCode;
import com.zentri.zentri_ble_command.ZentriOSBLEManager;
import com.zentri.zentri_ble_command.Result;

public class ZentriOSBLEService extends Service
{
    public static final String ACTION_SCAN_RESULT = "ACTION_SCAN_RESULT";
    public static final String ACTION_CONNECTED = "ACTION_CONNECTED";
    public static final String ACTION_DISCONNECTED = "ACTION_DISCONNECTED";
    public static final String ACTION_MODE_WRITE = "ACTION_MODE_WRITE";
    public static final String ACTION_MODE_READ = "ACTION_MODE_READ";
    public static final String ACTION_STRING_DATA_WRITE = "ACTION_STRING_DATA_WRITE";
    public static final String ACTION_STRING_DATA_READ = "ACTION_STRING_DATA_READ";
    public static final String ACTION_BINARY_DATA_WRITE = "ACTION_BINARY_DATA_WRITE";
    public static final String ACTION_BINARY_DATA_READ = "ACTION_BINARY_DATA_READ";
    public static final String ACTION_COMMAND_SENT = "ACTION_COMMAND_SENT";
    public static final String ACTION_COMMAND_RESULT = "ACTION_COMMAND_RESULT";
    public static final String ACTION_ERROR = "ACTION_ERROR";


    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    public static final String EXTRA_COMMAND = "EXTRA_COMMAND";
    public static final String EXTRA_ID = "EXTRA_ID";
    public static final String EXTRA_RESPONSE_CODE = "EXTRA_RESPONSE_CODE";
    public static final String EXTRA_ERROR = "EXTRA_ERROR";

    static final boolean DISABLE_TX_NOTIFY = true;

    private static final String TAG = ZentriOSBLEService.class.getSimpleName();

    private final int mStartMode = START_NOT_STICKY;
    private final IBinder mBinder = new LocalBinder();
    boolean mAllowRebind = true;
    private ZentriOSBLEManager mZentriOSBLEManager;

    private BLECallbacks mCallbacks;
    private LocalBroadcastManager mBroadcastManager;

    public class LocalBinder extends Binder
    {
        ZentriOSBLEService getService()
        {
            // Return this instance of LocalService so clients can call public methods
            return ZentriOSBLEService.this;
        }
    }

    @Override
    public void onCreate()
    {
        // The service is being created
        Log.d(TAG, "Creating service");

        mZentriOSBLEManager = new ZentriOSBLEManager();
        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        initCallbacks();
        initTruconnectManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // The service is starting, due to a call to startService()
        return mStartMode;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // A client is binding to the service with bindService()
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent)
    {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy()
    {
        // The service is no longer used and is being destroyed
        Log.d(TAG, "Destroying service");

        if (mZentriOSBLEManager != null)
        {
            mZentriOSBLEManager.stopScan();
            mZentriOSBLEManager.disconnect(!DISABLE_TX_NOTIFY);//ensure all connections are terminated
            mZentriOSBLEManager.deinit();
        }
    }

    public ZentriOSBLEManager getManager()
    {
        return mZentriOSBLEManager;
    }

    public boolean initTruconnectManager()
    {
        return mZentriOSBLEManager.init(ZentriOSBLEService.this, mCallbacks);
    }

    private void initCallbacks()
    {
        mCallbacks = new BLECallbacks()
        {
            @Override
            public void onScanResult(String deviceName, String address)
            {
                Log.d(TAG, "onScanResult: " + deviceName + " " + address);

                Intent intent = new Intent(ACTION_SCAN_RESULT);
                intent.putExtra(EXTRA_DATA, deviceName);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onFirmwareVersionRead(String deviceName, String version)
            {
                Log.d(TAG, "onFirmwareVersionRead: " + deviceName + " version: " + version);
            }

            @Override
            public void onConnected(String deviceName, int services)
            {
                Log.d(TAG, "onConnected");

                Intent intent = new Intent(ACTION_CONNECTED);
                intent.putExtra(EXTRA_DATA, deviceName);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onDisconnected()
            {
                Log.d(TAG, "onDisconnected");

                Intent intent = new Intent(ACTION_DISCONNECTED);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onModeWritten(int mode)
            {
                Log.d(TAG, "onModeWritten");

                Intent intent = new Intent(ACTION_MODE_WRITE);
                intent.putExtra(EXTRA_MODE, mode);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onModeRead(int mode)
            {
                Log.d(TAG, "onModeRead");

                Intent intent = new Intent(ACTION_MODE_READ);
                intent.putExtra(EXTRA_MODE, mode);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onStringDataWritten(String data)
            {
                Log.d(TAG, "onStringDataWritten");

                Intent intent = new Intent(ACTION_STRING_DATA_WRITE);
                intent.putExtra(EXTRA_DATA, data);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onStringDataRead(String data)
            {
                Log.d(TAG, "onStringDataRead");

                Intent intent = new Intent(ACTION_STRING_DATA_READ);
                intent.putExtra(EXTRA_DATA, data);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onBinaryDataWritten(byte[] data)
            {
                Log.d(TAG, "onBinaryDataWritten");

                Intent intent = new Intent(ACTION_BINARY_DATA_WRITE);
                intent.putExtra(EXTRA_DATA, data);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onBinaryDataRead(byte[] data)
            {
                Log.d(TAG, "onBinaryDataRead");

                Intent intent = new Intent(ACTION_BINARY_DATA_READ);
                intent.putExtra(EXTRA_DATA, data);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onCommandSent(int id, Command command)
            {
                Log.d(TAG, "onCommandSent");

                Intent intent = new Intent(ACTION_COMMAND_SENT);
                intent.putExtra(EXTRA_COMMAND, command);
                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onCommandResult(int id, Command command, Result result)
            {
                Log.d(TAG, "onCommandResult");

                Intent intent = new Intent(ACTION_COMMAND_RESULT);
                intent.putExtra(EXTRA_COMMAND, command);
                intent.putExtra(EXTRA_ID, id);

                if (result != null)
                {
                    intent.putExtra(EXTRA_RESPONSE_CODE, result.getResponseCode());
                    intent.putExtra(EXTRA_DATA, result.getData());
                }

                mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onError(ErrorCode error)
            {
                Intent intent = new Intent(ACTION_ERROR);
                intent.putExtra(EXTRA_ERROR, error);
                mBroadcastManager.sendBroadcast(intent);

                Log.d(TAG, "onError - " + error);

            }
        };
    }

    public static int getMode(Intent intent)
    {
        return intent.getIntExtra(EXTRA_MODE, 0);
    }

    public static String getData(Intent intent)
    {
        return intent.getStringExtra(EXTRA_DATA);
    }

    public static Command getCommand(Intent intent)
    {
        return (Command)intent.getSerializableExtra(EXTRA_COMMAND);
    }

    public static int getResponseCode(Intent intent)
    {
        return intent.getIntExtra(EXTRA_RESPONSE_CODE, -1);
    }

    public static int getCommandID(Intent intent)
    {
        return intent.getIntExtra(EXTRA_ID, 0);
    }

    public static ErrorCode getErrorCode(Intent intent)
    {
        return (ErrorCode)intent.getSerializableExtra(EXTRA_ERROR);
    }
}