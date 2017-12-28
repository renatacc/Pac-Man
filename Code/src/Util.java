import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class Util {
	public static final int fps = 50; 				// quantidade de vezes que a tela sera re-pintada por segundo
	
	public static final int TAMANHO_BLOCO = 20;		// tamanho de cada bloco no jogo
	public static final int TAMANHO_PACMAN = 19;
	public static final int TAMANHO_GHOST = 19;
	public static final int TAMANHO_MOVIMENTO = 5; 
	public static final int TAMANHO_MAPA_X = 27;
	public static final int NUM_LINHAS = 31;
	public static final int NUM_COLUNAS = 28;
	public static final int TAMANHO_LARGURA_JOGO = TAMANHO_BLOCO * NUM_COLUNAS + 15;	 // numeros sao as bordas
	public static final int TAMANHO_ALTURA_JOGO = TAMANHO_BLOCO * NUM_LINHAS + 30;
	public static final int TAMANHO_LARGURA_MENU = 150;
	
	public static final int JANELA_LARGURA = TAMANHO_LARGURA_JOGO + TAMANHO_LARGURA_MENU;// numeros sao as bordas
	public static final int JANELA_ALTURA = TAMANHO_ALTURA_JOGO;
	
	public static final int RAZAO_ACEITAR_MOVIMENTO = 1;	// na hora de verificar se é uma encruzilhada utiliza este numero
	public static final int NUM_GHOST = 4;
	
	public static final int QTD_VIDAS_INICIAL = 3;
	public static final int POSI_VIDAS_Y = 29*TAMANHO_BLOCO;
	public static final int POSI_VIDAS_X = 29*TAMANHO_BLOCO;
	public static final int POSI_SCORE_Y = 2*TAMANHO_BLOCO;
	public static final int POSI_SCORE_X = 30*TAMANHO_BLOCO;
	public static final int POSI_CONTROLES_Y = 22*TAMANHO_BLOCO;
	public static final int POSI_CONTROLES_X = 29*TAMANHO_BLOCO;
	// posição inicial do pacman
	public static final int PAC_POSI_X_INICIAL = 13;
	public static final int PAC_POSI_Y_INICIAL = 23;
	public static final int PAC_DIRE_Y_INICIAL = 0;
	public static final int PAC_DIRE_X_INICIAL = -TAMANHO_MOVIMENTO;
	public static final int PAC_DIRE_INICIAL = Teclado.VK_LEFT;
	// posição inicial dos fantasmas
	public static final int GHOST1_POSI_X_INICIAL = 9;
	public static final int GHOST1_POSI_Y_INICIAL = 11;
	
	public static final int GHOST2_POSI_X_INICIAL = 12;
	public static final int GHOST2_POSI_Y_INICIAL = 11;
	
	public static final int GHOST3_POSI_X_INICIAL = 15;
	public static final int GHOST3_POSI_Y_INICIAL = 11;

	public static final int GHOST4_POSI_X_INICIAL = 18;
	public static final int GHOST4_POSI_Y_INICIAL = 11;

	public static final int INICIAL_POINT = 0;
	public static final int MAX_POINT = 333;
	
	public static ETipoBloco[][] carregaMapa() throws IOException {
		String linha;
		int nLinha, nColuna, i;
		Reader reader = new FileReader("mapa.map");	// abre o arquivo para leitura
		//************************************************************************
		BufferedReader lerArq = new BufferedReader(reader);
		
		linha = lerArq.readLine();	
		// pegar numero de linhas e colunas na primeira linha do arquivo
		i = pegaPosicaoVirgula(linha);
		nLinha = (Integer.parseInt(linha.substring(0,i)));
		linha = linha.substring(i+1, linha.length());
		nColuna = (Integer.parseInt(linha));
		// cria o mapa com as dimençoes
		ETipoBloco[][] mapa = new ETipoBloco[nLinha][nColuna];

		i = 0;
		linha = lerArq.readLine();
		
		while (linha != null) {										// enquanto existir linhas a serem lidas
			for (int j = 0; j < nColuna; j++) {
				if (linha.toCharArray()[j] == '0') {
					mapa[i][j] = ETipoBloco.VAZIO;
				}
				if (linha.toCharArray()[j] == '1') {
					mapa[i][j] = ETipoBloco.PONTO;
				}
				if (linha.toCharArray()[j] == '2') {
					mapa[i][j] = ETipoBloco.BARRA_VERTICAL;
				}
				if (linha.toCharArray()[j] == '3') {
					mapa[i][j] = ETipoBloco.BARRA_HORIZONTAL;
				}
				if (linha.toCharArray()[j] == '4') {
					mapa[i][j] = ETipoBloco.CURVA_ES;
				}
				if (linha.toCharArray()[j] == '5') {
					mapa[i][j] = ETipoBloco.CURVA_DS;
				}
				if (linha.toCharArray()[j] == '6') {
					mapa[i][j] = ETipoBloco.CURVA_EI;
				}
				if (linha.toCharArray()[j] == '7') {
					mapa[i][j] = ETipoBloco.CURVA_DI;
				}
				if (linha.toCharArray()[j] == '8') {
					mapa[i][j] = ETipoBloco.PILULA;
				}
				if (linha.toCharArray()[j] == 'e') {
					mapa[i][j] = ETipoBloco.TELETRANSPORTE_ESQUERDO;
				}
				if (linha.toCharArray()[j] == 'd') {
					mapa[i][j] = ETipoBloco.TELETRANSPORTE_DIRETO;
				}
			}
			
			i++;
			linha = lerArq.readLine();
		}
		//************************************************************************
		reader.close();
		
		return mapa;
	}
	
	private static int pegaPosicaoVirgula(String linha) {
		return linha.indexOf(",");
	}
	
	public static int retornaMeioBlocoX(int objetoPosiX, int tamObjeto) {
		int newPosiX, blocoX;
		
		newPosiX = objetoPosiX + Math.round(tamObjeto/2);	// pega a posição do objeto com a metade do tamanho do objeto
		blocoX = newPosiX/Util.TAMANHO_BLOCO;				// localiza qual o bloco o meio deste objeto esta localizado
		
		return blocoX;										// retorna o bloco que é a posição x na matriz
	}
	
	public static int retornaMeioBlocoY(int objetoPosiY, int tamObjeto) {
		int newPosiY, blocoY;
		
		newPosiY = objetoPosiY + Math.round(tamObjeto/2);	// pega a posição do objeto com a metade do tamanho do objeto
		blocoY = newPosiY/Util.TAMANHO_BLOCO;				// localiza qual o bloco o meio deste objeto esta localizado
		
		return blocoY;										// retorna o bloco que é a posição y na matriz
	}
	
	public static boolean isBlocoValido(ETipoBloco[][] mapa, int blocoX, int blocoY) {
		// se a posição do bloco conter vazio, ponto ou pilula significa que é um bloco valido para movimentação
		if (mapa[blocoY][blocoX] == ETipoBloco.VAZIO || mapa[blocoY][blocoX] == ETipoBloco.PONTO || mapa[blocoY][blocoX] == ETipoBloco.PILULA) {
			return true;
		} else {		
			return false;
		}
	}
	
	public static boolean isMovimentoValidoGhost(ETipoBloco[][] mapa, int posiX, int posiY, int dire) {
		int direX = Util.returnDireX(dire); 	// pega o valor do movimento X a partir da direçao
		int direY = Util.returnDireY(dire);		// pega o valor do movimento Y a partir da direçao
		int xE, xD/*j*/, 
			yC, yB/*i*/;
		
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
	}
	
	public static int returnDireX(int dire) {
		// retorna o valor do movimento X a partir de uma direção 
		if (dire == Teclado.VK_DOWN) {
			return 0;
		} else {
			if (dire == Teclado.VK_LEFT) {
				return -Util.TAMANHO_MOVIMENTO;
			} else {
				if (dire == Teclado.VK_RIGHT) {
					return +Util.TAMANHO_MOVIMENTO;
				} else {
					if (dire == Teclado.VK_UP) {
						return 0;
					}
				}
			}
		}
		
		return 0;
	}

	public static int returnDireY(int dire) {
		// retorna o valor do movimento Y a partir de uma direção 
		if (dire == Teclado.VK_DOWN) {
			return +Util.TAMANHO_MOVIMENTO;
		} else {
			if (dire == Teclado.VK_LEFT) {
				return 0;
			} else {
				if (dire == Teclado.VK_RIGHT) {
					return 0;
				} else {
					if (dire == Teclado.VK_UP) {
						return -Util.TAMANHO_MOVIMENTO;
					}
				}
			}
		}
		
		return 0;
	}
}
