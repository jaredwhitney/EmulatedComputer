import java.io.*;
public class MPD implements Runnable	// main processing device
{
	Screen scr;
	KB keyboard;
	boolean exit = false;
	public MPD(int texNum)
	{
		scr = new KF0BASIC(texNum, this);
	}
	public static void main(String[] args) throws Exception
	{
		new Thread(new MPD(-1)).start();
	}
	public void run()
	{
		keyboard = new KB(this);
		
		int x = 10;
		
		try
		{
			File inp = new File("os.mpd");
			FileInputStream in = new FileInputStream(inp);
			int len = (int)inp.length();
			byte[] data = new byte[len];
			in.read(data);
			System.arraycopy(data, 0, GMEM.instance.data, 0, len);
		}
		catch(Exception ex){ex.printStackTrace();}
		
		Registers.ipointer = 0;
		while (!exit)
		{
			placeholder();
		}
		System.out.println("EXIT.");
	}
	public static void placeholder()
	{
		byte b = MemoryHandler.read(Registers.ipointer);
		Registers.ipointer++;
		switch (b)
		{
			case 0x0 :
				mov();
				break;
			case 0x1 :
				add();
				break;
			case 0x2 :
				sub();
				break;
			case 0x3 :
				gotorel();
				break;
			case 0x4 :
				gotoabs();
				break;
			case 0x5 :
				send();
				break;
			case 0x6 :
				jc('0');
				break;
			case 0x7 :
				jc('<');
				break;
			case 0x8 :
				jc('[');
				break;
			case 0x9 :
				jc('=');
				break;
			case 0xA :
				jc('!');
				break;
			case 0xB :
				jc(']');
				break;
			case 0xC :
				jc('>');
				break;
			case 0xD :
				mul();
				break;
			case 0xE :
				div();
				break;
			case 0xF :
				and();
				break;
			case 0x10 :
				or();
				break;
			case 0x11 :
				xor();
				break;
			case 0x12 :
				not();
				break;
			case 0x13 :
				lsh();
				break;
			case 0x14 :
				rsh();
				break;
			case 0x15 :
				push();
				break;
			case 0x16 :
				pop();
				break;
			case 0x17 :
				call();
				break;
			case 0x18 :
				ret();
				break;
		}
	}
	static void mov()
	{
		//System.out.println("Moving a thing!");
		Specifier dest = new Specifier(-1);
		Specifier src = new Specifier(dest.mod);
		
		int val = Specifier.getValIn(src);
		//System.out.println("Thing = " + val);
		Specifier.writeValOut(dest, val);
	}
	static void add()
	{
		Specifier dest = new Specifier(-1);
		Specifier src = new Specifier(dest.mod);
		
		int val1 = Specifier.getValIn(src);
		int val2 = Specifier.getValIn(dest);
		Specifier.writeValOut(dest, val1+val2);
	}
	static void sub()
	{
		Specifier dest = new Specifier(-1);
		Specifier src = new Specifier(dest.mod);
		
		int val1 = Specifier.getValIn(src);
		int val2 = Specifier.getValIn(dest);
		Specifier.writeValOut(dest, val2-val1);
	}
	static void gotorel()
	{
		Specifier dest = new Specifier(-1);
		int val = Specifier.getValIn(dest);
		Registers.ipointer += val;
	}
	static void gotoabs()
	{
		Specifier dest = new Specifier(-1);
		int val = Specifier.getValIn(dest);
		Registers.ipointer = val;
	}
	static void send()
	{
		Specifier dest = new Specifier(-1);
		Specifier data = new Specifier(-1);
		int val1 = Specifier.getValIn(dest);
		int val2 = Specifier.getValIn(data);
		IOHandler.send(val1, val2);
	}
	static void push()
	{
		Specifier valspec = new Specifier(-1);
		int val = Specifier.getValIn(valspec);
		Specifier dest = new Specifier();
		dest.type = Specifier.TYPE_RPT;
		dest.valb = 0xC;
		dest.mod = valspec.mod;
		Specifier.writeValOut(dest, val);
		if (dest.mod==-1)
			Registers.sp += 4;
		else
			Registers.sp += dest.mod;
		System.out.println("push " + val + "[" + dest.mod + "]");
	}
	static void spoofPush(Specifier valspec)
	{
		int val = Specifier.getValIn(valspec);
		Specifier dest = new Specifier();
		dest.type = Specifier.TYPE_RPT;
		dest.valb = 0xC;
		dest.mod = valspec.mod;
		System.out.println("Spoof pushed " + val + "[" + dest.mod + "]");
		Specifier.writeValOut(dest, val);
		if (dest.mod==-1)
			Registers.sp += 4;
		else
			Registers.sp += dest.mod;
	}
	static void pop()
	{
		Specifier destspec = new Specifier(-1);
		Specifier valspec = new Specifier();
		valspec.type = Specifier.TYPE_RPT;
		valspec.valb = 0xC;
		valspec.mod = destspec.mod;
		if (destspec.mod==-1)
			Registers.sp -= 4;
		else
			Registers.sp -= destspec.mod;
		int val = Specifier.getValIn(valspec);
		Specifier.writeValOut(destspec, val);
		System.out.println("pop " + val + "[" + destspec.mod + "]");
	}
	static void spoofPop(Specifier destspec)
	{
		Specifier valspec = new Specifier();
		valspec.type = Specifier.TYPE_RPT;
		valspec.valb = 0xC;
		valspec.mod = destspec.mod;
		if (destspec.mod==-1)
			Registers.sp -= 4;
		else
			Registers.sp -= destspec.mod;
		int val = Specifier.getValIn(valspec);
		System.out.println("Spoof popped " + val + "[" + destspec.mod + "]");
		Specifier.writeValOut(destspec, val);
	}
	static void jc(char s)
	{
		Specifier arg0 = new Specifier(-1);
		Specifier arg1;
		int val0, val1 = 0, destVal;
		val0 = Specifier.getValIn(arg0);
		if (s!='0')
		{
			arg1 = new Specifier(arg0.mod);
			val1 = Specifier.getValIn(arg1);
		}
		Specifier dest = new Specifier(-1);
		destVal = Specifier.getValIn(dest);
		boolean jump = false;
		switch (s)
		{
			case '0' :
				jump = (val0==0);
				break;
			case '<' :
				jump = (val0 < val1);
				break;
			case '[' :
				jump = (val0 <= val1);
				break;
			case '=' :
				jump = (val0 == val1);
				break;
			case '!' :
				jump = (val0 != val1);
				break;
			case ']' :
				jump = (val0 >= val1);
				break;
			case '>' :
				jump = (val0 > val1);
				break;
		}
		if (jump)
			Registers.ipointer = destVal;
	}
	static void mul()
	{
		Specifier dest = new Specifier(-1);
		Specifier src = new Specifier(dest.mod);
		
		int val1 = Specifier.getValIn(src);
		int val2 = Specifier.getValIn(dest);
		Specifier.writeValOut(dest, val1*val2);
	}
	static void div()
	{
		Specifier dest = new Specifier(-1);
		Specifier src = new Specifier(dest.mod);
		
		int val1 = Specifier.getValIn(src);
		int val2 = Specifier.getValIn(dest);
		Specifier.writeValOut(dest, val2/val1);
	}
	static void lsh()
	{
		Specifier dest = new Specifier(-1);
		Specifier src = new Specifier(dest.mod);
		
		int val1 = Specifier.getValIn(src);
		int val2 = Specifier.getValIn(dest);
		Specifier.writeValOut(dest, val2<<val1);
	}
	static void rsh()
	{
		Specifier dest = new Specifier(-1);
		Specifier src = new Specifier(dest.mod);
		
		int val1 = Specifier.getValIn(src);
		int val2 = Specifier.getValIn(dest);
		Specifier.writeValOut(dest, val2>>val1);
	}
	static void and()
	{
		Specifier dest = new Specifier(-1);
		Specifier src = new Specifier(dest.mod);
		
		int val1 = Specifier.getValIn(src);
		int val2 = Specifier.getValIn(dest);
		Specifier.writeValOut(dest, val1&val2);
	}
	static void or()
	{
		Specifier dest = new Specifier(-1);
		Specifier src = new Specifier(dest.mod);
		
		int val1 = Specifier.getValIn(src);
		int val2 = Specifier.getValIn(dest);
		Specifier.writeValOut(dest, val1|val2);
	}
	static void xor()
	{
		Specifier dest = new Specifier(-1);
		Specifier src = new Specifier(dest.mod);
		
		int val1 = Specifier.getValIn(src);
		int val2 = Specifier.getValIn(dest);
		Specifier.writeValOut(dest, val1^val2);
	}
	static void not()
	{
		Specifier src = new Specifier(-1);
		
		int val1 = Specifier.getValIn(src);
		Specifier.writeValOut(src, ~val1);
	}
	static void call()
	{
		Specifier destspec = new Specifier(-1);
		int dest = Specifier.getValIn(destspec);
		Specifier nextInstruction = new Specifier();
		nextInstruction.type = Specifier.TYPE_REG;
		nextInstruction.valb = 0xD;
		nextInstruction.mod = -1;
		spoofPush(nextInstruction);
		System.out.println("Calling " + dest + " from " + Registers.ipointer);
		Registers.ipointer = dest;
	}
	static void ret()
	{
		Specifier destspec = new Specifier();
		destspec.type = Specifier.TYPE_REG;
		destspec.valb = 0xD;
		destspec.mod = -1;
		spoofPop(destspec);
		System.out.println("Returned to " + Registers.ipointer);
	}
}
class Specifier
{
	byte type = -1;
	byte mod = -1;
	byte valb = -1;
	int val = -1;
	static final int TYPE_REG = 0x10, TYPE_RPT = 0x20, TYPE_VFL = 0x30;
	public Specifier(){}	// for manual construction
	public Specifier(int inheritmod)
	{
		//System.out.println("Looking at a thing: " + Registers.ipointer);
		if (inheritmod != -1)
			mod = (byte)inheritmod;
		int b = MemoryHandler.read(Registers.ipointer);
		Registers.ipointer++;
		type = (byte)(b&0xF0);
		valb = (byte)(b&0xF);
		if (x.b(type)==0x90)
		{
			mod = (byte)(b&0xF);
			b = MemoryHandler.read(Registers.ipointer);
			Registers.ipointer++;
			type = (byte)(b&0xF0);
			valb = (byte)(b&0xF);
			//System.out.println("\tHad a modifier!");
		}
		if (x.b(type)==TYPE_VFL)
		{
			//System.out.println("\tVal should follow... (" + mod + ")");
			if (x.b(valb)==1 || mod==-1 || x.b(mod)==4)
			{
				val = (x.b(MemoryHandler.read(Registers.ipointer))<<24) | (x.b(MemoryHandler.read(Registers.ipointer+1))<<16) | (x.b(MemoryHandler.read(Registers.ipointer+2))<<8) | x.b(MemoryHandler.read(Registers.ipointer+3));
				Registers.ipointer += 4;
			}
			else if (x.b(mod)==2)
			{
				val = (x.b(MemoryHandler.read(Registers.ipointer))<<8) | x.b(MemoryHandler.read(Registers.ipointer+1));
				Registers.ipointer += 2;
			}
			else if (x.b(mod)==1)
			{
				val = x.b(MemoryHandler.read(Registers.ipointer));
				Registers.ipointer++;
			}
			//System.out.println("\t\t" + val);
		}
	}
	public static int getValIn(Specifier src)
	{
		int val = 0;
		if (src.type==Specifier.TYPE_REG)
		{
			switch (src.valb)
			{
				case 0 :
					val = Registers.r0;
					break;
				case 1 :
					val = Registers.r1;
					break;
				case 2 :
					val = Registers.r2;
					break;
				case 3 :
					val = Registers.r3;
					break;
				case 4 :
					val = Registers.r4;
					break;
				case 0xC :
					val = Registers.sp;
					break;
				case 0xD :
					val = Registers.ipointer;
					break;
			}
			if (src.mod==2)
				val &= 0xFFFF;
			else if (src.mod==1)
				val &= 0xFF;
		}
		else if (src.type==Specifier.TYPE_RPT)
		{
			switch (src.valb)
			{
				case 0 :
					val = Registers.r0;
					break;
				case 1 :
					val = Registers.r1;
					break;
				case 2 :
					val = Registers.r2;
					break;
				case 3 :
					val = Registers.r3;
					break;
				case 4 :
					val = Registers.r4;
					break;
				case 0xC :
					val = Registers.sp;
					break;
				case 0xD :
					val = Registers.ipointer;
					break;
			}
			if (src.mod==2)
			{
				val = (x.b(MemoryHandler.read(val))<<8) | x.b(MemoryHandler.read(val+1));
			}
			else if (src.mod==1)
			{
				val = x.b(MemoryHandler.read(val));
			}
			else
			{
				val = (x.b(MemoryHandler.read(val))<<24) | (x.b(MemoryHandler.read(val+1))<<16) | (x.b(MemoryHandler.read(val+2))<<8) | x.b(MemoryHandler.read(val+3));
			}
		}
		else if (src.type==Specifier.TYPE_VFL)
		{
			val = src.val;
			if (src.valb==1)
			{
				if (src.mod==2)
				{
					val = (x.b(MemoryHandler.read(val))<<8) | x.b(MemoryHandler.read(val+1));
				}
				else if (src.mod==1)
				{
					val = x.b(MemoryHandler.read(val));
				}
				else
				{
					val = (x.b(MemoryHandler.read(val))<<24) | (x.b(MemoryHandler.read(val+1))<<16) | (x.b(MemoryHandler.read(val+2))<<8) | x.b(MemoryHandler.read(val+3));
				}
			}
		}
		return val;
	}
	public static void writeValOut(Specifier dest, int val)
	{
		if (dest.type==Specifier.TYPE_REG)
		{
			if (dest.mod==-1||dest.mod==4)
			{
				switch (dest.valb)
				{
					case 0:
						Registers.r0 = val;
						break;
					case 1:
						Registers.r1 = val;
						break;
					case 2:
						Registers.r2 = val;
						break;
					case 3:
						Registers.r3 = val;
						break;
					case 4:
						Registers.r4 = val;
						break;
					case 0xC:
						Registers.sp = val;
						break;
					case 0xD:
						Registers.ipointer = val;
						break;
				}
			}
			else if (dest.mod==2)
			{
				switch (dest.valb)
				{
					case 0:
						Registers.r0 &= 0xFFFF0000;
						Registers.r0 |= val;
						break;
					case 1:
						Registers.r1 &= 0xFFFF0000;
						Registers.r1 |= val;
						break;
					case 2:
						Registers.r2 &= 0xFFFF0000;
						Registers.r2 |= val;
						break;
					case 3:
						Registers.r3 &= 0xFFFF0000;
						Registers.r3 |= val;
						break;
					case 4:
						Registers.r4 &= 0xFFFF0000;
						Registers.r4 |= val;
						break;
					case 0xC:
						Registers.sp &= 0xFFFF0000;
						Registers.sp |= val;
						break;
					case 0xD:
						Registers.ipointer &= 0xFFFF0000;
						Registers.ipointer |= val;
						break;
				}
			}
			else if (dest.mod==1)
			{
				switch (dest.valb)
				{
					case 0:
						Registers.r0 &= 0xFFFFFF00;
						Registers.r0 |= val;
						break;
					case 1:
						Registers.r1 &= 0xFFFFFF00;
						Registers.r1 |= val;
						break;
					case 2:
						Registers.r2 &= 0xFFFFFF00;
						Registers.r2 |= val;
						break;
					case 3:
						Registers.r3 &= 0xFFFFFF00;
						Registers.r3 |= val;
						break;
					case 4:
						Registers.r4 &= 0xFFFFFF00;
						Registers.r4 |= val;
						break;
					case 0xC:
						Registers.sp &= 0xFFFFFF00;
						Registers.sp |= val;
						break;
					case 0xD:
						Registers.ipointer &= 0xFFFFFF00;
						Registers.ipointer |= val;
						break;
				}
			}
		}
		else if (dest.type==Specifier.TYPE_RPT)
		{
			int tempval = 0;
			switch (dest.valb)
			{
				case 0 :
					tempval = Registers.r0;
					break;
				case 1 :
					tempval = Registers.r1;
					break;
				case 2 :
					tempval = Registers.r2;
					break;
				case 3 :
					tempval = Registers.r3;
					break;
				case 4 :
					tempval = Registers.r4;
					break;
				case 0xC :
					tempval = Registers.sp;
					break;
				case 0xD :
					tempval = Registers.ipointer;
					break;
			}
			if (dest.mod==2)
			{
				MemoryHandler.write(tempval+1, (byte)(val&0xFF));
				MemoryHandler.write(tempval, (byte)((val>>8)&0xFF));
			}
			else if (dest.mod==1)
			{
				MemoryHandler.write(tempval, (byte)(val&0xFF));
			}
			else
			{
				MemoryHandler.write(tempval+3, (byte)(val&0xFF));
				MemoryHandler.write(tempval+2, (byte)((val>>8)&0xFF));
				MemoryHandler.write(tempval+1, (byte)((val>>16)&0xFF));
				MemoryHandler.write(tempval, (byte)((val>>24)&0xFF));
			}
		}
		else if (dest.type==Specifier.TYPE_VFL)
		{
			int tempval = dest.val;
			if (dest.valb==0)
				throw new RuntimeException("'do val, x' unallowed");
			if (dest.mod==2)
			{
				MemoryHandler.write(tempval+1, (byte)(val&0xFF));
				MemoryHandler.write(tempval, (byte)((val>>8)&0xFF));
			}
			else if (dest.mod==1)
			{
				MemoryHandler.write(tempval, (byte)(val&0xFF));
			}
			else
			{
				MemoryHandler.write(tempval+3, (byte)(val&0xFF));
				MemoryHandler.write(tempval+2, (byte)((val>>8)&0xFF));
				MemoryHandler.write(tempval+1, (byte)((val>>16)&0xFF));
				MemoryHandler.write(tempval, (byte)((val>>24)&0xFF));
			}
		}
	}
}