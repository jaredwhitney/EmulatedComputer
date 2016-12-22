MemoryHandler.structs.firstLink :
	dd 0x0

MemoryHandler.write :	; ecx = loc, edx = data
	pusha
		mov eax, ecx
		call MemoryHandler.getStructure
		sub eax, [ecx+Structure_memloc]
		mov ebx, ecx
		mov ecx, eax
		call Structure.write ; ebx = Structure, ecx = loc, edx = data
	popa
	ret

MemoryHandler.read :	; ecx = loc, return ecx = data
	push ebx
	push edx
		push ecx
		call MemoryHandler.getStructure
		mov ebx, ecx
		pop ecx
		sub ecx, [ebx+Structure_memloc]
		call Structure.read	; ebx = Structure, ecx =loc, return edx = data
		mov ecx, edx
		and ecx, 0xFF
	pop edx
	pop ebx
	ret
	
MemoryHandler.getStructure :	; ecx = loc, return ecx = Structure
	push edx
	push eax
		mov eax, [MemoryHandler.structs.firstLink]
		cmp eax, null
			je .useGMEM
		.loop :
		mov edx, [eax+Structure_memloc]
		cmp ecx, edx
			jg .cont
		add edx, [eax+Structure_memsize]
		cmp ecx, edx
			jle .cont
		jmp .aret
		.cont :
		mov eax, [eax+Structure_nextPointer]
		cmp eax, null
			jne .loop
		.useGMEM :
		mov eax, [GMEM.instancePointer]
		.aret :
		mov ecx, eax
	pop eax
	pop edx
	ret

MemoryHandler.register :	; ebx = Structure
	push eax
		mov eax, [MemoryHandler.structs.firstLink]
		mov [ebx+Structure_nextPointer], eax
		mov [MemoryHandler.structs.firstLink], ebx
	pop eax
	ret

GMEM.instancePointer :
	dd 0x0
GMEM.init :
	pusha
		mov ebx, Structure.CLASS_SIZE
		call Guppy2.malloc
		mov [GMEM.instancePointer], ebx
		mov edx, ebx
		mov ebx, 0xFFFFF
		call Guppy2.malloc
		mov [edx+Structure_data], ebx
		mov dword [edx+Structure_memloc], 0
		mov dword [edx+Structure_memsize], 0xFFFFF
		mov dword [edx+Structure_nextPointer], null
	popa
	ret

Registers.r0 :
	dd 0x0
Registers.r1 :
	dd 0x0
Registers.r2 :
	dd 0x0
Registers.r3 :
	dd 0x0
Registers.r4 :
	dd 0x0
Registers.sp :
	dd 0x0
Registers.ipointer :
	dd 0x0

IOHandler.devs.firstLink :
	dd 0x0

IOHandler.send :	; ebx = port, edx = data
	push ebx
		call IOHandler.getDevice ; return in ebx
		cmp ebx, null
			je .invalidDevice
		call IODevice.send
		.invalidDevice :
	pop ebx
	ret

IOHandler.getDevice :
	push eax
		mov eax, [IOHandler.devs.firstLink]
		cmp eax, null
			je .deviceMissing
		.loop :
		cmp ebx, [eax+IODevice_ID]
			je .aret
		mov eax, [eax+IODevice_nextPointer]
		cmp eax, null
			jne .loop
		.deviceMissing :
		mov ebx, null
		jmp .ret
		.aret :
		mov ebx, eax
		.ret :
	pop eax
	ret
	
IOHandler.register :	; ebx = Structure
	push eax
		mov eax, [IOHandler.devs.firstLink]
		mov [ebx+IODevice_nextPointer], eax
		mov [IOHandler.devs.firstLink], ebx
	pop eax
	ret

Structure.CLASS_SIZE equ 0x10
Structure_nextPointer equ 0x0
Structure_data equ 0x4
Structure_memloc equ 0x8
Structure_memsize equ 0xC

Structure.Create :
	push ebx
		mov ebx, Structure.CLASS_SIZE
		call Guppy2.malloc
		mov ecx, ebx
	pop ebx
	ret

Structure.write :	; ebx = Structure, ecx = loc, dl = data
	push eax
		mov eax, [ebx+Structure_data]
		add eax, ecx
		mov [eax], dl
	pop eax
	ret

Structure.read :	; ebx = Structure, ecx = loc, return dl = data
		mov edx, [ebx+Structure_data]
		add edx, ecx
		mov dl, [edx]
		and edx, 0xFF
	ret

IODevice_nextPointer equ 0x0
IODevice_ID equ 0x4
IODevice_sendFunc equ 0x8
IODevice.CLASS_SIZE equ 0xC

IODevice.Create :
	push ebx
		mov ebx, IODevice.CLASS_SIZE
		call Guppy2.malloc
		mov ecx, ebx
	pop ebx
	ret

IODevice.send :	; data in edx
	cmp dword [ebx+IODevice_sendFunc], null
		je .ret
	call dword [ebx+IODevice_sendFunc]
	.ret :
	ret