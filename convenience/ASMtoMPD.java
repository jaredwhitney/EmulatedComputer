import java.util.*;
import java.io.*;
public class ASMtoMPD
{
	static Map<String, String> definitions = new HashMap<String, String>();
	static Map<Integer, String> replacements = new HashMap<Integer, String>();
	static OutputStream out;
	static int base = -1;
	static byte rmmod = -2;	// eww gross workaround here...
	static boolean isData = false;
	static String lastLabel;
	public static void main(String[] args) throws Exception
	{
		Scanner sc = new Scanner(new File(args[0]));
		int bnum = 0;
		out = new FileOutputStream(new File("..\\os.mpd"));
		while (sc.hasNext())
		{
			String inp = sc.nextLine().trim();
			String[] words = inp.split(" ");
			if (inp.length() == 0)
				continue;
			if (words[0].equals("#DEFINE"))
			{
				definitions.put(words[1], words[2]);
			}
		}
		sc = new Scanner(new File(args[0]));
		while (sc.hasNext())
		{
			String rinp = sc.nextLine();
			String inp = rinp.trim();
			String[] words = inp.split(" ");
			if (inp.length() == 0)
				continue;
			if (inp.charAt(0)=='#')
			{
				if (words[0].equals("#LABEL"))
				{
					String word = words[1];
					if (word.charAt(0)!='.')
						lastLabel = word;
					else
						word = lastLabel + word;
					definitions.put(word, Integer.toHexString(bnum));
				}
				if (words[0].equals("#STRING"))
				{
					String str = rinp.substring(rinp.indexOf("#STRING ")+"#STRING ".length(), rinp.length());
					for (char c : str.toCharArray())
						out.write(c);
					out.write(0);
					bnum += 1+str.length();
				}
			}
			else
			{
				int sw=1;
				byte op = -1;
				if (words[0].equalsIgnoreCase("MOVE"))
				{
					op = 0x00;
				}
				else if (words[0].equalsIgnoreCase("ADD"))
				{
					op = 0x01;
				}
				else if (words[0].equalsIgnoreCase("SUB"))
				{
					op = 0x02;
				}
				else if (words[0].equalsIgnoreCase("RJUMP"))
				{
					op = 0x03;
				}
				else if (words[0].equalsIgnoreCase("AJUMP"))
				{
					op = 0x04;
				}
				else if (words[0].equalsIgnoreCase("SEND"))
				{
					op = 0x05;
				}
				else if (words[0].equalsIgnoreCase("ZJUMP"))
				{
					op = 0x06;
				}
				else if (words[0].equalsIgnoreCase("LJUMP"))
				{
					op = 0x07;
				}
				else if (words[0].equalsIgnoreCase("LEJUMP"))
				{
					op = 0x08;
				}
				else if (words[0].equalsIgnoreCase("EJUMP"))
				{
					op = 0x09;
				}
				else if (words[0].equalsIgnoreCase("NJUMP"))
				{
					op = 0x0A;
				}
				else if (words[0].equalsIgnoreCase("GEJUMP"))
				{
					op = 0x0B;
				}
				else if (words[0].equalsIgnoreCase("GJUMP"))
				{
					op = 0x0C;
				}
				else if (words[0].equalsIgnoreCase("MUL"))
				{
					op = 0x0D;
				}
				else if (words[0].equalsIgnoreCase("DIV"))
				{
					op = 0x0E;
				}
				else if (words[0].equalsIgnoreCase("AND"))
				{
					op = 0x0F;
				}
				else if (words[0].equalsIgnoreCase("OR"))
				{
					op = 0x10;
				}
				else if (words[0].equalsIgnoreCase("XOR"))
				{
					op = 0x11;
				}
				else if (words[0].equalsIgnoreCase("NOT"))
				{
					op = 0x12;
				}
				else if (words[0].equalsIgnoreCase("SHIFT"))
				{
					if (words[1].equalsIgnoreCase("LEFT"))
					{
						op = 0x13;
						sw=2;
					}
					else if (words[1].equalsIgnoreCase("RIGHT"))
					{
						op = 0x14;
						sw=2;
					}
				}
				else if (words[0].equalsIgnoreCase("PUSH"))
				{
					op = 0x15;
				}
				else if (words[0].equalsIgnoreCase("POP"))
				{
					op = 0x16;
				}
				else if (words[0].equalsIgnoreCase("CALL"))
				{
					op = 0x17;
				}
				else if (words[0].equalsIgnoreCase("RET"))
				{
					op = 0x18;
				}
				isData = true;
				if (!words[0].equalsIgnoreCase("DATA"))
				{
					System.out.println("op = " + Integer.toHexString(op));
					out.write(op);
					isData = false;
					bnum++;
				}
				rmmod = -2;
				for (int q = sw; q < words.length; q++)
				{
					words[q] = words[q].trim();
					byte mod = -1;
					if (words[q].equalsIgnoreCase("BYTE"))
					{
						mod = (byte)0x91;
					}
					else if (words[q].equalsIgnoreCase("WORD"))
					{
						mod = (byte)0x92;
					}
					else if (words[q].equalsIgnoreCase("DWORD"))
					{
						mod = (byte)0x94;
					}
					if (mod != -1)
					{
						System.out.println("mod = " + Integer.toHexString(mod));
						if (!isData)
						{
							out.write(mod);
							bnum++;
						}
						rmmod = mod;
					}
					else
					{
						for (Map.Entry s : definitions.entrySet())
						{
							if (words[q].equals((String)s.getKey()))
								words[q] = (String)s.getValue();
							else if (words[q].equals("["+s.getKey()+"]"))
								words[q] = "["+s.getValue()+"]";
							else if (words[q].equals("%"+s.getKey()))
								words[q] = "%" + s.getValue();
							else if (words[q].charAt(0)=='.')
							{
								words[q] = lastLabel + words[q];
								break;
							}
							//words[q] = words[q].replaceAll("\\Q"+s.getKey()+"\\E", (String)(s.getValue()));
						}
						if (words[q].charAt(0)=='[')
						{
							if (words[q].equalsIgnoreCase("[r0]"))
							{
								System.out.println(20);
								out.write(0x20);
								bnum++;
							}
							else if (words[q].equalsIgnoreCase("[r1]"))
							{
								System.out.println(21);
								out.write(0x21);
								bnum++;
							}
							else if (words[q].equalsIgnoreCase("[r2]"))
							{
								System.out.println(22);
								out.write(0x22);
								bnum++;
							}
							else if (words[q].equalsIgnoreCase("[r3]"))
							{
								System.out.println(23);
								out.write(0x23);
								bnum++;
							}
							else if (words[q].equalsIgnoreCase("[r4]"))
							{
								System.out.println(24);
								out.write(0x24);
								bnum++;
							}
							else if (words[q].equalsIgnoreCase("[sp]"))
							{
								System.out.println("2C");
								out.write(0x2C);
								bnum++;
							}
							else if (words[q].equalsIgnoreCase("[ip]"))
							{
								System.out.println("2D");
								out.write(0x2C);
								bnum++;
							}
							else
							{
								System.out.println(31);
								out.write(0x31);
								System.out.println(words[q]);
								if (words[q].charAt(0) == '%')
								{
									words[q] = words[q].substring(2, words[q].length()-1);
									base = 10;
								}
								else
									base = 16;
								try
								{
									writeInt((int)Long.parseLong(words[q].substring(1, words[q].length()-1), base));
									bnum += 1+4;
								}
								catch (NumberFormatException ex)
								{
									System.err.println("Failed to recognize symbol '" + words[q] + "'");
									writeInt(0);	// will need to go back and fix it later!
									replacements.put(bnum+1, words[q].substring(1, words[q].length()-1));
									bnum += 1+4;
								}
							}
						}
						else if (words[q].equalsIgnoreCase("r0"))
						{
							System.out.println(10);
							out.write(0x10);
							bnum++;
						}
						else if (words[q].equalsIgnoreCase("r1"))
						{
							System.out.println(11);
							out.write(0x11);
							bnum++;
						}
						else if (words[q].equalsIgnoreCase("r2"))
						{
							System.out.println(12);
							out.write(0x12);
							bnum++;
						}
						else if (words[q].equalsIgnoreCase("r3"))
						{
							System.out.println(13);
							out.write(0x13);
							bnum++;
						}
						else if (words[q].equalsIgnoreCase("r4"))
						{
							System.out.println(14);
							out.write(0x14);
							bnum++;
						}
						else if (words[q].equalsIgnoreCase("sp"))
						{
							System.out.println("1C");
							out.write(0x1C);
							bnum++;
						}
						else if (words[q].equalsIgnoreCase("ip"))
						{
							System.out.println("1D");
							out.write(0x1C);
							bnum++;
						}
						else
						{
							if (!isData)
							{
								System.out.println(30);
								out.write(0x30);
								bnum++;
							}
							if (rmmod != -2 && op != 0x06)	// zjump is a special type of stupid
							{
								mod = rmmod;
								if (!isData)
									rmmod = -2;
							}
							if (words[q].charAt(0) == '%')
							{
								words[q] = words[q].substring(1, words[q].length());
								base = 10;
							}
							else
								base = 16;
							if (words[q].charAt(0) == '\'')
							{
								if (mod==(byte)0x91)
								{
									out.write((byte)words[q].charAt(1));
									bnum++;
								}
								else if (mod==(byte)0x92)
								{
									writeWord((int)words[q].charAt(1));
									bnum+=2;
								}
								else
								{
									writeInt((int)words[q].charAt(1));
									bnum+=4;
								}
							}
							else if (mod==(byte)0x91)
							{
								System.out.println(words[q] + "b");
								out.write((byte)Integer.parseInt(words[q], base));
								bnum += 1;
							}
							else if (mod==(byte)0x92)
							{
								System.out.println(words[q] + "w");
								writeWord((int)Long.parseLong(words[q], base));
								bnum += 2;
							}
							else
							{
								try
								{
									System.out.println(words[q]);
									writeInt((int)Long.parseLong(words[q], base));
									bnum += 4;
								}
								catch (NumberFormatException ex)
								{
									System.err.println("Failed to recognize symbol '" + words[q] + "'");
									writeInt(0);	// will need to go back and fix it later!
									replacements.put(bnum, words[q]);
									bnum += 4;
								}
							}
						}
					}
				}
			}
		}
		for (Map.Entry s : definitions.entrySet())
		{
			System.out.println(s.getKey() + " defined as " + s.getValue());
		}
		for (Map.Entry s : replacements.entrySet())
		{
			System.out.println(s.getKey() + " needs to be set to " + definitions.get((String)s.getValue()));
			int val = (int)Long.parseLong(definitions.get((String)s.getValue()), 16);
			int loc = (Integer)s.getKey();
			((FileOutputStream)out).getChannel().position(loc);
			writeInt(val);
		}
		out.close();
	}
	static void writeInt(int q) throws IOException
	{
		out.write((q>>24)&0xFF);
		out.write((q>>16)&0xFF);
		out.write((q>>8)&0xFF);
		out.write((q>>0)&0xFF);
	}
	static void writeWord(int q) throws IOException
	{
		out.write((q>>8)&0xFF);
		out.write((q>>0)&0xFF);
	}
}


/*

#DEFINE KeyboardPort 5
#DEFINE GetBufferedKeyPress 0
#DEFINE ScreenMemory A0000
#DEFINE SendResult r4

MOVE r0, ScreenMemory
#LABEL loop
SEND KeyboardPort, GetBufferedKeyPress
ZJUMP SendResult, loop
MOVE BYTE [r0], SendResult
ADD r0, 1
AJUMP loop

*/

