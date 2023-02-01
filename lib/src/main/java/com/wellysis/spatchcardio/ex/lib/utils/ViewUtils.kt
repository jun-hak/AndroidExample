package com.wellysis.spatchcardio.ex.lib.utils

import android.content.Context
import android.widget.Toast

object ViewUtils {

    fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}