package com.xadrez.grpc;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.xadrez.models.Jogada;
import com.xadrez.models.Jogador;
import com.xadrez.models.Partida;
import com.xadrez.models.Posicao;
import com.xadrez.models.ValidadorRegras;
import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.EstadoDaPartidaEnum.EstadoDaPartida;
import com.xadrez.models.enums.ResultadoEnum.Resultado;

import io.grpc.Status;

//serviço gRPC do xadrez: recebe as chamadas dos clientes e usa a Partida para responder. Os métodos que lidam com a partida são synchronized: só uma chamada por vez mexe no estado, o que evita race condition
public class XadrezServiceImpl extends XadrezServiceGrpc.XadrezServiceImplBase {
	//a partida do servidor: é criada quando o servidor liga e só existe uma por servidor
	private final Partida partida = new Partida(UUID.randomUUID().toString());

	//---- chamadas dos clientes (uma por método do .proto) ----
	//conecta um jogador. Id já conhecido = reconexão (devolve os dados já cadastrados). Jogador novo passa pela validação do nome e da sala e recebe cor e id novo
	@Override
	public synchronized void conectarJogador(ConectarRequest request, io.grpc.stub.StreamObserver<com.xadrez.grpc.ConectarResponse> responseObserver) {
		
		//monta o jogador com o que o cliente mandou (o id só vale para reconexão; o jogador novo recebe um id novo mais abaixo)
		Jogador jogador = new Jogador(request.getIdJogador().trim(), request.getNomeJogador().trim());

		//reconexão: o id já é de um jogador da partida
		if(partida.verificaReconexao(jogador)) {
			//pega a cor do jogador que se reconectou
			Map.Entry<CorPecas, Jogador> jogadorReconectado = partida.getJogador(jogador);
			//usa o nome que já estava cadastrado (o nome enviado agora é ignorado)
			jogador.setNome(jogadorReconectado.getValue().getNome());
			CorPecas corPeca = jogadorReconectado.getKey();
			//responde com sucesso
			responderServidorSucessoConectarJogador(responseObserver, jogador, corPeca);
			return;
		} else {
			//jogador novo: confere se o nome é válido (3 a 15 caracteres e único)
			if(!jogador.verificaNome(partida.listaJogadores())) {
				//responde com erro
				responderServidorErroConectarJogador(responseObserver, "3", 
"Nome inválido, insira um nome válido e tente novamente. Nome deve ser maior ou igual a 3 carácteres, menor ou igual a 15 carácteres, não pode estar vazio e deve ser único.");
				return;
			}
			
			//confere se ainda há vaga na partida
			if(partida.verificaSeJogoEstaCheio()) {
				//responde com erro
				responderServidorErroConectarJogador(responseObserver, "9", "Jogo está cheio! Tente novamente mais tarde");
				return;
			}
			
			//há vaga: escolhe a cor e coloca o jogador na partida
			
			//cores livres (a primeira da lista é a escolhida: quem entra primeiro fica com as brancas)
			ArrayList<CorPecas> coresDisponiveis = partida.corDisponivel();
			CorPecas corPecaEscolhida = coresDisponiveis.get(0);
			partida.adicionarCorPecasJogadores(corPecaEscolhida, jogador);
			//o servidor sempre gera o id do jogador novo (o id enviado pelo cliente é ignorado)
			jogador.setId(UUID.randomUUID().toString());
			//responde com sucesso
			responderServidorSucessoConectarJogador(responseObserver, jogador, corPecaEscolhida);
			return;
		}
	}

