package com.xadrez.models.enums;

//só agrupa o enum dos resultados possíveis da partida
public class ResultadoEnum {
	public enum Resultado {
		//xeque-mate: as brancas venceram (as pretas levaram mate)
		XEQUE_MATE_BRANCAS,
		//xeque-mate: as pretas venceram (as brancas levaram mate)
		XEQUE_MATE_PRETAS,
		//empate: a cor da vez não tem lance legal, mas o rei não está em xeque
		EMPATE_AFOGAMENTO,
		//empate: sobraram só os reis, ou os reis e uma única peça menor
		EMPATE_MATERIAL_INSUFICIENTE,
		//empate: 50 lances de cada jogador sem captura e sem movimento de peão
		EMPATE_50_LANCES,
		//empate: a mesma posição, com a mesma vez de jogar, apareceu 3 vezes
		EMPATE_REPETICAO
	}
}
