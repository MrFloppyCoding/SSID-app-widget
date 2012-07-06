package de.mrfloppycoding.ssid_notify;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class PreferencesActivtiy extends PreferenceActivity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

	@Override
	protected void onStart() {
		super.onStart();

		String versionName = "";
		String versionCode = "";
		try {
			PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = "Version Name: " + pkgInfo.versionName;
			versionCode = "Version Code: " + pkgInfo.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PreferenceScreen prefScreen = (PreferenceScreen)findPreference(getString(R.string.pref2_app_version_key));
		prefScreen.setTitle(versionName);
		prefScreen.setSummary(versionCode);
	}
}