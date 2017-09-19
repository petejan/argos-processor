import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Processor
{
	public Processor()
	{
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		//sensorList.add(new Sensor("CheckSum0", 0, 8));
		//sensorList.add(new Sensor("FormatReg", 8, 8));
		//sensorList.add(new Sensor(" Page", 14, 2));
		//sensorList.add(new Sensor(" NS", 10, 1));
		//sensorList.add(new Sensor("JulianDay", 16, 8));
		//sensorList.add(new Sensor("JulianSec", 24, 16));
		//sensorList.add(new Sensor("Lat", 40, 24, 0, 1.833465E-05, "%8.6f"));
		//sensorList.add(new Sensor("Lon", 64, 24, 0, 1.833465E-05, "%8.6f"));
		//sensorList.add(new Sensor("AirTemp", 208, 10, -35, 0.05));
		//sensorList.add(new Sensor("IceT", 230, 10, -35, 0.05));
		//sensorList.add(new Sensor("Bat", 224, 4, 4, 0.6));
		//sensorList.add(new Sensor("test32 ", 32, 8, 0, 1, "%3.0f"));
		//sensorList.add(new Sensor("test40 ", 40, 16, 0, 1, "%6.0f"));
		
		// works for 9544x series
		ArrayList<Sensor> s9544x = new ArrayList<Sensor>();
		
		s9544x.add(new SecondSensor("seconds", 16, 25, "%-10.0f"));
		s9544x.add(new Sensor("GPSlat", 93, 24, -90, 1.28E-05, "%-8.6f"));
		s9544x.add(new Sensor("GPSlon", 117, 25, -180, 1.28E-05, "%-8.6f"));
		s9544x.add(new Sensor("AP", 47, 11, 850, 0.1, "%-6.1f"));
		s9544x.add(new Sensor("AirT", 68, 12, -40, 0.02, "%-4.2f"));
		
//		for (int i=0;i<135;i++)
//		{
//			s9544x.add(new SecondSensor("sec["+i+"]", i, 25, "%8.6f"));
//		}
		
		pttList.put(95446, s9544x);
		pttList.put(95447, s9544x);
		pttList.put(95448, s9544x);
		
//		for (int i=8;i<93;i++)
//		{
//			sensorList.add(new Sensor("lat["+i+"]", i, 24, -90, 1.28E-05, "%8.6f"));
//			sensorList.add(new Sensor("lon["+i+"]", i, 25, -180, 1.28E-05, "%8.6f"));
//			sensorList.add(new Sensor("ice["+i+"]", i, 12, -40, 0.02, "%4.2f"));
//		}
		//sensorList.add(new Sensor("Lon", 64+56-1, 24, 0, 1.833465E-05, "%8.6f"));

//		for (int i=0;i<141;i++)
//		{
//			sensorList.add(new Sensor("AP_["+i+"]", i, 11, 850, 0.1, "%6.1f"));
//		}

		// works for 82500
		ArrayList<Sensor> s82500 = new ArrayList<Sensor>();
		
		s82500.add(new SecondSensor("seconds", 16+8, 25, "%-10.0f"));
		s82500.add(new Sensor("GPSlat", 41+8, 24, -90, 1.28E-05, "%-8.6f"));
		s82500.add(new Sensor("GPSlon", 65+8, 25, -180, 1.28E-05, "%-8.6f"));
		s82500.add(new Sensor("iceT", 119+8, 12, -40, 0.02, "%-4.2f"));
		s82500.add(new Sensor("SST", 107+8, 12, -40, 0.02, "%-4.2f"));
		s82500.add(new Sensor("bat", 131+8, 8, 2.5, 0.05,"%-4.2f"));
		
		pttList.put(82500, s82500);
		pttList.put(82590, s82500);
		pttList.put(41404, s82500);
		pttList.put(41348, s82500);
		pttList.put(43566, s82500);
	}
	
	public class Sensor
	{
		int start;
		int bits;
		String name;
		double scale = 1;
		double offset = 0;
		String fmt;
		
		public Sensor(String n, int s, int b, String fmt)
		{
			name = n;
			start = s;
			bits = b;
			this.fmt = fmt;
		}
		public Sensor(String n, int s, int b, double offset, double scale, String fmt)
		{
			this(n, s, b, fmt);
			this.scale = scale;
			this.offset = offset;
		}
		
		public double getDouble(BigInteger b)
		{
			int msgBits = m.dataValues.size() * 8;

			BigInteger mask =  BigInteger.valueOf((1<<bits) - 1);
			
			// System.out.println("MASK : " + mask);
			
			BigInteger s1 = b.shiftRight((msgBits - (start + bits))).and(mask);
			
			return s1.doubleValue();			
			
		}
		
		public String toString(Message m)
		{
			double d1 = getDouble(m.b);
			
			return String.format(fmt, (d1 * scale + offset));			
		}
	}
	
	public class SecondSensor extends Sensor
	{
		Date dzero;
		
		public SecondSensor(String n, int s, int b, String fmt)
		{
			super(n, s, b, fmt);
			
			try
			{
				dzero = sdf.parse("2014-01-01 00:00:00");
			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public String toString(Message m)
		{
			double d1 = getDouble(m.b);
			
			long sec = (long) (d1 * scale + offset);
			Date d = new Date(sec * 1000 + dzero.getTime());
			
			return sdf.format(d);			
		}
		
	}
	
	public class Message
	{
		public Message(int id)
		{
			this.id = id;
		}
		int id;
		int msgRepts;
		Date msgTs;
		ArrayList<String> dataValues = new ArrayList<String>();
		BigInteger b;
	}
	
	public Message m;
	
	HashMap <Integer, ArrayList<Sensor>> pttList = new HashMap<Integer, ArrayList<Sensor>>();
	
	int program;
	int id;
	Date recordTs;
	
	int recordLines;
	int sensors;
	char sats;
	char type;
	double lat = Double.NaN;
	double lon = Double.NaN;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	int recordLineCount;
	
	public void parseRecordLine(String[] line) throws ParseException
	{
		recordLineCount = 0;
		
		id = Integer.parseInt(line[1]);
		recordLines = Integer.parseInt(line[2]);
		sensors = Integer.parseInt(line[3]);
		sats = line[4].charAt(0);
		//type = line[5].charAt(0);

		recordTs = null;
		lat = Double.NaN;
		lon = Double.NaN;
		
		if (line.length > 7)
		{
			// its a position record, with location and timestamp
			
			//System.out.println("DATE " + line[6] + " " + line[7]);
			
			recordTs = sdf.parse(line[6] + " " + line[7]);
			
			//System.out.println("RECORD: " + sdf.format(recordTs) + " ID " + id);
			if (line.length >= 12)
			{
				lat = Double.parseDouble(line[8]);
				lon = Double.parseDouble(line[9]);
				
				//System.out.println("LAT = " + lat + " ,lon = " + lon);
			}
		}
		else
		{
			//System.out.println("RECORD No Position " + line);
		}
	}
	
	public void parseMsgLine(String[] line) throws ParseException
	{
		m = new Message(id);

		m.msgTs = sdf.parse(line[1] + " " + line[2]);
		m.msgRepts = Integer.parseInt(line[3]);
		
		//System.out.println(" MSG : " + sdf.format(msgTs));	
		for(int i=4;i<line.length;i++)
		{
			m.dataValues.add(line[i]);
		}
	}
	
	public void parseDataLine(String[] line)
	{
		for(int i=1;i<line.length;i++)
		{
			m.dataValues.add(line[i]);
		}		
	}

	public void readFile(String fileName)
	{
		System.err.println("Read File : " + fileName);

		FileInputStream fis = null;
		BufferedReader reader = null;
		
		Pattern patNewRecord = Pattern.compile("\\d{5} \\d{5}.*");
		Pattern patNewMsg = Pattern.compile(" {6}\\d{4}-\\d{2}.*");
		
		try
		{
			fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));

			int lineNo = 0;
			boolean isRecord;
			boolean isMsg;
			String[] lSplit;
			String line = reader.readLine();
			while (line != null)
			{
				isRecord = patNewRecord.matcher(line).matches();
				isMsg = patNewMsg.matcher(line).matches();
				lSplit = line.split(" +");
				if (lSplit.length > 1)
				{
					try
					{
						if (isRecord)
							parseRecordLine(lSplit);
						else if (isMsg)
							parseMsgLine(lSplit);
						else
							parseDataLine(lSplit);
						
						recordLineCount++;
					}
					catch (ParseException e)
					{
							// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (isRecord)
					{
						if (recordTs != null)
						{
							System.out.print(String.format("%-6d", id) + " ," + sdf.format(recordTs) );
							System.out.println(" ,ArgosLat=" + lat + " ,ArgosLon=" + lon);
						}
					}

					//if (isRecord)
					//	System.out.printf("%4d : %5b : %5b : %3d : %2d : %2d : %s\n", lineNo, isRecord, isMsg, lSplit.length, recordLineCount, dataValues.size(), line);
					
					if ((m != null) && (m.dataValues.size() == sensors))
					{
						System.out.print(String.format("%-6d", id) + " ," + sdf.format(m.msgTs) + " ,REPS=" + m.msgRepts);
//						System.out.print("  ALL SENSORS ");
//						for (int i=0;i<dataValues.size();i++)
//						{
//							System.out.printf(" ,%3s", dataValues.get(i));
//						}
						//System.out.println();
												
						String v = "";
						for(int i=0;i<m.dataValues.size();i++)
						{
							v += String.format("%02X", Integer.parseInt(m.dataValues.get(i), 10));
						}
						//System.out.println("   VALUE : " + v);
						
						m.b = new BigInteger(v, 16);
						
						//System.out.println("   Int " + b + " bits " + bits);
						ArrayList<Sensor> sensorList = pttList.get(id);
						if (sensorList != null)
						{
							for (int i = 0;i<sensorList.size();i++)
							{
								Sensor s = sensorList.get(i);
		
								//System.out.println("    " + s.name + " : " + s1.toString(16) + " " + (d1 * s.scale + s.offset));						
								System.out.print(" ," + s.name + "=" + s.toString(m));	
								//System.out.println();
								//System.out.print(" ," + String.format(s.fmt, (d1 * s.scale + s.offset)));						
							}
						}
						System.out.println();
						
					}
					if (recordLineCount >= recordLines)
					{
						//System.out.println("END OF RECORD");
						m = null;
					}
					lineNo++;
				}
				line = reader.readLine();
			}

		}
		catch (FileNotFoundException ex)
		{
			Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IOException ex)
		{
			Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
		}
		finally
		{
			try
			{
				reader.close();
				fis.close();
			}
			catch (IOException ex)
			{
				Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

	}

	public static void main(String[] args)
	{
		Processor p = new Processor();

		for (String s : args)
		{
			p.readFile(s);
		}
	}

}
