package util;

public class Util {
	public static void log(Object message){
		StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
		//System.out.print(caller + " ");
		System.out.println(message);
	}

}
