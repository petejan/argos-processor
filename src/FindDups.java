import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class FindDups
{
	public static class LineReps
	{
		String line;
		int reps;
		
		public LineReps(int r, String l)
		{
			line = l;
			reps = r;
		}
	}

	public static void main(String[] args)
	{
		try
		{
			//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			
			HashMap <String, LineReps> uniq = new HashMap <String, LineReps>();
			
		    for(String line; (line = br.readLine()) != null; ) 
		    {
		    	int repsPos = line.indexOf("REPS=");
		    	if ((repsPos > 0) && (line.length() > (repsPos + 10)))
		    	{
			    	int reps = Integer.parseInt(line.substring(repsPos + 5, repsPos + 7).trim());
			    	
			    	//System.out.println("REPS " + reps + " " + line);
			    	
			    	LineReps r = new LineReps(reps, line);
			    	
			    	String key = line.substring(repsPos+8);
			    	LineReps i = uniq.put(key, r);
			    	if (i != null)
			    	{
			    		i.reps = i.reps + r.reps;
			    		uniq.remove(key);
			    		uniq.put(key, i);
				    	//System.out.println("i = " + i.reps);
			    	}			    	
		    	}
		    }
		    
		    br.close();
		    
		    for(String s : uniq.keySet())
		    {
		    	LineReps l = uniq.get(s);
		    	if (l.reps >= 3)
		    	{
		    		System.out.println(l.line.replaceAll("REPS=[0-9]", "REPS="+l.reps));
		    	}
		    }
		}	
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
		}
	}
}
