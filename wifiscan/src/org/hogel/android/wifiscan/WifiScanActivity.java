package org.hogel.android.wifiscan;

import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class WifiScanActivity extends ListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);

		// register WiFi scan results receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent arg1) {
				Toast.makeText(context, "receive wifi scan results", Toast.LENGTH_SHORT).show();

				List<ScanResult> results = manager.getScanResults();
				final String[] items = new String[results.size()];
				for (int i = 0; i < results.size(); ++i) {
					items[i] = results.get(i).SSID;
				}
				final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items);
				setListAdapter(adapter);
			}
		}, filter);

		if (manager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			// start WiFi Scan
			manager.startScan();
		}
	}
}