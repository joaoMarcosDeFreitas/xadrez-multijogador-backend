package com.xadrez.models.enums;

//só agrupa o enum dos estados da partida
public class EstadoDaPartidaEnum {
	//fases da partida: esperando o segundo jogador, em jogo e terminada
	public enum EstadoDaPartida {
		//esperando o segundo jogador entrar
		AGUARDANDO,
		//os dois jogadores entraram e a partida está sendo jogada
		EM_ANDAMENTO,
		//a partida terminou (por mate ou empate): não aceita mais jogadas
		CONCLUIDA
	}
}
