package com.xadrez.models;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.EstadoDaPartidaEnum.EstadoDaPartida;

//uma partida de xadrez: guarda os jogadores (por cor), o estado, de quem é a vez e o tabuleiro
public class Partida {
    private String idPartida;
	//jogadores da partida, indexados pela cor (no máximo 2)
	private final Map<CorPecas, Jogador> jogadores = new EnumMap<>(CorPecas.class);
	//aguardando, em andamento ou concluída
	private EstadoDaPartida estadoDaPartida;
	//cor de quem joga agora (null até a partida começar)
	private CorPecas turno;
    //tabuleiro da partida: nasce vazio e é montado quando o segundo jogador entra
    private final Tabuleiro tabuleiro;
	
	//a partida nasce aguardando jogadores, com o tabuleiro vazio
	public Partida(String idPartida) {
		this.idPartida = idPartida;
		this.estadoDaPartida = EstadoDaPartida.AGUARDANDO;
		this.tabuleiro = new Tabuleiro();
	}

	public String getIdPartida() {
		return idPartida;
	}
	
	//estado atual da partida
	public EstadoDaPartida getEstadoDaPartida() {
		return estadoDaPartida;
	}
	
	//cor da vez (null enquanto a partida não começou)
	public CorPecas getTurno() {
		return turno;
	}

	//verifica se o jogador já entrou na partida (compara pelo id). É assim que se reconhece uma reconexão
	public Boolean verificaReconexao(Jogador jogadorBuscado) {
		//looping na lista de jogadores
		for(Jogador jogador : jogadores.values()) {
			//verifica se o ID é igual
			if(jogadorBuscado.getId().equals(jogador.getId())) {
				return true;
			}
		}
		return false;
	}

	//acha o jogador pelo id e devolve a cor e o jogador cadastrado (null se não achar). O Entry dá a cor (getKey) e os dados registrados (getValue)
	public Map.Entry<CorPecas, Jogador> getJogador(Jogador jogadorBuscado) {
		for(Map.Entry<CorPecas, Jogador> jogador : jogadores.entrySet()) {
			if(jogador.getValue().getId().equals(jogadorBuscado.getId())) {
				return jogador;
			}
		}
		return null;
	}
	
	//verifica se já tem 2 jogadores
	public Boolean verificaSeJogoEstaCheio() {
		//a partida está cheia quando já tem 2 jogadores
		return jogadores.size() >= 2 ? true : false;
	}

	//verifica qual cor está disponível
	public ArrayList<CorPecas> corDisponivel() {
		//cria um array de CorPecas para armazenas os disponíveis
		ArrayList<CorPecas> listaDeDisponibilidade = new ArrayList<>();
		//looping para preencher quais são as cores disponíveis (as duas ou só uma)
		for(CorPecas cor : CorPecas.values()) {
			if(!jogadores.containsKey(cor)) {
				listaDeDisponibilidade.add(cor);
			}
		}
		return listaDeDisponibilidade;
	}
	
	//adiciona o jogador na cor escolhida. Ignora (sem avisar) se a partida já tem 2, se o jogador já entrou ou se a cor está ocupada
	public void adicionarCorPecasJogadores(CorPecas cor, Jogador jogadorParaAdicionar) {
		if(jogadores.size() < 2) {
			for(Map.Entry<CorPecas, Jogador> jogador : jogadores.entrySet()) {
				if(jogador.getValue().getId().equals(jogadorParaAdicionar.getId()) || jogador.getKey().equals(cor)) {
					return;
				}
			}
			//adiciona a cor e o jogador ao mapa de jogadores
			this.jogadores.put(cor, jogadorParaAdicionar);
			//segundo jogador entrou: a partida começa, as peças são montadas e as brancas jogam primeiro
			if(jogadores.size() == 2) {
				this.estadoDaPartida = EstadoDaPartida.EM_ANDAMENTO;
				this.tabuleiro.escreverPecasIniciais();
				this.turno = CorPecas.BRANCAS;
			}
		}
	}
	
	//pega a lista de jogadores que está presente
	public ArrayList<Jogador> listaJogadores() {
		//inicia o array de jogadores
		ArrayList<Jogador> jogadoresNaLista = new ArrayList<>();
		//percorre a lista de jogadores
		for(Jogador jogador : jogadores.values()) {
			//adiciona no array de jogadores os jogadores na lista
			jogadoresNaLista.add(jogador);
		}
		//retorna os jogadores
		return jogadoresNaLista;
	}
	
	//passa a vez para a outra cor (só deve ser chamado com a partida em andamento, senão o turno ainda é null)
	public void trocarTurno() {
		this.turno = this.turno.oposta();
	}
}