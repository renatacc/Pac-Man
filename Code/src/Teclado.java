
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Teclado implements KeyListener {

	private Janela janela;
	
	public static final int VK_UP 	 = 0;
	public static final int VK_DOWN  = 1;
	public static final int VK_LEFT  = 2;
	public static final int VK_RIGHT = 3;
	public static final int VK_ENTER = 4;
	public static final int VK_SPACE = 5;
	
	public Teclado(Janela janela) {
		this.janela = janela;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		setTecla(e.getKeyCode(), true);		// quando aperta a tecla
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setTecla(e.getKeyCode(), false);	// quando solta a tecla
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}
	
	private void setTecla(int tecla, boolean pressionada) {
		switch (tecla) {	// a partir da tecla pressionada define um comando para o pac-man executar
		case KeyEvent.VK_UP:
			janela.controlaTecla[VK_UP]    = pressionada;
			break;
		case KeyEvent.VK_DOWN: 
			janela.controlaTecla[VK_DOWN]  = pressionada;
			break;
		case KeyEvent.VK_LEFT: 
			janela.controlaTecla[VK_LEFT]  = pressionada;
			break;
		case KeyEvent.VK_RIGHT: 
			janela.controlaTecla[VK_RIGHT] = pressionada;
			break;
		case KeyEvent.VK_ENTER: 
			janela.controlaTecla[VK_ENTER] = pressionada;
			if (pressionada) {				// se a tecla pressionada for enter inicia o jogo
				janela.tela.jogando = true;
			}
			break;
		case KeyEvent.VK_SPACE: 
			janela.controlaTecla[VK_SPACE] = pressionada;
			if (pressionada) {  			// se a tecla pressionada for espaço
				if (janela.tela.jogando) {	// apenas se o jogo estiver iniciado pode pausar ou retomar
					janela.tela.pause = !janela.tela.pause;
				}
			}
			break;
		}
	}
}
