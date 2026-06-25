package com.auraplay.player.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auraplay.player.audio.AudioPresets
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel
import kotlin.math.roundToInt

// Search
@Composable fun SearchScreen(vm: MainViewModel, onNP: () -> Unit) {
    val q by vm.query.collectAsStateWithLifecycle(); val res by vm.searchResults.collectAsStateWithLifecycle(); val all by vm.tracks.collectAsStateWithLifecycle(); val c = LocalColors.current
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { OutlinedTextField(q, { vm.setQuery(it) }, placeholder = { Text("Search...") }, singleLine = true, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) }, trailingIcon = { if (q.isNotEmpty()) IconButton(onClick = { vm.setQuery("") }) { Icon(Icons.Default.Clear, null) } }, shape = MaterialTheme.shapes.large) })
        if (q.isBlank()) { LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            item { Text("Quick Access", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp), color = c.text) }
            item { Surface(Modifier.fillMaxWidth().clickable { vm.shuffleAll(); onNP() }) { Row(Modifier.padding(12.dp)) { Icon(Icons.Default.Shuffle, null, tint = c.primary); Spacer(Modifier.width(16.dp)); Column { Text("Shuffle All", fontWeight = FontWeight.Medium, color = c.text); Text("${all.size} tracks", style = MaterialTheme.typography.bodySmall, color = c.text2) } } } }
        } } else { LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            item { Text("${res.size} results", style = MaterialTheme.typography.bodySmall, color = c.text3, modifier = Modifier.padding(16.dp)) }
            items(res) { t -> TrackRow(t, { vm.play(t, res); onNP() }) }
        } }
    }
}

// Queue
@Composable fun QueueScreen(vm: MainViewModel, onBack: () -> Unit) {
    val queue by vm.q.collectAsStateWithLifecycle(); val idx by vm.idx.collectAsStateWithLifecycle(); val c = LocalColors.current
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Column { Text("Queue", fontWeight = FontWeight.Bold, color = c.text); Text("${queue.size} tracks", style = MaterialTheme.typography.bodySmall, color = c.text2) } }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = { IconButton(onClick = { vm.shuffleQ() }) { Icon(Icons.Default.Shuffle, null) }; IconButton(onClick = { vm.clearQ() }) { Icon(Icons.Default.ClearAll, null) } })
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            if (idx in queue.indices) { item { Text("Now Playing", color = c.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp)) }; item { TrackRow(queue[idx], {}, playing = true) }; item { Divider(Modifier.padding(horizontal = 16.dp)) } }
            val up = queue.drop(idx + 1); if (up.isNotEmpty()) { item { Text("Up Next", color = c.text3, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp)) }; items(up.size) { i -> TrackRow(up[i], { vm.goIdx(idx + 1 + i) }, showIdx = true, idx = idx + 1 + i) } }
        }
    }
}

