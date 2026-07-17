package com.seuapp.ytmp3

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seuapp.ytmp3.data.local.Musica
import com.seuapp.ytmp3.data.local.Playlist
import com.seuapp.ytmp3.ui.MainViewModel
import com.seuapp.ytmp3.ui.components.ALTURA_PLAYER_COLAPSADA
import com.seuapp.ytmp3.ui.components.MusicaSelectorDialog
import com.seuapp.ytmp3.ui.components.PlayerGaveta
import com.seuapp.ytmp3.ui.components.PlaylistSelectorDialog
import com.seuapp.ytmp3.ui.models.ListaMusicasEstavel
import com.seuapp.ytmp3.ui.navigation.Rota
import com.seuapp.ytmp3.ui.screens.BaixarScreen
import com.seuapp.ytmp3.ui.screens.BibliotecaScreen
import com.seuapp.ytmp3.ui.screens.PlaylistDetalhesScreen
import com.seuapp.ytmp3.ui.screens.PlaylistsScreen
import com.seuapp.ytmp3.ui.theme.YtMp3Theme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YtMp3Theme {
                TelaPrincipal(viewModel)
            }
        }
    }
}

@Composable
private fun TelaPrincipal(viewModel: MainViewModel) {
    val lancadorPermissao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lancadorPermissao.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val navController = rememberNavController()

    val musicas by viewModel.musicas.collectAsStateWithLifecycle()
    val musicasEstaveis by viewModel.musicasEstaveis.collectAsStateWithLifecycle()
    val termoBusca by viewModel.termoBusca.collectAsStateWithLifecycle()
    val termoBuscaPlaylists by viewModel.termoBuscaPlaylists.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val posicaoAtual by viewModel.posicaoAtualMs.collectAsStateWithLifecycle()

    var playerExpandido by remember { mutableStateOf(false) }
    var musicaParaPlaylist by remember { mutableStateOf<Musica?>(null) }
    var idPlaylistParaAdicionarMusica by remember { mutableStateOf<Long?>(null) }

    // Callbacks estáveis para evitar recomposições desnecessárias
    val aoBaixar = remember(viewModel) { { url: String -> viewModel.baixar(url) } }
    val aoResetarDownload = remember(viewModel) { { viewModel.resetarEstadoDownload() } }
    val aoTocar = remember(viewModel, musicas) { 
        { musica: Musica -> viewModel.tocar(musica, musicas, "Biblioteca") } 
    }
    val aoFavoritar = remember(viewModel) { { musica: Musica -> viewModel.alternarFavorito(musica); Unit } }
    val aoRemover = remember(viewModel) { { musica: Musica -> viewModel.remover(musica); Unit } }
    val aoAdicionarNaPlaylist = remember { { musica: Musica -> musicaParaPlaylist = musica } }
    val aoRenomear = remember(viewModel) { { musica: Musica, novoNome: String -> viewModel.renomearMusica(musica, novoNome); Unit } }
    val aoMudarBusca = remember(viewModel) { { termo: String -> viewModel.buscarMusica(termo) } }
    
    val aoClicarPlaylist = remember(navController) { 
        { id: Long, nome: String -> navController.navigate("playlist/$id?nome=$nome") } 
    }
    val aoCriarPlaylist = remember(viewModel) { { nome: String -> viewModel.criarPlaylist(nome); Unit } }
    val aoDeletarPlaylist = remember(viewModel) { { playlist: Playlist -> viewModel.deletarPlaylist(playlist); Unit } }
    val aoMudarBuscaPlaylists = remember(viewModel) { { termo: String -> viewModel.buscarPlaylists(termo) } }

    // Botão de voltar do sistema recolhe o player em vez de sair do app,
    // enquanto a gaveta estiver expandida.
    BackHandler(enabled = playerExpandido) {
        playerExpandido = false
    }

    val itensNavegacao = listOf(Rota.Baixar, Rota.Biblioteca, Rota.Playlists)

    // Medimos a altura real da NavigationBar para a gaveta colapsada
    // encaixar exatamente acima dela (e cobri-la por completo quando expandida).
    val densidade = LocalDensity.current
    var alturaNavBarPx by remember { mutableIntStateOf(0) }
    val alturaNavBarDp = with(densidade) { alturaNavBarPx.toDp() }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.onGloballyPositioned {
                        alturaNavBarPx = it.size.height
                    }
                ) {
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val rotaAtual = backStackEntry?.destination?.route

                    itensNavegacao.forEach { rota ->
                        NavigationBarItem(
                            selected = rotaAtual == rota.caminho,
                            onClick = {
                                navController.navigate(rota.caminho) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (rota) {
                                        Rota.Baixar -> Icons.Default.CloudDownload
                                        Rota.Biblioteca -> Icons.Default.LibraryMusic
                                        Rota.Playlists -> Icons.Default.PlaylistPlay
                                        else -> Icons.Default.LibraryMusic
                                    },
                                    contentDescription = rota.titulo
                                )
                            },
                            label = { Text(rota.titulo) }
                        )
                    }
                }
            }
        ) { paddingInterno ->
            val direcaoLayout = LocalLayoutDirection.current
            NavHost(
                navController = navController,
                startDestination = Rota.Baixar.caminho,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
                modifier = Modifier.padding(
                    start = paddingInterno.calculateStartPadding(direcaoLayout),
                    top = paddingInterno.calculateTopPadding(),
                    end = paddingInterno.calculateEndPadding(direcaoLayout),
                    bottom = paddingInterno.calculateBottomPadding() + (if (playerState.musicaAtual != null) ALTURA_PLAYER_COLAPSADA else 0.dp)
                )
            ) {
                composable(Rota.Baixar.caminho) {
                    BaixarScreen(
                        downloadState = downloadState,
                        aoBaixar = aoBaixar,
                        aoResetar = aoResetarDownload
                    )
                }
                composable(Rota.Biblioteca.caminho) {
                    BibliotecaScreen(
                        musicas = musicasEstaveis,
                        termoBusca = termoBusca,
                        aoMudarBusca = aoMudarBusca,
                        aoTocar = aoTocar,
                        aoFavoritar = aoFavoritar,
                        aoRemover = aoRemover,
                        aoAdicionarNaPlaylist = aoAdicionarNaPlaylist,
                        aoRenomear = aoRenomear
                    )
                }
                composable(Rota.Playlists.caminho) {
                    PlaylistsScreen(
                        playlists = playlists,
                        termoBusca = termoBuscaPlaylists,
                        aoMudarBusca = aoMudarBuscaPlaylists,
                        aoClicarPlaylist = aoClicarPlaylist,
                        aoCriarPlaylist = aoCriarPlaylist,
                        aoDeletarPlaylist = aoDeletarPlaylist
                    )
                }
                composable(
                    route = Rota.PlaylistDetalhes.caminho + "?nome={nome}",
                    arguments = listOf(
                        navArgument("id") { type = NavType.LongType },
                        navArgument("nome") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: 0L
                    val nome = backStackEntry.arguments?.getString("nome") ?: "Playlist"
                    val musicasPlaylist by viewModel.listarMusicasDaPlaylist(id)
                        .collectAsStateWithLifecycle(emptyList())

                    PlaylistDetalhesScreen(
                        titulo = nome,
                        musicas = ListaMusicasEstavel(musicasPlaylist),
                        aoVoltar = { navController.popBackStack() },
                        aoTocar = { musica -> viewModel.tocar(musica, musicasPlaylist, nome) },
                        aoFavoritar = { musica -> viewModel.alternarFavorito(musica) },
                        aoAdicionarMusica = { idPlaylistParaAdicionarMusica = id },
                        aoRemoverDaPlaylist = { musica -> viewModel.removerDaPlaylist(id, musica.id) }
                    )
                }
            }
        }

        if (playerState.musicaAtual != null) {
            PlayerGaveta(
                estado = playerState,
                posicaoAtualMs = posicaoAtual,
                expandido = playerExpandido,
                alturaNavBar = alturaNavBarDp,
                aoExpandir = { playerExpandido = true },
                aoRecolher = { playerExpandido = false },
                aoAlternarPlayPause = { viewModel.alternarPlayPause() },
                aoProxima = { viewModel.proxima() },
                aoAnterior = { viewModel.anterior() },
                aoBuscarPosicao = { ms -> viewModel.buscarPosicao(ms) },
                aoFavoritar = { playerState.musicaAtual?.let { viewModel.alternarFavorito(it) } },
                aoAlternarShuffle = { viewModel.alternarShuffle() },
                aoAdicionarNaPlaylist = { musicaParaPlaylist = playerState.musicaAtual }
            )
        }
    }

    if (musicaParaPlaylist != null) {
        PlaylistSelectorDialog(
            playlists = playlists,
            onDismiss = { musicaParaPlaylist = null },
            onPlaylistSelected = { playlistId ->
                musicaParaPlaylist?.let { musica ->
                    viewModel.adicionarNaPlaylist(playlistId, musica.id)
                }
                musicaParaPlaylist = null
            }
        )
    }

    if (idPlaylistParaAdicionarMusica != null) {
        MusicaSelectorDialog(
            musicas = musicas,
            onDismiss = { idPlaylistParaAdicionarMusica = null },
            onMusicaSelected = { musica ->
                idPlaylistParaAdicionarMusica?.let { playlistId ->
                    viewModel.adicionarNaPlaylist(playlistId, musica.id)
                }
                idPlaylistParaAdicionarMusica = null
            }
        )
    }
}
