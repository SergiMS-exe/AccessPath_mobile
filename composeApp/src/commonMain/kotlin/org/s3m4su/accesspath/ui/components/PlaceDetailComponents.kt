package org.s3m4su.accesspath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import org.s3m4su.accesspath.ui.PlatformBackHandler
import org.s3m4su.accesspath.ui.theme.AccessPathTheme

// =============================================================================
// FOTOS
// =============================================================================

/**
 * Placeholder visual de una foto. Mientras no haya carga real de imagenes,
 * deriva un degradado deterministico a partir del [seed] para que cada foto se
 * vea distinta y reconocible. Cuando se incorpore una libreria de imagenes,
 * este componente pasara a cargar la URL real.
 */
@Composable
fun PhotoPlaceholder(
    seed: String,
    modifier: Modifier = Modifier
) {
    val hash = abs(seed.hashCode())
    val hue = (hash % 360).toFloat()
    val top = Color.hsv(hue, 0.45f, if (AccessPathTheme.isDark) 0.40f else 0.78f)
    val bottom = Color.hsv((hue + 28f) % 360f, 0.55f, if (AccessPathTheme.isDark) 0.26f else 0.62f)

    Box(
        modifier = modifier.background(Brush.linearGradient(listOf(top, bottom))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.55f),
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * Galeria de fotos del detalle: una foto principal grande y, si hay mas, una
 * tira horizontal con el resto. [photos] son seeds/URLs. Tocar cualquiera abre
 * el visor a pantalla completa via [onPhotoClick] (indice dentro de [photos]).
 */
@Composable
fun PhotoGallery(
    photos: List<String>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AccessPathTheme.colors

    if (photos.isEmpty()) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceVariant
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Aun no hay fotos de este sitio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary
                )
            }
        }
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Foto principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onPhotoClick(0) }
        ) {
            PhotoPlaceholder(seed = photos.first(), modifier = Modifier.fillMaxSize())

            // Pildora con el total de fotos
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${photos.size} foto${if (photos.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // Tira con el resto de fotos
        if (photos.size > 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(photos.drop(1)) { index, photo ->
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPhotoClick(index + 1) }
                    ) {
                        PhotoPlaceholder(seed = photo, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

/**
 * Visor de fotos a pantalla completa con paginacion horizontal. Se cierra con
 * el boton o con el gesto/boton de retroceso.
 */
@Composable
fun PhotoLightbox(
    photos: List<String>,
    startIndex: Int,
    onClose: () -> Unit
) {
    PlatformBackHandler(enabled = true, onBack = onClose)

    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, (photos.size - 1).coerceAtLeast(0)),
        pageCount = { photos.size }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                PhotoPlaceholder(
                    seed = photos[page],
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${photos.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
