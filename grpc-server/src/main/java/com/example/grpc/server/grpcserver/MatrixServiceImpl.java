package com.example.grpc.server.grpcserver;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class MatrixServiceImpl extends MatrixServiceGrpc.MatrixServiceImplBase{
        public static int MAX = 4;
        public void add(MRequest request, StreamObserver<MReply> reply){
                int A[][] = new int[MAX][MAX];
                int B[][] = new int[MAX][MAX];
                int C[][] = new int[MAX][MAX];
                for(int i=0;i<MAX;i++){
                        for(int j=0;j<MAX;j++){
                                A[i][j] = request.getA().getRows(i).getNum(j);
                        }
                }
                for(int i=0;i<MAX;i++){
                        for(int j=0;j<MAX;j++){
                                B[i][j] = request.getB().getRows(i).getNum(j);
                        }
                }
                C = addBlock(A,B);
                Matrix.Builder m3 = Matrix.newBuilder();
                System.out.println("Request received from client:add");
                for(int i=0;i<MAX;i++){
                        Matrix.Row.Builder r = Matrix.Row.newBuilder();
                        for(int j=0;j<MAX;j++){
                                r.addNum(C[i][j]);
                        }
                        m3.addRows(r);
                }
                MReply response=MReply.newBuilder().setC(m3).build();
                reply.onNext(response);
                reply.onCompleted();
        }

        public void multiply(MRequest request, StreamObserver<MReply> reply){
                int A[][] = new int[MAX][MAX];
                int B[][] = new int[MAX][MAX];
                int C[][] = new int[MAX][MAX];
                for(int i=0;i<MAX;i++){
                        for(int j=0;j<MAX;j++){
                                A[i][j] = request.getA().getRows(i).getNum(j);
                        }
                }
                for(int i=0;i<MAX;i++){
                        for(int j=0;j<MAX;j++){
                                B[i][j] = request.getB().getRows(i).getNum(j);
                        }
                }
                C = multiplyBlock(A,B);
                Matrix.Builder m3 = Matrix.newBuilder();
                System.out.println("Request received from client:multiply");
                for(int i=0;i<MAX;i++){
                        Matrix.Row.Builder r = Matrix.Row.newBuilder();
                        for(int j=0;j<MAX;j++){
                               r.addNum(C[i][j]).build();
                        }
                        m3.addRows(r);
                }
                MReply response=MReply.newBuilder().setC(m3).build();
                reply.onNext(response);
                reply.onCompleted();
        }

        static int[][] addBlock(int A[][], int B[][]){
                int C[][]= new int[MAX][MAX];
                for (int i=0;i<C.length;i++){
                        for (int j=0;j<C.length;j++){
                                C[i][j]=A[i][j]+B[i][j];
                        }
                }
                return C;
        }

        static int[][] multiplyBlock(int A[][], int B[][]){
                int C[][]= new int[MAX][MAX];
                C[0][0]=A[0][0]*B[0][0]+A[0][1]*B[1][0];
                C[0][1]=A[0][0]*B[0][1]+A[0][1]*B[1][1];
                C[1][0]=A[1][0]*B[0][0]+A[1][1]*B[1][0];
                C[1][1]=A[1][0]*B[0][1]+A[1][1]*B[1][1];
                return C;
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