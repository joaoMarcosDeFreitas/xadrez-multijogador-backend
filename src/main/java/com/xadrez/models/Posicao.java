package com.xadrez.models;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//uma casa do tabuleiro (ex.: e2), guardada como índices de 0 a 7. Não muda depois de criada e só existe se o texto da casa for válido
public class Posicao {
	//coluna = letra da casa (a = 0 ... h = 7)
	private final int coluna;
	//linha = número da casa (1 = 0 ... 8 = 7). A linha 1 do xadrez é o índice 0 da matriz
	private final int linha;

	//cria a posição a partir do texto da casa (ex.: "e2"). Texto inválido lança exceção, então uma posição inválida nunca existe
	public Posicao(String casa) {
		super();
		//validador de padrão: uma letra de a a h seguida de um número de 1 a 8 (maiúscula ou minúscula) e nada mais
		Pattern padraoBuscado = Pattern.compile("\\b([a-h])([1-8])\\b", Pattern.CASE_INSENSITIVE);
		Matcher buscaPadrao = padraoBuscado.matcher(casa);	
				
		//só continua se o texto é exatamente uma letra de a a h seguida de um número de 1 a 8
		if(buscaPadrao.matches()) {
			//pega a letra em minúsculo (assim "E2" e "e2" dão o mesmo resultado)
			char letra = Character.toLowerCase(buscaPadrao.group(1).charAt(0));
			//pega o número como caractere
			char numero = Character.toLowerCase(buscaPadrao.group(2).charAt(0));
		
			//subtrai 'a' porque os caracteres têm códigos seguidos (a = 97, b = 98...): a vira 0, b vira 1 ... h vira 7
			this.coluna = letra - 'a';
			//subtrai '1' porque os dígitos têm códigos seguidos ('1' = 49, '2' = 50...): 1 vira 0, 2 vira 1 ... 8 vira 7
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

	//deslocamento de colunas até o destino = coluna do destino menos a desta posição. Mantém o sinal (negativo = esquerda, positivo = direita)
	public int getDeslocamentoColuna(Posicao posicaoDestino) {
		return posicaoDestino.coluna - this.coluna;
	}

	//deslocamento de linhas até o destino = linha do destino menos a desta posição. Mantém o sinal (positivo = linhas maiores, o sentido das pretas). O sinal importa para o peão e para o caminho livre
	public int getDeslocamentoLinha(Posicao posicaoDestino) {
		return posicaoDestino.linha - this.linha;
	}

	//duas posições são iguais quando têm a mesma coluna e a mesma linha (sem isso o Java compararia se são o mesmo objeto)
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

	//usa os mesmos campos do equals: posições iguais precisam ter o mesmo hash (regra do Java)
	@Override
	public int hashCode() {
		return Objects.hash(coluna, linha);
	}
}
