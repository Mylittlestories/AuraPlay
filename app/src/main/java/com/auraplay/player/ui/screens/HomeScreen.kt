package com.auraplay.player.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.auraplay.player.data.model.Track
import com.auraplay.player.ui.theme.LocalColors
import com.auraplay.player.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(vm: MainViewModel, onNP: () -> Unit) {
    val tracks by vm.tracks.collectAsStateWithLifecycle()
    val recent by vm.recentPlayed.collectAsStateWithLifecycle()
    val top by vm.topPlayed.collectAsStateWithLifecycle()
    val favs by vm.favorites.collectAsStateWithLifecycle()
    val scanning by vm.scanning.collectAsStateWithLifecycle()
    val msg by vm.scanMsg.collectAsStateWithLifecycle()
    val c = LocalColors.current

    LaunchedEffect(Unit) { if (tracks.isEmpty() && !scanning) vm.scan() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item { Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(c.primary.copy(0.3f), Color.Transparent))).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(c.primary, c.secondary))), contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                Spacer(Modifier.width(16.dp))
                Column { Text("AuraPlay", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = c.primary); Text(if (tracks.isNotEmpty()) "${tracks.size} tracks" else "Tap scan", style = MaterialTheme.typography.bodyLarge, color = c.text2) }
            }
        } }
        if (scanning) item { Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = c.primary.copy(0.2f))) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp); Spacer(Modifier.width(12.dp)); Text(msg, color = c.text) } } }
        item { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickCard(Modifier.weight(1f), Icons.Default.Shuffle, "Shuffle All", "${tracks.size} tracks", listOf(c.accent, c.secondary)) { vm.shuffleAll(); onNP() }
            QuickCard(Modifier.weight(1f), Icons.Default.Favorite, "Favorites", "${favs.size} tracks", listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E))) { if (favs.isNotEmpty()) { vm.play(favs.first(), favs); onNP() } }
        } }
        if (recent.isNotEmpty()) {
            item { SectionHeader("Recently Played", Icons.Default.History) }
            item { LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(recent.take(10)) { t -> Card(Modifier.width(140.dp), shape = RoundedCornerShape(12.dp)) { Column { Box(Modifier.fillMaxWidth().height(140.dp).background(Brush.linearGradient(listOf(c.primary, c.secondary))), contentAlignment = Alignment.Center) { if (t.albumArtUri != null) AsyncImage(t.albumArtUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) else Icon(Icons.Default.MusicNote, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(48.dp)) } Column(Modifier.padding(8.dp)) { Text(t.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(t.artist, style = MaterialTheme.typography.bodySmall, color = c.text2, maxLines = 1, overflow = TextOverflow.Ellipsis) } } } } } }
        }
        if (top.isNotEmpty()) {
            item { SectionHeader("Most Played", Icons.Default.TrendingUp) }
            items(top.take(20)) { t -> TrackRow(t, { vm.play(t, top); onNP() }) }
        }
        if (tracks.isEmpty() && !scanning) item { Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LibraryMusic, null, Modifier.size(64.dp), tint = c.primary.copy(0.5f)); Spacer(Modifier.height(16.dp))
            Text("No music found", style = MaterialTheme.typography.titleLarge, color = c.text2); Spacer(Modifier.height(24.dp))
            Button(onClick = { vm.scan() }, Modifier.fillMaxWidth(0.7f)) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(8.dp)); Text("Scan for Music") }
        } }
    }
}
