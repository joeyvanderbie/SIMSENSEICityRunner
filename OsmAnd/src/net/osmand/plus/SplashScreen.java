package net.osmand.plus;

import net.osmand.plus.activities.MainMenuActivity;
import net.osmand.plus.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;

public class SplashScreen extends Activity implements Eula.OnEulaAgreedTo {


    private boolean mAlreadyAgreedToEula = false;

    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);     
        
        mAlreadyAgreedToEula = Eula.show(this);
        
        startSplashThread();
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
    	startActivity(new Intent(SplashScreen.this,MainMenuActivity.class));
    }

    
    private void startSplashThread(){
        // thread for displaying the SplashScreen
        Thread splashTread = new Thread() {
            @Override
            public void run() {
 
                	if( mAlreadyAgreedToEula){
                		startActivity(new Intent(SplashScreen.this,MainMenuActivity.class));//Boskoi.class));
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
