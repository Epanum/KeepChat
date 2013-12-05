package com.ramis.keepchat;

import android.content.Intent;
import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class Settings extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	public static final String PREF_KEY_SNAP_IMAGES = "pref_key_snaps_images";
	public static final String PREF_KEY_SNAP_VIDEOS = "pref_key_snaps_videos";
	public static final String PREF_KEY_STORIES_IMAGES = "pref_key_stories_images";
	public static final String PREF_KEY_STORIES_VIDEOS = "pref_key_stories_videos";
	public static final String PREF_KEY_TOASTS_DURATION = "pref_key_toasts_duration";
	public static final String PREF_KEY_SAVE_LOCATION = "pref_key_save_location";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesMode(1);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// check if preference exists in SharedPreferences
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		if (sharedPref.contains(PREF_KEY_SAVE_LOCATION) == false) {
			// set default value
			String root = Environment.getExternalStorageDirectory().toString();
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(PREF_KEY_SAVE_LOCATION, root + "/keepchat");
			editor.apply();
		}

		// set on click listener
		Preference locationChooser = findPreference(PREF_KEY_SAVE_LOCATION);
		locationChooser
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {

						// opens new activity which asks the user to choose path
						final Intent chooserIntent = new Intent(getActivity(),
								DirectoryChooserActivity.class);
						startActivityForResult(chooserIntent, 0);
						return true;
					}
				});

		updateSummary(PREF_KEY_SNAP_IMAGES);
		updateSummary(PREF_KEY_SNAP_VIDEOS);
		updateSummary(PREF_KEY_STORIES_IMAGES);
		updateSummary(PREF_KEY_STORIES_VIDEOS);
		updateSummary(PREF_KEY_TOASTS_DURATION);
		updateSummary(PREF_KEY_SAVE_LOCATION);
	}

	@Override
	// function runs when the path chooser activity returns
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0) {
			if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
				SharedPreferences sharedPref = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString(
						PREF_KEY_SAVE_LOCATION,
						data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
				editor.apply();
				updateSummary(PREF_KEY_SAVE_LOCATION);
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updateSummary(key);
	}

	private void updateSummary(String key) {
		if (key.equals(PREF_KEY_SNAP_IMAGES)
				|| key.equals(PREF_KEY_SNAP_VIDEOS)
				|| key.equals(PREF_KEY_STORIES_IMAGES)
				|| key.equals(PREF_KEY_STORIES_VIDEOS)
				|| key.equals(PREF_KEY_TOASTS_DURATION)) {
			ListPreference changedPref = (ListPreference) findPreference(key);
			changedPref.setSummary(changedPref.getEntry());
		} else if (key.equals(PREF_KEY_SAVE_LOCATION)) {
			Preference changedPref = findPreference(key);
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			changedPref.setSummary(sharedPref.getString(PREF_KEY_SAVE_LOCATION,
					""));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

}
