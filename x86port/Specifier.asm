; byte type
; byte mod
; byte valb
; int val
Specifier.CLASS_SIZE equ 0x6
Specifier_type equ 0x0
Specifier_mod equ 0x1
Specifier_valb equ 0x2
Specifier_val equ 0x3


Specifier.TYPE_REG equ 0x10
Specifier.TYPE_RPT equ 0x20
Specifier.TYPE_VFL equ 0x30

Specifier.CreateBlank :
	push ebx
		mov ebx, Specifier.CLASS_SIZE
		call Guppy2.malloc
		mov ecx, ebx
	pop ebx
	ret

Specifier.Create :
	push edx
	push eax
	push ebx
		push ecx
		call Specifier.CreateBlank
		mov ebx, ecx
		pop ecx
		
		cmp ecx, -1
			je .noInherit
		mov [ebx+Specifier_mod], cl
		.noInherit :
		mov ecx, [Registers.ipointer]
		call MemoryHandler.read	; ecx -> loc in, ecx -> data out
		mov [.b], cl
		inc dword [Registers.ipointer]
		mov cl, [.b]
		and cl, 0xF0
		mov [ebx+Specifier_type], cl
		mov cl, [.b]
		and cl, 0xF
		mov [ebx+Specifier_valb], cl
		cmp byte [ebx+Specifier_type], 0x90
			jne .nomod
		mov cl, [.b]
		and cl, 0xF
		mov [ebx+Specifier_mod], cl
		mov ecx, [Registers.ipointer]
		call MemoryHandler.read
		mov [.b], cl
		inc dword [Registers.ipointer]
		mov cl, [.b]
		and cl, 0xF0
		mov [ebx+Specifier_type], cl
		mov cl, [.b]
		and cl, 0xF
		mov [ebx+Specifier_valb], cl
		.nomod :
		cmp byte [ebx+Specifier_type], Specifier.TYPE_VFL
			jne .notVFL
		cmp byte [ebx+Specifier_valb], 1
			je .isInt
		cmp byte [ebx+Specifier_mod], -1
			je .isInt
		cmp byte [ebx+Specifier_mod], 4
			je .isInt
		jmp .notInt
		.isInt :
		xor eax, eax
		mov ecx, [Registers.ipointer]
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [Registers.ipointer]
		add ecx, 1
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [Registers.ipointer]
		add ecx, 2
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [Registers.ipointer]
		add ecx, 3
		call MemoryHandler.read
		or eax, ecx
		mov [ebx+Specifier_val], eax
		add dword [Registers.ipointer], 4
		jmp .ret
		.notInt :
		cmp byte [ebx+Specifier_mod], 2
			jne .notWord
		xor eax, eax
		mov ecx, [Registers.ipointer]
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [Registers.ipointer]
		add ecx, 1
		call MemoryHandler.read
		or eax, ecx
		mov [eax+Specifier_val], eax
		add dword [Registers.ipointer], 2
		jmp .ret
		.notWord :
		cmp byte [ebx+Specifier_mod], 1
			jne .ret
		mov ecx, [Registers.ipointer]
		call MemoryHandler.read
		mov [eax+Specifier_val], ecx
		inc dword [Registers.ipointer]
		.notVFL :
	.ret :
	mov ecx, ebx
	pop ebx
	pop eax
	pop edx
	ret
	.b :
		db 0

Specifier.GetValIn :	; Specifier in ebx, returns val in ecx
	push edx
	push eax
		;mov dword [.val], 0
		cmp byte [ebx+Specifier_type], Specifier.TYPE_REG
			jne .notReg
		xor eax, eax
		mov al, [ebx+Specifier_valb]
		shl eax, 2
		add eax, Specifier.RegisterTable
		mov eax, [eax]
		mov [ebx+Specifier_val], eax
		cmp byte [ebx+Specifier_mod], 2
			jne .regNoMod2
		and dword [ebx+Specifier_val], 0xFFFF
		.regNoMod2 :
		cmp byte [ebx+Specifier_mod], 1
			jne .regNoMod1
		and dword [ebx+Specifier_val], 0xFF
		.regNoMod1 :
		.notReg :
		cmp byte [ebx+Specifier_type], Specifier.TYPE_RPT
			jne .notRPT
		xor eax, eax
		mov al, [ebx+Specifier_valb]
		shl eax, 2
		add eax, Specifier.RegisterTable
		mov eax, [eax]
		mov [ebx+Specifier_val], eax
		cmp byte [ebx+Specifier_mod], 2
			jne .rptNoMod2
		xor eax, eax
		mov ecx, [ebx+Specifier_val]
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [ebx+Specifier_val]
		add ecx, 1
		call MemoryHandler.read
		or eax, ecx
		mov [ebx+Specifier_val], eax
		.rptNoMod2 :
		cmp byte [ebx+Specifier_mod], 1
			jne .rptNoMod1
		mov ecx, [ebx+Specifier_val]
		call MemoryHandler.read
		mov [ebx+Specifier_val], ecx
		jmp .rptNoDwordMod
		.rptNoMod1 :
		xor eax, eax
		mov ecx, [ebx+Specifier_val]
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [ebx+Specifier_val]
		add ecx, 1
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [ebx+Specifier_val]
		add ecx, 2
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [ebx+Specifier_val]
		add ecx, 3
		call MemoryHandler.read
		or eax, ecx
		mov [ebx+Specifier_val], eax
		.rptNoDwordMod :
		.notRPT :
		cmp byte [ebx+Specifier_type], Specifier.TYPE_VFL
			jne .notVFL
		cmp byte [ebx+Specifier_valb], 1
			jne .vflNob1
		cmp byte [ebx+Specifier_mod], 2
			jne .vflNoMod2
		xor eax, eax
		mov ecx, [ebx+Specifier_val]
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [ebx+Specifier_val]
		add ecx, 1
		call MemoryHandler.read
		or eax, ecx
		mov [ebx+Specifier_val], eax
		.vflNoMod2 :
		cmp byte [ebx+Specifier_mod], 1
			jne .vflNoMod1
		mov ecx, [ebx+Specifier_val]
		call MemoryHandler.read
		mov [ebx+Specifier_val], ecx
		jmp .vflNoDwordMod
		.vflNoMod1 :
		xor eax, eax
		mov ecx, [ebx+Specifier_val]
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [ebx+Specifier_val]
		add ecx, 1
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [ebx+Specifier_val]
		add ecx, 2
		call MemoryHandler.read
		or eax, ecx
		shl eax, 8
		mov ecx, [ebx+Specifier_val]
		add ecx, 3
		call MemoryHandler.read
		or eax, ecx
		mov [ebx+Specifier_val], eax
		.vflNoDwordMod :
		.vflNob1 :
		.notVFL :
	pop eax
	pop edx
	mov ecx, [ebx+Specifier_val]
	ret