// Equalizer
@Composable fun EqualizerScreen(vm: MainViewModel, onBack: () -> Unit) {
    val eq by vm.eqS.collectAsStateWithLifecycle(); val bb by vm.bbS.collectAsStateWithLifecycle(); val vz by vm.vzS.collectAsStateWithLifecycle(); val le by vm.leS.collectAsStateWithLifecycle(); val c = LocalColors.current
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(c.bg, c.surface))).verticalScroll(rememberScrollState())) {
        TopAppBar(title = { Text("Equalizer & Sound", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        Text("Presets", fontWeight = FontWeight.Bold, color = c.text, modifier = Modifier.padding(16.dp))
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { AudioPresets.eqPresets.forEach { (n, _) -> Surface(Modifier.clip(RoundedCornerShape(20.dp)).clickable { vm.applyPreset(n) }, color = c.surfaceVar, shape = RoundedCornerShape(20.dp)) { Text(n, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = c.text2) } } }
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = c.surface), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Equalizer", fontWeight = FontWeight.Bold, color = c.text); Switch(eq.enabled, { vm.eqOn(it) }) }
                val bands = eq.bands; val freqs = listOf("60","170","310","600","1K","3K","6K","12K","14K","16K")
                for (i in 0 until minOf(bands.size, freqs.size)) { val lvl = bands[i].toFloat(); val norm = ((lvl + 1500) / 3000).coerceIn(0f, 1f); Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { Text(freqs[i], style = MaterialTheme.typography.labelSmall, color = c.text3, modifier = Modifier.width(32.dp)); Slider(norm, { vm.eqBand(i.toShort(), ((it * 3000) - 1500).roundToInt().toShort()) }, Modifier.weight(1f), colors = SliderDefaults.colors(thumbColor = c.primary, activeTrackColor = c.primary, inactiveTrackColor = c.progressBg)); Text("${bands[i]}", style = MaterialTheme.typography.labelSmall, color = c.text3, modifier = Modifier.width(40.dp), textAlign = TextAlign.End) } }
            }
        }
        Spacer(Modifier.height(8.dp))
        fxCard("Bass Boost", bb.enabled, { vm.bbOn(it) }, bb.strength.toFloat(), 1000f, { vm.bbStr(it.roundToInt().toShort()) }, "${(bb.strength/10).toInt()}%", c)
        fxCard("Virtualizer", vz.enabled, { vm.vzOn(it) }, vz.strength.toFloat(), 1000f, { vm.vzStr(it.roundToInt().toShort()) }, "${(vz.strength/10).toInt()}%", c)
        fxCard("Loudness", le.enabled, { vm.leOn(it) }, le.gain.toFloat(), 3000f, { vm.loudG(it.roundToInt()) }, "${(le.gain/100).toInt()} dB", c)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable private fun fxCard(title: String, on: Boolean, onTog: (Boolean)->Unit, value: Float, max: Float, onVal: (Float)->Unit, label: String, c: TColors) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = c.surface), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(title, fontWeight = FontWeight.Bold, color = c.text); Switch(on, onTog) }
            if (on) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Strength", color = c.text3, style = MaterialTheme.typography.bodySmall); Text(label, color = c.primary, style = MaterialTheme.typography.bodySmall) }; Slider(value, onVal, valueRange = 0f..max, colors = SliderDefaults.colors(thumbColor = c.primary, activeTrackColor = c.primary, inactiveTrackColor = c.progressBg)) }
        }
    }
}

// Settings
@Composable fun SettingsScreen(vm: MainViewModel, onEq: () -> Unit, onTheme: () -> Unit) {
    val spd by vm.spd.collectAsStateWithLifecycle(); val c = LocalColors.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) })
        Text("Audio", fontWeight = FontWeight.Bold, color = c.primary, modifier = Modifier.padding(16.dp))
        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = c.surfaceVar.copy(0.5f))) {
            Surface(Modifier.fillMaxWidth().clickable { onEq() }) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Equalizer, null, tint = c.primary); Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text("Equalizer & Sound", color = c.text); Text("EQ, bass boost, virtualizer", style = MaterialTheme.typography.bodySmall, color = c.text2) }; Icon(Icons.Default.ChevronRight, null, tint = c.text3) } }
            Surface(Modifier.fillMaxWidth().clickable { val sp = listOf(0.5f,0.75f,1f,1.25f,1.5f,2f); vm.setSpeed(sp[(sp.indexOf(spd).coerceAtLeast(0)+1)%sp.size]) }) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Speed, null, tint = c.primary); Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text("Playback Speed", color = c.text); Text("Current: ${spd}x", style = MaterialTheme.typography.bodySmall, color = c.text2) } } }
        }
        Text("Appearance", fontWeight = FontWeight.Bold, color = c.primary, modifier = Modifier.padding(16.dp))
        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = c.surfaceVar.copy(0.5f))) {
            Surface(Modifier.fillMaxWidth().clickable { onTheme() }) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Palette, null, tint = c.primary); Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text("Themes", color = c.text); Text("AuraPlay, Winamp, Cyberpunk & more", style = MaterialTheme.typography.bodySmall, color = c.text2) }; Icon(Icons.Default.ChevronRight, null, tint = c.text3) } }
        }
        Text("About", fontWeight = FontWeight.Bold, color = c.primary, modifier = Modifier.padding(16.dp))
        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = c.surfaceVar.copy(0.5f))) {
            Surface(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null, tint = c.primary); Spacer(Modifier.width(16.dp)); Column { Text("AuraPlay v1.0.0", color = c.text); Text("100% offline • No ads • No tracking", style = MaterialTheme.typography.bodySmall, color = c.text2) } } }
        }
    }
}

