package original;

import java.util.ArrayList;
import java.util.List;

public class DemonThread {
	
	static class Task implements Runnable{
		private String name;
		private int time;
		public Task(String s,int t) {
			name=s;
			time=t;
		}
		public int getTime(){
			return time;
		}
		public void run() {
				try {//------------------------------------------main course of this thread. 
					
					Thread.sleep(1000);
					
					
					//------------------------------------------ end of this thread. 
				}catch(InterruptedException e){
					System.out.println(name+" is interrupted");
					return; //注意这里如果不return的话，线程还会继续执行，所以任务超时后在这里处理结果然后返回
				}
			System.out.println("task "+name+" finished successfully");
		}
	}
	
	static class Daemon implements Runnable{
		List<Runnable> tasks=new ArrayList<Runnable>();
		private Thread thread;
		private int time; // The runtime of the monitored thread. But at this point only one thread could be fucked
		
		public Daemon(Thread r,int t) {
			thread=r;time=t;
		}
		public void addTask(Runnable r){
				tasks.add(r);
		}
		
		@Override
		public void run() {
			while(true){
				try{
					Thread.sleep(time*1000);
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
					thread.interrupt();
			}
		}
	}
	
	public static void main(String[] args) {
			Task task1=new Task("one", 5);
			Thread t1=new Thread(task1);
			Daemon daemon=new Daemon(t1, 3);
			Thread daemoThread=new Thread(daemon);
			daemoThread.setDaemon(true);
			t1.start();
			daemoThread.start();
	}
}
		
