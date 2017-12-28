
public class Pacman extends Thread {
	
	private Janela janela;
	private ETipoBloco[][] mapa;
	
	private int pacPosiX;	// posição x do pacman na tela
	private int pacPosiY;	// posição y do pacman na tela
	
	private int pacDireX;	// movimento x do pacman na tela
	private int pacDireY;	// movimento y do pacman na tela
	private int pacDire;	// direção do pacman
	
	public Pacman(Janela janela, ETipoBloco[][] mapa, int blocoXInicial, 
			int blocoYInicial, int pacDireXInicial, int pacDireYInicial, int direInicial) {
		// carrega valores iniciais
		this.janela   = janela;
		// multiplica a posição na matriz(bloco) pelo tamanho do bloco para encontrar a posição relativa na tela
		this.pacPosiX = blocoXInicial*Util.TAMANHO_BLOCO;	
		this.pacPosiY = blocoYInicial*Util.TAMANHO_BLOCO;
		this.pacDireX = pacDireXInicial;
		this.pacDireY = pacDireYInicial;
		this.pacDire  = direInicial;
		this.mapa	  = mapa;
		
		this.start();
	}
	
	@Override
	public void run() {
		movePac();
		while (!janela.tela.gameOver && !janela.tela.ganhou) {	// enquanto nao der game over ou ganhar 
			while (janela.tela.jogando) {						// enquanto estiver jogando
				if (!janela.tela.pause) {						// se nao foi pausado
					verificaTeletransporte();					// confirma se a posição do pacman nao foi alterada em teletransporte
					movePac();									// move pacman
				}
				try {											// aguarda um pouco para o pacman se mover de tempos em tempos
					sleep(Util.fps);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {												// aguarda um pouco para jogar
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void verificaTeletransporte() {
		// esta função é utilizada para atualizar a variavel de posição do pac-man caso ocorra um teletransporte
		pacPosiX = janela.tela.pacPosiX;
		pacPosiY = janela.tela.pacPosiY;
	}

	private void movePac() {
		// duas variaveis definindo destino
		int direX = pacDireX;
		int direY = pacDireY;
		int dire  = pacDire;
		// verifica qual movimento deve ser feito, e se é um movimento valido
		if (janela.controlaTecla[Teclado.VK_LEFT]) {
			direX = -Util.TAMANHO_MOVIMENTO;
			direY = 0;
			if (isMovimentoValido(direX, direY)) {
				dire = Teclado.VK_LEFT;
			}
		} else {
			if (janela.controlaTecla[Teclado.VK_RIGHT]) {
				direX = Util.TAMANHO_MOVIMENTO;
				direY = 0;
				if (isMovimentoValido(direX, direY)) {
					dire = Teclado.VK_RIGHT;
				}
			} else {
				if (janela.controlaTecla[Teclado.VK_UP]) {
					direX = 0;
					direY = -Util.TAMANHO_MOVIMENTO;
					if (isMovimentoValido(direX, direY)) {
						dire = Teclado.VK_UP;
					}
				} else {
					if (janela.controlaTecla[Teclado.VK_DOWN]) {
						direX = 0;
						direY = +Util.TAMANHO_MOVIMENTO;
						if (isMovimentoValido(direX, direY)) {
							dire = Teclado.VK_DOWN;
						}
					}
				}
			}
		}
		// se a nova direção for diferente da atual, oficializa movimento
		if (dire != pacDire) {
			oficializaMovimento(direX, direY, dire);
		} else { // se nao verifica se é possivel continuar se movendo
			if (isMovimentoValido(pacDireX, pacDireY)) {
				oficializaMovimento(pacDireX, pacDireY, pacDire);
			}
		}

	}
	
	private void oficializaMovimento(int direX, int direY, int direcao) {
		pacDireX = direX;		// atualiza direção X do pac
		pacDireY = direY;		// atualiza direção Y do pac
		pacDire  = direcao;		// atualiza direção do pac
		pacPosiX = pacPosiX + pacDireX;	// atualiza a posição X do pac
		pacPosiY = pacPosiY + pacDireY;	// atualiza a posição Y do pac
		
		// atualiza os valores do pac na tela para verifica por teletransportes e colisoes
		janela.tela.pacDire  = pacDire;
		janela.tela.pacPosiX = pacPosiX;
		janela.tela.pacPosiY = pacPosiY;
	}
	
	private boolean isMovimentoValido(int direX, int direY) {
		int xE, xD/*j*/, 
			yC, yB/*i*/;
		
		xE = Util.retornaMeioBlocoX(pacPosiX + direX, 0);						// retorna o bloco onde esta o x esquerdo
		// para pegar o lado esquerdo como meio do pac, é incrementado o tamanho do pac vezes 2, que serão dividido por 2 na função
		xD = Util.retornaMeioBlocoX(pacPosiX + direX, Util.TAMANHO_PACMAN*2);	// retorna o bloco onde esta o x direito
		yC = Util.retornaMeioBlocoY(pacPosiY + direY, 0);						// retorna o bloco onde esta o y cima
		// para pegar o lado de baixo como meio do pac, é incrementado o tamanho do pac vezes 2, que serão dividido por 2 na função
		yB = Util.retornaMeioBlocoY(pacPosiY + direY, Util.TAMANHO_PACMAN*2);	// retorna o bloco onde esta o y baixo
		// sera verificado todos os 4 pontos do pacman para verifica se é um movimento valido
		if (isBlocoValido(mapa, xE, yC) && isBlocoValido(mapa, xE, yB) && isBlocoValido(mapa, xD, yC) && isBlocoValido(mapa, xD, yB)) {
			return true;
		}
		return false;
	}
	
	public boolean isBlocoValido(ETipoBloco[][] mapa, int blocoX, int blocoY) {
		// se a posição do bloco conter vazio, ponto ou pilula significa que é um bloco valido para movimentação
		if (mapa[blocoY][blocoX] == ETipoBloco.VAZIO || mapa[blocoY][blocoX] == ETipoBloco.PONTO || mapa[blocoY][blocoX] == ETipoBloco.PILULA ||
				mapa[blocoY][blocoX] == ETipoBloco.TELETRANSPORTE_DIRETO || mapa[blocoY][blocoX] == ETipoBloco.TELETRANSPORTE_ESQUERDO) {
			
			return true;
		} else {		
			
			return false;
		}
	}
}
