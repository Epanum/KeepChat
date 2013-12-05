package com.ramis.keepchat;


import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new Settings()).commit();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}
}
