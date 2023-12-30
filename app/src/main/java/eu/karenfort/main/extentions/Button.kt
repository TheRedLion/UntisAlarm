package eu.karenfort.main.extentions

import android.widget.Button
import eu.karenfort.main.helper.DISABLED_BUTTON_ALPHA_VALUE

var Button.isDisabled: Boolean
    get() = this.isDisabled
    set(value) {
        if (value) {
            this.isClickable = false
            this.alpha = DISABLED_BUTTON_ALPHA_VALUE
        } else {
            this.isClickable = true
            this.alpha = 1F
        }
        this.isDisabled = value
    }