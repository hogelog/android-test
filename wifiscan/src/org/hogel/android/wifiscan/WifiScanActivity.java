package org.hogel.android.wifiscan;

import java.util.List;

import android.app.ListActivity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class WifiScanActivity extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (manager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
        	List<ScanResult> results = manager.getScanResults();
        	final String[] items = new String[results.size()];
        	for (int i=0;i<results.size();++i) {
        		items[i] = results.get(i).SSID;
        	}
        	final ArrayAdapter<String> adapter = 
        		new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        	setListAdapter(adapter);
        }
    }
}