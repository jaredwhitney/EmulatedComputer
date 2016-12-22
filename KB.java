import java.util.*;
import org.lwjgl.input.*;
import java.awt.event.*;
class KB implements IODevice, Runnable
{
	MPD mpd;
	int id;
	ArrayList<Character> keybuffer = new ArrayList<Character>();
	public KB(MPD mpd)
	{
		this.mpd = mpd;
		id = 0x5;
		IOHandler.register(this);
		new Thread(this).start();
	}
	public void send(int data)
	{
		if (data == 0)
		{
			//while (keybuffer.size()==0){try{Thread.sleep(10);if (mpd.exit)return;}catch(Exception e){}}
			if (keybuffer.size()==0){Registers.r4 = 0;return;}
			Registers.r4 = keybuffer.get(keybuffer.size()-1);
			keybuffer.remove(keybuffer.size()-1);
			//System.out.println("SEND KEY TO MPD: " + (char)Registers.r4);
		}
	}
	public void run()
	{
		if (EngineTest.inUse)
		{
			Keyboard.enableRepeatEvents(true);
			while (!mpd.exit)
			{
				try{Keyboard.poll();
				while (Keyboard.getNumKeyboardEvents() > 0)
				{
					if (Mouse.isButtonDown(0))
					{
						int l = Keyboard.getEventCharacter();
						if (l==13)
							l = 0x0A;
						keybuffer.add(0, (char)l);
					}
					Keyboard.next();
				}
				}catch(Exception e){e.printStackTrace();System.exit(-1);}
			}
			System.out.println("\tDestroy mpd keylistener.");
		}
		else
		{
			mpd.scr.frame.addKeyListener(new KeyboardEmulator(this));
		}
	}
	public void setID(int i)
	{
		id = i;
	}
	public int getID()
	{
		return id;
	}
}
class KeyboardEmulator extends KeyAdapter
{
	KB keyboard;
	public KeyboardEmulator(KB keyboard)
	{
		this.keyboard = keyboard;
	}
	public void keyPressed(KeyEvent e)
	{
		keyboard.keybuffer.add(0, Character.toLowerCase((char)e.getKeyCode()));
	}
}