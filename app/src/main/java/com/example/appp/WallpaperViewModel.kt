package com.example.appp
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class IntervalUnit(val label:String,val millis:Long){MINUTES("דקות",60000L),HOURS("שעות",3600000L),DAYS("ימים",86400000L)}
enum class Target{HOME,LOCK,BOTH}
data class WallpaperState(val wallpapers:List<String>=listOf("builtin:blue","builtin:sunset","builtin:green","builtin:purple"),val selected:Set<String>=emptySet(),val interval:Int=30,val unit:IntervalUnit=IntervalUnit.MINUTES,val target:Target=Target.BOTH,val running:Boolean=false)

class WallpaperViewModel:ViewModel(){
 private val flow=MutableStateFlow(WallpaperState())
 val state:StateFlow<WallpaperState>=flow.asStateFlow()
 private var prefs:SharedPreferences?=null
 fun addWallpapers(u:List<String>){val s=flow.value;flow.value=s.copy(wallpapers=(s.wallpapers+u.map{"uri:$it"}).distinct());save()}
 fun removeWallpaper(x:String){val s=flow.value;flow.value=s.copy(wallpapers=s.wallpapers-x,selected=s.selected-x);save()}
 fun toggleSelection(x:String){val s=flow.value;flow.value=s.copy(selected=if(x in s.selected)s.selected-x else s.selected+x);save()}
 fun setInterval(v:Int){flow.value=flow.value.copy(interval=v.coerceAtLeast(1));save()}
 fun setUnit(v:IntervalUnit){flow.value=flow.value.copy(unit=v);save()}
 fun setTarget(v:Target){flow.value=flow.value.copy(target=v);save()}
 fun toggleRunning(c:Context){val n=!flow.value.running;flow.value=flow.value.copy(running=n);save();if(n)WallpaperScheduler.schedule(c)else WallpaperScheduler.cancel(c)}
 private fun save(){prefs?.edit()?.putString("wallpapers",flow.value.wallpapers.joinToString("\n"))?.putString("selected",flow.value.selected.joinToString("\n"))?.putInt("interval",flow.value.interval)?.putString("unit",flow.value.unit.name)?.putString("target",flow.value.target.name)?.putBoolean("running",flow.value.running)?.apply()}
 companion object{
  fun factory(c:Context)=object:ViewModelProvider.Factory{
   override fun <T:ViewModel> create(k:Class<T>):T{val v=WallpaperViewModel();v.prefs=c.getSharedPreferences("wallpapers",0);val p=v.prefs!!;val d=WallpaperState();v.flow.value=d.copy(wallpapers=p.getString("wallpapers",null)?.split("\n")?.filter{it.isNotBlank()}?:d.wallpapers,selected=p.getString("selected",null)?.split("\n")?.filter{it.isNotBlank()}?.toSet()?:emptySet(),interval=p.getInt("interval",30),unit=runCatching{IntervalUnit.valueOf(p.getString("unit",d.unit.name)!!)}.getOrDefault(d.unit),target=runCatching{Target.valueOf(p.getString("target",d.target.name)!!)}.getOrDefault(d.target),running=p.getBoolean("running",false));return v as T}
  }
 }
}