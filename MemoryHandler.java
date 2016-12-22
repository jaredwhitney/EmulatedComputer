import java.util.*;
public class MemoryHandler
{
	static ArrayList<Structure> structs = new ArrayList<Structure>();
	public static void write(int loc, byte data)
	{
		Structure c = getStruct(loc);
		c.write(loc-c.memloc, data);
	}
	public static byte read(int loc)
	{
		Structure c = getStruct(loc);
		return c.read(loc-c.memloc);
	}
	public static Structure getStruct(int loc)
	{
		for (Structure s : structs)
		{
			if (loc >= s.memloc && loc < s.memloc+s.memsize)
				return s;
		}
		return GMEM.instance;
	}
	public static void register(Structure s)
	{
		structs.add(s);
	}
}
class GMEM extends Structure	// Generic MEMory
{
	static GMEM instance = new GMEM();
	private GMEM()
	{
		data = new byte[0xFFFFF];
	}
}
class Registers
{
	static int r0 = 0;
	static int r1 = 0;
	static int r2 = 0;
	static int r3 = 0;
	static int r4 = 0;
	static int sp = 0;
	static int ipointer = 0;
}
class IOHandler
{
	static ArrayList<IODevice> devs = new ArrayList<IODevice>();
	public static void send(int port, int data)
	{
		IODevice d = getDevice(port, data);
		d.send(data);
	}
	public static IODevice getDevice(int port, int data)
	{
		for (IODevice dev : devs)
		{
			if (dev.getID()==port)
				return dev;
		}
		System.err.println("No device found on port " + port + " [data=" + data + "]");
		System.err.println("\tipointer: " + Registers.ipointer);
		System.err.println("\tr0: " + Registers.r0);
		System.err.println("\tr1: " + Registers.r1);
		System.err.println("\tr2: " + Registers.r2);
		System.err.println("\tr3: " + Registers.r3);
		System.err.println("\tr4: " + Registers.r4);
		return null;
	}
	public static void register(IODevice d)
	{
		devs.add(d);
	}
}

class Structure
{
	byte[] data;
	int memloc;
	int memsize;
	public void write(int loc, byte dat)
	{
		data[loc] = dat;
	}
	public byte read(int loc)
	{
		return data[loc];
	}
}
interface IODevice
{
	public void send(int data);
	public int getID();
	public void setID(int i);
}