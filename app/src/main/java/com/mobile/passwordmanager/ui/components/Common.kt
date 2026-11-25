package com.mobile.passwordmanager.ui.components

import android.widget.ImageView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizePx
import com.mobile.passwordmanager.IconCatalog
import com.mobile.passwordmanager.ui.theme.GradientEnd
import com.mobile.passwordmanager.ui.theme.GradientMid
import com.mobile.passwordmanager.ui.theme.GradientStart

/**
 * 品牌图标（来自内置 Simple Icons 字体库，117 个常见网站/APP）。
 *
 * - 有 iconKey 时用 AndroidView 承载 IconicsDrawable，默认按品牌官方主色渲染；
 *   深色模式下自动提亮过暗的品牌色（如 GitHub / X），保证可见。
 * - [tint] 传入具体颜色可覆盖品牌色；[useBrandColor]=false 时回退到主题主色。
 * - [container] 为 true 时在外层绘制品牌色淡色圆角底，形成现代 App 图标质感。
 * - 无 iconKey 时回退到通用的钥匙图标。
 */
@Composable
fun BrandIcon(
    iconKey: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Int = 24,
    useBrandColor: Boolean = true,
    container: Boolean = false
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val entry = remember(iconKey) { IconCatalog.find(iconKey) }
    val primary = MaterialTheme.colorScheme.primary

    val brandColor = remember(entry, darkTheme) {
        entry?.let { IconCatalog.readableColor(it.color, darkTheme) }
    } ?: primary
    val resolvedTint = when {
        tint != Color.Unspecified -> tint
        useBrandColor -> brandColor
        else -> primary
    }

    val content: @Composable () -> Unit = {
        if (entry != null) {
            AndroidView(
                factory = { ctx ->
                    ImageView(ctx).apply { scaleType = ImageView.ScaleType.CENTER_INSIDE }
                },
                update = { iv ->
                    val drawable = IconicsDrawable(context, entry.icon)
                    drawable.sizePx = (size * context.resources.displayMetrics.density).toInt()
                    drawable.colorInt = resolvedTint.toArgb()
                    iv.setImageDrawable(drawable)
                    iv.contentDescription = contentDescription
                },
                modifier = Modifier.size(size.dp)
            )
        } else {
            Icon(
                Icons.Default.Key,
                contentDescription = contentDescription,
                modifier = Modifier.size(size.dp),
                tint = resolvedTint
            )
        }
    }

    if (container) {
        Box(
            modifier = modifier
                .size((size + 18).dp)
                .clip(RoundedCornerShape((size / 2 + 5).dp))
                .background(brandColor.copy(alpha = if (darkTheme) 0.24f else 0.13f)),
            contentAlignment = Alignment.Center
        ) { content() }
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) { content() }
    }
}

/** 取分组/条目的图标主色（品牌色，深色主题自动提亮），供列表卡片底色使用。 */
@Composable
fun iconColor(iconKey: String?): Color {
    val darkTheme = isSystemInDarkTheme()
    val primary = MaterialTheme.colorScheme.primary
    return remember(iconKey, darkTheme) {
        iconKey?.let { IconCatalog.readableColor(IconCatalog.colorOf(it), darkTheme) }
    } ?: primary
}

/** 渐变头部容器 */
@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(GradientStart, GradientMid, GradientEnd))
        ),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

/** 分区标题（如「分组」「密码」） */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(width = 4.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        trailing?.invoke()
    }
}

/** 空状态 */
@Composable
fun EmptyState(
    icon: @Composable () -> Unit,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Box(Modifier.padding(28.dp)) { icon() }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** 通用确认对话框 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    destructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmText,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/** 长按菜单的一项动作。destructive=true 时以红色显示（如删除）。 */
data class SheetAction(
    val icon: ImageVector,
    val label: String,
    val destructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * 通用长按操作菜单（底部弹窗）。用于替代列表项上常驻的复制/编辑/删除按钮。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionSheet(
    title: String,
    subtitle: String? = null,
    actions: List<SheetAction>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 20.dp)) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(10.dp))
            actions.forEach { action ->
                val tint = if (action.destructive) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurface
                val bg = if (action.destructive) MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                         else MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { action.onClick() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(action.icon, null, tint = tint, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(action.label, style = MaterialTheme.typography.bodyLarge, color = tint)
                }
            }
        }
    }
}
