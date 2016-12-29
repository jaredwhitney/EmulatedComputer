Screen_image equ 0
Screen_xres equ 4
Screen_yres equ 8
Screen_bpp equ 12
Screen_window equ 13
Screen_mode equ 17
Screen_texNum equ 21
Screen_id equ 25
Screen_structureStruct equ 29
Screen_iodeviceStruct equ 33
Screen.CLASS_SIZE equ 37

Screen.Create :
	push ebx
		mov ebx, Screen.CLASS_SIZE
		call Guppy2.malloc
		mov ecx, ebx
		
		call Structure.Create
		mov [ecx+Screen_structureStruct], ebx
		
		call IODevice.Create
		mov [ecx+Screen_iodeviceStruct], ebx
	pop ebx
	ret

Screen.setID :	; id in edx
	push ebx
		mov ebx, [ebx+Screen_iodeviceStruct]
		mov [ebx+IODevice_ID], edx
	pop ebx
	ret

Screen.getID : ; returns id in edx
		mov edx, [ebx+Screen_iodeviceStruct]
		mov edx, [edx+IODevice_ID]
	ret


KF0BASIC_fontxres equ Screen.CLASS_SIZE
KF0BASIC_fontyres equ Screen.CLASS_SIZE+4
KF0BASIC_charData equ Screen.CLASS_SIZE+8
KF0BASIC.CLASS_SIZE equ Screen.CLASS_SIZE+12

KF0BASIC.Create :
	push ebx
	push edx
	push eax
		
		; alloc the object
		mov ebx, KF0BASIC.CLASS_SIZE
		call Guppy2.malloc
		mov ecx, ebx
		
		; create the screen's memory structure sub
		call Structure.Create
		mov dword [ebx+Structure_writeFunc], Screen.write
		mov [ecx+Screen_structureStruct], ebx
		
		; create the screen's iodevice sub
		call IODevice.Create
		mov dword [ebx+IODevice_ID], 0x7
		mov dword [ebx+IODevice_sendFunc], Screen.send
		mov [ecx+Screen_iodeviceStruct], ebx
		
		; set up the screen's resolution
		mov dword [ecx+Screen_xres], 640
		mov dword [ecx+Screen_yres], 480
		mov dword [ecx+KF0BASIC_fontxres], 640/10
		mov dword [ecx+KF0BASIC_fontyres], 480/(22+2)
		mov byte [ecx+Screen_bpp], 4
		
		; set up the screen's memory structure information
		mov ebx, 640*480*4
		call Guppy2.malloc
		mov edx, [ecx+Screen_structureStruct]
		mov dword [edx+Structure_data], ebx
		mov dword [edx+Structure_memsize], 640*480*4
		mov dword [edx+Structure_memloc], 0xA0000
		
		; alloc space for the screen's image
		mov ebx, 640*480*4
		call Guppy2.malloc
		mov [ecx+Screen_image], ebx
		
		; create the screen's window
		mov edx, ecx
		push dword MPD.WINDOW_TITLE
		push dword 0*4
		push dword 0
		push dword 640*4
		push dword 480
		call WinMan.CreateWindow
		xchg ecx, edx
		mov dword [ecx+Screen_window], edx
		
		; create the canvas and add it to the window
		mov eax, ecx
		mov ebx, [ecx+Screen_structureStruct]
		mov ebx, [ebx+Structure_data]
		push ebx
		push dword 640*8
		push dword 480
		push dword 0*4
		push dword 0
		push dword 640*8
		push dword 480
		call Image.Create
		xchg ecx, eax
		mov ebx, edx
		call Grouping.Add
		
		; add the window callbacks
		mov eax, MPD.CloseCallback
		call WindowGrouping.RegisterCloseCallback
		mov [ebx+Component_keyHandlerFunc], MPD.KeyCallback
		mov [ebx+Component_mouseHandlerFunc], MPD.MouseCallback
		
		; register the screen as a memory structure
		mov ebx, [ecx+Screen_structureStruct]
		call MemoryHandler.register
		
		; register the screen as an iodevice
		mov ebx, [ecx+Screen_iodeviceStruct]
		call IODevice.register
	
	pop eax
	pop edx
	pop ebx
	ret
	
Screen.write :	; int loc, byte dat
	ret
	
Screen.send :	; int loc, byte dat
	ret
