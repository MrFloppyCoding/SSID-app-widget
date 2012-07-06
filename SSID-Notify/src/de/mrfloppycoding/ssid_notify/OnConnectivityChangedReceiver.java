package de.mrfloppycoding.ssid_notify;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class OnConnectivityChangedReceiver extends BroadcastReceiver {

	private static final String TAG = OnConnectivityChangedReceiver.class.toString();

	protected static final int NOTIFICATION_ID = 123456;

	private static final String INTENT_CLEAR_NOTIFICATION = "CLEAR_NOTIFICATION";

	protected static final String INTENT_WIFI_SETTINGS = Settings.ACTION_WIFI_SETTINGS;
//	private static final String INTENT_APP_PREFS = "de.steff.ssid_notify.SHOW_CURRENT";

	protected static String _SSID = "";

	@Override
	public void onReceive(Context context, Intent intent) {
		//incoming intent can only be a ConnectivityManager.CONNECTIVITY_ACTION
		//several extras are put in there to describe the circumstances how the connection changed
//		Debug.waitForDebugger();

		String action = intent.getAction();
		Log.e(TAG, "Action: "+action);

		if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			updateWidget(context, false, "");
			if(netInfo!=null) {
				State netState = netInfo.getState();

				if(netState.equals(State.CONNECTING)) {
				} else if(netState.equals(State.CONNECTED)) {

					WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInfo = wifi.getConnectionInfo();
					_SSID = wifiInfo.getSSID();

					updateWidget(context, true, _SSID);

					NotificationManager notificMgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
					long timeStart = System.currentTimeMillis();
					Notification notification = new Notification(R.drawable.wifi_logo, "Connected to "+_SSID, timeStart);

					Calendar calendar = Calendar.getInstance();
		            calendar.setTimeInMillis(System.currentTimeMillis());

					CharSequence contentTitle = "WiFi connected to "+_SSID;
					int min = calendar.getTime().getMinutes();
					int sec = calendar.getTime().getSeconds();
					String minutes, seconds;
					if(min<10) minutes = "0"+min;
					else minutes = ""+min;
					if(sec<10) seconds = "0"+sec;
					else seconds = ""+sec;
					CharSequence contentText = "Connection established at "+calendar.getTime().getHours()+":"+minutes+":"+seconds;

					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
					boolean isUnpersistent = prefs.getBoolean(context.getString(R.string.pref_persistent_key), Boolean.getBoolean(context.getString(R.string.pref_persistent_default)));
					boolean isTimeout = prefs.getBoolean(context.getString(R.string.pref_show_key), Boolean.getBoolean(context.getString(R.string.pref_show_default)));
					String intInd = prefs.getString(context.getString(R.string.pref_list_activity_key), "0");
					int intentIndex = Integer.parseInt(intInd);

					notification.setLatestEventInfo(context, contentTitle, contentText, getNotificationIntent(intentIndex, context));
					if(!isUnpersistent) notification.flags |= Notification.FLAG_ONGOING_EVENT;
					else notification.flags |= Notification.FLAG_AUTO_CANCEL;
					notificMgr.notify(NOTIFICATION_ID, notification);

		            if(isTimeout) {
		            	String dur = prefs.getString(context.getString(R.string.pref_show_duration_key), context.getString(R.string.pref_show_duration_default));
						int duration = Integer.parseInt(dur);

						calendar.add(Calendar.SECOND, duration);

						AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
						am.set(AlarmManager.RTC, calendar.getTimeInMillis(), getAlarmIntent(context));
		            }
				}
				else {
					NotificationManager notificMgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
					notificMgr.cancel(NOTIFICATION_ID);
//					updateWidget(context, false, "");
				}
			}
		}
		// Clear the notification
		else if(intent.getAction().equals(INTENT_CLEAR_NOTIFICATION)) {
			NotificationManager nMgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			nMgr.cancel(NOTIFICATION_ID);
		}
	}

	private void updateWidget(Context context, boolean isConnected, String ssid) {
//		Debug.waitForDebugger();
		Intent widgetUpdateIntent = new Intent(context, AppWidget.class);
		widgetUpdateIntent.putExtra(AppWidget._CONNECTED_STRING, isConnected);
		widgetUpdateIntent.putExtra(AppWidget._SSID_STRING, ssid);

		Log.e(TAG, "update ssid "+ssid);

		context.sendBroadcast(widgetUpdateIntent);
	}

	private PendingIntent getNotificationIntent(int intentIndex, Context context) {
		Intent notificationIntent = null;

		switch(intentIndex) {
		case 0:
			break;
		case 1:
			notificationIntent  = new Intent(INTENT_WIFI_SETTINGS);
			break;
		case 2:
			notificationIntent = new Intent(context, PreferencesActivtiy.class);
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			break;
		default:
			notificationIntent  = new Intent(INTENT_WIFI_SETTINGS);
		}

		PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		return notificationPendingIntent;
	}

	private PendingIntent getAlarmIntent(Context context) {
		Intent i = new Intent(context, OnConnectivityChangedReceiver.class);
		i.setAction(INTENT_CLEAR_NOTIFICATION);
		PendingIntent alarmSender = PendingIntent.getBroadcast(context, 12, i, 0);

		return alarmSender;
	}
}