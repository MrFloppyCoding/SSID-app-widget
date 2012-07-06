/**
 *
 */
package de.mrfloppycoding.ssid_notify;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * <code>AppWidget.java</code>
 *
 * <pre>
 * 14.02.2011
 * 22:26:41
 * </pre>
 *
 * @author
 * Stefan Dierdorf
 * <a href="mailto:Stefan.Dierdorf@uni-konstanz.de">Stefan.Dierdorf@uni-konstanz.de</a>
 * Human-Computer Interaction Group
 * University of Konstanz
 */
public class AppWidget extends AppWidgetProvider {

	protected static final String _CONNECTED_STRING = "is_connected_string";
	protected static final String _SSID_STRING = "ssid_string";

	public static boolean isConnected;
	public static String ssid = "";
	public static boolean isWifiEnabled;

//	private boolean isStartup;

//	@Override
//	public void onEnabled(Context context) {
//		super.onEnabled(context);
//
//		WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
//		WifiInfo wifiInfo = wifi.getConnectionInfo();
//		String ssid = wifiInfo.getSSID();
////		Debug.waitForDebugger();
//		if(ssid==null) {
//			isConnected = false;
//			ssid = "";
//		} else isConnected = true;
//
//		isStartup = true;
//		Log.e("AppWidget", "onEnabled ssid "+ssid);
//	}

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.e("AppWidget", "update ssid "+ssid);
		// To prevent any ANR timeouts, we perform the update in a service
		context.startService(new Intent(context, UpdateService.class));
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
//		if(intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED) || intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
		WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		isWifiEnabled = wifi.isWifiEnabled();
		
		WifiInfo wifiInfo = wifi.getConnectionInfo();
		ssid = wifiInfo.getSSID();
		
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		isConnected = netInfo.isConnected();
//		} 
//		else if(intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
//			isConnected = intent.getBooleanExtra(_CONNECTED_STRING, false);
//			ssid = isConnected ? intent.getStringExtra(_SSID_STRING) : "";
//		}

		Log.e("AppWidget", "intent.getAction() "+intent.getAction());
		Log.e("AppWidget", "isConnected: "+isConnected);
		Log.e("AppWidget", "receive ssid: "+ssid);

		if (intent.getAction()==null) {
			context.startService(new Intent(context, UpdateService.class));
		}
		else super.onReceive(context, intent);
	}


	public static class UpdateService extends IntentService {

		public UpdateService() {
			super("AppWidget$UpdateService");
		}

		@Override
        public void onStart(Intent intent, int startId) {
            // Build the widget update for today
            RemoteViews updateViews = buildUpdate(this);

            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, AppWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
        }

		@Override
		protected void onHandleIntent(Intent intent) {
//			isConnected = intent.getBooleanExtra(_CONNECTED_STRING, false);
//			ssid = isConnected ? intent.getStringExtra(_SSID_STRING) : "";
		}

        /**
         * Build a widget update to show the current Wiktionary
         * "Word of the day." Will block until the online API returns.
         */
        public RemoteViews buildUpdate(Context context) {
        	Log.e("UpdateService", "ssid: "+ssid);
        	RemoteViews rViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        	rViews.setViewVisibility(R.id.widget_tv_ssid2, View.GONE);
        	if(isConnected) {
        		rViews.setTextViewText(R.id.widget_tv, context.getText(R.string.connected));
        		if(ssid.contains(" ")) {
        			String[] ar_ssid = ssid.split(" ");
        			int ar_mitte = ar_ssid.length/2;
        			String lbl1 = "", lbl2 = "";
        			for(int i=0; i<ar_ssid.length; i++) {
        				if(i<ar_mitte) lbl1 += ar_ssid[i];
        				else lbl2 += ar_ssid[i];
        			}
        			rViews.setTextViewText(R.id.widget_tv_ssid, lbl1);
        			rViews.setTextViewText(R.id.widget_tv_ssid2, lbl2);
        			rViews.setViewVisibility(R.id.widget_tv_ssid2, View.VISIBLE);
        		}
        		else rViews.setTextViewText(R.id.widget_tv_ssid, ssid);
        	} else {
        		rViews.setTextViewText(R.id.widget_tv, context.getText(R.string.not_connected));
        		rViews.setTextViewText(R.id.widget_tv_ssid, context.getText(R.string.not_connected_2));
        	}

        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        	String intInd = prefs.getString(context.getString(R.string.pref_list_activity_key), "0");
			int intentIndex = Integer.parseInt(intInd);
        	rViews.setOnClickPendingIntent(R.id.widget_layout, getWidgetIntent(intentIndex, context));

        	return rViews;
        }

        private PendingIntent getWidgetIntent(int intentIndex, Context context) {
    		Intent notificationIntent = null;

    		switch(intentIndex) {
    		case 0:
    			notificationIntent  = new Intent(OnConnectivityChangedReceiver.INTENT_WIFI_SETTINGS);
    			break;
    		case 1:
    			notificationIntent = new Intent(context, PreferencesActivtiy.class);
    			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			break;
    		default:
    			notificationIntent  = new Intent(OnConnectivityChangedReceiver.INTENT_WIFI_SETTINGS);
    		}
    		PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
    		return notificationPendingIntent;
    	}

		@Override
		public IBinder onBind(Intent arg0) {
			// We don't need to bind to this service
			return null;
		}
	}
}