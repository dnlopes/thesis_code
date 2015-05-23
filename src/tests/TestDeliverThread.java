package tests;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by dnlopes on 23/05/15.
 */
public class TestDeliverThread
{

	public static void main(String[] args) throws InterruptedException {
		Runnable r = new Updater();
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(r, 0, 3, TimeUnit.SECONDS);
	}


	private static class Updater implements Runnable {

		@Override
		public void run() {
			System.out.println("3 seconds passed");
		}
	}

}
