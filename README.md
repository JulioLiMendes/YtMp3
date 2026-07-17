# YtMp3

Aplicativo Android para baixar áudio do YouTube em formato MP3, com reprodução local, playlists e gerenciamento de downloads.

## Funcionalidades

- Busca e download de áudio a partir de links do YouTube
- Conversão para MP3 com suporte a download em segundo plano
- Reprodutor integrado com controle de play/pause, próxima/anterior e shuffle
- Biblioteca local com favoritos, renomeação e remoção de músicas
- Criação e gerenciamento de playlists
- Notificações de progresso do download

## Estrutura do projeto

- app/src/main/java/com/seuapp/ytmp3/
  - MainActivity.kt: ponto de entrada da interface principal
  - YtMp3Application.kt: inicialização dos componentes do app
  - data/: entidades Room, DAOs e banco local
  - download/: serviços e estados de download
  - player/: reprodução de áudio via Media3/ExoPlayer
  - ui/: telas, ViewModel, navegação e componentes Compose
  - util/: helpers de notificações e utilidades
- app/src/main/AndroidManifest.xml: permissões e serviços do aplicativo
- app/src/main/res/: recursos visuais e strings
- app/build.gradle.kts: configuração do módulo Android e dependências

## APK

O arquivo de instalação do app está em:
- [releases/YtMp3.apk](releases/YtMp3.apk)

Para instalar:
1. Baixe o arquivo acima.
2. No celular, abra o APK.
3. Se solicitado, permita a instalação de fontes desconhecidas.
4. Conclua a instalação.

## Como navegar nas pastas

1. Abra a pasta app para ver o código do aplicativo Android.
2. Entre em app/src/main/java para encontrar a lógica principal.
3. Use app/src/main/res para alterar ícones, temas e textos.
4. Consulte app/build.gradle.kts para ajustar dependências e configuração do módulo.
5. O APK está sempre em [releases/YtMp3.apk](releases/YtMp3.apk).

## Como executar localmente

### Requisitos

- Android Studio
- JDK 17
- Android SDK com API 35

### Passos

1. Clone o projeto
2. Abra a pasta no Android Studio
3. Sincronize o Gradle
4. Execute o app em um dispositivo/emulador Android

## Instalação

Baixe o arquivo APK em [releases/YtMp3.apk](releases/YtMp3.apk) e instale no seu dispositivo Android.

## Observações

Este projeto usa:

- Jetpack Compose
- Room
- Media3/ExoPlayer
- WorkManager
- yt-dlp + ffmpeg

## Repositório GitHub

O projeto está preparado para ser enviado ao GitHub com os arquivos principais, o README e o APK visível na pasta releases/.
