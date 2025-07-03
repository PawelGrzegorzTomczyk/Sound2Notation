from flask import Flask, session, g, send_from_directory
import os
from flask import request, jsonify
import sqlite3
from werkzeug.security import generate_password_hash, check_password_hash
import mimetypes
import uuid
import time
import threading
from basicPitchConverter import process

UPLOAD_FOLDER = 'uploads'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

XML_FOLDER = 'xmlFiles'
if not os.path.exists(XML_FOLDER):
    os.makedirs(XML_FOLDER)

guestFolder = os.path.join(XML_FOLDER, 'guest')
if not os.path.exists(guestFolder):
    os.makedirs(guestFolder)

tasks = {}

app = Flask(__name__)
app.secret_key = "secret_key_do_zmiany_bo_bezpieczenstwo_w_tym_momencie_jest_praktycznie_zerowe"

DB_PATH = 'dataBases/app.db'  # jedna baza dla wszystkiego

def process_file(task_id, user_login='guest'):
    filename_input = tasks[task_id]["filename"]
    filename_midi = os.path.join(UPLOAD_FOLDER, f"{task_id}.mid") # właściwie bez znaczenia gdzie, bo i tak od razu będzie usunięte
    if user_login == 'guest':
        filename_xml = os.path.join(guestFolder, f"{task_id}.musicxml")
    else:
        filename_xml = os.path.join(XML_FOLDER, f"{task_id}.musicxml")

    tasks[task_id]["status"] = "processing"
    print(f"Processing {filename_input}")

    try:
        process(filename_input, filename_midi, filename_xml)
        print(f"Processed {filename_input}")
        tasks[task_id]["xml_file"] = filename_xml
        tasks[task_id]["status"] = "done"
    except Exception as e:
        print(f"Error processing file {filename_input}: {e}")
        tasks[task_id]["status"] = "error"
        tasks[task_id]["error_message"] = str(e)

