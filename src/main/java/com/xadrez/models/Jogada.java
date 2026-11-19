package com.xadrez.models;

//uma tentativa de jogada: quem jogou, a casa de origem e a de destino (não mudam) e o resultado da validação (aceita ou não, e o motivo)
public class Jogada {
	//quem fez a jogada
	private Jogador jogador;
	//casa de onde a peça sai
	private final Posicao posicaoOrigem;
	//casa para onde a peça vai
	private final Posicao posicaoDestino;
	//resultado da validação: começa falso e passa a verdadeiro quando a jogada é aceita
	private boolean jogadaAceita;
	//motivo mostrado ao jogador quando a jogada é recusada (começa vazio)
	private String motivoResposta;

	//cria a jogada. Origem igual ao destino não é uma jogada, então lança exceção
	public Jogada(Jogador jogador, Posicao posicaoOrigem, Posicao posicaoDestino) {
		if(posicaoOrigem.equals(posicaoDestino)) throw new IllegalArgumentException("Posição de origem e destino precisam ser diferentes!");
		else {
			this.jogador = jogador;
			this.posicaoOrigem = posicaoOrigem;
			this.posicaoDestino = posicaoDestino;
			this.motivoResposta = "";
		}
	}

	//quem fez a jogada
	public Jogador getJogador() {
		return jogador;
	}

	//casa de origem
	public Posicao getPosicaoOrigem() {
		return posicaoOrigem;
	}

	//casa de destino
	public Posicao getPosicaoDestino() {
		return posicaoDestino;
	}	

	//diz se a jogada foi aceita
	public boolean getJogadaAceita() {
		return jogadaAceita;
	}

	//marca a jogada como aceita ou recusada
	public void setJogadaAceita(boolean jogadaAceita) {
		this.jogadaAceita = jogadaAceita;
	}

	//motivo da recusa (vazio se não houve recusa)
	public String getMotivoResposta() {
		return motivoResposta;
	}

	//define o motivo que será mostrado ao jogador
	public void setMotivoResposta(String motivoResposta) {
		this.motivoResposta = motivoResposta;
	}
}