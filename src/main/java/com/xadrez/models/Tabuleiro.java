package com.xadrez.models;

import com.xadrez.models.enums.PecaEnum.TipoPeca;

public class Tabuleiro {
	private final Peca[][] tabuleiro;

	public Tabuleiro(Peca peca) {
		super();
		this.tabuleiro = new Peca[8][8];
	}
	
	//método para pegar a peça ou null de uma posição
	public Peca getPecaNaCasa(Posicao posicao) {
		return tabuleiro[posicao.getLinha()][posicao.getColuna()];
	}
	
	//método para escrever no tabuleiro as peças
	private void escreverPeca(Peca peca, Posicao posicao) {
		if(tabuleiro[posicao.getLinha()][posicao.getColuna()] == null) {
			tabuleiro[posicao.getLinha()][posicao.getColuna()] = peca;
		}
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
		
		for(int i = 0; i < tabuleiro.length; i++) {
			
		}
	}
}
