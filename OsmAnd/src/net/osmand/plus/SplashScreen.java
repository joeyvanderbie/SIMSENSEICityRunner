package net.osmand.plus;

import net.osmand.plus.activities.MainMenuActivity;
import net.osmand.plus.activities.UserDataActivity;
import net.osmand.sensei.data.UserData;
import net.osmand.sensei.db.UserDataSource;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Window;

public class SplashScreen extends Activity implements Eula.OnEulaAgreedTo {


    private boolean mAlreadyAgreedToEula = false;
    private SplashScreen sp;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);     
        sp= this;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	mAlreadyAgreedToEula = Eula.show(sp);
                startSplashThread();
            }
        }, 3000);
        
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
           // active = false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    public void onEulaAgreedTo() {
    	
 			Intent newIntent = new Intent(SplashScreen.this,MainMenuActivity.class);
 		//	newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

 			newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
 			startActivity(newIntent);
		
    }

    
    private void startSplashThread(){
        // thread for displaying the SplashScreen
        Thread splashTread = new Thread() {
            @Override
            public void run() {
 
                	if( mAlreadyAgreedToEula){
                		
            	 			Intent newIntent = new Intent(SplashScreen.this,MainMenuActivity.class);
            	 			newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            	 			//newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            	 			startActivity(newIntent);
            	 		
                		finish();
                	}
            }
            
        };
        splashTread.start();
    }
    
    @Override
    public void onResume(){
    	super.onResume();      
    }
    
    
}
