package com.xadrez.models;

import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.PecaEnum.TipoPeca;

//uma peça do jogo: o tipo e a cor não mudam; só o "já mexeu" muda, no primeiro movimento
public class Peca {
	//tipo da peça (peão, torre, cavalo...)
	private final TipoPeca tipoPeca;
	//cor da peça (brancas ou pretas)
	private final CorPecas cor;
	//começa falso e vira verdadeiro no primeiro movimento. Serve para o avanço duplo do peão (e, no futuro, para o roque)
	private boolean jaMexeu;

	//cria a peça com tipo, cor e se já se moveu (as peças iniciais nascem com falso)
	public Peca(TipoPeca tipoPeca, CorPecas cor, boolean jaMexeu) {
		super();
		this.tipoPeca = tipoPeca;
		this.cor = cor;
		this.jaMexeu = jaMexeu;
	}

	//tipo da peça (enum)
	public TipoPeca getTipoPeca() {
		return tipoPeca;
	}

	//cor da peça (enum). É esta que se usa para comparar cores
	public CorPecas getCor() {
		return cor;
	}

	//diz se a peça já se moveu
	public boolean getJaMexeu() {
		return jaMexeu;
	}

	//atualiza o "já mexeu" (o tabuleiro marca ao mover e restaura ao desfazer uma simulação)
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
