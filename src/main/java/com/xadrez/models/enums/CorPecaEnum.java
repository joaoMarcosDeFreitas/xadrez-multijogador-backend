package com.xadrez.models.enums;

//só agrupa o enum das cores das peças
public class CorPecaEnum {
	public enum CorPecas {
		//as duas cores do jogo
		BRANCAS, 
		PRETAS;
		
		//devolve a cor do adversário (usada para passar a vez)
		public CorPecas oposta() {
			return this == BRANCAS ? PRETAS : BRANCAS;
		}
	}
}
