package com.example.appp
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class BootReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent?){
  if(context.getSharedPreferences("wallpapers",0).getBoolean("running",false)) WallpaperScheduler.schedule(context)
 }
}