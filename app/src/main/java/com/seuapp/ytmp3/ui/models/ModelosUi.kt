package com.seuapp.ytmp3.ui.models

import androidx.compose.runtime.Immutable
import com.seuapp.ytmp3.data.local.Musica

/**
 * Wrapper estável para uma lista de músicas.
 * Isso ajuda o Jetpack Compose a otimizar a recomposição do LazyColumn,
 * pois o compilador do Compose não consegue garantir a estabilidade da interface List por padrão.
 */
@Immutable
data class ListaMusicasEstavel(
    val itens: List<Musica> = emptyList()
)
