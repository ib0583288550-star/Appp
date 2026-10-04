package com.example.appp
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object WallpaperScheduler {
    private const val REQUEST_CODE = 8042
    fun schedule(context: Context) {
        val p=context.getSharedPreferences("wallpapers",0)
        val interval=p.getInt("interval",30).coerceAtLeast(1)
        val unit=runCatching{IntervalUnit.valueOf(p.getString("unit",IntervalUnit.MINUTES.name)!!)}.getOrDefault(IntervalUnit.MINUTES)
        val alarm=context.getSystemService(AlarmManager::class.java)
        val pi=PendingIntent.getBroadcast(context,REQUEST_CODE,Intent(context,WallpaperAlarmReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+interval*unit.millis,pi)
    }
    fun cancel(context: Context) {
        val alarm=context.getSystemService(AlarmManager::class.java)
        val pi=PendingIntent.getBroadcast(context,REQUEST_CODE,Intent(context,WallpaperAlarmReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pi)
    }
}