# Inicjalizacja bazy SQLite i tworzenie tabel
def init_db():
    if not os.path.exists('dataBases'):
        os.makedirs('dataBases')

    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute('''
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            login TEXT UNIQUE NOT NULL,
            password_hash TEXT NOT NULL
        )
    ''')
    c.execute('''
        CREATE TABLE IF NOT EXISTS uploads (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_login TEXT NOT NULL,
            uuid TEXT UNIQUE NOT NULL,
            original_filename TEXT NOT NULL,
            upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    conn.commit()
    conn.close()

init_db()

# Pobranie połączenia do bazy, reużywane w kontekście app
def get_db():
    db = getattr(g, '_database', None)
    if db is None:
        db = g._database = sqlite3.connect(DB_PATH)
    return db

@app.teardown_appcontext
def close_connection(exception):
    db = getattr(g, '_database', None)
    if db is not None:
        db.close()

@app.route('/xmlFiles/<path:filename>')
def serve_xml_files(filename):
    return send_from_directory('xmlFiles', filename)

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    login = data.get('login')
    password = data.get('password')

    print(f"DEBUG: Received registration request with login: {login, password}")

    if not login or not password:
        return jsonify({'error': 'Brakuje loginu lub hasła'}), 400

    password_hash = generate_password_hash(password)

    try:
        db = get_db()
        c = db.cursor()
        c.execute('INSERT INTO users (login, password_hash) VALUES (?, ?)', (login, password_hash))
        db.commit()
    except sqlite3.IntegrityError:
        return jsonify({'error': 'Użytkownik o takim loginie już istnieje'}), 409

    return jsonify({'message': 'Rejestracja zakończona sukcesem'}), 201


@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    login = data.get('login')
    password = data.get('password')

    if not login or not password:
        return jsonify({'error': 'Brakuje loginu lub hasła'}), 400

    db = get_db()
    c = db.cursor()
    c.execute('SELECT password_hash FROM users WHERE login = ?', (login,))
    row = c.fetchone()

    if row is None or not check_password_hash(row[0], password):
        return jsonify({'error': 'Nieprawidłowy login lub hasło'}), 401

    session['user'] = login
    return jsonify({'message': f'Zalogowano jako {login}'})

@app.route('/profile')
def profile():
    if 'user' not in session:
        return jsonify({'error': 'Nie jesteś zalogowany'}), 401
    return jsonify({'message': f'Witaj, {session["user"]}!'})

@app.route('/myfiles', methods=['GET'])
def my_files():
    if 'user' not in session:
        return jsonify({'error': 'Nie jesteś zalogowany'}), 403

    user_login = session['user']
    db = get_db()
    cursor = db.cursor()
    cursor.execute('SELECT uuid, original_filename, upload_date FROM uploads WHERE user_login = ?', (user_login,))
    files = cursor.fetchall()

    files_list = [{'uuid': f[0], 'filename': f[1], 'upload_date': f[2]} for f in files]

    return jsonify({'files': files_list}), 200

@app.route('/test', methods=['GET'])
def test():
    return jsonify({"status": "ok"}), 200


@app.route('/upload', methods=['POST'])
def upload():
    f = request.files.get('file')
    if not f:
        return jsonify({"error": "No file provided"}), 400

    client_mimetype = f.mimetype
    client_filename = f.filename
    print(f"DEBUG: Received file with MIME type: {client_mimetype}, Filename: {client_filename}")

    mime_to_extension_map = {
        'audio/wav': 'wav',
        'audio/x-wav': 'wav',
        'audio/mpeg': 'mp3',
        'audio/ogg': 'ogg',
        'audio/flac': 'flac',
        'audio/aac': 'aac',
        'audio/x-m4a': 'm4a',
        'application/octet-stream': 'wav'
    }

    file_extension = None

    if client_filename and '.' in client_filename:
        potential_extension = client_filename.split('.')[-1].lower()
        if potential_extension in ['wav', 'mp3', 'ogg', 'flac', 'm4a', 'aac']:
            file_extension = potential_extension
    
    if not file_extension:
        if client_mimetype in mime_to_extension_map:
            file_extension = mime_to_extension_map[client_mimetype]
        else:
            guessed_ext = mimetypes.guess_extension(client_mimetype, strict=False)
            if guessed_ext:
                file_extension = guessed_ext[1:]

    print(f"DEBUG: Determined file_extension: {file_extension}")

    if not file_extension:
        return jsonify({
            "error": f"Invalid or unrecognized file type: MIME type ({client_mimetype}), Filename ({client_filename}). Could not determine a valid file extension.",
            "supported_mimes_expected": list(mime_to_extension_map.keys())
        }), 400

    logged = 'user' in session and session['user'] is not None
    user_login = session['user'] if logged else 'guest'

    task_id = str(uuid.uuid4())
    filename = f"{task_id}.{file_extension}"
    filepath = os.path.join(UPLOAD_FOLDER, filename)

    try:
        f.save(filepath)
        print(f"DEBUG: File saved as: {filepath}")
    except Exception as e:
        print(f"ERROR: Failed to save file: {e}")
        return jsonify({"error": f"Server failed to save file: {e}"}), 500

    # Zapisywanie do bazy
    db = get_db()
    cursor = db.cursor()
    cursor.execute(
        'INSERT INTO uploads (user_login, uuid, original_filename) VALUES (?, ?, ?)',
        (user_login, task_id, client_filename)
    )
    db.commit()

    tasks[task_id] = {"status": 'processing', "filename": filepath, "mid_file": None, "created_at": time.time()}
    threading.Thread(target=process_file, args=(task_id, user_login)).start()

    return jsonify({"status": 'processing', "task_id": task_id}), 202


@app.route('/result/<task_id>', methods=['GET'])
def result(task_id):
    task_info = tasks.get(task_id)
    if not task_info:
        if 'user' in session and session['user'] is not None:
            user_login = session['user']
            db = get_db()
            cursor = db.cursor()
            cursor.execute('SELECT uuid FROM uploads WHERE user_login =? and uuid = ?', (user_login, task_id, ))
            row = cursor.fetchone()
            if row:
                return jsonify({"status": "done", "xml_file":os.path.join(XML_FOLDER, f"{task_id}.musicxml")}), 200
        else:
            return jsonify({"error": "There is no such id!"}), 404
    
    if task_info["status"] == 'done' and task_info["xml_file"] and os.path.exists(task_info["xml_file"]):
        print(f"DEBUG: Returning XML filename for task {task_id}")
        return jsonify({"status": "done", "xml_file": task_info["xml_file"]}), 200
    elif task_info["status"] == 'error':
        return jsonify({"status": "error", "message": task_info.get("error_message", "Processing failed")}), 500
    else:
        return jsonify({"status": task_info["status"], "message": "File is not ready yet."}), 202



if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
