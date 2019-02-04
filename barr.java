/**
*
* Implementation of a thead barrier using Java monitors.
* Author: Simon Haile
*/
import java.lang.InterruptedException;

public class barr  {
	private int NOTH;
	private int RESET;

	public barr(int NOTH){
		this.NOTH = NOTH;
		this.RESET = NOTH;
	}

	public synchronized void awaite()throws InterruptedException{
		NOTH --;
		if(NOTH == 0){
			NOTH = RESET;
			notifyAll();
		}
		else 
			wait();	
	}
}