	//recebe uma jogada. Erros de contexto (partida, jogador, estado, vez) viram erro gRPC; jogada ilegal vira resposta normal com jogadaValida = false e o motivo. Se for aprovada, move a peça e passa a vez
	@Override 
	public synchronized void enviarJogada(JogadaRequest request, io.grpc.stub.StreamObserver<com.xadrez.grpc.JogadaResponse> responseObserver) {
		//1. a partida informada existe?
		if(!partida.getIdPartida().equals(request.getIdPartida())) {
			responseObserver.onError(Status.NOT_FOUND.withDescription("ID da partida enviado não corresponde com o ID da partida registrado").asRuntimeException()); 
			return;
		}
		
		//2. o jogador pertence à partida? (guarda a cor dele para as conferências seguintes)
		CorPecas corJogador = partida.getCorJogadorPeloId(request.getIdJogador());					
		if(corJogador == null) {
			responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Jogador não encontrado!").asRuntimeException()); 
			return;
		}
		
		//3. a partida está em andamento?
		if(!partida.getEstadoDaPartida().equals(EstadoDaPartida.EM_ANDAMENTO)) {
			responseObserver.onError(Status.FAILED_PRECONDITION.withDescription("Partida não está em andamento!").asRuntimeException()); 
			return;
		}
		
		//4. é a vez desse jogador?
		if(!partida.getTurno().equals(corJogador)) {
			responseObserver.onError(Status.FAILED_PRECONDITION.withDescription("Não é sua vez").asRuntimeException()); 
			return;
		}
		
		//5. converte os textos de origem e destino em posições (texto inválido = jogada recusada)
		Posicao posicaoOrigem;
		Posicao posicaoDestino;
		
		try {
			posicaoOrigem = new Posicao(request.getPosicaoOrigem());
			posicaoDestino = new Posicao(request.getPosicaoDestino());
		} catch (IllegalArgumentException exception) {
			JogadaResponse mensagem = JogadaResponse.newBuilder()
					.setJogadaValida(false)
					.setMensagemRetorno(exception.getMessage())
					.build();
			responseObserver.onNext(mensagem);
			responseObserver.onCompleted();
			return;
		}
		
		//6. existe peça na origem e ela é da cor do jogador?
		if(partida.getPecaNaCasa(posicaoOrigem) == null || !partida.getPecaNaCasa(posicaoOrigem).getCor().equals(corJogador)) {
			JogadaResponse mensagem = JogadaResponse.newBuilder()
					.setJogadaValida(false)
					.setMensagemRetorno("A cor da peça para mover deve ser a mesma cor do jogador!")
					.build();
			responseObserver.onNext(mensagem);
			responseObserver.onCompleted();
			return;
		}
		
		//7. o movimento é permitido pelas regras da peça?
		if(!partida.validaMovimento(posicaoOrigem, posicaoDestino)) {
			JogadaResponse mensagem = JogadaResponse.newBuilder()
					.setJogadaValida(false)
					.setMensagemRetorno("Movimento inválido!")
					.build();
			responseObserver.onNext(mensagem);
			responseObserver.onCompleted();
			return;
		}
		
		boolean eRoque = partida.verificaRoque(posicaoOrigem, posicaoDestino);
		
		//8. faz o movimento (o tabuleiro ainda recusa destino com peça da mesma cor)
		if(!partida.moverOuCapturar(posicaoOrigem, posicaoDestino)) {
			JogadaResponse mensagem = JogadaResponse.newBuilder()
					.setJogadaValida(false)
					.setMensagemRetorno("Não pode mover uma peça para uma casa que já tenha uma peça da mesma cor!")
					.build();
			responseObserver.onNext(mensagem);
			responseObserver.onCompleted();
			return;
		}
		
		if(eRoque) partida.moverTorreRoque(posicaoOrigem, posicaoDestino);
		
		if(partida.verificaPromocao(posicaoDestino)) partida.promocao(posicaoDestino, request.getPromocao());
		
		//9. jogada feita: passa a vez e responde com sucesso
		partida.trocarTurno();
		
		if(partida.verificaXequeMate()) {
			if(partida.getTurno().equals(CorPecas.PRETAS)) partida.fimDeJogo(Resultado.XEQUE_MATE_BRANCAS);
			else partida.fimDeJogo(Resultado.XEQUE_MATE_PRETAS);
		} else if(partida.verificaAfogamento()) partida.fimDeJogo(Resultado.EMPATE_AFOGAMENTO);
		
		
		JogadaResponse mensagem = JogadaResponse.newBuilder()
				.setJogadaValida(true)
				.setMensagemRetorno("Jogada feita!")
				.build();
		responseObserver.onNext(mensagem);
		responseObserver.onCompleted();
		return;	
		
		
	}

	//devolve o estado da partida: texto das peças, de quem é a vez e o estado (aguardando, em andamento ou concluída)
	@Override
	public synchronized void obterEstadoDoJogo(EstadoRequest request, io.grpc.stub.StreamObserver<com.xadrez.grpc.EstadoResponse> responseObserver) {
		if(!partida.getIdPartida().equals(request.getIdPartida())) {
			responseObserver.onError(Status.NOT_FOUND.withDescription("ID da partida enviado não corresponde com o ID da partida registrado").asRuntimeException()); 
			return;
		}
		
		//enquanto a partida não começou o turno é null, então envia "NENHUM"
		EstadoResponse mensagem = EstadoResponse.newBuilder()
				.setPosicaoPecas(partida.getPecasTabuleiro())
				.setTurnoAtual(partida.getTurno() == null ? "NENHUM" : partida.getTurno().toString())
				.setEstadoJogo(partida.getEstadoDaPartida().toString())
				.setResultado(partida.getResultado() == null ? "" : partida.getResultado().toString())
				.build();
		responseObserver.onNext(mensagem);
		responseObserver.onCompleted();
	}

	//---- métodos de apoio (resposta da conexão de jogadores) ----
	//responde com erro gRPC. Código "9" (sala cheia) vira FAILED_PRECONDITION, "3" (nome inválido) vira INVALID_ARGUMENT e qualquer outro vira UNKNOWN
	public void responderServidorErroConectarJogador(io.grpc.stub.StreamObserver<com.xadrez.grpc.ConectarResponse> responseObserver, String codigo, String descricao) {
		//erro que será enviado ao cliente
		Throwable excecao;
		//escolhe o status gRPC conforme o código
		switch(codigo) {
			case "9":
				//sala cheia
				excecao = Status.FAILED_PRECONDITION.withDescription(descricao).asRuntimeException(); 
				break;
			case "3":
				//nome inválido
				excecao = Status.INVALID_ARGUMENT.withDescription(descricao).asRuntimeException(); 
				break;
			default:
				//qualquer outro código
				excecao = Status.UNKNOWN.withDescription(descricao).asRuntimeException(); 
		}
		//envia o erro e encerra a chamada
		responseObserver.onError(excecao);
	}

	//responde com sucesso: devolve o id, a partida, o nome e a cor do jogador
	public void responderServidorSucessoConectarJogador(io.grpc.stub.StreamObserver<com.xadrez.grpc.ConectarResponse> responseObserver, Jogador jogador, CorPecas corPecaEscolhida) {
		//monta a resposta
		ConectarResponse mensagem = ConectarResponse.newBuilder()
				.setIdJogador(jogador.getId())
				.setIdPartida(partida.getIdPartida())
				.setNomeJogador(jogador.getNome())
				.setCorPecas(corPecaEscolhida.toString())
				.build();
				
		//envia a resposta
		responseObserver.onNext(mensagem);
		//encerra a chamada
		responseObserver.onCompleted();
	}
}