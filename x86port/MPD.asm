MPD.onLoad :
	pusha
		push dword MPD.conStruct
		call iConsole2.RegisterCommand
		push dword MPD.fsBindingStruct
		call iConsole2.RegisterFiletypeBinding
	popa
	ret
MPD.fsBindingStruct :
	dd .name
	dd MPD.initFromBinding
	dd null
	.name :
		db "mpdimg", 0
MPD.conStruct :
	dd .name
	dd MPD.initFromConsole
	dd null
	.name :
		db "mpd", 0

MPD.initFromBinding :
	pusha
		; arg stored directly in iConsole2.commandStore
		; no need to return blind
		mov dword [MPD.imgFileNamePointer], iConsole2.commandStore
		call MPD.init
	popa
	ret

MPD.initFromConsole :
	enter 0, 0
	pusha
		; arg pushed to the stack
		; need to return blind
		cmp dword [iConsole2.argNum], 1
			jne .invalidArgs
		mov eax, [ebp+8]
		mov [MPD.imgFileNamePointer], eax
		call MPD.init
	.aret :
	popa
	leave
	call iConsole2.returnBlind
	.invalidArgs :
		mov ebx, MPD.ERROR_INVALID_ARGNUM
		call iConsole2.Echo
	jmp .aret
MPD.ERROR_INVALID_ARGNUM :
	db "[MPD][Fatal] Invalid number of arguments supplied.", 0

MPD.init :
	pusha
		
		mov eax, [MPD.imgFileNamePointer]
		call Minnow4.getFilePointer
		cmp ebx, Minnow4.SUCCESS
			jne .loadFail
		
		call GMEM.init
		
		; copy entire file (filepointer eax) into GMEM.instance:data
		mov ecx, [GMEM.instancePointer]
		mov ecx, [ecx+Structure_data]
		mov edx, [GMEM.instancePointer]
		mov edx, [edx+Structure_memsize]	; fill the entire memory buffer (yes, this is a bad idea)
		call Minnow4.readBuffer
		
		; create a new screen and whatnot
		; ... keyboard too
		; also, the screen window's closeCallback should de-register the task
		
		mov dword [Registers.ipointer], 0
		
		; register the task here!
		push dword MPD.taskStruct
		call iConsole2.RegisterTask
		
	popa
	ret
	
	.loadFail :
			push dword MPD.ERROR_LOADING_FILE
			call iConsole2.Echo
		popa
		ret
MPD.imgFileNamePointer :
	dd null

MPD.ERROR_LOADING_FILE :
	db "[MPD][Fatal] Could not open/read input file.", 0

MPD.taskStruct :
	dd MPD.loop
	dd null
MPD.loop :	; placeholder() in the Java emulator
	pusha
		mov ecx, [Registers.ipointer]
		call MemoryHandler.read
		cmp ecx, 0x18
			jg MPD.unrecognizedOpcode
		inc dword [Registers.ipointer]
		shl ecx, 2
		add ecx, MPD.OPCODE_FUNCTION_MAP
		call [ecx]
	popa
	ret

MPD.mov :
	pusha
		mov ecx, -1
		call Specifier.Create
		mov eax, ecx	; eax = dest
		push eax
		
		xor ecx, ecx
		mov cl, [eax+Specifier_mod]
		call Specifier.Create	; ecx = src
		push ecx
		
		mov ebx, ecx
		call Specifier.GetValIn	; ecx = src val
		
		mov edx, ecx
		mov ebx, eax
		call Specifier.WriteValOut
		
		pop ebx
		call Guppy2.free
		pop ebx
		call Guppy2.free
	popa
	ret

