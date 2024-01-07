/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 */
package eu.karenfort.main.extentions

import android.widget.Button
import eu.karenfort.main.helper.DISABLED_BUTTON_ALPHA_VALUE

/*
 * This extension variable is used to disable and grey out Buttons
 */
var Button.isDisabled: Boolean
    get() = this.isClickable.not() && this.alpha == DISABLED_BUTTON_ALPHA_VALUE
    set(value) {
        if (value) {
            this.isClickable = false
            this.alpha = DISABLED_BUTTON_ALPHA_VALUE
        } else {
            this.isClickable = true
            this.alpha = 1F
        }
    }