package com.xadrez.models;

import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.PecaEnum.TipoPeca;

//uma peça do jogo: tipo, cor e se já se moveu
public class Peca {
	private final TipoPeca tipoPeca;
	private final CorPecas cor;
	//começa falso e vira verdadeiro no primeiro movimento (vai ser usado no roque e no avanço duplo do peão)
	private boolean jaMexeu;
	
	public Peca(TipoPeca tipoPeca, CorPecas cor, boolean jaMexeu) {
		super();
		this.tipoPeca = tipoPeca;
		this.cor = cor;
		this.jaMexeu = jaMexeu;
	}
	
	public TipoPeca getTipoPeca() {
		return tipoPeca;
	}

	public CorPecas getCor() {
		return cor;
	}

	public boolean getJaMexeu() {
		return jaMexeu;
	}

	public void setJaMexeu(boolean jaMexeu) {
		this.jaMexeu = jaMexeu;
	}
	
	//tipo da peça em texto (ex.: TORRE). Usado no texto do estado enviado ao cliente
	public String getNomePeca() {
		return this.tipoPeca.toString();
	}
	
	//cor da peça em texto (ex.: BRANCAS). Usado no texto do estado enviado ao cliente
	public String getCorPeca() {
		return this.cor.toString();
	}
}
