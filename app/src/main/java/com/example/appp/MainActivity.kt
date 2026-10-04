package com.example.appp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WallpaperApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperApp() {
    val context = LocalContext.current
    val vm: WallpaperViewModel =
        viewModel(factory = WallpaperViewModel.factory(context))
    val state by vm.state.collectAsState()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
        }
        vm.addWallpapers(uris.map { it.toString() })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("מחליף הטפטים") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "בחר טפטים",
                style = MaterialTheme.typography.headlineSmall
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.wallpapers) { wallpaper ->
                    Card(
                        modifier = Modifier
                            .height(120.dp)
                            .clickable { vm.toggleSelection(wallpaper) }
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            Preview(wallpaper, Modifier.fillMaxSize())

                            if (wallpaper in state.selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                )
                            }

                            if (wallpaper.startsWith("uri:")) {
                                IconButton(
                                    onClick = { vm.removeWallpaper(wallpaper) },
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "מחק"
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .height(120.dp)
                            .clickable {
                                picker.launch(arrayOf("image/*"))
                            }
                    ) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null
                                )
                                Text("הוסף תמונות")
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.interval.toString(),
                    onValueChange = {
                        vm.setInterval(it.toIntOrNull() ?: 1)
                    },
                    label = { Text("כל כמה") },
                    modifier = Modifier.width(110.dp)
                )
                UnitMenu(state.unit, vm::setUnit)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                TargetBtn(
                    "בית",
                    state.target == Target.HOME
                ) {
                    vm.setTarget(Target.HOME)
                }
                TargetBtn(
                    "נעילה",
                    state.target == Target.LOCK
                ) {
                    vm.setTarget(Target.LOCK)
                }
                TargetBtn(
                    "שניהם",
                    state.target == Target.BOTH
                ) {
                    vm.setTarget(Target.BOTH)
                }
            }

            Button(
                onClick = { vm.toggleRunning(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selected.isNotEmpty()
            ) {
                Icon(
                    if (state.running) Icons.Default.Pause
                    else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(6.dp))
                Text(if (state.running) "עצור" else "הפעל החלפה")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitMenu(
    unit: IntervalUnit,
    onUnitSelected: (IntervalUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true }
        ) {
            Text(unit.label)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            IntervalUnit.entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    onClick = {
                        onUnitSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TargetBtn(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(onClick = onClick) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null
            )
        }
        Text(text)
    }
}

@Composable
fun Preview(
    wallpaper: String,
    modifier: Modifier
) {
    when (wallpaper) {
        "builtin:blue" -> Box(
            modifier.background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1565C0),
                        Color(0xFF64B5F6)
                    )
                )
            )
        )

        "builtin:sunset" -> Box(
            modifier.background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFF6F00),
                        Color(0xFFFFB300),
                        Color(0xFF5E35B1)
                    )
                )
            )
        )

        "builtin:green" -> Box(
            modifier.background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1B5E20),
                        Color(0xFF66BB6A)
                    )
                )
            )
        )

        "builtin:purple" -> Box(
            modifier.background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF4A148C),
                        Color(0xFFBA68C8)
                    )
                )
            )
        )

        else -> Box(
            modifier.background(Color.LightGray)
        )
    }
}