MPD.add :
	pusha
		mov ecx, -1
		call Specifier.Create
		mov eax, ecx	; eax = dest
		push eax
		
		xor ecx, ecx
		mov cl, [eax+Specifier_mod]
		call Specifier.Create
		mov edx, ecx	; edx = src
		push edx
		
		mov ebx, edx
		call Specifier.GetValIn
		mov edx, ecx	; edx = src val
		
		mov ebx, eax
		call Specifier.GetValIn	; ecx = dest val
		
		add edx, ecx
		mov ebx, eax
		call Specifier.WriteValOut
		
		pop ebx
		call Guppy2.free
		pop ebx
		call Guppy2.free
	popa
	ret

MPD.sub :
	pusha
		mov ecx, -1
		call Specifier.Create
		mov eax, ecx	; eax = dest
		push eax
		
		xor ecx, ecx
		mov cl, [eax+Specifier_mod]
		call Specifier.Create
		mov edx, ecx	; edx = src
		push edx
		
		mov ebx, edx
		call Specifier.GetValIn
		mov edx, ecx	; edx = src val
		
		mov ebx, eax
		call Specifier.GetValIn	; ecx = dest val
		
		sub ecx, edx
		mov edx, ecx
		mov ebx, eax
		call Specifier.WriteValOut
		
		pop ebx
		call Guppy2.free
		pop ebx
		call Guppy2.free
	popa
	ret

MPD.gotorel :
	pusha
		mov ecx, -1
		call Specifier.Create
		mov ebx, ecx	; ebx = dest
		call Specifier.GetValIn	; ecx = dest val
		
		add [Registers.ipointer], ecx
		
		call Guppy2.free
	popa
	ret

MPD.gotoabs :
	pusha
		mov ecx, -1
		call Specifier.Create
		mov ebx, ecx
		call Specifier.GetValIn
		
		mov [Registers.ipointer], ecx
		
		call Guppy2.free
	popa
	ret

MPD.send :
	pusha
		mov ecx, -1
		call Specifier.Create
		mov eax, ecx	; eax = dest
		push eax
		
		mov ecx, -1
		call Specifier.Create
		mov ebx, ecx	; ebx = data
		push ebx
		
		call Specifier.GetValIn
		mov ecx, edx	; ecx = data val
		
		mov ebx, eax
		call Specifier.GetValIn	; edx = dest val
		
		mov ebx, edx
		mov edx, ecx
		call IOHandler.send
		
		pop ebx
		call Guppy2.free
		pop ebx
		call Guppy2.free
	popa
	ret

MPD.push :
	ret

MPD.spoofPush :
	ret

MPD.pop :
	ret

MPD.spoofPop :
	ret

MPD.jc :	; just check ecx to figure out which comparison to perform
	ret

MPD.mul :
	ret

MPD.div :
	ret

MPD.lsh :
	ret

MPD.rsh :
	ret

MPD.and :
	ret

MPD.or :
	ret

MPD.xor :
	ret

MPD.not :
	ret

MPD.call :
	ret

MPD.ret :
	ret

MPD.unrecognizedOpcode :
		push dword MPD.ERROR_UNRECOGNIZED_OPCODE
		call iConsole2.Echo
		call MPD.cleanup
	popa
	ret

MPD.cleanup :
	pusha
		push dword MPD.taskStruct
		call iConsole2.UnregisterTask
		; need some way to gracefully shut down all peripherals and free memory here...
	popa
	ret

MPD.ERROR_UNRECOGNIZED_OPCODE :
	db "[MPD][Fatal] Attempt to execute unrecognized opcode.", 0

MPD.OPCODE_FUNCTION_MAP :
	dd MPD.mov, MPD.add, MPD.sub, MPD.gotorel, MPD.gotoabs, MPD.send, MPD.jc, MPD.jc
	dd MPD.jc, MPD.jc, MPD.jc, MPD.jc, MPD.jc, MPD.mul, MPD.div, MPD.and
	dd MPD.or, MPD.xor, MPD.not, MPD.lsh, MPD.rsh, MPD.push, MPD.pop, MPD.call
	dd MPD.ret

%include "MemoryHandler.asm"
%include "Specifier.asm"
%include "OSStubs.asm" ; will not remain in the final
