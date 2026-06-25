package com.auraplay.player.ui.screens
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.auraplay.player.audio.AmpMode
import com.auraplay.player.data.model.RepeatMode
import com.auraplay.player.ui.theme.LocalColors
import com.auraplay.player.ui.viewmodel.MainViewModel
import kotlin.math.abs

@Composable
fun NowPlayingScreen(vm: MainViewModel, onBack: () -> Unit) {
    val track by vm.cur.collectAsStateWithLifecycle()
    val isPlaying by vm.playing.collectAsStateWithLifecycle()
    val position by vm.pos.collectAsStateWithLifecycle()
    val duration by vm.dur.collectAsStateWithLifecycle()
    val rep by vm.rep.collectAsStateWithLifecycle()
    val shufOn by vm.shufOn.collectAsStateWithLifecycle()
    val spd by vm.spd.collectAsStateWithLifecycle()
    val amp by vm.ampMode.collectAsStateWithLifecycle()
    val c = LocalColors.current
    val t = track ?: return

    val rot by rememberInfiniteTransition(label = "r").animateFloat(0f, 360f, infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart), label = "rot")
    var seeking by remember { mutableStateOf(false) }; var seekPos by remember { mutableStateOf(0f) }
    val prog = if (duration > 0) (if (seeking) seekPos else position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(c.bg, c.surface, c.bg)))) {
        if (t.albumArtUri != null) AsyncImage(t.albumArtUri, null, Modifier.fillMaxSize().blur(60.dp), contentScale = ContentScale.Crop, alpha = 0.15f)
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).pointerInput(Unit) { detectHorizontalDragGestures { _, d -> if (abs(d) > 100) { if (d > 0) vm.prev() else vm.next() } } }) {
            Row(Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.KeyboardArrowDown, "Back", tint = c.text, modifier = Modifier.size(32.dp)) }
                Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall, color = c.text3, letterSpacing = 3.sp)
                Row { IconButton(onClick = { /* sleep timer */ }) { Icon(Icons.Default.Bedtime, "Timer", tint = c.text) } }
            }
            Spacer(Modifier.weight(0.3f))
            Box(Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize(0.95f).rotate(if (isPlaying) rot else 0f).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D), Color(0xFF1A1A1A)))), contentAlignment = Alignment.Center) {
                    Box(Modifier.fillMaxSize(0.50f).clip(CircleShape).background(Brush.linearGradient(listOf(c.primary, c.secondary))), contentAlignment = Alignment.Center) {
                        if (t.albumArtUri != null) AsyncImage(t.albumArtUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }
            }
            Spacer(Modifier.weight(0.3f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(t.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${t.artist} • ${t.album}", style = MaterialTheme.typography.bodyMedium, color = c.text2, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${t.formatName} • ${t.formattedBitrate} • ${t.formattedSampleRate}", style = MaterialTheme.typography.bodySmall, color = c.text3) }
                IconButton(onClick = { vm.toggleFav(t.id) }) { Icon(if (t.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (t.isFavorite) Color.Red else c.text2, modifier = Modifier.size(28.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            Slider(prog, onValueChange = { seeking = true; seekPos = it }, onValueChangeFinished = { seeking = false; vm.seek((seekPos * duration).toLong()) }, Modifier.fillMaxWidth(), colors = SliderDefaults.colors(thumbColor = c.progress, activeTrackColor = c.progress, inactiveTrackColor = c.progressBg))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(fmtDur(if (seeking) (seekPos * duration).toLong() else position), style = MaterialTheme.typography.bodySmall, color = c.text3); Text(fmtDur(duration), style = MaterialTheme.typography.bodySmall, color = c.text3) }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.toggleShuf() }) { Icon(Icons.Default.Shuffle, null, tint = if (shufOn) c.accent else c.text3, modifier = Modifier.size(24.dp)) }
                IconButton(onClick = { vm.prev() }) { Icon(Icons.Default.SkipPrevious, null, tint = c.text, modifier = Modifier.size(40.dp)) }
                Box(Modifier.size(72.dp).clip(CircleShape).background(Brush.linearGradient(listOf(c.primary, c.secondary))).padding(4.dp), contentAlignment = Alignment.Center) { IconButton(onClick = { vm.toggle() }, Modifier.size(64.dp)) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(40.dp)) } }
                IconButton(onClick = { vm.next() }) { Icon(Icons.Default.SkipNext, null, tint = c.text, modifier = Modifier.size(40.dp)) }
                IconButton(onClick = { vm.toggleRep() }) { Icon(if (rep == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat, null, tint = if (rep != RepeatMode.OFF) c.primary else c.text3, modifier = Modifier.size(24.dp)) }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { val sp = listOf(0.5f,0.75f,1f,1.25f,1.5f,2f); vm.setSpeed(sp[(sp.indexOf(spd).coerceAtLeast(0)+1)%sp.size]) }) { Text("${spd}x", color = if (spd != 1f) c.accent else c.text3, fontWeight = FontWeight.Medium) }
                TextButton(onClick = { vm.cycleAmp() }) { Text(amp.name.replace("_"," "), color = if (amp != AmpMode.OFF) c.accent else c.text3, style = MaterialTheme.typography.bodySmall) }
                IconButton(onClick = { /* eq */ }) { Icon(Icons.Default.Equalizer, null, tint = c.text2) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
