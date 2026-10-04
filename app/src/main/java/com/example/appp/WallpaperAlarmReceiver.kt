package com.example.appp
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

class WallpaperAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val p=context.getSharedPreferences("wallpapers",0)
        if(!p.getBoolean("running",false)) return
        val items=p.getString("selected","")!!.split("\n").filter{it.isNotBlank()}
        if(items.isEmpty()){ WallpaperScheduler.schedule(context); return }
        val index=(p.getInt("current_index",-1)+1)%items.size
        p.edit().putInt("current_index",index).apply()
        apply(context,items[index])
        WallpaperScheduler.schedule(context)
    }
    private fun apply(c:Context,item:String){
        val p=c.getSharedPreferences("wallpapers",0)
        val target=runCatching{Target.valueOf(p.getString("target",Target.BOTH.name)!!)}.getOrDefault(Target.BOTH)
        val wm=WallpaperManager.getInstance(c)
        try{
            if(item.startsWith("builtin:")){
                val res=when(item){"builtin:blue"->R.drawable.wallpaper_blue;"builtin:sunset"->R.drawable.wallpaper_sunset;"builtin:green"->R.drawable.wallpaper_green;else->R.drawable.wallpaper_purple}
                if(target==Target.HOME||target==Target.BOTH) wm.setResource(res,WallpaperManager.FLAG_SYSTEM)
                if(target==Target.LOCK||target==Target.BOTH) wm.setResource(res,WallpaperManager.FLAG_LOCK)
            }else{
                val uri=Uri.parse(item.removePrefix("uri:"))
                if(target==Target.HOME||target==Target.BOTH)c.contentResolver.openInputStream(uri)?.use{wm.setStream(it,null,true,WallpaperManager.FLAG_SYSTEM)}
                if(target==Target.LOCK||target==Target.BOTH)c.contentResolver.openInputStream(uri)?.use{wm.setStream(it,null,true,WallpaperManager.FLAG_LOCK)}
            }
        }catch(_:Exception){}
    }
}