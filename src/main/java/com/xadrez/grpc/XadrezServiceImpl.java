package com.xadrez.grpc;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.xadrez.models.Jogador;
import com.xadrez.models.Partida;
import com.xadrez.models.Peca;
import com.xadrez.models.Posicao;
import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.EstadoDaPartidaEnum.EstadoDaPartida;
import com.xadrez.models.enums.PecaEnum.TipoPeca;
import com.xadrez.models.enums.ResultadoEnum.Resultado;

import io.grpc.Status;

//serviço gRPC do xadrez: recebe as chamadas dos clientes e usa a Partida para responder
//os três métodos das chamadas são synchronized: só uma chamada por vez mexe no estado, o que evita race condition
public class XadrezServiceImpl extends XadrezServiceGrpc.XadrezServiceImplBase {
	//a partida do servidor: é criada quando o servidor liga e só existe uma por servidor
	private final Partida partida = new Partida(UUID.randomUUID().toString());

	//---- chamadas dos clientes (uma por método do .proto) ----
	//conecta um jogador. Id já conhecido = reconexão (devolve os dados já cadastrados). Jogador novo passa pela validação do nome e da sala e recebe cor e id novo
	@Override
	public synchronized void conectarJogador(ConectarRequest request, io.grpc.stub.StreamObserver<com.xadrez.grpc.ConectarResponse> responseObserver) {
		try {
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
					responderErro(responseObserver, Status.INVALID_ARGUMENT, "Nome inválido, insira um nome válido e tente novamente. Nome deve ser maior ou igual a 3 carácteres, menor ou igual a 15 carácteres, não pode estar vazio e deve ser único.");
					return;
				}
				
				//confere se ainda há vaga na partida
				if(partida.verificaSeJogoEstaCheio()) {
					//responde com erro
					responderErro(responseObserver, Status.FAILED_PRECONDITION, "Jogo está cheio! Tente novamente mais tarde");
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
		} catch(Exception exception) {
			//qualquer erro inesperado vira INTERNAL (erro do servidor)
			responderErro(responseObserver, Status.INTERNAL, "Erro interno do servidor!");
		}
	}

