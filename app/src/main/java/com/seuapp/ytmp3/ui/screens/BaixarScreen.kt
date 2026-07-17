package com.seuapp.ytmp3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seuapp.ytmp3.download.DownloadState
import kotlinx.coroutines.delay

@Composable
fun BaixarScreen(
    downloadState: DownloadState,
    aoBaixar: (String) -> Boolean,
    aoResetar: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    var erroValidacao by remember { mutableStateOf(false) }

    val baixando = downloadState is DownloadState.BuscandoInformacoes ||
        downloadState is DownloadState.Baixando ||
        downloadState is DownloadState.Convertendo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "YTMp3",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EE), // Roxo vibrante similar ao da imagem
            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
        )

        Text("Baixar música", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Cole o link de um vídeo do YouTube para baixar o áudio em mp3.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = url,
            onValueChange = {
                url = it
                erroValidacao = false
            },
            label = { Text("Link do YouTube") },
            singleLine = true,
            isError = erroValidacao,
            supportingText = {
                if (erroValidacao) Text("Cole um link válido do YouTube")
            },
            enabled = !baixando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val sucesso = aoBaixar(url)
                if (!sucesso) {
                    erroValidacao = true
                } else {
                    url = ""
                }
            },
            enabled = !baixando && url.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (baixando) "Baixando..." else "Baixar mp3")
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val estado = downloadState) {
            is DownloadState.Ocioso -> Unit

            is DownloadState.BuscandoInformacoes ->
                BlocoProgresso("Buscando informações do vídeo...")

            is DownloadState.Baixando ->
                BlocoProgresso(
                    texto = "Baixando: ${estado.progresso.toInt()}%",
                    progresso = estado.progresso / 100f
                )

            is DownloadState.Convertendo ->
                BlocoProgresso("Convertendo para mp3...")

            is DownloadState.Concluido -> {
                Text(
                    text = "✅ \"${estado.musica.titulo}\" baixada com sucesso!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                // Some sozinho depois de alguns segundos, sem precisar de ação do usuário
                LaunchedEffect(estado) {
                    delay(3000)
                    aoResetar()
                }
            }

            is DownloadState.Erro -> {
                Text(
                    text = "❌ Erro ao baixar: ${estado.mensagem}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun BlocoProgresso(texto: String, progresso: Float? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(texto, style = MaterialTheme.typography.bodyMedium)
        if (progresso != null) {
            LinearProgressIndicator(progress = { progresso }, modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}
