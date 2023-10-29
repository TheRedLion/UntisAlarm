package com.carlkarenfort.test

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDate


class Misc {
    companion object {
        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
            return false
        }

        //this function returns the next working day
        fun getNextDay(): LocalDate {
            //Log.i(TAG, "called getNextDay()")
            var nextDay = LocalDate.now()
            nextDay = nextDay.plusDays(1)

            // Check if the next day is a weekend (Saturday or Sunday)
            while (nextDay.dayOfWeek == DayOfWeek.SATURDAY || nextDay.dayOfWeek == DayOfWeek.SUNDAY) {
                nextDay = nextDay.plusDays(1)
            }
            return nextDay
        }
    }


}