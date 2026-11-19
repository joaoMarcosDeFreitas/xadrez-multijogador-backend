package com.xadrez.program;

import com.xadrez.grpc.XadrezServiceImpl;

import io.grpc.ServerBuilder;

//ponto de entrada: liga o servidor gRPC
public class Main {
	//liga o servidor e fica esperando conexões até ele ser encerrado
	public static void main(String[] args) {
		try {
			System.out.println("Servidor rodando na porta 50040 🚀");
			//cria o servidor na porta 50040 com o serviço do xadrez, liga e espera até ser encerrado
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
