package hce_demo;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;

import hce_demo.IsoDepTransceiver.OnMessageReceived;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import hce.demo.R;

public class MainActivity extends Activity implements OnMessageReceived, ReaderCallback, SEService.CallBack {
	private SEService seService;
	Reader uicc = null;
	private NfcAdapter nfcAdapter;
	private ListView listView;
	private IsoDepAdapter isoDepAdapter;
	String LOG_TAG;

	public void serviceConnected(SEService service) {
		Log.i(LOG_TAG, "seviceConnected()");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		listView = (ListView)findViewById(R.id.listView);
		isoDepAdapter = new IsoDepAdapter(getLayoutInflater());
//		listView.setAdapter(isoDepAdapter);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);

		try {
			Log.i(LOG_TAG, "creating SEService object");
			seService = new SEService(this, this);
		} catch (SecurityException e) {
			Log.e(LOG_TAG, "Binding not allowed, uses-permission org.simalliance.openmobileapi.SMARTCARD?");
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception: " + e.getMessage());
		}

		Button button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Log.i(LOG_TAG, "Retrieve available readers...");
					Reader[] readers = seService.getReaders();
					for (int i = 0; i < readers.length; i++) {
						Log.i(LOG_TAG, "readers[i].getName() = " + readers[i].getName());
						if (readers[i].getName().contains("SIM")) {
							uicc = readers[i];
							break;
						}
					}

					if (uicc == null) {
						Log.e(LOG_TAG, "cardNotPresentException");
					}

					Log.e(LOG_TAG, "Connected to " + uicc.getName());

					Log.i(LOG_TAG, "Create Session from the first reader...");
					Session session = readers[0].openSession();

					Log.i(LOG_TAG, "Create logical channel within the session...");
					Channel channel = session.openLogicalChannel(new byte[]{
							(byte) 0xF0, 0x39, 0x41, 0x48, 0x14, (byte) 0x81, 0x00});

					Log.d(LOG_TAG, "Send add money command");
					byte[] respApdu = channel.transmit(new byte[]{(byte) 0x80, 0x01, 0x00, 0x00, 0x01, 0x64});

					channel.close();

					// Parse response APDU and show text but remove SW1 SW2 first 
					byte[] helloStr = new byte[respApdu.length - 2];
					System.arraycopy(respApdu, 0, helloStr, 0, respApdu.length - 2);
					Toast.makeText(MainActivity.this, new String(helloStr), Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Log.e(LOG_TAG, "Error occured:", e);
					return;
				}
			}
		});

		Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Log.i(LOG_TAG, "Retrieve available readers...");
					Reader[] readers = seService.getReaders();
					for (int i = 0; i < readers.length; i++) {
						Log.i(LOG_TAG, "readers[i].getName() = " + readers[i].getName());
						if (readers[i].getName().contains("SIM")) {
							uicc = readers[i];
							break;
						}
					}

					if (uicc == null) {
						Log.e(LOG_TAG, "cardNotPresentException");
					}

					Log.e(LOG_TAG, "Connected to " + uicc.getName());

					Log.i(LOG_TAG, "Create Session from the first reader...");
					Session session = readers[0].openSession();

					Log.i(LOG_TAG, "Create logical channel within the session...");
					Channel channel = session.openLogicalChannel(new byte[]{
							(byte) 0xF0, 0x39, 0x41, 0x48, 0x14, (byte) 0x81, 0x00});

					Log.d(LOG_TAG, "Send check balance command");
					byte[] respApdu = channel.transmit(new byte[]{(byte) 0x80, 0x03, 0x00, 0x00, 0x00});

					channel.close();

					// Parse response APDU and show text but remove SW1 SW2 first 
					byte[] helloStr = new byte[respApdu.length - 2];
					System.arraycopy(respApdu, 0, helloStr, 0, respApdu.length - 2);
					Toast.makeText(MainActivity.this, new String(helloStr), Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Log.e(LOG_TAG, "Error occured:", e);
					return;
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
//		nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
//				null);
	}

	@Override
	public void onPause() {
		super.onPause();
//		nfcAdapter.disableReaderMode(this);
	}

	@Override
	public void onTagDiscovered(Tag tag) {
		IsoDep isoDep = IsoDep.get(tag);
		IsoDepTransceiver transceiver = new IsoDepTransceiver(isoDep, this);
		Thread thread = new Thread(transceiver);
		thread.start();
	}

	@Override
	public void onMessage(final byte[] message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				isoDepAdapter.addMessage(new String(message));
			}
		});
	}

	@Override
	public void onError(Exception exception) {
		onMessage(exception.getMessage().getBytes());
	}

	@Override
	protected void onDestroy() {
		if (seService != null && seService.isConnected()) {
			seService.shutdown();
		}
		super.onDestroy();
	}

}
