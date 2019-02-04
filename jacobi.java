/*Program to read in a matrix and perform the jacobi iteration
*
* Author: Simon Haile
* Date: May 19, 2017
*/
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.concurrent.BrokenBarrierException;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.lang.Math;
public class jacobi{

//private static CyclicBarrier barrier;
private static barr barrier;
private	static double[][] realGrid = new double[2048][2048];
private	static double[][] newGrid = new  double[2048][2048];
private	static double[][] outputGrid = new  double[2048][2048];
private final static int SIZE = 2048;
private static Integer NOTH = 0;
private static final double eps = 0.0001;
private static double[] maxdiffs;
private static int count =0;

	public static void main(String arg[]){

		try{
			NOTH = NOTH.parseInt(arg[0]);
		}
		catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Usage: Please enter a number of threads.");
			return;
		}
		loadGrid();
		maxdiffs= new double[NOTH];
		maxdiffs[0] = 5.0;
		
		runJacobi(NOTH);
		//System.out.println("outputGrid: [10][10]: "+outputGrid[1000][1000]);
		//System.out.println("realGrid: [10][10]: "+realGrid[10][10]);
		//System.out.println(realGrid[1][1]);
		System.out.println("difference b/n this and test: "+compareGrids());
		//System.out.println(count);
	}
	private static double maxDiff(){
		double max = 0;
		
		for(int i=0; i<NOTH; i++){
			
			if(maxdiffs[i] > max){
				max = maxdiffs[i];
			}
		}
		
		return max;
	}
	private static double compareGrids(){
		double difference = 0;
		for(int i=0; i<2048; i++){
			for(int j=0; j<2048; j++){
				if(Math.abs(outputGrid[i][j] - realGrid[i][j]) > difference){
					difference = Math.abs(outputGrid[i][j] - realGrid[i][j]);
					//System.out.println("difference at["+i+"]"+"["+j+"]: " + difference);
				}
				
			}
		}
		return difference;
	}
	private static void loadGrid(){
		BufferedReader bReader = null;
		FileReader fReader = null;
		BufferedReader boReader = null;
		FileReader foReader = null;

		try{
			fReader = new FileReader("input.mtx");
			bReader = new BufferedReader(fReader);
			foReader = new FileReader("output_new.mtx");
			boReader = new BufferedReader(foReader);


			String currInLine;
			String currOutLine;
			String[] tempi;
			String[] tempo;
			Double d = 0.0;
			Double dout = 0.0;
			for(int i = 0; i < 2048; i++){
				currInLine = bReader.readLine();
				currOutLine = boReader.readLine();
				tempi = currInLine.split(" ");
				tempo = currOutLine.split(" ");
				for(int j = 0; j < 2048; j++){
					realGrid[i][j] = d.parseDouble(tempi[j]);
					newGrid[i][j] = realGrid[i][j];
					outputGrid[i][j] = dout.parseDouble(tempo[j]);
				//	System.out.println(tempi[j]);
				}
			}

		}
		catch(IOException e){
			e.printStackTrace();
		}

	}
	private static void runJacobi(int NOTH){
		float[] S = new float[NOTH];
		int t = NOTH;
		int n = SIZE;
		Boolean MAXITER = true;
		int HEIGHT = n/NOTH;
		final long startTime = System.currentTimeMillis();
		// for(int i = 0; i < 2048; i++){
		// 	System.out.println(realGrid[0][i]);
		// }
		//barrier = new CyclicBarrier(NOTH);
		barrier = new barr(NOTH);
			Thread[] thds = new Thread[NOTH];
			for(int j=0; j<t; j++){
				final int J = j+1;
				//Definition of barrier

				thds[j] = new Thread(
					new Runnable() {
						public void run(){

						  //int firstRow = ((J-1)* HEIGHT)+1;
							// int lastRow = ((((J-1)* HEIGHT)+1) + HEIGHT)-1;

							 int firstRow = 1+ (((J-1) * 2046)/NOTH);
							 int lastRow = 1+  ( ( (J)*2046)/NOTH)  ;
							 System.out.println(J + ": firstRow: "+ firstRow);
							 System.out.println(J + ": lastRow: "+ lastRow);
							 double myDiff;
							 try{barrier.awaite();}
							 catch(InterruptedException i){return;}
							 //catch(BrokenBarrierException br){return;}
								//for(int d=1; d > 0; d--){
								while(maxDiff() > eps){
									
									for(int i=firstRow; i<lastRow; i++){
											
										for(int k=1; k<(n-1); k++)
										{
											newGrid[i][k] = ((realGrid[i-1][k] + realGrid[i+1][k] + realGrid[i][k-1] + realGrid[i][k+1]) * 0.25);
										}
									}
									try{barrier.awaite();}
	 							 	catch(InterruptedException i){return;}
	 							 	//catch(BrokenBarrierException br){return;}
									for(int i=firstRow; i<lastRow; i++){
										for(int k=1; k<(n-1); k++)
										{
											realGrid[i][k] = ((newGrid[i-1][k] + newGrid[i+1][k] + newGrid[i][k-1] + newGrid[i][k+1]) * 0.25);
										}
									}
									try{barrier.awaite();}
	 							 	catch(InterruptedException i){return;}//PRINT STACK TRACE AND EXIT
	 							 	
	 							 	//compute the maximum difference for my strip
	 							 	myDiff = 0;
	 							 	for(int i=firstRow; i<lastRow; i++){
	 							 		for(int k=1; k<(n-1); k++){
	 							 			if(Math.abs(realGrid[i][k] - newGrid[i][k]) > myDiff)
	 							 				myDiff = Math.abs(realGrid[i][k] - newGrid[i][k]);
	 							 		}
	 							 	}

	 							 	maxdiffs[J-1] = myDiff;
	 							 	try{barrier.awaite();}
	 							 	catch(InterruptedException i){return;}
	 							 	//catch(BrokenBarrierException br){return;}
								}
						}
					}
				);
				thds[j].start();
			}
			for(int s = 0; s < t; s++){
				try{ thds[s].join();}
				catch(InterruptedException i){return;}
			}
			final long endTime = System.currentTimeMillis();
			System.out.println("Execution time: "+ (endTime - startTime));
	}

}
