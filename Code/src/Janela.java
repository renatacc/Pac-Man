import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Janela extends JFrame {

	private static final long serialVersionUID = 1L;
	public BufferedImage buffer;
	public Graphics2D g2d;
	
	public Tela tela;
	
	public boolean[] controlaTecla = new boolean[6];					// vetor de controle de teclas
	
	public Janela() {
		super("Pac-Man");
		
		Teclado teclado = new Teclado(this);							// adiciona um leitor do teclado a tela
		addKeyListener(teclado);
		
		buffer = new BufferedImage(Util.JANELA_LARGURA, Util.JANELA_ALTURA, BufferedImage.TYPE_INT_RGB);	//cria buffer
		g2d = buffer.createGraphics();									//pincel que escreve no buffer
		
		tela = new Tela(this);
		tela.iniciaJogo();
		add(tela);														// adiciona tela ao jogo
		
		setSize(Util.JANELA_LARGURA, Util.JANELA_ALTURA);				// define dimenções da tela
		getContentPane().setBackground(Color.BLACK);					// deixa fundo preto
		
		setVisible(true);												// deixa tela visivel
        setDefaultCloseOperation(EXIT_ON_CLOSE);						// define função ao fechar janela
        setLocationRelativeTo(null);									// centraliza janela
		setResizable(false);											// desativa redimencionar da tela
		
		iniciaAnimacao();
	}
	
	private void iniciaAnimacao() {
		long atualiza = 0;
		
		while (true) {
			if (!tela.gameOver && !tela.ganhou) {
				while (tela.jogando) {
					if (!tela.pause) {
						if (System.currentTimeMillis() >= atualiza) {			// de 60 em 60 ele repinta a tela
							this.repaint();
							atualiza = System.currentTimeMillis() + Util.fps;	// atualiza o tempo atual de execução
						}
					}else{
						if (System.currentTimeMillis() >= atualiza) {
							this.repaint();
							atualiza = System.currentTimeMillis() + Util.fps;
						}
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				if (tela.jogando) {
					tela.iniciaJogo();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
}
