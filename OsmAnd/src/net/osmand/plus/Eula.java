package net.osmand.plus;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import net.osmand.plus.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


/**
 * Displays an EULA ("End User License Agreement") that the user has to accept before
 * using the application. Your application should call {@link Eula#show(android.app.Activity)}
 * in the onCreate() method of the first activity. If the user accepts the EULA, it will never
 * be shown again. If the user refuses, {@link android.app.Activity#finish()} is invoked
 * on your activity.
 */
class Eula {
    private static final String ASSET_EULA = "EULA";
    private static final String PREFERENCE_EULA_ACCEPTED = "eula.accepted";
    private static final String PREFERENCES_EULA = "eula";

    /**
     * callback to let the activity know when the user has accepted the EULA.
     */
    static interface OnEulaAgreedTo {

        /**
         * Called when the user has accepted the eula and the dialog closes.
         */
        void onEulaAgreedTo();
    }

    /**
     * Displays the EULA if necessary. This method should be called from the onCreate()
     * method of your main Activity.
     *
     * @param activity The Activity to finish if the user rejects the EULA.
     * @return Whether the user has agreed already.
     */
    static boolean show(final Activity activity) {
        final SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES_EULA,
                Activity.MODE_PRIVATE);

        //testcode for eala
        //preferences.edit().putBoolean(PREFERENCE_EULA_ACCEPTED, false).commit();
        //end testcode
        
        if (!preferences.getBoolean(PREFERENCE_EULA_ACCEPTED, false)) {
        	
      		Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.eula);
            dialog.setTitle(R.string.eula_title);
            dialog.setCancelable(true);
            //there are a lot of settings, for dialog, check them all out!

            TextView versionName = (TextView) dialog.findViewById(R.id.version_name);
            PackageManager manager = activity.getPackageManager(); 
            PackageInfo info;
    		try {
    			info = manager.getPackageInfo(activity.getPackageName(), 0);
    			versionName.setText(versionName.getText()+" "+info.versionName);
    		} catch (NameNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            
    		
            Linkify.addLinks((TextView) dialog.findViewById(R.id.privacy), Linkify.ALL);
            Linkify.addLinks((TextView) dialog.findViewById(R.id.license), Linkify.ALL);
            
            //set up button
            Button refuse = (Button) dialog.findViewById(R.id.Disagree);
            refuse.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            		// false
            		refuse(activity);
                }
            });
            
            Button accept = (Button) dialog.findViewById(R.id.Agree);
            accept.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            		//true
            		accept(preferences);
	              if (activity instanceof OnEulaAgreedTo) {
	                  ((OnEulaAgreedTo) activity).onEulaAgreedTo();
	              }
                }
            });
            //now that the dialog is set up, it's time to show it    
            dialog.show();
        	
            return false;
        }
        return true;
    }

    private static void accept(SharedPreferences preferences) {
        preferences.edit().putBoolean(PREFERENCE_EULA_ACCEPTED, true).commit();
    }

    private static void refuse(Activity activity) {
        activity.finish();
    }

    private static CharSequence readEula(Activity activity) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(activity.getAssets().open(ASSET_EULA)));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = in.readLine()) != null) buffer.append(line).append('\n');
            return buffer;
        } catch (IOException e) {
            return "";
        } finally {
            closeStream(in);
        }
    }

    /**
     * Closes the specified stream.
     *
     * @param stream The stream to close.
     */
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
