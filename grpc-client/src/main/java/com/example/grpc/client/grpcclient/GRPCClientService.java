package com.example.grpc.client.grpcclient;

import com.example.grpc.server.grpcserver.Matrix;
import com.example.grpc.server.grpcserver.MRequest;
import com.example.grpc.server.grpcserver.MReply;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc.MatrixServiceBlockingStub;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc.MatrixServiceStub;
import com.example.grpc.server.grpcserver.PingRequest;
import com.example.grpc.server.grpcserver.PongResponse;
import com.example.grpc.server.grpcserver.PingPongServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.util.ArrayList;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
@Service
public class GRPCClientService {
	public static int MAX = 4;
	public static final int MAXSERVERMUNBER = 2;
	static long ddl = 400000000L;
	ManagedChannel[] channel;
	MatrixServiceBlockingStub[] stub;
	Thread[] t;
	String[] ipaddress = new String[]{"3.87.57.153",
										"54.221.76.41",
										"localhost",
										"localhost",
										"localhost",
										"localhost",
										"localhost",
										"localhost"};
	

	public String ping() {
    	ManagedChannel channel = ManagedChannelBuilder.forAddress("3.87.40.121", 19090)
            .usePlaintext()
            .build();        
	PingPongServiceGrpc.PingPongServiceBlockingStub stub
            = PingPongServiceGrpc.newBlockingStub(channel);        
	PongResponse helloResponse = stub.ping(PingRequest.newBuilder()
            .setPing("")
            .build());        
	channel.shutdown();        
	return helloResponse.getPong();
}

	static int[][] addBlock(int A[][], int B[][], MatrixServiceBlockingStub stub){
        Matrix.Builder m1 = Matrix.newBuilder();
        for(int i=0;i<MAX;i++){
                Matrix.Row.Builder r = Matrix.Row.newBuilder();
                for(int j=0;j<MAX;j++){
                        r.addNum(A[i][j]);
                }
                m1.addRows(r);
        }
        Matrix.Builder m2 = Matrix.newBuilder();
        for(int i=0;i<MAX;i++){
                Matrix.Row.Builder r = Matrix.Row.newBuilder();
                for(int j=0;j<MAX;j++){
                        r.addNum(B[i][j]);
                }
                m2.addRows(r);
        }
        MReply ans = stub.add(MRequest.newBuilder().setA(m1).setB(m2).build());

        int C[][] = new int[MAX][MAX];
        for(int i=0;i<MAX;i++){
                for(int j=0;j<MAX;j++){
                        C[i][j] = ans.getC().getRows(i).getNum(j);
                }
        }
        return C;
	}
	static int[][] multiplyBlock(int A[][], int B[][], MatrixServiceBlockingStub stub){
        Matrix.Builder m1 = Matrix.newBuilder();
        for(int i=0;i<MAX;i++){
                Matrix.Row.Builder r = Matrix.Row.newBuilder();
                for(int j=0;j<MAX;j++){
                        r.addNum(A[i][j]);
                        }
                m1.addRows(r);
                }
        Matrix.Builder m2 = Matrix.newBuilder();
        for(int i=0;i<MAX;i++){
                Matrix.Row.Builder r = Matrix.Row.newBuilder();
                for(int j=0;j<MAX;j++){
                        r.addNum(B[i][j]);
                }
                m2.addRows(r);
        }
        MReply ans = stub.multiply(MRequest.newBuilder().setA(m1).setB(m2).build());
        int C[][] = new int[MAX][MAX];
        for(int i=0;i<MAX;i++){
                for(int j=0;j<MAX;j++){
                        C[i][j] = ans.getC().getRows(i).getNum(j);
                }
        }
        return C;
	}

	public int[][] multiplyScaling(int A[][], int B[][], int size){
		int nos = footprinting(size);
		System.out.println("number of server: "+nos);
		channel = new ManagedChannel[nos];
		stub = new MatrixServiceBlockingStub[nos];
		t = new Thread[12];
		for(int i=0;i<nos;i++) {
			channel[i] = ManagedChannelBuilder.forAddress(ipaddress[i],19090).usePlaintext().build();
			stub[i] = MatrixServiceGrpc.newBlockingStub(channel[i]);
		}
		int C[][] = multiplyMatrixBlock(A,B,size);
		return C;
	}
	
