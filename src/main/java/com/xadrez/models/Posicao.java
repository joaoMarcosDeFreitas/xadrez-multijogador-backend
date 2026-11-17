package com.xadrez.models;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Posicao {
	private final int coluna;
	private final int linha;
	
	public Posicao(String casaDestino) {
		super();
		//validador de padrão
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
	
	public int getColuna() {
		return coluna;
	}
	
	public int getLinha() {
		return linha;
	}
	
	//método para transformar a jogada em um texto novamente
	public String getCasa() {
		return Character.toString((this.coluna) + 'a') + Character.toString((this.linha) + '1');
	}
	
	//método para calcular o deslocamento da coluna
	public int getDeslocamentoColuna(Posicao posicaoDestino) {
		return posicaoDestino.coluna - this.coluna;
	}
	
	//método para calcular o deslocamento da linha
	public int getDeslocamentoLinha(Posicao posicaoDestino) {
		return posicaoDestino.linha - this.linha;
	}

	//sobreescreve os metodos para poder comparar posições iguais
	
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
