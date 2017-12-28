
public class Ghost extends Thread {
	
	private int numGhost;
	
	private Janela janela;
	private ETipoBloco[][] mapa;
	
	private int ghostPosiX;		// posi��o x do ghost na tela
	private int ghostPosiY;		// posi��o y do ghost na tela
	
	private int ghostDireX;		// movimento x do ghost na tela
	private int ghostDireY;		// movimento y do ghost na tela
	private int ghostDire;		// dire��o do ghost
	
	private int velocidade;
	
	public Ghost(Janela janela, ETipoBloco[][] mapa, int posiXInicial, 
			int posiYInicial, int ghostDireXInicial, int ghostDireYInicial, int direInicial, int numGhost) {
		// carrega valores iniciais
		this.janela 	= janela;
		this.mapa		= mapa;
		// multiplica a posi��o na matriz(bloco) pelo tamanho do bloco para encontrar a posi��o relativa na tela
		this.ghostPosiX = posiXInicial*Util.TAMANHO_BLOCO;
		this.ghostPosiY = posiYInicial*Util.TAMANHO_BLOCO;
		this.ghostDireX = ghostDireXInicial;
		this.ghostDireY = ghostDireYInicial;
		this.ghostDire	= direInicial;
		this.numGhost	= numGhost;
		this.velocidade = (int) (Math.random()*40) + 40;	// tempo em ms que o fantasma vai se movimentar
		
		start();
	}
	
	@Override
	public void run() {
		moveGhost();
		while (!janela.tela.gameOver && !janela.tela.ganhou) {		// enquanto nao der game over ou ganhar 
			while (janela.tela.jogando) {							// enquanto estiver jogando
				if (!janela.tela.pause) {							// se nao foi pausado
					if (!janela.tela.ghostEncruzilhada[numGhost]) {	// se o ghost nao esta em uma encruzilhada aguardando por uma instru��o
						atualizaDirecao();							// atualiza qual sua posi��o com a tela
						moveGhost();								// move ghost
					}
				}
				try {
					sleep(velocidade);								// aguarda o tempo para se movimentar novamente
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {													// aguarda um pouco para jogar
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void moveGhost() {
		// verifica se o movimento � valido
		if (Util.isMovimentoValidoGhost(mapa, ghostPosiX, ghostPosiY, ghostDire)) {
			// atualiza dire��o x e y para o movimento
			ghostDireX = Util.returnDireX(ghostDire);
			ghostDireY = Util.returnDireY(ghostDire);
			oficializaMovimento();				// oficializa o movimento
			
			if (qtdCaminhosValidos() > 2){		//significa que existem mais de 2 caminhos entao avisar ao arbitro que precisa de uma dire��o
				if (deveReceberInstrucao()) {	// fator aleatorio para verificar se aceita a dire��o informada pelo arbitro
					janela.tela.ghostEncruzilhada[numGhost] = true;	// ghost esta em uma encruzilhada
				} else {
					movimentaAleatoriamente();	// faz um movimento aleatorio para qualquer dire��o valida
				}
			}
		} else {
			janela.tela.ghostEncruzilhada[numGhost] = true;			// ghost esta em uma encruzilhada
		}
	}
	
	private void movimentaAleatoriamente() {
		int direSorte = (int) (Math.random()*4);	// sorteia uma dire��o
		
		while (!Util.isMovimentoValidoGhost(mapa, ghostPosiX, ghostPosiY, direSorte)) {
			// enquanto nao for uma dire��o valida, sorteia novamente
			direSorte = (int) (Math.random()*4);
		}
		// define qual sera o proximo movimento atrav�s da dire��o
		ghostDire  = direSorte;
		janela.tela.ghostDire[numGhost] = direSorte;
	}
	
	private boolean deveReceberInstrucao() {
		int numSort = (int) (Math.random()*10);
		
		if (numSort > Util.RAZAO_ACEITAR_MOVIMENTO) {
			// se o numero sorteador for maior que a razao, ele deve aguardar o arbitro definir uma nova dire��o
			return true;
		}
		
		return false;
	}

	private int qtdCaminhosValidos() {
		int xE = Util.retornaMeioBlocoX(ghostPosiX, 0);
		int xD = Util.retornaMeioBlocoX(ghostPosiX, Util.TAMANHO_GHOST*2);
		int yC = Util.retornaMeioBlocoY(ghostPosiY, 0);
		int yB = Util.retornaMeioBlocoY(ghostPosiY, Util.TAMANHO_GHOST*2);
		// verifica as 4 posi��es do ghost est�o na mesma posi��o
		if(xE == xD && yC == yB) {
			int qtd = 0;
			int blocoX = Util.retornaMeioBlocoX(ghostPosiX, Util.TAMANHO_GHOST);
			int blocoY = Util.retornaMeioBlocoY(ghostPosiY, Util.TAMANHO_GHOST);
			
			if (Util.isBlocoValido(mapa, blocoX, blocoY-1)) {	// se bloco de cima � valido
				qtd++;
			}
			if (Util.isBlocoValido(mapa, blocoX, blocoY+1)) {	// se bloco de baixo � valido
				qtd++;
			}
			if (Util.isBlocoValido(mapa, blocoX-1, blocoY)) {	// se bloco da esquerda � valido
				qtd++;
			}
			if (Util.isBlocoValido(mapa, blocoX+1, blocoY)) {	// se bloco da direita � valido
				qtd++;
			}
			
			return qtd;
		}
		
		return 0;
	}

	/*private boolean isMovimentoValido(int dire) {
		int direX = Util.returnDireX(dire); 
		int direY = Util.returnDireY(dire);
		int xE, xD, 
			yC, yB;
		
		xE = Util.retornaMeioBlocoX(ghostPosiX + direX, 0);
		xD = Util.retornaMeioBlocoX(ghostPosiX + direX, Util.TAMANHO_GHOST*2);
		yC = Util.retornaMeioBlocoY(ghostPosiY + direY, 0);
		yB = Util.retornaMeioBlocoY(ghostPosiY + direY, Util.TAMANHO_GHOST*2);
		// sera verificado todos os 4 pontos do pacman para verifica se � um movimento valido
		if(Util.isBlocoValido(mapa, xE,yC) && Util.isBlocoValido(mapa, xE, yB) && 
			Util.isBlocoValido(mapa, xD, yC) && Util.isBlocoValido(mapa, xD, yB)) {
			
			return true;
		}
		
		return false;
	}*/

	private void oficializaMovimento() {
		ghostPosiX = ghostPosiX + ghostDireX;	// atualiza a posi��o X do ghost
		ghostPosiY = ghostPosiY + ghostDireY;	// atualiza a posi��o Y do ghost
		// atualiza os valores do ghost na tela
		janela.tela.ghostPosiX[numGhost] = ghostPosiX;
		janela.tela.ghostPosiY[numGhost] = ghostPosiY;
	}

	private void atualizaDirecao() {
		// atualiza os valores do ghost da tela para verificar por colisoes
		ghostDire = janela.tela.ghostDire[numGhost];
		ghostPosiX = janela.tela.ghostPosiX[numGhost];
		ghostPosiY = janela.tela.ghostPosiY[numGhost];
	}
}
