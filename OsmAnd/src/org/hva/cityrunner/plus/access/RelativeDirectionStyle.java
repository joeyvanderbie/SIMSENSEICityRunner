package org.hva.cityrunner.plus.access;

import org.hva.cityrunner.plus.ClientContext;
import org.hva.cityrunner.plus.R;


public enum RelativeDirectionStyle {

    SIDEWISE(R.string.direction_style_sidewise),
    CLOCKWISE(R.string.direction_style_clockwise);

    private final int key;

    RelativeDirectionStyle(int key) {
        this.key = key;
    }

    public String toHumanString(ClientContext ctx) {
        return ctx.getString(key);
    }

}
