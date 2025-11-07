/*
 * Copyright 2019 F1ReKing.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.f1reking.android_serialport;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import me.f1reking.serialportlib.SerialPortHelper;
import me.f1reking.serialportlib.entity.DATAB;
import me.f1reking.serialportlib.entity.FLOWCON;
import me.f1reking.serialportlib.entity.PARITY;
import me.f1reking.serialportlib.entity.STOPB;
import me.f1reking.serialportlib.listener.IOpenSerialPortListener;
import me.f1reking.serialportlib.listener.ISerialPortDataListener;
import me.f1reking.serialportlib.listener.Status;

/**
 * @author F1ReKing
 * @date 2019/11/1 09:38
 * @Description
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    protected EditText etInput;
    protected Button btnSend;
    protected Button btnOpen;
    protected Button btnClose;
    private AppCompatTextView tv_log;

    private SerialPortHelper mSerialPortHelper;
    private LinearLayoutCompat log_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_send) {
            String str = etInput.getText().toString();
            if (mSerialPortHelper != null) {
                mSerialPortHelper.sendTxt(str);
            }
        } else if (view.getId() == R.id.btn_open) {
            open();
        } else if (view.getId() == R.id.btn_close) {
            close();
        }
    }

    private void open() {
        if (mSerialPortHelper == null) {
            mSerialPortHelper = new SerialPortHelper();
            mSerialPortHelper.setPort("/dev/ttyS2");
            mSerialPortHelper.setBaudRate(19200);
            mSerialPortHelper.setStopBits(STOPB.getStopBit(STOPB.B1));
            mSerialPortHelper.setDataBits(DATAB.getDataBit(DATAB.CS8));
            mSerialPortHelper.setParity(PARITY.getParity(PARITY.NONE));
            mSerialPortHelper.setFlowCon(FLOWCON.getFlowCon(FLOWCON.NONE));
        }
        Log.i(TAG, "open: " + Arrays.toString(mSerialPortHelper.getAllDeicesPath()));
        mSerialPortHelper.setIOpenSerialPortListener(new IOpenSerialPortListener() {
            @Override
            public void onSuccess(final File device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, device.getPath() + " :串口打开成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFail(final File device, final Status status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (status) {
                            case NO_READ_WRITE_PERMISSION:
                                Toast.makeText(MainActivity.this, device.getPath() + " :没有读写权限", Toast.LENGTH_SHORT).show();
                                break;
                            case OPEN_FAIL:
                            default:
                                Toast.makeText(MainActivity.this, device.getPath() + " :串口打开失败", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            }
        });
        StringBuilder sb = new StringBuilder();
        mSerialPortHelper.setISerialPortDataListener(new ISerialPortDataListener() {
            @Override
            public void onDataReceived(byte[] bytes) {
                /*byte[] test = new byte[]{};
                for (int y = 0; y < 100; y++) {
                    test = mergeByteArrays(test, bytes);
                }*/
//                String str = new String(bytes, StandardCharsets.UTF_8);
                String str=Util.bytesToHex(bytes);
                sb.append(Util.formatCurrentTime("HH:mm:ss") + "--" + str);
                sb.append("\n");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_log.setText("");
                        tv_log.setText(sb.toString());
                        /*AppCompatTextView appCompatTextView = new AppCompatTextView(MainActivity.this);
                        appCompatTextView.setText(str);
                        log_root.addView(appCompatTextView);*/
                    }
                });
//                Log.i(TAG, "onDataReceived: " + Arrays.toString(bytes));
                Log.i(TAG, "onDataReceived: " + str);
            }

            @Override
            public void onDataSend(byte[] bytes) {
                Log.i(TAG, "onDataSend: " + Arrays.toString(bytes));
            }
        });
        Log.i(TAG, "open: " + mSerialPortHelper.open());
    }

    private void close() {
        if (mSerialPortHelper != null) {
            mSerialPortHelper.close();
        }
    }

    private void initView() {
        etInput = (EditText) findViewById(R.id.et_input);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(MainActivity.this);
        btnOpen = (Button) findViewById(R.id.btn_open);
        btnOpen.setOnClickListener(MainActivity.this);
        tv_log = findViewById(R.id.tv_log);
        btnClose = (Button) findViewById(R.id.btn_close);
        btnClose.setOnClickListener(MainActivity.this);
        log_root = findViewById(R.id.log_root);

    }

    /**
     * 合并多个 byte 数组
     *
     * @param arrays 待合并的 byte 数组（支持任意数量，可含空数组或 null）
     * @return 合并后的新 byte 数组
     */
    public static byte[] mergeByteArrays(byte[]... arrays) {
        if (arrays == null || arrays.length == 0) {
            return new byte[0]; // 空输入返回空数组
        }

        // 1. 计算总长度（跳过 null 和空数组）
        int totalLength = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
            }
        }

        // 2. 创建总长度的新数组
        byte[] result = new byte[totalLength];

        // 3. 依次复制各数组元素到新数组
        int offset = 0; // 偏移量：记录当前复制到新数组的位置
        for (byte[] array : arrays) {
            if (array != null && array.length > 0) {
                // 从 array 的 0 位置，复制 array.length 个元素到 result 的 offset 位置
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length; // 更新偏移量
            }
        }

        return result;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "使用Home或Menu退出？", Toast.LENGTH_SHORT).show();
//        super.onBackPressed();
    }
}
