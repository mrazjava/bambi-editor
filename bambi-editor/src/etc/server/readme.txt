This directory contains examples of scripts that can be written on a remote 
server to process input sent from bambi editor. Bambi does not require any 
specific technology on the server side to process its output, so long the 
server script is comptabile with bambi's output method (eg. form post) and 
it supports bambi's response protocol for that output format. The response 
protocol is used by bambi to communicate the status of upload with the server 
and report it back to end user.

FormPost response protocol:

KEY|VALUE - one line, lines can be in any order
Keys:

	STATUS - OK or ERROR, required
	RECEIVED - number of bytes received, required
	PROCESSED - number of bytes actually processed, required
	DATE - server date/time when request was processed, optional
	MSG - message from server (error or info), optional