package com.gnss.Dog;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {

	public boolean isOnLongClick = false;
	private MyThread myThread = null;
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Layout Views
	private TextView mTitle;
	private ListView mConversationView;
	private Button btn_foward;
	private Button btn_back;
	private Button btn_stop;
	private Button btn_left;
	private Button btn_right;
	private Button btn_turnRotate;
	private Button btn_up;
	private Button btn_down;
	private Button btn_x;
	private Button btn_y;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	int cout = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	private int mBackKeyPressedTimes = 0;

	@Override
	public void onBackPressed() {
		if (mBackKeyPressedTimes == 0) {
			openOptionsMenu();
			Toast.makeText(this, "再按一次退出程序 ", Toast.LENGTH_SHORT).show();
			mBackKeyPressedTimes = 1;
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						mBackKeyPressedTimes = 0;
					}
				}
			}.start();
			return;

		}
		super.onBackPressed();
	} // @Override

	// public void onBackPressed() {
	// Log.i(TAG, "back button pressed");
	//
	// Toast.makeText(getApplicationContext(), "打开菜单", 0).show();
	// openOptionsMenu();
	// super.onBackPressed();
	// }

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				sendCarMessage("f");
				break;
			case 2:
				sendCarMessage("b");
				break;
			case 3:
				sendCarMessage("l");
				break;
			case 4:
				sendCarMessage("r");
				break;
			case 5:
				sendCarMessage("z");
				break;
			default:
				break;
			}
		}
	};

	public class MyThread extends Thread {

		private String sendMessage;

		public MyThread(String sendMessage) {
			this.sendMessage = sendMessage;
		}

		@Override
		public void run() {
			while (isOnLongClick) {
				try {
					Thread.sleep(200);// 线程暂停200毫秒
					Message message = new Message();
					if (sendMessage == "f") {
						message.what = 1;
					}
					if (sendMessage == "b") {
						message.what = 2;
					}
					if (sendMessage == "l") {
						message.what = 3;
					}
					if (sendMessage == "r") {
						message.what = 4;
					}
					if (sendMessage == "z") {
						message.what = 5;
					}
					handler.sendMessage(message);// 发送消息
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// new Thread(new MyThread()).start();

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key

		btn_foward = (Button) findViewById(R.id.btn_foward);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_left = (Button) findViewById(R.id.btn_left);
		btn_right = (Button) findViewById(R.id.btn_right);
		btn_turnRotate = (Button) findViewById(R.id.btn_turnRotate);
		btn_up = (Button) findViewById(R.id.btn_up);
		btn_down = (Button) findViewById(R.id.btn_down);
		btn_x = (Button) findViewById(R.id.btn_x);
		btn_y = (Button) findViewById(R.id.btn_y);

		// menjin button
		btn_foward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent evt) {

				switch (evt.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					String message = "f";

					myThread = new MyThread(message);
					isOnLongClick = true;
					myThread.start();
					break;
				}
				case MotionEvent.ACTION_UP: {
					// Send a message using content of the edit text widget
					String message = "s";
					sendCarMessage(message);
					if (myThread != null) {
						isOnLongClick = false;
					}
					break;
				}
				}

				return false;
			}

		});
		btn_back.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent evt) {
				switch (evt.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					// Send a message using content of the edit text widget
					String message = "b";

					myThread = new MyThread(message);
					isOnLongClick = true;
					myThread.start();
					break;
				}
				case MotionEvent.ACTION_UP: {
					// Send a message using content of the edit text widget
					String message = "s";
					sendCarMessage(message);
					if (myThread != null) {
						isOnLongClick = false;
					}
					break;
				}
				}

				return false;
			}

		});

		// curtain down
		btn_left.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent evt) {
				switch (evt.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					// Send a message using content of the edit text widget
					String message = "l";

					myThread = new MyThread(message);
					isOnLongClick = true;
					myThread.start();
					break;
				}
				case MotionEvent.ACTION_UP: {
					// Send a message using content of the edit text widget
					String message = "s";
					sendCarMessage(message);
					if (myThread != null) {
						isOnLongClick = false;
					}
					break;
				}
				}

				return false;
			}

		});

		// curtain stop
		btn_right.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent evt) {
				switch (evt.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					// Send a message using content of the edit text widget
					String message = "r";

					myThread = new MyThread(message);
					isOnLongClick = true;
					myThread.start();
					break;
				}
				case MotionEvent.ACTION_UP: {
					// Send a message using content of the edit text widget
					String message = "s";
					sendCarMessage(message);
					if (myThread != null) {
						isOnLongClick = false;
					}
					break;
				}
				}

				return false;
			}

		});

		btn_turnRotate.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent evt) {
				switch (evt.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					// Send a message using content of the edit text widget
					String message = "z";

					myThread = new MyThread(message);
					isOnLongClick = true;
					myThread.start();
					break;
				}
				case MotionEvent.ACTION_UP: {
					// Send a message using content of the edit text widget
					String message = "s";
					sendCarMessage(message);
					if (myThread != null) {
						isOnLongClick = false;
					}
					break;
				}
				}

				return false;
			}

		});
		btn_up.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				String message = "u";
				sendCarMessage(message);
			}
		});
		btn_down.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				String message = "d";
				sendCarMessage(message);
			}
		});
		btn_x.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				String message = "x";
				sendCarMessage(message);
			}
		});
		btn_y.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				String message = "y";
				sendCarMessage(message);
			}
		});

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendCarMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
		}
	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendCarMessage(message);
			}
			if (D)
				Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("send message is:  "
						+ writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				long time;
				Calendar now = Calendar.getInstance();
				TimeZone timeZone = now.getTimeZone();

				long totalMilliseconds = System.currentTimeMillis()
						+ timeZone.getRawOffset();
				long totalSeconds = totalMilliseconds / 1000;
				int currentSecond = (int) (totalSeconds % 60);
				long totalMinutes = totalSeconds / 60;
				int currentMinute = (int) (totalMinutes % 60);
				long totalHours = totalMinutes / 60;
				int currentHour = (int) (totalHours % 24);
				int totalDays = (int) (totalHours / 24);

				int goDays = 0;
				int surplusDays = 0;
				int goYears = 0;
				int leapyear = 0;

				for (int i = 1970; goDays < totalDays; i++) {
					if (i % 400 == 0 || (i % 4 == 0 && i % 100 != 0)) {
						goDays = goDays + 366;
						leapyear = 1;
					} else {
						goDays = goDays + 365;
						leapyear = 0;
					}
					goYears++;
				}
				String readMessage = new String(readBuf, 0, msg.arg1);
				char ch = readMessage.charAt(0);
				if (ch == 'T') {

					cout++;
					mConversationArrayAdapter.add(currentHour + ":"
							+ currentMinute + ":" + currentSecond
							+ ",distance is :" + cout + "cm");
				} else {
					mConversationArrayAdapter.add(currentHour + ":"
							+ currentMinute + ":" + currentSecond
							+ ",distance is:" + readMessage);
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
		case R.id.insecure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent,
					REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	

}
