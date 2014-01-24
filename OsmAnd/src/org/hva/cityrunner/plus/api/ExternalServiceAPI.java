package org.hva.cityrunner.plus.api;

import org.hva.cityrunner.plus.ClientContext;

public interface ExternalServiceAPI {
	
	public boolean isWifiConnected();
	
	public boolean isInternetConnected();
	

	public boolean isLightSensorEnabled();
	
	public String getExternalStorageDirectory();
	
	public AudioFocusHelper getAudioFocuseHelper();
	
	public interface AudioFocusHelper {
		
		public boolean requestFocus(ClientContext context, int streamType);
		
		public boolean abandonFocus(ClientContext context, int streamType);
	}
}
