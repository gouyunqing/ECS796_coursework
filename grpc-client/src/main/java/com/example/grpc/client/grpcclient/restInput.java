package com.example.grpc.client.grpcclient;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@RestController
public class restInput {    

	GRPCClientService grpcClientService;    
	@Autowired
    	public restInput(GRPCClientService grpcClientService) {
        	this.grpcClientService = grpcClientService;
    	}    
	
		public static boolean isPowerOfTwo(int n) {
		    return n>0 && (n&(n-1)) == 0;
		}
	@PostMapping("/input")
    	public String input(@RequestBody String input) {
		String matrix[] = input.split("\\|\\|");
		String ma = matrix[0];
		String mb = matrix[1];
		String rowsA[] = ma.split("\\|");
		String rowsB[] = mb.split("\\|");
		String colomnsA[] = rowsA[0].split(",");
		String colomnsB[] = rowsB[0].split(",");
		
		if(rowsA.length != colomnsA.length || rowsB.length != colomnsB.length || rowsA.length != rowsB.length) {
			return "invalid matrix";
		}
		if(!isPowerOfTwo(rowsA.length) || !isPowerOfTwo(rowsB.length)) {
			return "invalid matrix";
		}
		int[][] A = new int[rowsA.length][colomnsA.length];
		int[][] B = new int[rowsB.length][colomnsB.length];
		for(int i=0;i<rowsA.length;i++) {
			String colA[] = rowsA[i].split(",");
			for(int j=0;j<colA.length;j++) {
				A[i][j] = Integer.valueOf(colA[j]);
			}
		}
		for(int i=0;i<rowsB.length;i++) {
			String colB[] = rowsB[i].split(",");
			for(int j=0;j<colB.length;j++) {
				B[i][j] = Integer.valueOf(colB[j]);
			}
		}
		long startTime = System.nanoTime();
		int C[][] = grpcClientService.multiplyScaling(A, B, A.length);
		long costTime = System.nanoTime() - startTime;
		StringBuffer buf = new StringBuffer();
		for(int i=0;i<C.length;i++) {
			for(int j=0;j<C[i].length;j++) {
				buf.append(C[i][j]);
				if(j!=C[i].length-1)
					buf.append(",");
			}
			buf.append("\r\n");
		}
		buf.append("Time cost: ");
		buf.append(costTime);
        	return buf.toString();
    	}
}
