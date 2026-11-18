package com.xadrez.models;

import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.PecaEnum.TipoPeca;

//guarda as 64 casas numa matriz [linha][coluna]. Cada casa tem uma Peca ou null (vazia). A linha 1 do xadrez é o índice 0 (brancas)
public class Tabuleiro {
	//matriz privada: só os métodos desta classe alteram as casas, ninguém de fora escreve direto
	private final Peca[][] tabuleiro;
	private Peca pecaCapturadaMomento;
	private boolean jaMexeuMomento;
	private Posicao posicaoOrigemMomento;
	private Posicao posicaoDestinoMomento;

	//cria o tabuleiro vazio (todas as casas null). As peças só entram em escreverPecasIniciais
	public Tabuleiro() {
		super();
		this.tabuleiro = new Peca[8][8];
	}
	
	//devolve a peça da casa, ou null se estiver vazia. Quem chama precisa conferir null antes de usar a peça
	public Peca getPecaNaCasa(Posicao posicao) {
		return this.tabuleiro[posicao.getLinha()][posicao.getColuna()];
	}
	
	//único ponto que grava na matriz. Grava sem conferir nada (as conferências ficam em quem chama). Passar null esvazia a casa
	private void escreverPeca(Peca peca, int linha, int coluna) {
		this.tabuleiro[linha][coluna] = peca;
	}
	
	//monta as 32 peças da posição inicial. É chamado pela Partida quando o segundo jogador entra
	public void escreverPecasIniciais() {
		//ordem das peças da linha de trás, da coluna a até h (o índice do vetor é a coluna). É a mesma para brancas e pretas
		TipoPeca[] tipoPeca = {
				TipoPeca.TORRE, 
				TipoPeca.CAVALO, 
				TipoPeca.BISPO, 
				TipoPeca.RAINHA, 
				TipoPeca.REI, 
				TipoPeca.BISPO, 
				TipoPeca.CAVALO, 
				TipoPeca.TORRE
				};
		
		//uma volta por coluna: cada volta cria as 4 peças daquela coluna, todas como objetos novos (cada peça tem o seu próprio jaMexeu)
		for(int j = 0; j < this.tabuleiro.length; j++) {
			//cria peças brancas (linha 0 = peça maior, linha 1 = peão)
			escreverPeca(new Peca(tipoPeca[j], CorPecas.BRANCAS, false), 0, j);
			escreverPeca(new Peca(TipoPeca.PEAO, CorPecas.BRANCAS, false), 1, j);
		
			//cria peças pretas (linha 7 = peça maior, linha 6 = peão)
			escreverPeca(new Peca(TipoPeca.PEAO, CorPecas.PRETAS, false), 6, j);
			escreverPeca(new Peca(tipoPeca[j], CorPecas.PRETAS, false), 7, j);
		}
	}
	
	//devolve o texto das peças no formato casa,TIPO,COR separado por ; (ex.: a1,TORRE,BRANCAS;b1,CAVALO,BRANCAS). Casa vazia não entra. É o texto que vai para o cliente
	public String getPecasTabuleiro() {
		String retornoTodasPecas = "";
		for(int i = 0; i < this.tabuleiro.length; i++) {
			for(int j = 0; j < this.tabuleiro[i].length; j++) {
				Peca pecaAtual = this.tabuleiro[i][j];
				//só casas com peça entram no texto; o ; vai antes de cada peça, menos da primeira
				if(pecaAtual != null) retornoTodasPecas += retornoTodasPecas.isBlank() ? getCasa(i, j) + "," + pecaAtual.getNomePeca().toUpperCase() + "," + pecaAtual.getCorPeca().toUpperCase() : ";" + getCasa(i, j) + "," + pecaAtual.getNomePeca().toUpperCase() + "," + pecaAtual.getCorPeca().toUpperCase();
			}
		}
		return retornoTodasPecas;
	}
	
