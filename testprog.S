
.
    . d d d d .... dsdfdfd


PROG START F   .comment
TEST LDCH BUFFER, X         . comment
	LDA	DATA
	STA	DATA, X
	RSUB		. comment
.comment
DATA	RESB	5	.comment
	END	TEST