package com.auraplay.player.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.auraplay.player.data.model.Track
import com.auraplay.player.ui.theme.LocalColors

fun fmtDur(ms: Long): String { val s = ms / 1000; return "%d:%02d".format(s / 60, s % 60) }

@Composable
fun TrackRow(track: Track, onClick: () -> Unit, onMore: () -> Unit = {}, playing: Boolean = false, showIdx: Boolean = false, idx: Int = 0) {
    val c = LocalColors.current
    Surface(Modifier.fillMaxWidth().clickable { onClick() }, color = if (playing) c.primary.copy(alpha = 0.15f) else Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (showIdx) { Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { Text("${idx+1}", style = MaterialTheme.typography.bodyMedium, color = c.text3) } }
            else { Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(listOf(c.primary, c.secondary))), contentAlignment = Alignment.Center) {
                if (track.albumArtUri != null) AsyncImage(track.albumArtUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(Icons.Default.MusicNote, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(24.dp))
            } }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (playing) FontWeight.Bold else FontWeight.Normal, color = if (playing) c.primary else c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${track.artist} • ${track.formattedDuration}", style = MaterialTheme.typography.bodySmall, color = c.text2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onMore, Modifier.size(36.dp)) { Icon(Icons.Default.MoreVert, null, Modifier.size(20.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector, sub: String? = null) {
    val c = LocalColors.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = c.primary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
        Column { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = c.text); if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = c.text2) }
    }
}

@Composable
fun QuickCard(modifier: Modifier = Modifier, icon: ImageVector, title: String, sub: String, grad: List<Color>, onClick: () -> Unit) {
    Card(modifier.height(100.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp)) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(grad)).padding(16.dp)) {
            Column { Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp)); Spacer(Modifier.weight(1f)); Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White); Text(sub, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f)) }
        }
    }
}
