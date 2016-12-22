import java.util.*;
import java.io.*;
public class FontGrabber
{
	public static void main(String[] args) throws Exception
	{
		Scanner sc = new Scanner(new FileInputStream(new File("font.txt")));
		while (true)
		{
			System.out.print("\tDATA byte ");
			for (int q = 0; q < 7; q++)
			{
				String hexStr = Integer.toString(Integer.parseInt(sc.nextLine().trim(), 2), 16);
				if (hexStr.length()==1) hexStr = "0" + hexStr;
				System.out.print(hexStr + " ");
			}
			System.out.println();
			sc.nextLine();
		}
	}
}