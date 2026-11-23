package com.xadrez.models;

import java.util.ArrayList;

//jogador conectado à partida: o id (gerado pelo servidor) e o nome
public class Jogador {
	//id gerado pelo servidor; é com ele que o jogador se reconecta
	private String id;
	//nome escolhido pelo jogador (3 a 15 caracteres, único na partida)
	private String nome;

	//cria o jogador com o id e o nome informados
	public Jogador(String id, String nome) {
		super();
		this.id = id;
		this.nome = nome;
	}

	//id do jogador
	public String getId() {
		return id;
	}

	//troca o id (o serviço coloca aqui o id novo gerado pelo servidor)
	public void setId(String id) {
		this.id = id;
	}

	//nome do jogador
	public String getNome() {
		return nome;
	}

	//troca o nome (usado na reconexão, para voltar ao nome já cadastrado)
	public void setNome(String nome) {
		this.nome = nome;
	}

	//devolve true se o nome é válido: de 3 a 15 caracteres e diferente do nome dos jogadores que já estão na partida (sem diferenciar maiúscula de minúscula)
	public boolean verificaNome(ArrayList<Jogador> jogadores) {
		//verifica se tem menos que 3 letras ou mais que 15
		if(this.nome.length() > 15 || this.nome.length() < 3) {
			return false;
		}
		
		//looping na lista de jogadores
		for(Jogador jogador : jogadores) {
			//verifica se nome já está sendo utilizado
			if(this.nome.equalsIgnoreCase(jogador.getNome())) 
				return false;
		}
		
		return true;
	}
}