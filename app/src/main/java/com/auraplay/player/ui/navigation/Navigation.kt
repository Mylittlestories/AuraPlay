package com.auraplay.player.ui.navigation
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.auraplay.player.ui.screens.*
import com.auraplay.player.ui.viewmodel.MainViewModel

sealed class S(val r: String) {
    object Home:S("home"); object Lib:S("lib"); object Search:S("search"); object Pl:S("pl"); object Set:S("set"); object NP:S("np"); object Q:S("q"); object Eq:S("eq"); object Theme:S("theme")
    object Alb:S("alb/{n}"){fun rt(n:String)="alb/$n"}; object Art:S("art/{n}"){fun rt(n:String)="art/$n"}; object Fol:S("fol/{n}"){fun rt(n:String)="fol/$n"}; object PlD:S("pld/{id}"){fun rt(id:Long)="pld/$id"}
}
data class BI(val s:S, val l:String, val si:ImageVector, val ui:ImageVector)
val tabs = listOf(BI(S.Home,"Home",Icons.Filled.Home,Icons.Outlined.Home),BI(S.Lib,"Library",Icons.Filled.LibraryMusic,Icons.Outlined.LibraryMusic),BI(S.Search,"Search",Icons.Filled.Search,Icons.Outlined.Search),BI(S.Pl,"Playlists",Icons.Filled.QueueMusic,Icons.Outlined.QueueMusic),BI(S.Set,"Settings",Icons.Filled.Settings,Icons.Outlined.Settings))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraPlayNavHost(vm: MainViewModel) {
    val nc = rememberNavController(); val entry by nc.currentBackStackEntryAsState(); val route = entry?.destination?.route
    val showBar = route in tabs.map { it.s.r }; val cur by vm.cur.collectAsStateWithLifecycle(); val showMini = cur != null && route != S.NP.r
    Scaffold(bottomBar = { Column {
        if (showMini) { val t by vm.cur.collectAsStateWithLifecycle(); val p by vm.playing.collectAsStateWithLifecycle(); val pos by vm.pos.collectAsStateWithLifecycle(); val d by vm.dur.collectAsStateWithLifecycle(); val c = com.auraplay.player.ui.theme.LocalColors.current
            Surface(Modifier.fillMaxWidth(), color = c.surfaceVar, tonalElevation = 8.dp, onClick = { nc.navigate(S.NP.r) }) {
                Column { LinearProgressIndicator(progress = { if (d > 0) pos.toFloat() / d else 0f }, Modifier.fillMaxWidth().height(3.dp), color = c.primary, trackColor = c.surfaceVar)
                    Row(Modifier.fillMaxWidth().padding(12.dp, 8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        t?.let { Text(it.title, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, color = c.text) }
                        IconButton(onClick = { vm.prev() }) { Icon(Icons.Default.SkipPrevious, null, Modifier.size(28.dp)) }
                        IconButton(onClick = { vm.toggle() }) { Icon(if (p) Icons.Default.Pause else Icons.Default.PlayArrow, null, Modifier.size(36.dp)) }
                        IconButton(onClick = { vm.next() }) { Icon(Icons.Default.SkipNext, null, Modifier.size(28.dp)) }
                    }
                }
            }
        }
        if (showBar) { NavigationBar(containerColor = MaterialTheme.colorScheme.surface) { tabs.forEach { b -> val sel = route == b.s.r; NavigationBarItem(sel, { nc.navigate(b.s.r) { popUpTo(nc.graph.findStartDestination().id){saveState=true}; launchSingleTop=true; restoreState=true } }, { Icon(if (sel) b.si else b.ui, b.l) }, label = { Text(b.l) }) } } }
    } }) { pv ->
        NavHost(nc, S.Home.r, Modifier.padding(pv), enterTransition = { fadeIn(tween(300)) }, exitTransition = { fadeOut(tween(300)) }) {
            composable(S.Home.r) { HomeScreen(vm) { nc.navigate(S.NP.r) } }
            composable(S.Lib.r) { LibraryScreen(vm, { nc.navigate(S.Alb.rt(it)) }, { nc.navigate(S.Art.rt(it)) }, { nc.navigate(S.Fol.rt(it)) }) { nc.navigate(S.NP.r) } }
            composable(S.Search.r) { SearchScreen(vm) { nc.navigate(S.NP.r) } }
            composable(S.Pl.r) { PlaylistsScreen(vm) { nc.navigate(S.PlD.rt(it)) } }
            composable(S.Set.r) { SettingsScreen(vm, { nc.navigate(S.Eq.r) }, { nc.navigate(S.Theme.r) }) }
            composable(S.NP.r) { NowPlayingScreen(vm) { nc.popBackStack() } }
            composable(S.Q.r) { QueueScreen(vm) { nc.popBackStack() } }
            composable(S.Eq.r) { EqualizerScreen(vm) { nc.popBackStack() } }
            composable(S.Theme.r) { val t by vm.currentTheme.collectAsStateWithLifecycle(); ThemeScreen(t, { vm.setTheme(it) }) { nc.popBackStack() } }
            composable(S.Alb.r) { AlbumDetail(vm, it.arguments?.getString("n") ?: "", { nc.popBackStack() }) { nc.navigate(S.NP.r) } }
            composable(S.Art.r) { ArtistDetail(vm, it.arguments?.getString("n") ?: "", { nc.popBackStack() }) { nc.navigate(S.NP.r) } }
            composable(S.Fol.r) { FolderDetail(vm, it.arguments?.getString("n") ?: "", { nc.popBackStack() }) { nc.navigate(S.NP.r) } }
            composable(S.PlD.r) { PlistDetail(vm, it.arguments?.getString("id")?.toLongOrNull() ?: 0, { nc.popBackStack() }) { nc.navigate(S.NP.r) } }
        }
    }
}