	//converte linha e coluna (índices) no texto da casa (ex.: 0 e 4 viram "e1"). É o caminho inverso do construtor da Posicao
	public String getCasa(int linha, int coluna) {
		return Character.toString((coluna) + 'a') + Character.toString((linha) + '1');
	}
	
	//devolve true se NÃO há peça entre a origem e o destino (as duas pontas não contam). Só vale para reta ou diagonal (torre, bispo, rainha, avanço duplo do peão)
	public boolean verificarCasasDoMeio(Posicao posicaoOrigem, Posicao posicaoDestino) {
		//passo de cada volta (-1, 0 ou 1 em cada direção): o signum reduz o deslocamento a uma casa por vez
		int linhasAndadas = Integer.signum(posicaoOrigem.getDeslocamentoLinha(posicaoDestino));
		int colunasAndadas = Integer.signum(posicaoOrigem.getDeslocamentoColuna(posicaoDestino));
		
		//começa na primeira casa depois da origem
		int linhaAtual = posicaoOrigem.getLinha() + linhasAndadas;
		int colunaAtual = posicaoOrigem.getColuna() + colunasAndadas;
		
		//anda casa por casa até chegar no destino (o destino não é conferido)
		while(linhaAtual != posicaoDestino.getLinha() || colunaAtual != posicaoDestino.getColuna()) {
			//achou peça no caminho: o caminho não está livre
			if(this.tabuleiro[linhaAtual][colunaAtual] != null) return false;
			linhaAtual += linhasAndadas;
			colunaAtual += colunasAndadas;
		}
		return true;
	}
	
	//move a peça da origem para o destino (se o destino tem peça adversária, é captura). Só recusa origem vazia e destino com peça da mesma cor; as regras de cada peça ficam no validador. Devolve false se recusou
	public boolean moverOuCapturar(Posicao posicaoOrigem, Posicao posicaoDestino) {
		//peça que está se movendo e peça do destino (null se o destino estiver vazio)
		Peca peca = this.tabuleiro[posicaoOrigem.getLinha()][posicaoOrigem.getColuna()];
		Peca pecaDestino = this.tabuleiro[posicaoDestino.getLinha()][posicaoDestino.getColuna()];
		
		//recusa: não há peça na origem
		if(peca == null) return false;
		
		//recusa: o destino já tem peça da mesma cor (só compara a cor quando o destino não é null)
		if(pecaDestino != null && peca.getCor().equals(pecaDestino.getCor())) return false;
		
		//coloca a peça no destino (substitui a capturada, se houver) e esvazia a origem
		escreverPeca(peca, posicaoDestino.getLinha(), posicaoDestino.getColuna());;
		escreverPeca(null, posicaoOrigem.getLinha(), posicaoOrigem.getColuna());
		//marca que a peça já se moveu (roque e avanço duplo do peão)
		peca.setJaMexeu(true);
		return true;
	}
	
	public String procuraRei(CorPecas cor) {
		for(int i = 0; i < this.tabuleiro.length; i++) {
			for(int j = 0; j < this.tabuleiro[i].length; j++) {
				Peca peca = this.tabuleiro[i][j];
				if(peca != null) {
					if(peca.getTipoPeca().equals(TipoPeca.REI) && peca.getCor().equals(cor)) return getCasa(i, j);
				}
			}
		}
		throw new IllegalStateException("O rei não foi encontrado! Um erro ocorreu");
	}
	
	public boolean simulacaoMovimento(Posicao posicaoOrigem, Posicao posicaoDestino) {
		this.pecaCapturadaMomento = this.tabuleiro[posicaoDestino.getLinha()][posicaoDestino.getColuna()];
		this.jaMexeuMomento = this.tabuleiro[posicaoOrigem.getLinha()][posicaoOrigem.getColuna()].getJaMexeu();
		this.posicaoOrigemMomento = posicaoOrigem;
		this.posicaoDestinoMomento = posicaoDestino;
		
		return moverOuCapturar(posicaoOrigem, posicaoDestino);
	}
	
	public void desfazerMovimento() {
		
	}
}
