package com.axoloth.captaggenerator.screen.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axoloth.captaggenerator.ui.theme.CapTagGeneratorTheme

// Colors to match the side menu design
private val MenuBackground = Color(0xFF0D0D17)
private val MenuGlowPurple = Color(0xFFA020F0)
private val MenuTextPrimary = Color.White
private val MenuTextSecondary = Color.Gray
private val MenuIconPurple = Color(0xFFBB86FC)

@Composable
fun SideMenuContent(
    onClose: () -> Unit = {},
    onItemClick: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.7f) // Fix max 70% width
            .clip(RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp))
            .background(MenuBackground)
    ) {
        // Glowing Border Effect on the right side
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .align(Alignment.CenterEnd)
                .blur(8.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MenuGlowPurple, Color.Transparent)
                    )
                )
        )
        
        // Actual Border Line
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .align(Alignment.CenterEnd)
                .background(MenuGlowPurple)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MenuIconPurple, modifier = Modifier.size(32.dp))
                }
            }

            // Header: User Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Warung Kopi Jaya",
                        color = MenuTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "user UMKM profile",
                        color = MenuTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Menu Items
            val menuItems = listOf(
                MenuItemData(Icons.Default.AccountCircle, "Pengaturan Akun"),
                MenuItemData(Icons.Default.Settings, "Pengaturan Aplikasi"),
                MenuItemData(Icons.Default.Campaign, "Umpan Balik"),
                MenuItemData(Icons.Default.HelpCenter, "Pusat Bantuan"),
                MenuItemData(Icons.Default.Info, "Tentang LapakAI")
            )

            menuItems.forEach { item ->
                SideMenuItem(item.icon, item.label, onClick = { onItemClick(item.label) })
                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            HorizontalDivider(color = MenuIconPurple.copy(alpha = 0.5f), thickness = 1.dp)
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { /* Handle Logout */ }
                    .padding(vertical = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MenuIconPurple, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Keluar", color = MenuIconPurple, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "v1.0.1 (UMKM Cerdas)",
                color = MenuTextSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = "Pembaruan Saat Ini: UMKM Cerdas",
                color = MenuTextSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SideMenuItem(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MenuIconPurple,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = label,
            color = MenuTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

data class MenuItemData(val icon: ImageVector, val label: String)

@Preview
@Composable
fun SideMenuPreview() {
    CapTagGeneratorTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            SideMenuContent()
        }
    }
}
