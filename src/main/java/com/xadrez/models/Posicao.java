package com.xadrez.models;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//representa UMA casa do tabuleiro (ex.: e2) em índices de 0 a 7. Não muda depois de criada (final) e só existe se o texto for válido
public class Posicao {
	//coluna = letra da casa (a = 0 ... h = 7)
	private final int coluna;
	//linha = número da casa (1 = 0 ... 8 = 7). A linha 1 é o índice 0 da matriz
	private final int linha;
	
	//cria a posição a partir do texto da casa (ex.: "e2"). Se o texto for inválido lança exceção, então uma posição inválida nunca existe
	public Posicao(String casaDestino) {
		super();
		//validador de padrão: uma letra de a a h seguida de um número de 1 a 8 (maiúscula ou minúscula) e nada mais
		Pattern padraoBuscado = Pattern.compile("\\b([a-h])([1-8])\\b", Pattern.CASE_INSENSITIVE);
		Matcher buscaPadrao = padraoBuscado.matcher(casaDestino);	
				
		//verifica se o padrão foi encontrado
		if(buscaPadrao.matches()) {
			//cria o char para receber o chatAt da letra
			char letra = Character.toLowerCase(buscaPadrao.group(1).charAt(0));
			//cria o char para receber o chatAt do numero
			char numero = Character.toLowerCase(buscaPadrao.group(2).charAt(0));
		
			//diminui pois a conversão de 'a' para inteiro é 97, 'b' é 98 e por ai vai, portanto 97 - 97 - 0, 98 - 97 = 1... (esses são os índices corretos)
			this.coluna = letra - 'a';
			//diminui pois o número é string e sua conversão para inteiro é 49, '2' é 50 e por ai vai, portanto 49 - 49 = 0, 50 - 49 = 1... (esses são os índices corretos)
			this.linha = numero - '1';
		} else {
			throw new IllegalArgumentException("Jogada inválida! Deve ser de A a H seguido de 1 a 8.");
		}
	}
	
	//índice da coluna (0 a 7)
	public int getColuna() {
		return coluna;
	}
	
	//índice da linha (0 a 7)
	public int getLinha() {
		return linha;
	}
	
	//devolve o texto da casa (ex.: coluna 4 e linha 1 viram "e2"). É o caminho inverso do construtor
	public String getCasa() {
		return Character.toString((this.coluna) + 'a') + Character.toString((this.linha) + '1');
	}
	
	//deslocamento da coluna = destino menos origem (mantém o sinal: negativo = esquerda, positivo = direita). As regras de movimento usam isso
	public int getDeslocamentoColuna(Posicao posicaoDestino) {
		return posicaoDestino.coluna - this.coluna;
	}
	
	//deslocamento da linha = destino menos origem (mantém o sinal: positivo = linha maior). O sinal importa para o peão e para o caminho livre
	public int getDeslocamentoLinha(Posicao posicaoDestino) {
		return posicaoDestino.linha - this.linha;
	}

	//duas posições são iguais se têm a mesma coluna e a mesma linha. O hashCode usa os mesmos campos (regra do Java: iguais têm o mesmo hash)
	
	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(coluna), Integer.valueOf(linha));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Posicao other = (Posicao) obj;
		return coluna == other.coluna && linha == other.linha;
	}
}
