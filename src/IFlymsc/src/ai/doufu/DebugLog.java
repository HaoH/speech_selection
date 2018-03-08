package ai.doufu;
import java.text.SimpleDateFormat;

public class DebugLog {
	
	public static Boolean show_tag = true;
	
	public static void Log(String tag,String log)
	{
		if(show_tag)
		    System.out.println(log);
	}
	
	public static void Log(String log)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		String date=dateFormat.format(new java.util.Date());
		if(show_tag)
		    System.out.println("<" + date + ">" + log);
	}
	
	public static boolean isEmpty(String string){
		if(string == null)
		{
			return true;
		}
		if(string.isEmpty())
		{
			return true;
		}
		return false;
	}
}
