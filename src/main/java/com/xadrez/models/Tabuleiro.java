package com.xadrez.models;

import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.PecaEnum.TipoPeca;

public class Tabuleiro {
	private final Peca[][] tabuleiro;

	public Tabuleiro() {
		super();
		this.tabuleiro = new Peca[8][8];
	}
	
	//método para pegar a peça ou null de uma posição
	public Peca getPecaNaCasa(Posicao posicao) {
		return tabuleiro[posicao.getLinha()][posicao.getColuna()];
	}
	
	//método para escrever no tabuleiro as peças
	private void escreverPeca(Peca peca, int linha, int coluna) {
		tabuleiro[linha][coluna] = peca;
	}
	
	//método para escrever as posições iniciais
	public void escreverPecasIniciais() {
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
		
		for(int j = 0; j < tabuleiro.length; j++) {
			//cria peças brancas
			escreverPeca(new Peca(tipoPeca[j], CorPecas.BRANCAS, false), 0, j);
			escreverPeca(new Peca(TipoPeca.PEAO, CorPecas.BRANCAS, false), 1, j);
		
			//cria peças pretas
			escreverPeca(new Peca(TipoPeca.PEAO, CorPecas.PRETAS, false), 6, j);
			escreverPeca(new Peca(tipoPeca[j], CorPecas.PRETAS, false), 7, j);
		}
	}
	
	//método para retornar posicionamento das peças
	public String getPecasTabuleiro() {
		String retornoTodasPecas = "";
		for(int i = 0; i < tabuleiro.length; i++) {
			for(int j = 0; j < tabuleiro[i].length; j++) {
				Peca pecaAtual = tabuleiro[i][j];
				if(pecaAtual != null) {
					retornoTodasPecas += retornoTodasPecas.isBlank() ? getCasa(i, j) + "," + pecaAtual.getNomePeca().toUpperCase() + "," + pecaAtual.getCorPeca().toUpperCase() : ";" + getCasa(i, j) + "," + pecaAtual.getNomePeca().toUpperCase() + "," + pecaAtual.getCorPeca().toUpperCase();
				}
			}
		}
		return retornoTodasPecas;
	}
	
	//método para transformar linha e coluna em um texto novamente
	public String getCasa(int linha, int coluna) {
		return Character.toString((coluna) + 'a') + Character.toString((linha) + '1');
	}
	
	//método para verificar se tem peças entre as casas de origem e destino
	public boolean verificarCasasDoMeio(Posicao posicaoOrigem, Posicao posicaoDestino) {
		int linhasAndadas = Integer.signum(posicaoOrigem.getDeslocamentoLinha(posicaoDestino));
		int colunasAndadas = Integer.signum(posicaoOrigem.getDeslocamentoColuna(posicaoDestino));
		
		int linhaAtual = posicaoOrigem.getLinha() + linhasAndadas;
		int colunaAtual = posicaoOrigem.getColuna() + colunasAndadas;
		
		while(linhaAtual != posicaoDestino.getLinha() || colunaAtual != posicaoDestino.getColuna()) {
			if(tabuleiro[linhaAtual][colunaAtual] != null) {
				return false;
			}
			linhaAtual += linhasAndadas;
			colunaAtual += colunasAndadas;
		}
		return true;
	}
}
