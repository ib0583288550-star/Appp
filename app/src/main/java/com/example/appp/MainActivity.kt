package com.example.appp
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity:ComponentActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContent{MaterialTheme{WallpaperApp()}}}
}
@Composable fun WallpaperApp(){
 val context=LocalContext.current
 val vm:WallpaperViewModel=viewModel(factory=WallpaperViewModel.factory(context))
 val s by vm.state.collectAsState()
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){uris->uris.forEach{try{context.contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){}};vm.addWallpapers(uris.map{it.toString()})}
 Scaffold(topBar={TopAppBar(title={Text("מחליף הטפטים")})}){pad->
  Column(Modifier.padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   Text("בחר טפטים",style=MaterialTheme.typography.headlineSmall)
   LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
    items(s.wallpapers){x->Card(Modifier.height(120.dp).clickable{vm.toggleSelection(x)}){Box(Modifier.fillMaxSize()){Preview(x,Modifier.fillMaxSize());if(x in s.selected)Icon(Icons.Default.Check,null,Modifier.align(Alignment.TopEnd).padding(8.dp));if(x.startsWith("uri:"))IconButton({vm.removeWallpaper(x)},Modifier.align(Alignment.BottomEnd)){Icon(Icons.Default.Delete,"מחק")}}}}
    item{Card(Modifier.height(120.dp).clickable{picker.launch(arrayOf("image/*"))}){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Add,null);Text("הוסף תמונות")}}}}
   }
   Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(s.interval.toString(),{vm.setInterval(it.toIntOrNull()?:1)},label={Text("כל כמה")},modifier=Modifier.width(110.dp));UnitMenu(s.unit,vm::setUnit)}
   Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){TargetBtn("בית",s.target==Target.HOME){vm.setTarget(Target.HOME)};TargetBtn("נעילה",s.target==Target.LOCK){vm.setTarget(Target.LOCK)};TargetBtn("שניהם",s.target==Target.BOTH){vm.setTarget(Target.BOTH)}}
   Button({vm.toggleRunning(context)},Modifier.fillMaxWidth(),enabled=s.selected.isNotEmpty()){Icon(if(s.running)Icons.Default.Pause else Icons.Default.PlayArrow,null);Spacer(Modifier.width(6.dp));Text(if(s.running)"עצור" else "הפעל החלפה")}
  }
 }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun UnitMenu(u:IntervalUnit,on:(IntervalUnit)->Unit){var e by remember{mutableStateOf(false)};Box{OutlinedButton({e=true}){Text(u.label)};DropdownMenu(e,{e=false}){IntervalUnit.entries.forEach{DropdownMenuItem({Text(it.label)},{on(it);e=false})}}}}
@Composable fun TargetBtn(t:String,sel:Boolean,on:()->Unit){OutlinedButton(on){if(sel)Icon(Icons.Default.Check,null);Text(t)}}
@Composable fun Preview(x:String,m:Modifier){when(x){"builtin:blue"->Box(m.background(Brush.linearGradient(listOf(Color(0xFF1565C0),Color(0xFF64B5F6)))));"builtin:sunset"->Box(m.background(Brush.horizontalGradient(listOf(Color(0xFFFF6F00),Color(0xFFFFB300),Color(0xFF5E35B1)))));"builtin:green"->Box(m.background(Brush.linearGradient(listOf(Color(0xFF1B5E20),Color(0xFF66BB6A))));else->Box(m.background(Color.LightGray))}}
