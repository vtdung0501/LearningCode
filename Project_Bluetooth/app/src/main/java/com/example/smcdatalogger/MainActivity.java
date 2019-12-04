package com.example.smcdatalogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
public class MainActivity extends AppCompatActivity {
    Button btnBTOnOff, btnDiscover, btnNewPairDecives;
    Button btnSelectSensorUp, btnSelectSensorDown, btnSet;
    TextView txtStatus,txtReadBuffer;
    ListView lvDevices;
    GraphView graphView, graphView1;
    private double lastX=0;
    private double valueFlow =0;
    private double valuePressure = 0;
    public BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairDecives;
    private ArrayAdapter<String> mBTArrayAdapter;
    public Handler mHandler;
    public Runnable runnable;
    private  int i=1,j=1,k=1;
    CountDownTimer timer;
    private ConnectedThread mConnectedThread;
    private BluetoothSocket mBTSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final  int REQUESR_ENABLE_BT = 1;
    private static final int  MESSAGE_READ = 2;
    private static final int CONNECTING_STATUS = 3;
    private String readMessage;
    private LineGraphSeries<DataPoint> series, series1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        maps();
        mHandler= new Handler(){
            public void handleMessage(android.os.Message message){
                if (message.what == MESSAGE_READ){
                    readMessage=null;
                    try {
                        readMessage = new String((byte[])message.obj,"UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    txtReadBuffer.setText(readMessage );
                    if (readMessage!=null) {
                        String[] array = txtReadBuffer.getText().toString().split(":");
                       valueFlow = Double.parseDouble(array[0]);
                       valuePressure = Double.parseDouble(array[1]);
                    }
                    series = new LineGraphSeries<>();
                    series1 = new LineGraphSeries<>();

                    graphView.getViewport().setMinX(0);
                    //graphView.getViewport().setMaxX(1000);
                    graphView.getViewport().setScrollable(true);
                    graphView.getViewport().setScalable(true);
                    // graphView.getViewport().setMaxY(10);
                    series.setColor(Color.RED);
                    series1.setColor(Color.GREEN);
                    graphView1.getViewport().setMinX(0);
//                        graphView1.getViewport().setYAxisBoundsManual(true);
//                        graphView1.getViewport().setXAxisBoundsManual(true);
                    graphView1.getViewport().setScrollable(true);
                    graphView1.getViewport().setScalable(true);
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            lastX+=1d;
                                series.appendData(new DataPoint(lastX,valueFlow),true,100);
                                series1.appendData(new DataPoint(lastX,valuePressure),true,100);
                                mHandler.post(this);
                        }
                    };
                        mHandler.postDelayed(runnable,500);
                        graphView.addSeries(series);
                        graphView1.addSeries(series1);
                    }
                if (message.what == CONNECTING_STATUS){
                    if (message.arg1==1){
                        txtStatus.setText("Connected To Decive ");
                    }
                    else {
                        txtStatus.setText("Connected Failed");
                    }
                }
            }
        };
        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        lvDevices.setAdapter(mBTArrayAdapter);
        lvDevices.setOnItemClickListener(mDeciveClickListener);
        if (mBTArrayAdapter == null){
            txtStatus.setText("Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth decive not found !",Toast.LENGTH_LONG).show();
        }
        else {
            btnSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mConnectedThread!=null){
                        k++;
                        if(k==10||mConnectedThread==null) k=1;
                        mConnectedThread.write("2");
                        if(i==1)Toast.makeText(MainActivity.this,"1st sensor selected",Toast.LENGTH_LONG).show();
                        if(i==2)Toast.makeText(MainActivity.this,"2nd sensor selected",Toast.LENGTH_LONG).show();
                        if(i==3)Toast.makeText(MainActivity.this,"3rd sensor selected",Toast.LENGTH_LONG).show();
                        if (j == 1)
                            Toast.makeText(MainActivity.this, "\n" +
                                    "Sampling time is selected 1 second", Toast.LENGTH_LONG).show();
                        if (j == 2)
                            Toast.makeText(MainActivity.this, "\n" +
                                    "Sampling time is selected 5 seconds", Toast.LENGTH_LONG).show();
                        if (j == 3)
                            Toast.makeText(MainActivity.this, "\n" +
                                    "Sampling time is selected 15 seconds", Toast.LENGTH_LONG).show();
                        if (j == 4)
                            Toast.makeText(MainActivity.this, "\n" +
                                    "Sampling time is selected 30 seconds", Toast.LENGTH_LONG).show();
                        if (j == 5)
                            Toast.makeText(MainActivity.this, "\n" +
                                    "Sampling time is selected 1 minutes", Toast.LENGTH_LONG).show();
                        if (j == 6)
                            Toast.makeText(MainActivity.this, "\n" +
                                    "Sampling time is selected 5 minutes", Toast.LENGTH_LONG).show();
                        if (j == 7)
                            Toast.makeText(MainActivity.this, "\n" +
                                    "Sampling time is selected 15 minutes", Toast.LENGTH_LONG).show();
                        if (j == 8)
                            Toast.makeText(MainActivity.this, "\n" +
                                    "Sampling time is selected 30 minutes", Toast.LENGTH_LONG).show();
                        if (j == 9)
                            Toast.makeText(MainActivity.this, "\n" +
                                    "Sampling time is selected 60 minutes", Toast.LENGTH_LONG).show();
                    }
                }
            });
            btnSelectSensorUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mConnectedThread!=null){
                        mConnectedThread.write("1");
                       if(k==1){
                           i++;
                           if(i==1)
                               Toast.makeText(MainActivity.this,"Select the 1st sensor",Toast.LENGTH_LONG).show();
                           if(i==2)
                               Toast.makeText(MainActivity.this,"Select the 2nd sensor",Toast.LENGTH_LONG).show();
                           if(i==3)
                               Toast.makeText(MainActivity.this,"Select the 3rd sensor",Toast.LENGTH_LONG).show();
                           if(i==4) {
                               i=1;
                               Toast.makeText(MainActivity.this,"Select the 1st sensor",Toast.LENGTH_LONG).show();
                           }
                       }
                       if (k>1) {
                            j++;
                            if (j == 1)
                                Toast.makeText(MainActivity.this, "Sampling time is 1 second", Toast.LENGTH_LONG).show();
                            if (j == 2)
                                Toast.makeText(MainActivity.this, "Sampling time is 5 seconds", Toast.LENGTH_LONG).show();
                            if (j == 3)
                                Toast.makeText(MainActivity.this, "Sampling time is 15 seconds", Toast.LENGTH_LONG).show();
                            if (j == 4)
                                Toast.makeText(MainActivity.this, "Sampling time is 30 seconds", Toast.LENGTH_LONG).show();
                            if (j == 5)
                                Toast.makeText(MainActivity.this, "Sampling time is 1 minute", Toast.LENGTH_LONG).show();
                            if (j == 6)
                                Toast.makeText(MainActivity.this, "Sampling time is 5 minutes", Toast.LENGTH_LONG).show();
                            if (j == 7)
                                Toast.makeText(MainActivity.this, "Sampling time is 15 minute", Toast.LENGTH_LONG).show();
                            if (j == 8)
                                Toast.makeText(MainActivity.this, "Sampling time is 30 minutes", Toast.LENGTH_LONG).show();
                            if (j == 9)
                                Toast.makeText(MainActivity.this, "Sampling time is 60 minutes", Toast.LENGTH_LONG).show();
                            if (j == 10) {
                                j = 1;
                                Toast.makeText(MainActivity.this, "Sampling time is 1 second", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            });
            btnSelectSensorDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mConnectedThread!=null){
                        mConnectedThread.write("3");
                    }
                }
            });
            btnDiscover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listPairDecives(view);
                }
            });

            btnNewPairDecives.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    discover(view);
                }
            });
        }

    }
   @Override
   protected void onActivityResult (int requestCode, int resultCode, Intent Data) {

       super.onActivityResult(requestCode, resultCode, Data);
       if (requestCode == REQUESR_ENABLE_BT){
           if (resultCode == RESULT_OK){
               txtStatus.setText("Enable");
           }
           else txtStatus.setText("Disable");
       }
   }

    private void discover(View view){
        if (mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stop",Toast.LENGTH_LONG).show();
        }
        else {
            if (mBTAdapter.isEnabled()){
                mBTArrayAdapter.clear();
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(),"Discovery started",Toast.LENGTH_LONG).show();
                registerReceiver(blReceiver,new IntentFilter(BluetoothDevice.ACTION_FOUND));
                checkBTPermission();
            }
            else {
                Toast.makeText(getApplicationContext(),"Bluetooth not on",Toast.LENGTH_LONG).show();
            }
        }
    }
    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairDecives(View view){
        mPairDecives = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()){
            for (BluetoothDevice device :mPairDecives)
                mBTArrayAdapter.add(device.getName()+"\n"+device.getAddress());
            Toast.makeText(getApplicationContext(),"Show pair decives",Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(getApplicationContext(),"Bluetooth not on",Toast.LENGTH_LONG).show();
    }

    private AdapterView.OnItemClickListener mDeciveClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (!mBTAdapter.isEnabled()){
                Toast.makeText(getBaseContext(),"Bluetooth not on",Toast.LENGTH_LONG).show();
                return;
            }

            txtStatus.setText("Connecting...");
            String infor = ((TextView)view).getText().toString();
            final String address = infor.substring(infor.length() - 17);
            final String name = infor.substring(infor.length() - 17);
            mBTAdapter.cancelDiscovery();
             new Thread(){
                public void run(){
                    boolean fail = false;
                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                    try {
                        mBTSocket = createBluetoothSocket(device);
                    }
                    catch (IOException e){
                        fail = true;
                        Toast.makeText(getBaseContext(),"Socket create failed",Toast.LENGTH_SHORT).show();
                    }
                    try {
                        mBTSocket.connect();
                    }catch (IOException e){
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1,-1).sendToTarget();
                        }catch (IOException e2){
                            Toast.makeText(getBaseContext(),"Socket create failed",Toast.LENGTH_LONG).show();
                        }
                    }
                    if (fail == false){
                        mConnectedThread = new  ConnectedThread(mBTSocket);
                        mConnectedThread.start();
                        mHandler.obtainMessage(CONNECTING_STATUS,1,-1).sendToTarget();
                    }
                }
            }.start();
        }
    };
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public void btnBTOnOff(View view) {
        bluetoothOnOff();
    }

    private void bluetoothOnOff() {
        if (!mBTAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent,REQUESR_ENABLE_BT);
            btnBTOnOff.setBackgroundResource(R.drawable.bluetooth_on);
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_LONG).show();
        }
        if (mBTAdapter.isEnabled()){
            mBTAdapter.disable();
            btnBTOnOff.setBackgroundResource(R.drawable.bluetooth_off);
            Toast.makeText(getApplicationContext(),"Bluetooth turned off",Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectedThread extends  Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private  final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e){}
            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }
        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;


            while (true){
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0){
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer,0,bytes);
                        mHandler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    break;
                }
            }
        }
        public void write(String input){
            byte[] bytes = input.getBytes();
            try{
                mmOutStream.write(bytes);
            }catch (IOException e){}
        }
        public void cancel(){
            try {
                mmSocket.close();
            }catch (IOException e){}
        }
    }
    public void maps(){
        btnBTOnOff = (Button)findViewById(R.id.btnBTOnOff);
        btnDiscover = (Button)findViewById(R.id.btnDiscover);
        btnNewPairDecives = (Button)findViewById(R.id.btnNewPairDecives);
        btnSelectSensorUp = (Button)findViewById(R.id.btnSelectSensorUp);
        btnSelectSensorDown = (Button)findViewById(R.id.btnSelectSensorDown);
        btnSet = (Button)findViewById(R.id.btnSet);
        txtStatus = (TextView)findViewById(R.id.txtStatus);
        txtReadBuffer= (TextView)findViewById(R.id.txtReadBuffer);
        lvDevices = (ListView)findViewById(R.id.lvDecives);
        graphView = (GraphView)findViewById(R.id.graphView);
        graphView1 = (GraphView)findViewById(R.id.graphView1);
    }
    private void checkBTPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck !=0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1001);
            }
            else {
                Toast.makeText(MainActivity.this,"No need check permission. 50K version < LOLLIPOP",Toast.LENGTH_LONG).show();
            }

        }
    }
}
