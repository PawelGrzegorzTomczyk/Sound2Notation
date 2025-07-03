🖥️ Sound2Notation Server
This is a simple Flask-based backend for the Sound2Notation Android application. It is intended only for testing and development purposes – not for production use.

📁 Project Structure
	-server.py – Main Flask server file. Run using: 
		python3.11 server.py
		
		Required Python packages (install if not already available):
			from flask import Flask, session, g, send_from_directory, request, jsonify
			import os
			import sqlite3
			from werkzeug.security import generate_password_hash, check_password_hash
			import mimetypes
			import uuid
			import time
			import threading
			from basicPitchConverter import process
			
	-basicPitchConverter.py – Handles audio-to-MusicXML conversion using Spotify’s Basic Pitch. Requires a path for temporary MIDI storage and final XML output.
		Required packages:
			from basic_pitch.inference import predict
			from basic_pitch import ICASSP_2022_MODEL_PATH
			from music21 import midi, converter
			import os
	
	-static/displayScore.html – HTML page that uses OpenSheetMusicDisplay to visualize MusicXML files in the browser.
	
	-static/osmd.min.js – Minified JavaScript library for rendering MusicXML with OpenSheetMusicDisplay (OSMD).
	
Possible Improvements
	-Automatic cleanup of unused files from:
		uploads/
		xmlFiles/guest/

	-User file management:
		Add ability to delete specific files from both the server and database.

	-Security:
		Replace the test-only app.secret_key with a secure, randomly generated key for production environments.
		
🎼 Viewing MusicXML Scores
To display generated MusicXML sheet music in a web browser, use the following URL format:
	http://<server_ip>:<port>/static/displayScore.html?scoreUrl=http://<server_ip>:<port>/<filename>.xml
	
	Example: http://127.0.0.1:5000/static/displayScore.html?scoreUrl=http://127.0.0.1:5000/static/piano.musicxml

