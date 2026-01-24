package org.s3m4su.accesspath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

data class UserProfile(
    val name: String,
    val badge: String? = null,
    val contributorLevel: Int = 1,
    val reviewCount: Int = 0,
    val avatarUrl: String? = null
)

enum class DrawerMenuItem {
    HOME,
    SAVED_PLACES,
    MY_REVIEWS,
    SETTINGS
}

@Composable
fun DrawerMenu(
    userProfile: UserProfile,
    selectedItem: DrawerMenuItem,
    isDarkMode: Boolean,
    onItemSelected: (DrawerMenuItem) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors

    // Fondo surface para consistencia con el popup
    val drawerBgColor = colors.surface
    val contentColor = colors.textPrimary
    val secondaryContentColor = colors.textSecondary
    val itemBgColor = colors.surfaceVariant
    val selectedItemBgColor = colors.primary
    val selectedItemContentColor = Color.White

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(
                color = drawerBgColor,
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            )
            .padding(24.dp)
    ) {
        // User Profile Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )

                userProfile.badge?.let { badge ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.primary
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Stats
        Text(
            text = "Nivel de contribuidor ${userProfile.contributorLevel} · ${userProfile.reviewCount} Valoraciones",
            style = MaterialTheme.typography.bodySmall,
            color = secondaryContentColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Menu Items
        DrawerMenuItemRow(
            icon = Icons.Default.Home,
            label = "Inicio",
            isSelected = selectedItem == DrawerMenuItem.HOME,
            itemBgColor = itemBgColor,
            selectedBgColor = selectedItemBgColor,
            contentColor = contentColor,
            selectedContentColor = selectedItemContentColor,
            onClick = { onItemSelected(DrawerMenuItem.HOME) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrawerMenuItemRow(
            icon = Icons.Default.Favorite,
            label = "Sitios Guardados",
            isSelected = selectedItem == DrawerMenuItem.SAVED_PLACES,
            itemBgColor = itemBgColor,
            selectedBgColor = selectedItemBgColor,
            contentColor = contentColor,
            selectedContentColor = selectedItemContentColor,
            onClick = { onItemSelected(DrawerMenuItem.SAVED_PLACES) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrawerMenuItemRow(
            icon = Icons.Default.Star,
            label = "Mis Valoraciones",
            isSelected = selectedItem == DrawerMenuItem.MY_REVIEWS,
            itemBgColor = itemBgColor,
            selectedBgColor = selectedItemBgColor,
            contentColor = contentColor,
            selectedContentColor = selectedItemContentColor,
            onClick = { onItemSelected(DrawerMenuItem.MY_REVIEWS) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrawerMenuItemRow(
            icon = Icons.Default.Settings,
            label = "Configuración",
            isSelected = selectedItem == DrawerMenuItem.SETTINGS,
            itemBgColor = itemBgColor,
            selectedBgColor = selectedItemBgColor,
            contentColor = contentColor,
            selectedContentColor = selectedItemContentColor,
            onClick = { onItemSelected(DrawerMenuItem.SETTINGS) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dark Mode Toggle
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = itemBgColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = contentColor
                    )
                    Text(
                        text = "Modo Oscuro",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                }

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = colors.divider
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = secondaryContentColor
                )
                Text(
                    text = "Cerrar Sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = secondaryContentColor
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuItemRow(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    itemBgColor: Color,
    selectedBgColor: Color,
    contentColor: Color,
    selectedContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) selectedBgColor else itemBgColor,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) selectedContentColor else contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) selectedContentColor else contentColor
            )
        }
    }
}
