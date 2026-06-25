package com.auraplay.player.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.auraplay.player.ui.theme.LocalColors
import com.auraplay.player.ui.viewmodel.MainViewModel

enum class LibTab { TRACKS, ALBUMS, ARTISTS, FOLDERS, FAVORITES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: MainViewModel, onAlbum: (String)->Unit, onArtist: (String)->Unit, onFolder: (String)->Unit, onNP: () -> Unit) {
    val tracks by vm.tracks.collectAsStateWithLifecycle()
    val favs by vm.favorites.collectAsStateWithLifecycle()
    val c = LocalColors.current
    var tab by remember { mutableStateOf(LibTab.TRACKS) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Library", fontWeight = FontWeight.Bold) }, actions = { IconButton(onClick = { vm.scan() }) { Icon(Icons.Default.Refresh, "Refresh") } })
        ScrollableTabRow(selectedTabIndex = tab.ordinal, edgePadding = 16.dp, containerColor = c.surface) {
            LibTab.entries.forEach { t -> Tab(selected = tab == t, onClick = { tab = t }, text = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }) }) }
        }
        when (tab) {
            LibTab.TRACKS -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                item { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.playAll(tracks); onNP() }, Modifier.weight(1f)) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(4.dp)); Text("Play All") }
                    OutlinedButton(onClick = { vm.shuffleAll(); onNP() }, Modifier.weight(1f)) { Icon(Icons.Default.Shuffle, null); Spacer(Modifier.width(4.dp)); Text("Shuffle") }
                } }
                items(tracks) { t -> TrackRow(t, { vm.play(t); onNP() }) }
            }
            LibTab.ALBUMS -> { val g = tracks.groupBy { it.album }; LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(g.entries.toList()) { (n, ts) -> Surface(Modifier.fillMaxWidth().clickable { onAlbum(n) }) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(listOf(c.primary, c.secondary))), contentAlignment = Alignment.Center) { if (ts.first().albumArtUri != null) AsyncImage(ts.first().albumArtUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) else Icon(Icons.Default.Album, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                    Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text(n, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = c.text); Text("${ts.first().artist} • ${ts.size} tracks", style = MaterialTheme.typography.bodySmall, color = c.text2) }; Icon(Icons.Default.ChevronRight, null, tint = c.text3)
                } } }
            } }
            LibTab.ARTISTS -> { val g = tracks.groupBy { it.artist }; LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(g.entries.toList()) { (n, ts) -> Surface(Modifier.fillMaxWidth().clickable { onArtist(n) }) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(56.dp).clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(c.secondary, c.accent))), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                    Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text(n, fontWeight = FontWeight.SemiBold, color = c.text); Text("${ts.map{it.album}.distinct().size} albums • ${ts.size} tracks", style = MaterialTheme.typography.bodySmall, color = c.text2) }; Icon(Icons.Default.ChevronRight, null, tint = c.text3)
                } } }
            } }
            LibTab.FOLDERS -> { val g = tracks.groupBy { it.folderName }; LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(g.entries.toList()) { (n, ts) -> Surface(Modifier.fillMaxWidth().clickable { onFolder(n) }) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(c.surfaceVar), contentAlignment = Alignment.Center) { Icon(Icons.Default.Folder, null, tint = c.primary, modifier = Modifier.size(24.dp)) }
                    Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text(n, fontWeight = FontWeight.SemiBold, color = c.text); Text("${ts.size} tracks", style = MaterialTheme.typography.bodySmall, color = c.text2) }; Icon(Icons.Default.ChevronRight, null, tint = c.text3)
                } } }
            } }
            LibTab.FAVORITES -> if (favs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.FavoriteBorder, null, Modifier.size(64.dp), tint = c.text3); Text("No favorites yet", color = c.text2) } }
            else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) { items(favs) { t -> TrackRow(t, { vm.play(t, favs); onNP() }) } }
        }
    }
}
