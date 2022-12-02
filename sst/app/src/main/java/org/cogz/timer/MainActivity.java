package org.cogz.timer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private int seq = -1;
    private long penaltyTime;
    private long startTime;
    private boolean setupMode;
    private boolean isPenalty;
    private Set<Integer> setupSeq = new LinkedHashSet<>();

    private TextView minText;
    private TextView secText;
    private TextView msText;
    private TextView setupText;
    private TextView penaltyText;

    private Button startBtn;
    private Button stopBtn;
    private Button resetBtn;
    private Button setupBtn;
    private Button clrBtn;
    private Button okBtn;

    private Handler btHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            char toggledSwitch = (char)((byte[]) msg.obj)[0];
            int toggledSwitchNum = toggledSwitch;
            if (setupMode) {
                setupSeq.add(toggledSwitchNum);
                setupText.setText(setupText.getText() + " " + toggledSwitch);
            } else {
                List<Integer> setupList = new ArrayList<>(setupSeq);
                if (setupList.isEmpty()) {
                    return;
                }
                for (int i = 0; i < setupList.size(); i++) {
                    if (toggledSwitchNum != setupList.get(seq + 1)) {
                        penaltyTime += Integer.parseInt((String) penaltyText.getText());
                        isPenalty = true;
                        break;
                    }
                }
                if (setupList.get(0) == toggledSwitchNum) {
                    startTimer();
                } else if (setupList.get(setupList.size() - 1) == toggledSwitchNum) {
                    stopTimer();
                } else if (!isPenalty) {
                    seq++;
                }
            }
        }
    };
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long currTime = System.currentTimeMillis() - startTime;
            int seconds = (int) (currTime / 1000);
            int minutes = seconds / 60;
            minText.setText(String.format("%02d", seconds / 60));
            secText.setText(String.format("%02d", seconds % 60));
            if (seconds != 0) {
                msText.setText(String.format("%03d", currTime % (seconds * 1000)));
            } else {
                msText.setText(String.format("%03d", currTime));
            }
            timerHandler.post(this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minText = findViewById(R.id.minText);
        secText = findViewById(R.id.secText);
        msText = findViewById(R.id.msText);
        setupText = findViewById(R.id.setupText);
        penaltyText = findViewById(R.id.penaltyText);

        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        resetBtn = findViewById(R.id.resetBtn);
        setupBtn = findViewById(R.id.setupBtn);
        clrBtn = findViewById(R.id.clrBtn);
        okBtn = findViewById(R.id.okBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                timerHandler.removeCallbacks(timerRunnable);
                minText.setText("00");
                secText.setText("00");
                msText.setText("000");
                seq = -1;
                penaltyTime = 0;
            }
        });

        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupBtn.setEnabled(false);
                clrBtn.setEnabled(true);
                okBtn.setEnabled(true);
                setupMode = true;
            }
        });

        clrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupBtn.setEnabled(false);
                clrBtn.setEnabled(true);
                okBtn.setEnabled(true);
                setupText.setText("");
                setupSeq.clear();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupBtn.setEnabled(true);
                clrBtn.setEnabled(false);
                okBtn.setEnabled(false);
                setupMode = false;
            }
        });

        try {
            connectBt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectBt() throws Exception {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        BluetoothDevice hc05 = btAdapter.getRemoteDevice("00:20:04:BD:A0:6F");
        System.out.println(hc05.getName());

        BtConnectThread btConnectThread = new BtConnectThread(btAdapter, hc05, btHandler);
        btConnectThread.start();
    }

    private void startTimer() {
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        startTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);
        seq = 0;
    }

    private void stopTimer() {
        stopBtn.setEnabled(false);
        timerHandler.removeCallbacks(timerRunnable);
        seq = -1;
        penaltyTime = 0;
    }
}