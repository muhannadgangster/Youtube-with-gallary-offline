package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YtAvatarPurple
import com.example.ui.theme.YtBorder
import com.example.ui.theme.YtDarkBackground
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary

@Composable
fun YouTubeBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(YtDarkBackground)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            color = YtBorder.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 0: Home
            BottomNavItem(
                icon = {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home",
                        tint = if (selectedTab == 0) YtTextPrimary else YtTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "Home",
                isSelected = selectedTab == 0,
                testTag = "nav_home",
                onClick = { onTabSelected(0) }
            )

            // 1: Shorts
            BottomNavItem(
                icon = {
                    // Modern Shorts S-glyph representation
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                color = if (selectedTab == 1) YtTextPrimary else YtTextSecondary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 11.sp,
                            color = if (selectedTab == 1) YtTextPrimary else YtTextSecondary
                        )
                    }
                },
                label = "Shorts",
                isSelected = selectedTab == 1,
                testTag = "nav_shorts",
                onClick = { onTabSelected(1) }
            )

            // 2: Center Plus (+)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.2.dp, YtTextPrimary, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(2) }
                    .testTag("nav_add_plus"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Video / Clip",
                    tint = YtTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // 3: Subscriptions
            BottomNavItem(
                icon = {
                    Icon(
                        imageVector = if (selectedTab == 3) Icons.Filled.Subscriptions else Icons.Outlined.Subscriptions,
                        contentDescription = "Subscriptions",
                        tint = if (selectedTab == 3) YtTextPrimary else YtTextSecondary,
                        modifier = Modifier.size(23.dp)
                    )
                },
                label = "Subscriptions",
                isSelected = selectedTab == 3,
                testTag = "nav_subscriptions",
                onClick = { onTabSelected(3) }
            )

            // 4: You / Profile
            BottomNavItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(YtAvatarPurple)
                            .then(
                                if (selectedTab == 4) Modifier.border(1.5.dp, YtTextPrimary, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                },
                label = "You",
                isSelected = selectedTab == 4,
                testTag = "nav_you",
                onClick = { onTabSelected(4) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: @Composable () -> Unit,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) YtTextPrimary else YtTextSecondary
        )
    }
}
