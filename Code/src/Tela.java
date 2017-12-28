import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Tela extends JPanel {

	private static final long serialVersionUID = 1L;
	private Janela janela;		// janela onde é desenhado o jogo
	private ETipoBloco[][] mapa;// mapa do jogo
	// propriedades que armazenam as imagens do jogo
	private Image pacman1;
	private Image ghost1, ghost2, ghost3, ghost4;
	private Image pacman2up,    pacman3up,    pacman4up;
	private Image pacman2down,  pacman3down,  pacman4down;
	private Image pacman2left,  pacman3left,  pacman4left;
	private Image pacman2right, pacman3right, pacman4right;
	// propriedades de posições e direção do pac-man
	public int pacPosiX;	// armazena a posição X na tela
	public int pacPosiY;	// armazena a posição Y na tela
	public int pacDire;		// armazena em qual direção ele esta seguindo
	public int pacState;	// variavel utilizada para trocar as imagens do pac e simular um come come
	// propriedades de posições e direção dos ghost, como são 4 ghost sera utilizado um vetor onde cada thread ira acessar apenas seu indice
	public boolean[] ghostEncruzilhada = {false, false, false, false};
	public int[] ghostDire  = {Teclado.VK_DOWN, Teclado.VK_LEFT, Teclado.VK_RIGHT, Teclado.VK_DOWN};
	public int[] ghostPosiX = {Util.GHOST1_POSI_X_INICIAL*Util.TAMANHO_BLOCO, Util.GHOST2_POSI_X_INICIAL*Util.TAMANHO_BLOCO, 
								Util.GHOST3_POSI_X_INICIAL*Util.TAMANHO_BLOCO, Util.GHOST4_POSI_X_INICIAL*Util.TAMANHO_BLOCO};
	public int[] ghostPosiY = {Util.GHOST1_POSI_Y_INICIAL*Util.TAMANHO_BLOCO, Util.GHOST2_POSI_Y_INICIAL*Util.TAMANHO_BLOCO, 
								Util.GHOST3_POSI_Y_INICIAL*Util.TAMANHO_BLOCO, Util.GHOST4_POSI_Y_INICIAL*Util.TAMANHO_BLOCO};
	// propriedades de controle do jogo
	public boolean pause   = false;
	public boolean jogando = false;
	public boolean gameOver = false; 
	public boolean ganhou = false; 
	public int score;
	public int vidas;
	
	public Tela(Janela janela) {
		setBackground(Color.BLACK);
		this.janela = janela;
		this.loadImages();			// carrega imagens do pac-man e dos fantasmas
	}

	@Override
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);
		carregaMapa(g);				// desenha o mapa na tela
        modificaMapa(g);			// caso o pac man coma uma pilula ou ponto o mapa deve ser modificado
        desenhaImagens(g);			// desenha os componentes novamente
        desenhaMenu(g);				// desenha menu lateral
        redirecionaGhosts();		// se ghost chegou a uma encruzilhada é enviada uma instrução para continuar a se movimentar
        verificaColisaoPacGhost(g);	// verifica se o pac colidiu com algum fantasma
        
        if (!jogando) {				// se não esta jogando, mostra mensagem com instrução
        	desenhaMensagem(g,"Pressione Enter para jogar"); 
        }
        if (gameOver) {				// se der Game Over, mostra mensagem com instrução
        	desenhaMensagem(g, "Você perdeu :( Pressione Enter para jogar");   
        }
        if (ganhou) {				// se ganhou, mostra mensagem com instrução
        	desenhaMensagem(g, "Você ganhou :) Pressione Enter para jogar");   
        }
        if (pause) {				// se pause, mostra mensagem com instrução
        	desenhaMensagem(g, "Pressione Espaço para voltar a jogar");
        }
	}
	
	/*private void redirecionaGhosts() {
	 	// movimento aleatorio
		int direSort;
		
		for (int i = 0; i < Util.NUM_GHOST; i++) {
			if (ghostEncruzilhada[i]) {
				direSort = (int) (Math.random()*4);
				
				if (direSort == Teclado.VK_DOWN && ghostDire[i] != Teclado.VK_UP && direSort != ghostDire[i]) {
					if (isMovimentoValido(ghostPosiX[i], ghostPosiY[i], direSort)) {
						ghostDire[i] = Teclado.VK_DOWN;
						ghostEncruzilhada[i] = false;
					}
				}
				if (direSort == Teclado.VK_UP && ghostDire[i] != Teclado.VK_DOWN && direSort != ghostDire[i]) {
					if (isMovimentoValido(ghostPosiX[i], ghostPosiY[i], direSort)) {
						ghostDire[i] = Teclado.VK_UP;
						ghostEncruzilhada[i] = false;
					}
				}
				if (direSort == Teclado.VK_LEFT && ghostDire[i] != Teclado.VK_RIGHT && direSort != ghostDire[i]) {
					if (isMovimentoValido(ghostPosiX[i], ghostPosiY[i], direSort)) {
						ghostDire[i] = Teclado.VK_LEFT;
						ghostEncruzilhada[i] = false;
					}
				}
				if (direSort == Teclado.VK_RIGHT && ghostDire[i] != Teclado.VK_LEFT && direSort != ghostDire[i]) {
					if (isMovimentoValido(ghostPosiX[i], ghostPosiY[i], direSort)) {
						ghostDire[i] = Teclado.VK_RIGHT;
						ghostEncruzilhada[i] = false;
					}
				}
			}
		}
	}*/

	private void redirecionaGhosts() {
		for (int i = 0; i < Util.NUM_GHOST; i++) {
			if (ghostEncruzilhada[i]) {						// verifica se algum dos fantasmas estao travados em uma encruzilhada
				ghostDire[i] 		 = novaDirecaoGhost(i); // arbitro define uma direção para seguir tentando aproximar do pacman
				ghostEncruzilhada[i] = false;				// informa para o ghost que ele nao esta mais travado e pode seguir
			}
		}
	}
	
	private int novaDirecaoGhost(int numGhost) {
		// função de movimentação inteligente do ghost em casos de encruzilhada, tentando direcionar o ghost para o mais proximo possivel do pacman 
		int proximidadeX, proximidadeY, dire;
		
		proximidadeX = pacPosiX - ghostPosiX[numGhost]; 	// proximidade na horizontal entre o pac-man e o ghost
		proximidadeY = pacPosiY - ghostPosiY[numGhost];		// proximidade na vertical entre o pac-man e o ghost
		
		if (Math.abs(proximidadeX) < Math.abs(proximidadeY)) {	// se true significa que x esta mais perto

			// verifica movimento na horizontal mais proxima do pac-man
			if (proximidadeX < 0) { 				// se a proximidadeX for menor que 0, a direção será para esquerda
				dire = Teclado.VK_LEFT;
			} else {
				if (proximidadeX > 0) {				// se a proximidadeX for maior que 0, a direção será para direita
					dire = Teclado.VK_RIGHT;
				} else {							// se a proximidadeX for igual a 0, entao estao na mesma coluna e deve olhar a proximidadeY
					if (proximidadeY < 0) {			// se a proximidadeY for menor que 0, a direção será para cima
						dire = Teclado.VK_UP;		
					} else {						// se a proximidadeY for maior que 0, a direção será para baixo
						dire = Teclado.VK_DOWN;
					}
				}
			}
			if (Util.isMovimentoValidoGhost(mapa, ghostPosiX[numGhost], ghostPosiY[numGhost], dire)) {
				return dire;
			}
			// verifica movimento na vertical mais proxima do pac-man
			if (proximidadeY < 0) {					// se a proximidadeY for menor que 0, a direção será para cima
				dire = Teclado.VK_UP;
			} else {
				if (proximidadeY > 0) {				// se a proximidadeY for maior que 0, a direção será para baixo
					dire = Teclado.VK_DOWN;
				} else {							// se a proximidadeY for igual a 0, entao estao na mesma linha e deve olhar a proximidadeX
					if (proximidadeX < 0) {			// se a proximidadeX for menor que 0, a direção será para esquerda
						dire = Teclado.VK_LEFT;
					} else {						// se a proximidadeX for maior que 0, a direção será para direita
						dire = Teclado.VK_RIGHT;
					}
				}
			}
			if (Util.isMovimentoValidoGhost(mapa, ghostPosiX[numGhost], ghostPosiY[numGhost], dire)) {
				return dire;
			}

			// verifica movimento na horizontal mais distante do pac-man
			if (proximidadeX < 0) { 				// se a proximidadeX for menor que 0, a direção será para direita
				dire = Teclado.VK_RIGHT;
			} else {
				if (proximidadeX > 0) {				// se a proximidadeX for maior que 0, a direção será para esquerda
					dire = Teclado.VK_LEFT;
				} else {							// se a proximidadeX for igual a 0, entao estao na mesma coluna e deve olhar a proximidadeY
					if (proximidadeY < 0) {			// se a proximidadeY for menor que 0, a direção será para baixo
						dire = Teclado.VK_DOWN;		
					} else {						// se a proximidadeY for maior que 0, a direção será para cima
						dire = Teclado.VK_UP;
					}
				}
			}
			if (Util.isMovimentoValidoGhost(mapa, ghostPosiX[numGhost], ghostPosiY[numGhost], dire)) {
				return dire;
			}
			// verifica movimento na vertical mais distante do pac-man
			if (proximidadeY < 0) {					// se a proximidadeY for menor que 0, a direção será para baixo
				dire = Teclado.VK_DOWN;
			} else {
				if (proximidadeY > 0) {				// se a proximidadeY for maior que 0, a direção será para cima
					dire = Teclado.VK_UP;
				} else {							// se a proximidadeY for igual a 0, entao estao na mesma linha e deve olhar a proximidadeX
					if (proximidadeX < 0) {			// se a proximidadeX for menor que 0, a direção será para direita
						dire = Teclado.VK_RIGHT;
					} else {						// se a proximidadeX for maior que 0, a direção será para esquerda
						dire = Teclado.VK_LEFT;
					}
				}
			}
			if (Util.isMovimentoValidoGhost(mapa, ghostPosiX[numGhost], ghostPosiY[numGhost], dire)) {
				return dire;
			}
		} else {												// se false significa que y esta mais perto
			// verifica movimento na vertical mais proxima do pac-man
			if (proximidadeY < 0) {					// se a proximidadeY for menor que 0, a direção será para cima
				dire = Teclado.VK_UP;
			} else {
				if (proximidadeY > 0) {				// se a proximidadeY for maior que 0, a direção será para baixo
					dire = Teclado.VK_DOWN;
				} else {							// se a proximidadeY for igual a 0, entao estao na mesma linha e deve olhar a proximidadeX
					if (proximidadeX < 0) {			// se a proximidadeX for menor que 0, a direção será para esquerda
						dire = Teclado.VK_LEFT;
					} else {						// se a proximidadeX for maior que 0, a direção será para direita
						dire = Teclado.VK_RIGHT;
					}
				}
			}
			if (Util.isMovimentoValidoGhost(mapa, ghostPosiX[numGhost], ghostPosiY[numGhost], dire)) {
				return dire;
			}
			// verifica movimento na horizontal mais proxima do pac-man
			if (proximidadeX < 0) { 				// se a proximidadeX for menor que 0, a direção será para esquerda
				dire = Teclado.VK_LEFT;
			} else {
				if (proximidadeX > 0) {				// se a proximidadeX for maior que 0, a direção será para direita
					dire = Teclado.VK_RIGHT;
				} else {							// se a proximidadeX for igual a 0, entao estao na mesma coluna e deve olhar a proximidadeY
					if (proximidadeY < 0) {			// se a proximidadeY for menor que 0, a direção será para cima
						dire = Teclado.VK_UP;		
					} else {						// se a proximidadeY for maior que 0, a direção será para baixo
						dire = Teclado.VK_DOWN;
					}
				}
			}
			if (Util.isMovimentoValidoGhost(mapa, ghostPosiX[numGhost], ghostPosiY[numGhost], dire)) {
				return dire;
			}
			// verifica movimento na vertical mais distante do pac-man
			if (proximidadeY < 0) {					// se a proximidadeY for menor que 0, a direção será para baixo
				dire = Teclado.VK_DOWN;
			} else {
				if (proximidadeY > 0) {				// se a proximidadeY for maior que 0, a direção será para cima
					dire = Teclado.VK_UP;
				} else {							// se a proximidadeY for igual a 0, entao estao na mesma linha e deve olhar a proximidadeX
					if (proximidadeX < 0) {			// se a proximidadeX for menor que 0, a direção será para direita
						dire = Teclado.VK_RIGHT;
					} else {						// se a proximidadeX for maior que 0, a direção será para left
						dire = Teclado.VK_LEFT;
					}
				}
			}
			if (Util.isMovimentoValidoGhost(mapa, ghostPosiX[numGhost], ghostPosiY[numGhost], dire)) {
				return dire;
			}
			// verifica movimento na horizontal mais distante do pac-man
			if (proximidadeX < 0) { 				// se a proximidadeX for menor que 0, a direção será para direita
				dire = Teclado.VK_RIGHT;
			} else {
				if (proximidadeX > 0) {				// se a proximidadeX for maior que 0, a direção será para esquerda
					dire = Teclado.VK_LEFT;
				} else {							// se a proximidadeX for igual a 0, entao estao na mesma coluna e deve olhar a proximidadeY
					if (proximidadeY < 0) {			// se a proximidadeY for menor que 0, a direção será para baixo
						dire = Teclado.VK_DOWN;		
					} else {						// se a proximidadeY for maior que 0, a direção será para cima
						dire = Teclado.VK_UP;
					}
				}
			}
			if (Util.isMovimentoValidoGhost(mapa, ghostPosiX[numGhost], ghostPosiY[numGhost], dire)) {
				return dire;
			}
		}
		
		return 0;
	}

	/*private boolean isMovimentoValido(int posiX, int posiY, int dire) {
		int direX = Util.returnDireX(dire); 	// pega o valor do movimento X a partir da direçao
		int direY = Util.returnDireY(dire);		// pega o valor do movimento Y a partir da direçao
		int xE, xD, 
			yC, yB;
		
		xE = Util.retornaMeioBlocoX(posiX + direX, 0);								// retorna o bloco onde esta o x esquerdo
		// para pegar o lado esquerdo como meio do ghost, é incrementado o tamanho do ghost vezes 2, que serão dividido por 2 na função
		xD = Util.retornaMeioBlocoX(posiX + direX, Util.TAMANHO_GHOST*2);			// retorna o bloco onde esta o x direito
		yC = Util.retornaMeioBlocoY(posiY + direY, 0);								// retorna o bloco onde esta o y cima
		// para pegar o lado de baixo como meio do ghost, é incrementado o tamanho do ghost vezes 2, que serão dividido por 2 na função
		yB = Util.retornaMeioBlocoY(posiY + direY, Util.TAMANHO_GHOST*2);			// retorna o bloco onde esta o y baixo

		// sera verificado todos os 4 pontos do ghost para verifica se é um movimento valido
		if(Util.isBlocoValido(mapa, xE,yC) && Util.isBlocoValido(mapa, xE, yB) && 
			Util.isBlocoValido(mapa, xD, yC) && Util.isBlocoValido(mapa, xD, yB)) {
			
			return true;
		}
		return false;
	}*/

	private void desenhaMenu(Graphics g) {
		desenhaLinhaScore(g);	// desenha a linha de divisão do jogo com score
        desenhaScore(g);		// desenha score
        desenhaVidas(g);		// desenha vidas
        desenhaInstrucoes(g);	// desenha legenda das intruções do jogo
	}

	private void desenhaInstrucoes(Graphics g) {
		Font fonte1 = new Font("Tahoma", Font.BOLD, 14);
		Font fonte2 = new Font("Tahoma", Font.BOLD, 16);
		
		g.setFont(fonte2);
		g.drawString("Controles ", Util.POSI_CONTROLES_X+Util.TAMANHO_BLOCO, Util.POSI_CONTROLES_Y);
		g.setFont(fonte1);
		g.drawString("Start: Enter", Util.POSI_CONTROLES_X, Util.POSI_CONTROLES_Y+2*Util.TAMANHO_BLOCO);
		g.drawString("Pause: Espaço", Util.POSI_CONTROLES_X, Util.POSI_CONTROLES_Y+4*Util.TAMANHO_BLOCO);
	}

	private void desenhaMensagem(Graphics g, String mensagem) {
        g.setColor(new Color(0, 32, 48));
        g.fillRect(50, Util.TAMANHO_ALTURA_JOGO / 2 - 30, Util.TAMANHO_LARGURA_JOGO - 100, 50);
        g.setColor(Color.WHITE);
        g.drawRect(50, Util.TAMANHO_ALTURA_JOGO / 2 - 30, Util.TAMANHO_LARGURA_JOGO - 100, 50);

        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g.setColor(Color.WHITE);
        g.setFont(small);
        g.drawString(mensagem, (Util.TAMANHO_ALTURA_JOGO - metr.stringWidth(mensagem)-50) / 2, Util.TAMANHO_LARGURA_JOGO / 2 + 40);
	}
	
	private void verificaColisaoPacGhost(Graphics g) {
		int xPac, yPac;
		int xGhost1, yGhost1;
		int xGhost2, yGhost2;
		int xGhost3, yGhost3;
		int xGhost4, yGhost4;
		// pega todas as posições onde os pacman e os ghost estão, será comparado o meio do bloco onde estao posicionados
		xPac = Util.retornaMeioBlocoX(pacPosiX, Util.TAMANHO_PACMAN);
		yPac = Util.retornaMeioBlocoY(pacPosiY, Util.TAMANHO_PACMAN);
		
		xGhost1 = Util.retornaMeioBlocoX(ghostPosiX[0], Util.TAMANHO_PACMAN);
		yGhost1 = Util.retornaMeioBlocoY(ghostPosiY[0], Util.TAMANHO_PACMAN);
		
		xGhost2 = Util.retornaMeioBlocoX(ghostPosiX[1], Util.TAMANHO_PACMAN);
		yGhost2 = Util.retornaMeioBlocoY(ghostPosiY[1], Util.TAMANHO_PACMAN);
		
		xGhost3 = Util.retornaMeioBlocoX(ghostPosiX[2], Util.TAMANHO_PACMAN);
		yGhost3 = Util.retornaMeioBlocoY(ghostPosiY[2], Util.TAMANHO_PACMAN);
		
		xGhost4 = Util.retornaMeioBlocoX(ghostPosiX[3], Util.TAMANHO_PACMAN);
		yGhost4 = Util.retornaMeioBlocoY(ghostPosiY[3], Util.TAMANHO_PACMAN);
		// se algum fantasma colidiu com o pacman, reposiciona todas as posições para inicial
		if ((xPac == xGhost1 && yPac == yGhost1) || (xPac == xGhost2 && yPac == yGhost2) || 
				(xPac == xGhost3 && yPac == yGhost3) || (xPac == xGhost4 && yPac == yGhost4)) {
			// reposiciona pacman
			pacPosiX = Util.PAC_POSI_X_INICIAL*Util.TAMANHO_BLOCO;
			pacPosiY = Util.PAC_POSI_Y_INICIAL*Util.TAMANHO_BLOCO;
			// reposiciona todos os ghost
			ghostDire[0] = Teclado.VK_DOWN;
			ghostDire[1] = Teclado.VK_LEFT;
			ghostDire[2] = Teclado.VK_RIGHT;
			ghostDire[3] = Teclado.VK_DOWN;
			
			ghostPosiX[0] = Util.GHOST1_POSI_X_INICIAL*Util.TAMANHO_BLOCO;
			ghostPosiX[1] = Util.GHOST2_POSI_X_INICIAL*Util.TAMANHO_BLOCO;
			ghostPosiX[2] = Util.GHOST3_POSI_X_INICIAL*Util.TAMANHO_BLOCO;
			ghostPosiX[3] = Util.GHOST4_POSI_X_INICIAL*Util.TAMANHO_BLOCO;

			ghostPosiY[0] = Util.GHOST1_POSI_Y_INICIAL*Util.TAMANHO_BLOCO;
			ghostPosiY[1] = Util.GHOST2_POSI_Y_INICIAL*Util.TAMANHO_BLOCO;
			ghostPosiY[2] = Util.GHOST3_POSI_Y_INICIAL*Util.TAMANHO_BLOCO;
			ghostPosiY[3] = Util.GHOST4_POSI_Y_INICIAL*Util.TAMANHO_BLOCO;
			vidas--;
			
			if (vidas == 0) {	// se quantidade de vidas for 0 termina o jogo
				jogando = false;
				gameOver = true;
			}
		}
	}

	private void desenhaVidas(Graphics g) {
		if (vidas >= 1) {
			g.drawImage(pacman2left, Util.POSI_VIDAS_X, Util.POSI_VIDAS_Y, this);
		}
		if (vidas >= 2) {
			g.drawImage(pacman2left, Util.POSI_VIDAS_X+2*Util.TAMANHO_BLOCO, Util.POSI_VIDAS_Y, this);
		}
		if (vidas == 3) {
			g.drawImage(pacman2left, Util.POSI_VIDAS_X+4*Util.TAMANHO_BLOCO, Util.POSI_VIDAS_Y, this);
		}
	}
	
	private void desenhaScore(Graphics g) {
		Font fonte = new Font("Tahoma", Font.BOLD, 18);
		g.setFont(fonte);
		g.drawString("Score:  " + score, Util.POSI_SCORE_X, Util.POSI_SCORE_Y);
	}
	
	private void desenhaLinhaScore(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect((Util.TAMANHO_MAPA_X+1)*Util.TAMANHO_BLOCO, 0, 5, Util.TAMANHO_ALTURA_JOGO);						// borda esquerdo
		g.fillRect(Util.JANELA_LARGURA - Util.TAMANHO_BLOCO/2, 0, 5, Util.TAMANHO_ALTURA_JOGO);						// borda direita
		g.fillRect((Util.TAMANHO_MAPA_X+1)*Util.TAMANHO_BLOCO, 0, Util.JANELA_LARGURA, 5);							// borda superior
		g.fillRect((Util.TAMANHO_MAPA_X+1)*Util.TAMANHO_BLOCO, Util.JANELA_ALTURA - 33, Util.JANELA_LARGURA, 5);	// borda inferior
	}
	
	private void modificaMapa(Graphics g) {
		// verifica se a posição que se encontra tem algum ponto ou pilula
		int i = Util.retornaMeioBlocoX(pacPosiX, Util.TAMANHO_PACMAN);
		int j = Util.retornaMeioBlocoY(pacPosiY, Util.TAMANHO_PACMAN);
		
		if (mapa[j][i] == ETipoBloco.PONTO) {					// se a posição for um ponto conta 1 ponto
			mapa[j][i] = ETipoBloco.VAZIO;
			score++;
		}
		if (mapa[j][i] == ETipoBloco.PILULA) {					// se a posição for uma pilula conta mais 10 pontos
			mapa[j][i] = ETipoBloco.VAZIO;
			score+=10;
		}
		if (mapa[j][i] == ETipoBloco.TELETRANSPORTE_DIRETO) {	// se esta no teletranporte direito passa para o lado esquerdo
			pacPosiX = 1*Util.TAMANHO_BLOCO;
		}
		if (mapa[j][i] == ETipoBloco.TELETRANSPORTE_ESQUERDO) { // se esta no teletranporte esquerdo passa para o lado direito
			pacPosiX = (Util.TAMANHO_MAPA_X-1)*Util.TAMANHO_BLOCO;
		}
		if (score == Util.MAX_POINT) {							// verifica se a quantidade de pontos foi atingida para finalizar o jogo
			ganhou = true;
			jogando = false;
		}
	}

	private void desenhaImagens(Graphics g) {
		// Pac-Man, a partir da direção do pac man desenha uma imagem diferente
		if (pacDire == Teclado.VK_LEFT) {
			desenhaPacMan(g, pacman2left, pacman3left, pacman4left);
		}
		if (pacDire == Teclado.VK_RIGHT) {
			desenhaPacMan(g, pacman2right, pacman3right, pacman4right);
		}
		if (pacDire == Teclado.VK_UP) {
			desenhaPacMan(g, pacman2up, pacman3up, pacman4up);
		}
		if (pacDire == Teclado.VK_DOWN) {
			desenhaPacMan(g, pacman2down, pacman3down, pacman4down);
		}
		// Ghost 1
		g.drawImage(ghost1, ghostPosiX[0], ghostPosiY[0], this);
		// Ghost 2
		g.drawImage(ghost2, ghostPosiX[1], ghostPosiY[1], this);
		// Ghost 3
		g.drawImage(ghost3, ghostPosiX[2], ghostPosiY[2], this);
		// Ghost 4
		g.drawImage(ghost4, ghostPosiX[3], ghostPosiY[3], this);
	}
	
	private void desenhaPacMan(Graphics g, Image pac2, Image pac3, Image pac4) {
		switch (pacState) {	// é utilizado um controle de 4 estados para que o pacman fique comendo constantemente
		case 0:
			g.drawImage(pacman1, pacPosiX, pacPosiY, this);
			pacState++;
			break;
		case 1:
			g.drawImage(pac2, pacPosiX, pacPosiY, this);
			pacState++;
			break;
		case 2:
			g.drawImage(pac3, pacPosiX, pacPosiY, this);
			pacState++;
			break;
		case 3:
			g.drawImage(pac4, pacPosiX, pacPosiY, this);
			pacState = 0;
			break;
		}
	}

	private void carregaMapa(Graphics g) {
		// varre toda a matriz com o mapa, e desenha em cada posição o seu determinado campo
		// cada posição na matriz corresponde a um bloco 20x20
		for (int i = 0; i < mapa.length; i++) {
			for (int j = 0; j < mapa[0].length; j++) {
				if(mapa[i][j] == ETipoBloco.VAZIO) {			// vazio
					g.setColor(Color.BLACK);
					g.fillRect(j*Util.TAMANHO_BLOCO, i*Util.TAMANHO_BLOCO, Util.TAMANHO_BLOCO, Util.TAMANHO_BLOCO);
				}
				if(mapa[i][j] == ETipoBloco.PONTO) {			// ponto
					g.setColor(Color.YELLOW);
					g.fillOval(j*Util.TAMANHO_BLOCO+Util.TAMANHO_BLOCO/2, i*Util.TAMANHO_BLOCO+Util.TAMANHO_BLOCO/2, 5, 5);
				}
				if(mapa[i][j] == ETipoBloco.BARRA_VERTICAL) {	// |
					g.setColor(Color.BLUE);
					g.fillRect(j*Util.TAMANHO_BLOCO+7, i*Util.TAMANHO_BLOCO, 6, Util.TAMANHO_BLOCO);
				}
				if(mapa[i][j] == ETipoBloco.BARRA_HORIZONTAL) {	// -
					g.setColor(Color.BLUE);
					g.fillRect(j*Util.TAMANHO_BLOCO, i*Util.TAMANHO_BLOCO+7, Util.TAMANHO_BLOCO, 6);
				}
				if(mapa[i][j] == ETipoBloco.CURVA_ES) {			// ES
					g.setColor(Color.BLUE);
					drawCorner(g, j*Util.TAMANHO_BLOCO+Util.TAMANHO_BLOCO/2, i*Util.TAMANHO_BLOCO+Util.TAMANHO_BLOCO/2, j*Util.TAMANHO_BLOCO, i*Util.TAMANHO_BLOCO);
				}
				if(mapa[i][j] == ETipoBloco.CURVA_DS) {			// DS
					g.setColor(Color.BLUE);
					drawCorner(g, j*Util.TAMANHO_BLOCO-Util.TAMANHO_BLOCO/2, i*Util.TAMANHO_BLOCO+Util.TAMANHO_BLOCO/2, j*Util.TAMANHO_BLOCO, i*Util.TAMANHO_BLOCO);
				}
				if(mapa[i][j] == ETipoBloco.CURVA_EI) { 		// EI
					g.setColor(Color.BLUE);
					drawCorner(g, j*Util.TAMANHO_BLOCO+Util.TAMANHO_BLOCO/2, i*Util.TAMANHO_BLOCO-Util.TAMANHO_BLOCO/2, j*Util.TAMANHO_BLOCO, i*Util.TAMANHO_BLOCO);
				}
				if(mapa[i][j] == ETipoBloco.CURVA_DI) {			// DI
					g.setColor(Color.BLUE);
					drawCorner(g, j*Util.TAMANHO_BLOCO-Util.TAMANHO_BLOCO/2, i*Util.TAMANHO_BLOCO-Util.TAMANHO_BLOCO/2, j*Util.TAMANHO_BLOCO, i*Util.TAMANHO_BLOCO);
				}
				if(mapa[i][j] == ETipoBloco.PILULA) {			// pilula
					g.setColor(Color.YELLOW);
					g.fillOval(j*Util.TAMANHO_BLOCO+Util.TAMANHO_BLOCO/2-4, i*Util.TAMANHO_BLOCO+Util.TAMANHO_BLOCO/2-4, 10, 10);
				}
			}
		}
	}
	
	private void drawCorner(Graphics g, int xBase, int yBase, int posBlockX, int posBlockY) {
		// função apra desenhar curva
		Graphics2D g2 = (Graphics2D) g;
		Rectangle oldClip = g.getClipBounds();
		g2.setClip(posBlockX, posBlockY, Util.TAMANHO_BLOCO, Util.TAMANHO_BLOCO);
		g2.setColor(Color.BLUE);
		
		Shape oval = new Ellipse2D.Double(xBase, yBase, Util.TAMANHO_BLOCO, Util.TAMANHO_BLOCO);
		
		g2.setStroke(new BasicStroke(6));
		g2.draw(oval);
		g2.setClip(oldClip);
	}
	
	private void loadImages() {
		// as imagens devem estar em uma pasta images junto da pasta do executavel
        ghost1 = new ImageIcon("images/ghost11.jpg").getImage();
        ghost2 = new ImageIcon("images/ghost2.jpg").getImage();
        ghost3 = new ImageIcon("images/ghost3.jpg").getImage();
        ghost4 = new ImageIcon("images/ghost41.jpg").getImage();
        pacman1 = new ImageIcon("images/pacman.gif").getImage();
        pacman2up = new ImageIcon("images/up1.gif").getImage();
        pacman3up = new ImageIcon("images/up2.gif").getImage();
        pacman4up = new ImageIcon("images/up3.gif").getImage();
        pacman2down = new ImageIcon("images/down1.gif").getImage();
        pacman3down = new ImageIcon("images/down2.gif").getImage();
        pacman4down = new ImageIcon("images/down3.gif").getImage();
        pacman2left = new ImageIcon("images/left1.gif").getImage();
        pacman3left = new ImageIcon("images/left2.gif").getImage();
        pacman4left = new ImageIcon("images/left3.gif").getImage();
        pacman2right = new ImageIcon("images/right1.gif").getImage();
        pacman3right = new ImageIcon("images/right2.gif").getImage();
        pacman4right = new ImageIcon("images/right3.gif").getImage();
    }

	public void iniciaJogo() {
		// contém todas as inicializações do jogo
		vidas 		= Util.QTD_VIDAS_INICIAL;
		score 		= Util.INICIAL_POINT;
		gameOver 	= false;
		ganhou 		= false;
		
		try {
			this.mapa = Util.carregaMapa();				// carrega o mapa do arquivo para uma matriz
		} catch (IOException e) {
			e.printStackTrace();
		}
		// inicia pacman, a posição inicial será o bloco inicial vezes o numero de bloco
		pacPosiX = Util.PAC_POSI_X_INICIAL*Util.TAMANHO_BLOCO;
		pacPosiY = Util.PAC_POSI_Y_INICIAL*Util.TAMANHO_BLOCO;
		pacDire	 = Util.PAC_DIRE_INICIAL;
		
		// inicia ghosts
		for (int i = 0; i < ghostEncruzilhada.length; i++) {	// todos os ghost iniciam com encruzilhada false
			ghostEncruzilhada[i] = false;
		}
		// cada ghost inicia com uma direção
		ghostDire[0] = Teclado.VK_DOWN;
		ghostDire[1] = Teclado.VK_LEFT;
		ghostDire[2] = Teclado.VK_RIGHT;
		ghostDire[3] = Teclado.VK_DOWN;
		// cada ghost é posicionado em um local diferente
		ghostPosiX[0] = Util.GHOST1_POSI_X_INICIAL*Util.TAMANHO_BLOCO;
		ghostPosiX[1] = Util.GHOST2_POSI_X_INICIAL*Util.TAMANHO_BLOCO;
		ghostPosiX[2] = Util.GHOST3_POSI_X_INICIAL*Util.TAMANHO_BLOCO;
		ghostPosiX[3] = Util.GHOST4_POSI_X_INICIAL*Util.TAMANHO_BLOCO;

		ghostPosiY[0] = Util.GHOST1_POSI_Y_INICIAL*Util.TAMANHO_BLOCO;
		ghostPosiY[1] = Util.GHOST2_POSI_Y_INICIAL*Util.TAMANHO_BLOCO;
		ghostPosiY[2] = Util.GHOST3_POSI_Y_INICIAL*Util.TAMANHO_BLOCO;
		ghostPosiY[3] = Util.GHOST4_POSI_Y_INICIAL*Util.TAMANHO_BLOCO;
		// inicia thread do pac-man com os valores iniciais
		new Pacman(janela, mapa, Util.PAC_POSI_X_INICIAL, Util.PAC_POSI_Y_INICIAL, 
					Util.PAC_DIRE_X_INICIAL, Util.PAC_DIRE_Y_INICIAL, Util.PAC_DIRE_INICIAL);
		// inicia thread dos ghost com os valores iniciais, e cada um recebe seu indice no vetor de posições e direções 
		new Ghost(janela, mapa, Util.GHOST1_POSI_X_INICIAL, Util.GHOST1_POSI_Y_INICIAL, 
					0, +Util.TAMANHO_MOVIMENTO, Teclado.VK_DOWN, 0);
		new Ghost(janela, mapa, Util.GHOST2_POSI_X_INICIAL, Util.GHOST2_POSI_Y_INICIAL, 
					-Util.TAMANHO_MOVIMENTO, 0, Teclado.VK_LEFT, 1);
		new Ghost(janela, mapa, Util.GHOST3_POSI_X_INICIAL, Util.GHOST3_POSI_Y_INICIAL, 
					+Util.TAMANHO_MOVIMENTO, 0, Teclado.VK_RIGHT, 2);
		new Ghost(janela, mapa, Util.GHOST4_POSI_X_INICIAL, Util.GHOST4_POSI_Y_INICIAL, 
					0, +Util.TAMANHO_MOVIMENTO, Teclado.VK_DOWN, 3);
	}
}
