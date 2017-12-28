@echo off

if exist "./mapa.map" goto mapaEncontrado

echo Nao foi possivel encontrar o mapa do jogo.
pause
goto fim

:mapaEncontrado

if exist "./images" goto imagemEncontrada

echo Nao foi possivel encontrar as imagens do jogo.
pause
goto fim

:imagemEncontrada

if exist "./Pac-Man.jar" goto programaEncontrado

echo Nao foi possivel encontrar o programa especificado.
pause
goto fim

:programaEncontrado

java -jar ./Pac-Man.jar

:fim