package org.hva.cityrunner.plus.access;

import org.hva.cityrunner.plus.ClientContext;
import org.hva.cityrunner.plus.R;


public enum AccessibilityMode {

    ON(R.string.accessibility_on),
    OFF(R.string.accessibility_off),
    DEFAULT(R.string.accessibility_default);

    private final int key;

    AccessibilityMode(int key) {
        this.key = key;
    }

    public String toHumanString(ClientContext ctx) {
        return ctx.getString(key);
    }

}
