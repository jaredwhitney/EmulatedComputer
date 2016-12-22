import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
public class Screen extends Structure implements IODevice
{
	BufferedImage image;
	int xres, yres;
	byte bpp;
	JFrame frame;
	int mode;
	int texNum;
	int id = 0x7;
	public void setID(int i)
	{
		id = i;
	}
	public int getID()
	{
		return id;
	}
	public void send(int data){System.err.println("Use of the default Screen send handler is not recommended.");}
}
class KF0BASIC extends Screen implements Runnable
{
	int fontxres, fontyres;
	char[][] charData;
	MPD mpd;
	public KF0BASIC(int texNum, MPD mpd)
	{
		this.mpd = mpd;
		this.texNum = texNum;
		xres = 640;
		yres = 480;
		fontxres = xres/10;
		fontyres = yres/(22+2);
		bpp = 4;
		data = new byte[xres*yres*bpp];
		charData = new char[fontyres][fontxres];
		image = new BufferedImage(xres, yres, BufferedImage.TYPE_INT_RGB);
		for (int a = 0; a < data.length; a++)
			data[a] = 0;
		if (!EngineTest.inUse)
		{
			frame = new JFrame();
			frame.add(new DisplayComponent(this));
			frame.setSize(xres, yres);
			frame.setDefaultCloseOperation(3);
			frame.setVisible(true);
		}
		
		memloc = 0xa0000;
		memsize = data.length;
		MemoryHandler.register(this);
		IOHandler.register(this);
		
		new Thread(this).start();
	}
	public void write(int loc, byte dat)
	{
		data[loc] = dat;
		if (mode==0)
		{
			Graphics g = image.getGraphics();
			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, xres, yres);
			g.setColor(Color.WHITE);
			int ln = 0;
			int last = 0;
			for (int o = 0; ln < fontyres-1; o++)
			{
				char in = (char)x.b(data[o]);
				if (in==(char)13 || o-last >= fontxres)
				{
					ln++;
					last = o;
					//System.out.println("Last: " + last);
				}
				//System.out.println("Line " + ln + " char " + (o-last));
				charData[ln][o-last] = (char)x.b(data[o]);	// deal with newline (char 13)
			}
			int q = 0;
			for (char[] row : charData)
				g.drawChars(row, 0, row.length, 0, (g.getFontMetrics().getHeight()+2)*++q);
		}
		if (mode==1)
		{
			int pos = loc/bpp;
			int xk = pos%xres;
			int y = pos/xres;
			if (y < yres)
			{
				int i = x.b(data[pos*bpp+3]) | (x.b(data[pos*bpp+2])<<8) | (x.b(data[pos*bpp+1])<<16);
				image.setRGB(xk, y, i);
			}
		}
	}
	public void send(int data)
	{
		if (data==0)
			Registers.r4 = mode;
		else if (data==1)
		{
			if (mode==0)
				Registers.r4 = fontxres;
			else
				Registers.r4 = xres;
		}
		else if (data==2)
		{
			if (mode==0)
				Registers.r4 = fontyres;
			else
				Registers.r4 = yres;
		}
		else if (data==3)
			Registers.r4 = bpp;
		else if (data==4)
		{
			System.out.println("r4=" + memloc);
			Registers.r4 = memloc;
		}
		else if (data==5)
			Registers.r4 = memsize;
		else if (data==6)
		{
			System.out.println("mode=" + Registers.r4);
			mode = Registers.r4;
		}
	}
	public void run()
	{
		while (!mpd.exit)
		{
			if (texNum != -1)
				Texture.modifyImageLater(texNum, image);
			else
				frame.repaint();
			try{Thread.sleep(30);}catch(Exception e){}
		}
	}
}
class x
{
	public static int b(byte b)
	{
		if (b >= 0)
			return (int)b;
		else
			return 256+(int)b;
	}
}
class DisplayComponent extends JComponent
{
	Screen scr;
	public DisplayComponent(Screen scr)
	{
		this.scr = scr;
	}
	public void paint(Graphics g)
	{
		g.drawImage(scr.image, 0, 0, null);
	}
}