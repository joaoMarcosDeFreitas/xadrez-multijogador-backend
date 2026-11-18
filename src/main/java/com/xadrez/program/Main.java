package com.xadrez.program;

import com.xadrez.grpc.XadrezServiceImpl;

import io.grpc.ServerBuilder;

//ponto de entrada: liga o servidor gRPC
public class Main {

	public static void main(String[] args) {
		try {
			System.out.println("Servidor rodando na porta 50040 🚀");
			//sobe o servidor na porta 50040 com o serviço do xadrez e fica esperando conexões
			ServerBuilder.forPort(50040)
			.addService(new XadrezServiceImpl())
			.build()
			.start()
			.awaitTermination();
			
		} catch(Exception e) {
			//mostra o erro se o servidor não conseguir subir
			System.out.println(e.getMessage().toString());
		}
	}

}
