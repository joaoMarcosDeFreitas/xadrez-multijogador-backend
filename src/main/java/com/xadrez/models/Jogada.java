package com.xadrez.models;

//ainda em construção (item 5.7): vai guardar o jogador, a origem e o destino de uma jogada
public class Jogada {
	private Jogador jogador;
	private String posicaoOrigem;
	private String posicaoDestino;
	
	public Jogada(Jogador jogador, String posicaoOrigem, String posicaoDestino) {
		this.jogador = jogador;
		this.posicaoOrigem = posicaoOrigem;
		this.posicaoDestino = posicaoDestino;
	}

	public Jogador getJogador() {
		return jogador;
	}

	public void setJogador(Jogador jogador) {
		this.jogador = jogador;
	}

	public String getPosicaoOrigem() {
		return posicaoOrigem;
	}

	public void setPosicaoOrigem(String posicaoOrigem) {
		this.posicaoOrigem = posicaoOrigem;
	}

	public String getPosicaoDestino() {
		return posicaoDestino;
	}

	public void setPosicaoDestino(String posicaoDestino) {
		this.posicaoDestino = posicaoDestino;
	}
	
	
}