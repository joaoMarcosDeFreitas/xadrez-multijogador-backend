package com.xadrez.program;

import com.xadrez.grpc.XadrezServiceImpl;

import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.ServerBuilder;

//ponto de entrada: liga o servidor gRPC
public class Main {
	//liga o servidor e fica esperando conexões até ele ser encerrado. A porta pode ser informada como primeiro argumento do programa (no Eclipse: Run Configurations > Arguments)
	public static void main(String[] args) {
		//porta padrão: usada quando nenhum argumento é informado ou quando ele não é um número
		int porta = 50040;

		//se veio um argumento, tenta usá-lo como porta (conferir o tamanho evita erro de índice quando não vem nada)
		if(args.length > 0) {
			try {
				//converte o texto em número; texto que não é número cai no catch e a porta continua sendo a padrão
				porta = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				//avisa que o argumento não era um número
				System.out.println("Porta inválida, usando 50040");
			}
		}

		try {
			//aviso no console, com a porta que vai ser usada
			System.out.println("Servidor rodando na porta " + porta + " 🚀");
			//cria o servidor na porta escolhida, liga e espera até ser encerrado
			ServerBuilder.forPort(porta)
			//o serviço do xadrez (conectar, enviar jogada e obter estado)
			.addService(new XadrezServiceImpl())
			//reflexão gRPC: faz o servidor descrever os próprios métodos, para o Postman e outras ferramentas descobrirem sem importar o .proto
			.addService(ProtoReflectionService.newInstance())
			.build()
			.start()
			.awaitTermination();
		} catch(Exception e) {
			//mostra o erro se o servidor não conseguir subir (por exemplo, porta já em uso)
			System.out.println(e);
		}
	}
}