// Theme Screen
@Composable fun ThemeScreen(current: AppTheme, onSelect: (AppTheme)->Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Themes", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        Text("Tap to apply instantly", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(AppTheme.entries.toList()) { theme -> val tc = Themes.get(theme); val sel = theme == current
                Card(Modifier.fillMaxWidth().height(100.dp).clickable { onSelect(theme) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = tc.surface), border = if (sel) ButtonDefaults.outlinedButtonBorder else null) {
                    Box(Modifier.fillMaxSize()) { Row(Modifier.fillMaxWidth().height(6.dp)) { Box(Modifier.weight(1f).fillMaxHeight().background(tc.primary)); Box(Modifier.weight(1f).fillMaxHeight().background(tc.secondary)); Box(Modifier.weight(1f).fillMaxHeight().background(tc.accent)) }
                        Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(tc.primary, tc.secondary))), contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                            Spacer(Modifier.width(16.dp)); Column { Text(theme.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = tc.text); Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf(tc.primary, tc.secondary, tc.accent).forEach { c -> Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(c)) } } }
                            if (sel) Icon(Icons.Default.CheckCircle, null, tint = tc.primary, modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                }
            }
        }
    }
}

// Detail screens
@Composable fun AlbumDetail(vm: MainViewModel, name: String, onBack: () -> Unit, onNP: () -> Unit) { val t by vm.albumT.collectAsStateWithLifecycle(); val c = LocalColors.current; LaunchedEffect(name) { vm.loadAlbum(name) }
    Column(Modifier.fillMaxSize()) { TopAppBar(title = { Text(name, fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        if (t.isNotEmpty()) { Row(Modifier.padding(16.dp)) { Button(onClick = { vm.playAll(t); onNP() }, Modifier.weight(1f)) { Text("Play All") }; Spacer(Modifier.width(8.dp)); OutlinedButton(onClick = { vm.playAll(t, true); onNP() }, Modifier.weight(1f)) { Text("Shuffle") } }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) { items(t) { track -> TrackRow(track, { vm.play(track, t); onNP() }) } } }
    }
}
@Composable fun ArtistDetail(vm: MainViewModel, name: String, onBack: () -> Unit, onNP: () -> Unit) { val t by vm.artistT.collectAsStateWithLifecycle(); LaunchedEffect(name) { vm.loadArtist(name) }
    Column(Modifier.fillMaxSize()) { TopAppBar(title = { Text(name, fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        if (t.isNotEmpty()) { Row(Modifier.padding(16.dp)) { Button(onClick = { vm.playAll(t); onNP() }, Modifier.weight(1f)) { Text("Play All") }; Spacer(Modifier.width(8.dp)); OutlinedButton(onClick = { vm.playAll(t, true); onNP() }, Modifier.weight(1f)) { Text("Shuffle") } }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) { items(t) { track -> TrackRow(track, { vm.play(track, t); onNP() }) } } }
    }
}
@Composable fun FolderDetail(vm: MainViewModel, name: String, onBack: () -> Unit, onNP: () -> Unit) { val t by vm.folderT.collectAsStateWithLifecycle(); LaunchedEffect(name) { vm.loadFolder(name) }
    Column(Modifier.fillMaxSize()) { TopAppBar(title = { Text(name, fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        if (t.isNotEmpty()) { Row(Modifier.padding(16.dp)) { Button(onClick = { vm.playAll(t); onNP() }, Modifier.weight(1f)) { Text("Play All") }; Spacer(Modifier.width(8.dp)); OutlinedButton(onClick = { vm.playAll(t, true); onNP() }, Modifier.weight(1f)) { Text("Shuffle") } }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) { items(t) { track -> TrackRow(track, { vm.play(track, t); onNP() }) } } }
    }
}
@Composable fun PlaylistsScreen(vm: MainViewModel, onPlist: (Long) -> Unit) { val pl by vm.playlists.collectAsStateWithLifecycle(); val c = LocalColors.current; var show by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) { TopAppBar(title = { Text("Playlists", fontWeight = FontWeight.Bold) }, actions = { IconButton(onClick = { show = true }) { Icon(Icons.Default.Add, null) } })
        if (pl.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.QueueMusic, null, Modifier.size(64.dp), tint = c.text3); Spacer(Modifier.height(16.dp)); Text("No playlists", color = c.text2); Spacer(Modifier.height(16.dp)); Button(onClick = { show = true }) { Text("Create Playlist") } } }
        else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) { items(pl) { p -> Surface(Modifier.fillMaxWidth().clickable { onPlist(p.id) }) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.QueueMusic, null, tint = c.primary, modifier = Modifier.size(32.dp)); Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text(p.name, fontWeight = FontWeight.SemiBold, color = c.text); Text("${p.trackCount} tracks", style = MaterialTheme.typography.bodySmall, color = c.text2) }; IconButton(onClick = { vm.delPlist(p) }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) } } } } }
    }
    if (show) { var n by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = { show = false }, title = { Text("Create Playlist") }, text = { OutlinedTextField(n, { n = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }, confirmButton = { TextButton(onClick = { if (n.isNotBlank()) { vm.createPlist(n); show = false } }) { Text("Create") } }, dismissButton = { TextButton(onClick = { show = false }) { Text("Cancel") } }) }
}
@Composable fun PlistDetail(vm: MainViewModel, id: Long, onBack: () -> Unit, onNP: () -> Unit) { val t by vm.plistT.collectAsStateWithLifecycle(); LaunchedEffect(id) { vm.loadPlist(id) }
    Column(Modifier.fillMaxSize()) { TopAppBar(title = { Text("Playlist", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        if (t.isNotEmpty()) { Row(Modifier.padding(16.dp)) { Button(onClick = { vm.playAll(t); onNP() }, Modifier.weight(1f)) { Text("Play All") }; Spacer(Modifier.width(8.dp)); OutlinedButton(onClick = { vm.playAll(t, true); onNP() }, Modifier.weight(1f)) { Text("Shuffle") } }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) { items(t) { track -> TrackRow(track, { vm.play(track, t); onNP() }) } } }
    }
}

// Permission
@Composable fun PermissionScreen(onGranted: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { if (it) onGranted() }
    val c = LocalColors.current; val p = if (android.os.Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_AUDIO else android.Manifest.permission.READ_EXTERNAL_STORAGE
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(c.bg, c.surface))), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(120.dp).clip(CircleShape).background(Brush.linearGradient(listOf(c.primary, c.secondary))), contentAlignment = Alignment.Center) { Icon(Icons.Default.LibraryMusic, null, tint = Color.White, modifier = Modifier.size(60.dp)) }
            Spacer(Modifier.height(32.dp)); Text("Welcome to AuraPlay", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = c.text)
            Spacer(Modifier.height(16.dp)); Text("AuraPlay needs access to your music library.\nYour data never leaves your device.", style = MaterialTheme.typography.bodyLarge, color = c.text2, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))
            Button(onClick = { launcher.launch(p) }, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("Grant Access to Music", fontWeight = FontWeight.Bold) }
        }
    }
}
