package net.osmand.sensei.sensors;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.askcs.android.affectbutton.Settings;
import com.askcs.android.widget.AffectButton;

public class StaticAffectButton extends AffectButton {

	public StaticAffectButton(Context context) {
		super(context);
	}

	public StaticAffectButton(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public StaticAffectButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	public void setPAD(double[] PAD) {
		double pleasure = PAD[0];
		double dominance = PAD[2];
		double arousal = PAD[1];
		if (Settings.SWAP_AD) {
			getAffect().setPAD(pleasure, dominance, arousal);
		} else {
			getAffect().setPAD(pleasure, arousal, dominance);
		}
		getAffect().setPAD(pleasure, arousal, dominance);
		mFeatures.setAffect(getAffect());
		mFace.setFace(getAffect(), mFeatures, pleasure, arousal);
		requestRender();
	}

}