	private int footprinting(int size) {
		ManagedChannel channel = ManagedChannelBuilder.forAddress(ipaddress[0],19090).usePlaintext().build();
	    MatrixServiceBlockingStub stub = MatrixServiceGrpc.newBlockingStub(channel);
		int numofserver,numofcall=1,p;
		double a = (double)size;
		int A[][] = {{1,2,0,0},{3,4,0,0},{0,0,0,0},{0,0,0,0}};
		int B[][] = {{1,2,0,0},{3,4,0,0},{0,0,0,0},{0,0,0,0}};
		long startTime = System.nanoTime();
		int C[][] = multiplyBlock(A,B,stub);
		long costTime = System.nanoTime() - startTime;
		for(p=2;;p++) {
			a = a/2;
			if(a == 2)
				break;
		}
		for(;p>1;p--) {
			numofcall = numofcall*8+4;
		}
		numofserver = (int)(costTime * numofcall / ddl + 1);
		System.out.println("cost time: "+costTime);
		if(numofserver > MAXSERVERMUNBER)
			return MAXSERVERMUNBER;
		return numofserver;
	}
	
    private int[][] multiplyMatrixBlock( int A[][], int B[][], int size) {
    	int nos = stub.length;
    	int bSize= size/2;
    	int[][] A1 = new int[size][size];
    	int[][] A2 = new int[size][size];
    	int[][] A3 = new int[size][size];
    	int[][] B1 = new int[size][size];
    	int[][] B2 = new int[size][size];
    	int[][] B3 = new int[size][size];
    	int[][] C1 = new int[size][size];
    	int[][] C2 = new int[size][size];
    	int[][] C3 = new int[size][size];
    	int[][] D1 = new int[size][size];
    	int[][] D2 = new int[size][size];
    	int[][] D3 = new int[size][size];
    	int[][] res= new int[size][size];
    	for (int i = 0; i < bSize; i++) 
        { 
            for (int j = 0; j < bSize; j++)
            {
                A1[i][j]=A[i][j];
                A2[i][j]=B[i][j];
            }
        }
    	for (int i = 0; i < bSize; i++) 
        { 
            for (int j = bSize; j < size; j++)
            {
                B1[i][j-bSize]=A[i][j];
                B2[i][j-bSize]=B[i][j];
            }
        }
    	for (int i = bSize; i < size; i++) 
        { 
            for (int j = 0; j < bSize; j++)
            {
                C1[i-bSize][j]=A[i][j];
                C2[i-bSize][j]=B[i][j];
            }
        } 
    	for (int i = bSize; i < size; i++) 
        { 
            for (int j = bSize; j < size; j++)
            {
                D1[i-bSize][j-bSize]=A[i][j];
                D2[i-bSize][j-bSize]=B[i][j];
            }
        }  
    	if(bSize == 4) {
    		ArrayList<int[][]> mlist1 = new ArrayList<int[][]>(8);
    		ArrayList<int[][]> mlist2 = new ArrayList<int[][]>(8);
    		ArrayList<int[][]> mlist3 = new ArrayList<int[][]>(8);
    		ArrayList<int[][]> mlist4 = new ArrayList<int[][]>(4);
    		mlist1.add(A1);
    		mlist1.add(B1);
    		mlist1.add(A1);
    		mlist1.add(B1);
    		mlist1.add(C1);
    		mlist1.add(D1);
    		mlist1.add(C1);
    		mlist1.add(D1);
    		mlist2.add(A2);
    		mlist2.add(C2);
    		mlist2.add(B2);
    		mlist2.add(D2);
    		mlist2.add(A2);
    		mlist2.add(C2);
    		mlist2.add(B2);
    		mlist2.add(D2);
	    		for(int i=0;i<8;i++){
	    			int k=i;
	        		Runnable r = new Runnable() {
	        			public void run() {
	        				mlist3.add(k,multiplyMatrixBlock(mlist1.get(k),mlist2.get(k),stub[k%nos]));
	        			}		
	        		};
	    			t[i] = new Thread(r);
	    		}
	    		for(int i=0;i<8;i++)
	    			t[i].start();
	    		for(int i=0;i<8;i+=2){
	    			int k=i;
	    			Runnable r = new Runnable() {
	        			public void run() {
	        				mlist4.add(k/2,addBlock(mlist3.get(k),mlist3.get(k+1),stub[k%nos]));
	        			}		
	        		};
	    			t[i] = new Thread(r);
	    		}   
	    		for(int i=0;i<8;i+=2)
	    			t[i].start();
	    	A3=mlist4.get(0);
	    	B3=mlist4.get(1);
	    	C3=mlist4.get(2);
	    	D3=mlist4.get(3);
    	}else {
    		A3=addBlock(multiplyMatrixBlock(A1,A2,bSize),multiplyMatrixBlock(B1,C2,bSize), stub[1%nos]);
	    	B3=addBlock(multiplyMatrixBlock(A1,B2,bSize),multiplyMatrixBlock(B1,D2,bSize), stub[2%nos]);
	    	C3=addBlock(multiplyMatrixBlock(C1,A2,bSize),multiplyMatrixBlock(D1,C2,bSize), stub[3%nos]);
	    	D3=addBlock(multiplyMatrixBlock(C1,B2,bSize),multiplyMatrixBlock(D1,D2,bSize), stub[4%nos]);
    	}
    	for (int i = 0; i < bSize; i++) 
        { 
            for (int j = 0; j < bSize; j++)
            {
                res[i][j]=A3[i][j];
            }
        }
    	for (int i = 0; i < bSize; i++) 
        { 
            for (int j = bSize; j < size; j++)
            {
                res[i][j]=B3[i][j-bSize];
            }
        }
    	for (int i = bSize; i < size; i++) 
        { 
            for (int j = 0; j < bSize; j++)
            {
                res[i][j]=C3[i-bSize][j];
            }
        } 
    	for (int i = bSize; i < size; i++) 
        { 
            for (int j = bSize; j < size; j++)
            {
                res[i][j]=D3[i-bSize][j-bSize];
            }
        } /*
    	for (int i=0; i<size; i++)
    	{
    		for (int j=0; j<size;j++)
    		{
    			System.out.print(res[i][j]+" ");
    		}
    		System.out.println("");
    	}*/
    	return res;
    }
    static int[][] multiplyMatrixBlock( int A[][], int B[][], MatrixServiceBlockingStub stub) 
    {
    	int bSize=2;
    	int[][] A1 = new int[MAX][MAX];
    	int[][] A2 = new int[MAX][MAX];
    	int[][] A3 = new int[MAX][MAX];
    	int[][] B1 = new int[MAX][MAX];
    	int[][] B2 = new int[MAX][MAX];
    	int[][] B3 = new int[MAX][MAX];
    	int[][] C1 = new int[MAX][MAX];
    	int[][] C2 = new int[MAX][MAX];
    	int[][] C3 = new int[MAX][MAX];
    	int[][] D1 = new int[MAX][MAX];
    	int[][] D2 = new int[MAX][MAX];
    	int[][] D3 = new int[MAX][MAX];
    	int[][] res= new int[MAX][MAX];
    	for (int i = 0; i < bSize; i++) 
        { 
            for (int j = 0; j < bSize; j++)
            {
                A1[i][j]=A[i][j];
                A2[i][j]=B[i][j];
            }
        }
    	for (int i = 0; i < bSize; i++) 
        { 
            for (int j = bSize; j < MAX; j++)
            {
                B1[i][j-bSize]=A[i][j];
                B2[i][j-bSize]=B[i][j];
            }
        }
    	for (int i = bSize; i < MAX; i++) 
        { 
            for (int j = 0; j < bSize; j++)
            {
                C1[i-bSize][j]=A[i][j];
                C2[i-bSize][j]=B[i][j];
            }
        } 
    	for (int i = bSize; i < MAX; i++) 
        { 
            for (int j = bSize; j < MAX; j++)
            {
                D1[i-bSize][j-bSize]=A[i][j];
                D2[i-bSize][j-bSize]=B[i][j];
            }
        }  
    	A3=addBlock(multiplyBlock(A1,A2,stub),multiplyBlock(B1,C2,stub),stub);
    	B3=addBlock(multiplyBlock(A1,B2,stub),multiplyBlock(B1,D2,stub),stub);
    	C3=addBlock(multiplyBlock(C1,A2,stub),multiplyBlock(D1,C2,stub),stub);
    	D3=addBlock(multiplyBlock(C1,B2,stub),multiplyBlock(D1,D2,stub),stub);
    	for (int i = 0; i < bSize; i++) 
        { 
            for (int j = 0; j < bSize; j++)
            {
                res[i][j]=A3[i][j];
            }
        }
    	for (int i = 0; i < bSize; i++) 
        { 
            for (int j = bSize; j < MAX; j++)
            {
                res[i][j]=B3[i][j-bSize];
            }
        }
    	for (int i = bSize; i < MAX; i++) 
        { 
            for (int j = 0; j < bSize; j++)
            {
                res[i][j]=C3[i-bSize][j];
            }
        } 
    	for (int i = bSize; i < MAX; i++) 
        { 
            for (int j = bSize; j < MAX; j++)
            {
                res[i][j]=D3[i-bSize][j-bSize];
            }
        } 
    	return res;
    }
	static void displayBlock(int C[][]){
        for(int i=0;i<C.length;i++){
                System.out.print("\n[");
                for(int j=0;j<C[i].length;j++){
                        if(j!=C.length-1)
                               System.out.print(C[i][j]+",");
                        else
                               System.out.print(C[i][j]);
                }
                System.out.print("]\n");
        }
	}
}