Specifier.WriteValOut :	; Specifier in ebx, val in edx
	pusha
		cmp byte [ebx+Specifier_type], Specifier.TYPE_REG
			jne .notReg
		cmp byte [ebx+Specifier_mod], -1
			je .regIs4
		cmp byte [ebx+Specifier_mod], 4
			jne .regNot4
		.regIs4 :
		xor eax, eax
		mov al, [ebx+Specifier_valb]
		shl eax, 2
		add eax, Specifier.RegisterTable
		mov eax, [eax]
		mov [eax], edx
		jmp .kret
		.regNot4 :
		cmp byte [ebx+Specifier_mod], 2
			jne .regNot2
		xor eax, eax
		mov al, [ebx+Specifier_valb]
		shl eax, 2
		add eax, Specifier.RegisterTable
		mov eax, [eax]
		and dword [eax], 0xFFFF0000
		or [eax], edx
		jmp .kret
		.regNot2 :
		cmp byte [ebx+Specifier_mod], 1
			jne .regNot1
		xor eax, eax
		mov al, [ebx+Specifier_valb]
		shl eax, 2
		add eax, Specifier.RegisterTable
		mov eax, [eax]
		and dword [eax], 0xFFFFFF00
		or [eax], edx
		jmp .kret
		.regNot1 :
		.notReg :
		cmp byte [ebx+Specifier_type], Specifier.TYPE_RPT
			jne .notRPT
		xor eax, eax
		mov al, [ebx+Specifier_valb]
		shl eax, 2
		add eax, Specifier.RegisterTable
		mov eax, [eax]
		mov eax, [eax]
		cmp byte [ebx+Specifier_mod], 2
			jne .rptNoMod2
		push edx
		mov ecx, eax
		add ecx, 1
		and edx, 0xFF
		call MemoryHandler.write
		pop edx
		dec ecx
		shr edx, 8
		and edx, 0xFF
		call MemoryHandler.write
		jmp .kret
		.rptNoMod2 :
		cmp byte [ebx+Specifier_mod], 1
			jne .rptNoMod1
		mov ecx, eax
		and edx, 0xFF
		call MemoryHandler.write
		jmp .kret
		.rptNoMod1 :	; dont even bother checking for -1 / 4
		push edx
		mov ecx, eax
		add ecx, 3
		and edx, 0xFF
		call MemoryHandler.write
		pop edx
		push edx
		dec ecx
		shr edx, 8
		and edx, 0xFF
		call MemoryHandler.write
		pop edx
		push edx
		dec ecx
		shr edx, 16
		and edx, 0xFF
		call MemoryHandler.write
		pop edx
		dec ecx
		shr edx, 24
		and edx, 0xFF
		call MemoryHandler.write
		jmp .kret
		.notRPT :
		cmp byte [ebx+Specifier_type], Specifier.TYPE_VFL
			jne .kret
		mov eax, [ebx+Specifier_val]
		cmp byte [ebx+Specifier_valb], 0
			je .kret	; should throw a 'do val, x' unallowed error
		cmp byte [ebx+Specifier_valb], 2
			jne .vflNoMod2
		push edx
		mov ecx, eax
		add ecx, 1
		and edx, 0xFF
		call MemoryHandler.write
		pop edx
		dec ecx
		shr edx, 8
		and edx, 0xFF
		call MemoryHandler.write
		jmp .kret
		.vflNoMod2 :
		cmp byte [ebx+Specifier_valb], 1
			jne .vflNoMod1
		mov ecx, eax
		and edx, 0xFF
		call MemoryHandler.write
		jmp .kret
		.vflNoMod1 :	; dont bother checking for 4 / -1
		push edx
		mov ecx, eax
		add ecx, 3
		and edx, 0xFF
		call MemoryHandler.write
		pop edx
		push edx
		dec ecx
		shr edx, 8
		and edx, 0xFF
		call MemoryHandler.write
		pop edx
		push edx
		dec ecx
		shr edx, 16
		and edx, 0xFF
		call MemoryHandler.write
		pop edx
		dec ecx
		shr edx, 24
		and edx, 0xFF
		call MemoryHandler.write
	.kret :
	popa
	ret

Specifier.RegisterTable :
	dd Registers.r0, Registers.r1, Registers.r2, Registers.r3, Registers.r4, 0, 0, 0, 0, 0, 0, 0, Registers.sp, Registers.ipointer, 0, 0