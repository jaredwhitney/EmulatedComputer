Guppy2.malloc :
Guppy2.free :
iConsole2.returnBlind :
iConsole2.Echo :
iConsole2.RegisterCommand :
iConsole2.RegisterFiletypeBinding :
iConsole2.RegisterTask :
iConsole2.UnregisterTask :
Minnow4.getFilePointer :
Minnow4.readBuffer :
	cli
	hlt
	jmp $

iConsole2.commandStore :
	dd 0x0
iConsole2.argNum :
	dd 0x0

null equ 0
Minnow4.SUCCESS equ 0