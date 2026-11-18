package com.xadrez.models.enums;

public class CorPecaEnum {
	public enum CorPecas {
		BRANCAS, 
		PRETAS;
		
		//devolve a cor do adversário (usada para passar a vez)
		public CorPecas oposta() {
			return this == BRANCAS ? PRETAS : BRANCAS;
		}
	}
}
