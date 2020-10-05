package net.contentcube.robot;

import net.contentcube.robot.nxt.NXTController;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ControlActivity extends Activity
{

	Receive rxThread;
	DatagramSocket gSocket;

	InetAddress serverAddr;
	int serverPort = 8002;


	private Button mForwardButton;
	private Button mBackwardButton;
	private Button mBrakeButton;
	private Button mTurnLeftButton;
	private Button mTurnRightButton;
	private Button mStartServiceButton;

	public void send( final DatagramSocket skt, final String msg ) throws UnknownHostException {

		serverAddr = InetAddress.getByName("192.168.2.108");

		if( serverAddr == null || serverPort == 0 ) return;

		final DatagramPacket pkt;
		pkt = new DatagramPacket( msg.getBytes(), msg.length(), serverAddr, serverPort );

		new Thread(new Runnable() {
			public void run() {
				try {
					skt.send( pkt );
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent)
		{	
			if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
			{
				AlertDialog.Builder dialog = new AlertDialog.Builder(ControlActivity.this);
				dialog.setTitle("Connection");
				dialog.setMessage("Connection lost to device");
				dialog.setCancelable(false);
				dialog.setPositiveButton("OK", new AlertDialog.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						ControlActivity.this.finish();
						
					}
				});
				
				dialog.create().show();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		
		final NXTController controller = NXTController.getInstance();




		try {
			gSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		try {
			send(gSocket, "nxt45;New User;");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		rxThread = new Receive(gSocket);

		new Thread(rxThread).start();

		mForwardButton = (Button) findViewById(R.id.button_forward);
		mForwardButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				controller.forward(500);
				
			}
		});
		
		mBackwardButton = (Button) findViewById(R.id.button_backward);
		mBackwardButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				controller.backward(500);
				
			}
		});
		
		
		mBrakeButton = (Button) findViewById(R.id.button_brake);	
		mBrakeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				controller.brake();
				
			}
		});
		
		mTurnLeftButton = (Button) findViewById(R.id.button_turn_left);	
		mTurnLeftButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				controller.turnLeft(500);
				
			}
		});
		
		mTurnRightButton = (Button) findViewById(R.id.button_turn_right);	
		mTurnRightButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				controller.turnRight(500);
				
			}
		});
		
		mStartServiceButton = (Button) findViewById(R.id.button_start_service);	
		mStartServiceButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent serviceIntent = new Intent(ControlActivity.this, ServiceActivity.class);
				ControlActivity.this.startActivity(serviceIntent);
				
			}
		});
	}
	
	private void registerConnectionReceiver()
    {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		registerReceiver(mConnectionReceiver, filter);
    }
	
	@Override
	protected void onStart()
	{
		super.onStart();
		registerConnectionReceiver();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mConnectionReceiver);
	}
}
class Receive implements Runnable {

	DatagramSocket rSocket = null;
	DatagramPacket rPacket = null;
	byte[] rMessage = new byte[12000];

	private volatile boolean stopRequested;

	public Receive(DatagramSocket sck) {
		this.rSocket = sck;

		stopRequested = false;
	}

	public void requestStop() {
		stopRequested = true;
	}

	public void run() {
		while (stopRequested == false) {
			try {// cjoo: debug
				rPacket = new DatagramPacket(rMessage, rMessage.length);
				rSocket.receive(rPacket);
				handlePacket(rPacket);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void handlePacket(DatagramPacket pkt) {
		String msg;
		int index;
		String msgType;

		msg = new String(rPacket.getData(), 0, pkt.getLength());

		final NXTController controller = NXTController.getInstance();

		if (msg.equals("moveforward")){
			controller.forward(500);
		}
		else if (msg.equals("backward")){
			controller.backward(500);
		}
		else if (msg.equals("turnleft")){
			controller.turnLeft(500);
		}
		else if (msg.equals("turnright")){
			controller.turnRight(500);
		}

	}

}