public class SEBI	// simple early boot interface
{
	public static void main(String[] args)
	{
		KF0BASIC scr = new KF0BASIC(-1, null);
		writeString(scr, 0xa0000, "SEBI Booting...");
	}
	public static void writeString(Screen scr, int loc, String s)
	{
		for (char c : s.toCharArray())
		{
			MemoryHandler.write(loc++, (byte)c);
		}
	}
}
