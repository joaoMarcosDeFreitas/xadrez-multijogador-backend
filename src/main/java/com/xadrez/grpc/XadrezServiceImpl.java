package com.xadrez.grpc;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.xadrez.models.Jogador;
import com.xadrez.models.Partida;
import com.xadrez.models.Tabuleiro;
import com.xadrez.models.enums.CorPecaEnum.CorPecas;

import io.grpc.Status;

public class XadrezServiceImpl extends XadrezServiceGrpc.XadrezServiceImplBase {
	//O synchronized durante todos os métodos que comunicam com o servidor (recebendo ou enviando dados) servem para não permitir que duas chamadas ocorram simultâneamente. Dessa forma, ele impede o race condition.
	
	//inicia a partida assim que o servidor liga (existe uma só partida por servidor)
	private final Partida partida = new Partida(UUID.randomUUID().toString());
	
	//INICIO DOS MÉTODOS DE CONEXÃO DO JOGADOR
	
	//conecta um jogador. Id já conhecido = reconexão (devolve os dados dele). Jogador novo passa pela validação do nome e da sala e recebe cor e id novo
	@Override
	public synchronized void conectarJogador(ConectarRequest request, io.grpc.stub.StreamObserver<com.xadrez.grpc.ConectarResponse> responseObserver) {
		//verificações da partida (se está livre para entrada de jogadores e quais cores estão livres -> assim define quantos jogadores tem em sala)
		
		//De qualquer forma, precisamos iniciar o jogador que vai entrar.
		//cria o jogador com o id e o nome que vieram do cliente (o id só vale para reconexão; o jogador novo recebe um id gerado mais abaixo)
		Jogador jogador = new Jogador(request.getIdJogador().trim(), request.getNomeJogador().trim());

		//verifica se é reconexão
		if(partida.verificaReconexao(jogador)) {
			//pega a cor do jogador que se reconectou
			Map.Entry<CorPecas, Jogador> jogadorReconectado = partida.getJogador(jogador);
			//define as informações ja registradas do jogador
			jogador.setNome(jogadorReconectado.getValue().getNome());
			CorPecas corPeca = jogadorReconectado.getKey();
			//utiliza o método para retornar sucesso ao servidor
			responderServidorSucessoConectarJogador(responseObserver, jogador, corPeca);
			//return para parar o código
			return;
		} else {
			//verifica se o nome passado está válido (não é vazio e tem tamanho adequado)
			if(!jogador.verificaNome(partida.listaJogadores())) {
				//utiliza o método para retornar erro ao servidor
				responderServidorErroConectarJogador(responseObserver, "3", 
"Nome inválido, insira um nome válido e tente novamente. Nome deve ser maior ou igual a 3 carácteres, menor ou igual a 15 carácteres, não pode estar vazio e deve ser único.");
				//return para parar o código
				return;
			}
			
			//Verificações de tamanho da sala 
			if(partida.verificaSeJogoEstaCheio()) {
				//utiliza o método para retornar erro ao servidor
				responderServidorErroConectarJogador(responseObserver, "9", "Jogo está cheio! Tente novamente mais tarde");
				//return para parar o código
				return;
			}
			
			//jogo tem vaga
			
			//verifica cores disponíveis no jogo. Aqui é definido quantas vagas sobraram
			ArrayList<CorPecas> coresDisponiveis = partida.corDisponivel();
			//Adiciona a cor ao jogador, sempre com índice 0 para garantir que pega sempre o primeiro. (Quem entra primeiro sempre é branca)
			CorPecas corPecaEscolhida = coresDisponiveis.get(0);
			partida.adicionarCorPecasJogadores(corPecaEscolhida, jogador);
			//o servidor sempre gera o id do jogador novo (o id enviado pelo cliente é ignorado)
			jogador.setId(UUID.randomUUID().toString());
			//utiliza o método para retornar sucesso ao servidor
			responderServidorSucessoConectarJogador(responseObserver, jogador, corPecaEscolhida);
			//return para parar o código
			return;
		}
	}
	
	//responde com erro gRPC. Código "9" (sala cheia) vira FAILED_PRECONDITION, "3" (nome inválido) vira INVALID_ARGUMENT e qualquer outro vira UNKNOWN
	public void responderServidorErroConectarJogador(io.grpc.stub.StreamObserver<com.xadrez.grpc.ConectarResponse> responseObserver, String codigo, String descricao) {
		//inicia a exceção que será jogada ao usuário (para saber porque não funcionou)
		Throwable excecao;
		//switch para definir qual código foi usado.
		switch(codigo) {
			case "9":
				//cria a exceção para retornar erro
				excecao = Status.FAILED_PRECONDITION.withDescription(descricao).asRuntimeException(); 
				break;
			case "3":
				//cria a exceção para retornar erro
				excecao = Status.INVALID_ARGUMENT.withDescription(descricao).asRuntimeException(); 
				break;
			default:
				//cria a exceção para retornar erro
				excecao = Status.UNKNOWN.withDescription(descricao).asRuntimeException(); 
		}
		//lança o erro ao usuário
		responseObserver.onError(excecao);
	}
		
	//método para responder o servidor com sucesso. (REFERENTE A CONEXÃO DE JOGADOR)
	public void responderServidorSucessoConectarJogador(io.grpc.stub.StreamObserver<com.xadrez.grpc.ConectarResponse> responseObserver, Jogador jogador, CorPecas corPecaEscolhida) {
		//monta a mensagem de sucesso ao servidor
		ConectarResponse mensagem = ConectarResponse.newBuilder()
				.setIdJogador(jogador.getId())
				.setIdPartida(partida.getIdPartida())
				.setNomeJogador(jogador.getNome())
				.setCorPecas(corPecaEscolhida.toString())
				.build();
				
		//envia a mensagem ao servidor
		responseObserver.onNext(mensagem);
		//encerra a resposta após completar
		responseObserver.onCompleted();
	}

	//FIM DOS MÉTODOS DE CONEXÃO DO JOGADOR
	
	//INICIO DOS MÉTODOS DE ENVIAR JOGADAS
	
	//ainda vazio (Etapa 6): vai validar e aplicar a jogada. É synchronized para não processar duas jogadas ao mesmo tempo
	@Override 
	public synchronized void enviarJogada(JogadaRequest request, io.grpc.stub.StreamObserver<com.xadrez.grpc.JogadaResponse> responseObserver) {
		
	}
}