	//recebe uma jogada. Erros de contexto (partida, jogador, estado, vez) viram erro gRPC; jogada ilegal vira resposta normal com jogadaValida = false e o motivo
	//se for aprovada: move a peça (e a torre, no roque), promove o peão, atualiza as contagens de empate, passa a vez e confere o fim de jogo
	@Override 
	public synchronized void enviarJogada(JogadaRequest request, io.grpc.stub.StreamObserver<com.xadrez.grpc.JogadaResponse> responseObserver) {
		try {
			//1. a partida informada existe?
			if(!partida.getIdPartida().equals(request.getIdPartida())) {
				responderErro(responseObserver, Status.NOT_FOUND, "ID da partida enviado não corresponde com o ID da partida registrado");
				return;
			}
			
			//2. o jogador pertence à partida? (guarda a cor dele para as conferências seguintes)
			CorPecas corJogador = partida.getCorJogadorPeloId(request.getIdJogador());					
			if(corJogador == null) {
				responderErro(responseObserver, Status.PERMISSION_DENIED, "Jogador não encontrado!");
				return;
			}
			
			//3. a partida está em andamento?
			if(!partida.getEstadoDaPartida().equals(EstadoDaPartida.EM_ANDAMENTO)) {
				responderErro(responseObserver, Status.FAILED_PRECONDITION, "Partida não está em andamento!");
				return;
			}
			
			//4. é a vez desse jogador?
			if(!partida.getTurno().equals(corJogador)) {
				responderErro(responseObserver, Status.FAILED_PRECONDITION, "Não é sua vez");
				return;
			}
			
			//5. converte os textos de origem e destino em posições (texto inválido = jogada recusada)
			Posicao posicaoOrigem;
			Posicao posicaoDestino;
			
			try {
				posicaoOrigem = new Posicao(request.getPosicaoOrigem());
				posicaoDestino = new Posicao(request.getPosicaoDestino());
			} catch (IllegalArgumentException exception) {
				responderJogadaRecusada(responseObserver, exception.getMessage());
				return;
			}
			
			//6. existe peça na origem e ela é da cor do jogador?
			if(partida.getPecaNaCasa(posicaoOrigem) == null || !partida.getPecaNaCasa(posicaoOrigem).getCor().equals(corJogador)) {
				responderJogadaRecusada(responseObserver, "A cor da peça para mover deve ser a mesma cor do jogador!");
				return;
			}
			
			//7. o lance é permitido? (formato da peça, rei sem xeque depois dele, ou roque seguro)
			if(!partida.validaMovimento(posicaoOrigem, posicaoDestino)) {
				responderJogadaRecusada(responseObserver, "Movimento inválido!");
				return;
			}
			
			//guarda ANTES de mover se o lance é um roque (depois do movimento o "já mexeu" do rei muda e a pergunta deixa de valer)
			boolean eRoque = partida.verificaRoque(posicaoOrigem, posicaoDestino);
			
			//guarda ANTES de mover se houve captura ou movimento de peão (usado na regra dos 50 lances; depois do movimento a origem fica vazia)
			boolean temPecaNoDestinoOuEPeaoMovendo = false;
			Peca pecaNaCasaOrigem = partida.getPecaNaCasa(posicaoOrigem);
			Peca pecaNaCasaDestino = partida.getPecaNaCasa(posicaoDestino);
			
			if(pecaNaCasaOrigem != null && pecaNaCasaOrigem.getTipoPeca().equals(TipoPeca.PEAO)) temPecaNoDestinoOuEPeaoMovendo = true;
			if(pecaNaCasaDestino != null) temPecaNoDestinoOuEPeaoMovendo = true;
			
			//8. faz o movimento (o tabuleiro ainda recusa destino com peça da mesma cor)
			if(!partida.moverOuCapturar(posicaoOrigem, posicaoDestino)) {
				responderJogadaRecusada(responseObserver, "Não pode mover uma peça para uma casa que já tenha uma peça da mesma cor!");
				return;
			}
			
			//se era roque, move também a torre
			if(eRoque) partida.moverTorreRoque(posicaoOrigem, posicaoDestino);
			
			//peão que chegou à última linha é promovido (antes de passar a vez, para o xeque-mate e o afogamento já enxergarem a peça nova)
			if(partida.verificaPromocao(posicaoDestino)) partida.promocao(posicaoDestino, request.getPromocao());
			
			//atualiza a contagem dos 50 lances
			partida.contarJogada(temPecaNoDestinoOuEPeaoMovendo);
			
			//9. jogada feita: passa a vez
			partida.trocarTurno();
			
			//anota a posição (depois de passar a vez, porque a vez faz parte dela)
			partida.registrarRepeticao();
			
			//confere se a partida acabou, nesta ordem: xeque-mate (a cor da vez levou mate, então a outra venceu), afogamento, material insuficiente, 50 lances, repetição
			if(partida.verificaXequeMate()) {
				if(partida.getTurno().equals(CorPecas.PRETAS)) partida.fimDeJogo(Resultado.XEQUE_MATE_BRANCAS);
				else partida.fimDeJogo(Resultado.XEQUE_MATE_PRETAS);
			} 
			else if(partida.verificaAfogamento()) partida.fimDeJogo(Resultado.EMPATE_AFOGAMENTO);
			else if(partida.verificaMaterialInsuficiente()) partida.fimDeJogo(Resultado.EMPATE_MATERIAL_INSUFICIENTE);
			else if(partida.verificaLimiteDeJogadas()) partida.fimDeJogo(Resultado.EMPATE_50_LANCES);
			else if(partida.verificaRepeticao()) partida.fimDeJogo(Resultado.EMPATE_REPETICAO);
			
			//jogada aceita (se ela encerrou a partida, os clientes descobrem pelo obterEstadoDoJogo)
			JogadaResponse mensagem = JogadaResponse.newBuilder()
					.setJogadaValida(true)
					.setMensagemRetorno("Jogada feita!")
					.build();
			responseObserver.onNext(mensagem);
			responseObserver.onCompleted();
			return;	
		} catch (Exception exception) {
			//qualquer erro inesperado vira INTERNAL (erro do servidor)
			responderErro(responseObserver, Status.INTERNAL, "Erro interno do servidor!");
			return;
		}
	}

	//devolve o estado da partida: texto das peças, de quem é a vez, o estado (aguardando, em andamento ou concluída) e o resultado (vazio enquanto não terminou)
	@Override
	public synchronized void obterEstadoDoJogo(EstadoRequest request, io.grpc.stub.StreamObserver<com.xadrez.grpc.EstadoResponse> responseObserver) {
		try {
			//a partida informada existe?
			if(!partida.getIdPartida().equals(request.getIdPartida())) {
				responderErro(responseObserver, Status.NOT_FOUND, "ID da partida enviado não corresponde com o ID da partida registrado");
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
		} catch(Exception exception) {
			//qualquer erro inesperado vira INTERNAL (erro do servidor)
			responderErro(responseObserver, Status.INTERNAL, "Erro interno do servidor!");
			return;
		}
	}

	//---- métodos de apoio (respostas padronizadas) ----
	//responde com erro gRPC (o cliente recebe o status e a descrição) e encerra a chamada. Serve para qualquer método do serviço
	private void responderErro(io.grpc.stub.StreamObserver<?> responseObserver, Status status, String descricao) {
		responseObserver.onError(status.withDescription(descricao).asRuntimeException());
	}

	//responde que a jogada foi recusada: não é erro gRPC, é uma resposta normal com jogadaValida falso e o motivo
	private void responderJogadaRecusada(io.grpc.stub.StreamObserver<com.xadrez.grpc.JogadaResponse> responseObserver, String motivo) {
		JogadaResponse mensagem = JogadaResponse.newBuilder()
				.setJogadaValida(false)
				.setMensagemRetorno(motivo)
				.build();
		responseObserver.onNext(mensagem);
		responseObserver.onCompleted();
	}

	//responde com sucesso à conexão (ou reconexão): devolve o id, a partida, o nome e a cor do jogador
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