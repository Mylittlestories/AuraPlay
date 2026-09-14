package com.lostf1sh.pixelplayeross.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lostf1sh.pixelplayeross.BuildConfig
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.ui.theme.Aurora

sealed class DrawerDestination(val route: String) {
    object Home : DrawerDestination("home")
    object Equalizer : DrawerDestination("equalizer")
    object Settings : DrawerDestination("settings")
}

@Composable
fun AppSidebarDrawer(
    drawerState: DrawerState,
    selectedRoute: String,
    onDestinationSelected: (DrawerDestination) -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)),
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                DrawerContent(
                    selectedRoute = selectedRoute,
                    onDestinationSelected = onDestinationSelected
                )
            }
        },
        content = content
    )
}

@Composable
private fun DrawerContent(
    selectedRoute: String,
    onDestinationSelected: (DrawerDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Branded header: gradient wordmark + tagline + version chip.
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.presentation_batch_g_app_name),
                style = MaterialTheme.typography.headlineMedium.copy(
                    brush = Aurora.brush()
                ),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.presentation_batch_g_app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .background(
                        brush = Aurora.tint(
                            primary = MaterialTheme.colorScheme.primary,
                            tertiary = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "v${BuildConfig.VERSION_NAME} · DRVsoft",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Aurora hairline under the brand header.
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.40f),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    ),
                    shape = MaterialTheme.shapes.extraSmall
                )
        )

        DrawerSectionLabel(text = stringResource(R.string.drawer_section_listen))
        Spacer(modifier = Modifier.height(4.dp))

        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = stringResource(R.string.tab_home)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.tab_home),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            selected = selectedRoute == DrawerDestination.Home.route,
            onClick = { onDestinationSelected(DrawerDestination.Home) },
            modifier = Modifier.padding(vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(20.dp)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = stringResource(R.string.settings_category_equalizer_title)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.settings_category_equalizer_title),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            selected = selectedRoute == DrawerDestination.Equalizer.route,
            onClick = { onDestinationSelected(DrawerDestination.Equalizer) },
            modifier = Modifier.padding(vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        DrawerSectionLabel(text = stringResource(R.string.drawer_section_system))
        Spacer(modifier = Modifier.height(4.dp))

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.rounded_settings_24),
                    contentDescription = stringResource(R.string.settings_top_bar_title)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.settings_top_bar_title),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            selected = selectedRoute == DrawerDestination.Settings.route,
            onClick = { onDestinationSelected(DrawerDestination.Settings) },
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 0.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "github.com/Mylittlestories/AuraPlay",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
