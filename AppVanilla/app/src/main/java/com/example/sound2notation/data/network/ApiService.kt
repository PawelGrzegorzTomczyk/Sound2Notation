package com.example.sound2notation.data.network

import com.example.sound2notation.data.model.LoginRequest
import com.example.sound2notation.data.model.LoginResponse
import com.example.sound2notation.data.model.MyFilesResponse
import com.example.sound2notation.data.model.ProfileResponse
import com.example.sound2notation.data.model.RegisterRequest
import com.example.sound2notation.data.model.RegisterResponse
import com.example.sound2notation.data.model.ResultResponse
import com.example.sound2notation.data.model.TestResponse
import com.example.sound2notation.data.model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


interface ApiService {
//    @app.route('/register', methods=['POST'])
//    return jsonify({'error': 'Użytkownik o takim loginie już istnieje'}), 409
//    return jsonify({'message': 'Rejestracja zakończona sukcesem'}), 201
//    return jsonify({'error': 'Brakuje loginu lub hasła'}), 400
    @POST("/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

//    @app.route('/login', methods=['POST'])
//    return jsonify({'error': 'Brakuje loginu lub hasła'}), 400
//    return jsonify({'error': 'Nieprawidłowy login lub hasło'}), 401
//    return jsonify({'message': f'Zalogowano jako {login}'})
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

//    @app.route('/profile')
//    return jsonify({'error': 'Nie jesteś zalogowany'}), 401
//    return jsonify({'message': f'Witaj, {session["user"]}!'})
    @GET("/profile")
    suspend fun profile(): Response<ProfileResponse>

//    @app.route('/myfiles', methods=['GET'])
//    return jsonify({'error': 'Nie jesteś zalogowany'}), 403
//    return jsonify({'files': files_list})
    @GET("/myfiles")
    suspend fun myFiles(): Response<MyFilesResponse>

//    @app.route('/test', methods=['GET'])
//    return jsonify({"status": "ok"}), 200
    @GET("/test")
    suspend fun test(): Response<TestResponse>

//    @app.route('/upload', methods=['POST'])
//    return jsonify({"error": "No file provided"}), 400
//    return jsonify({
//        "error": f"Invalid or unrecognized file type: MIME type ({client_mimetype}), Filename ({client_filename}). Could not determine a valid file extension.",
//        "supported_mimes_expected": list(mime_to_extension_map.keys())
//    }), 400
//    return jsonify({"error": f"Server failed to save file: {e}"}), 500
//    return jsonify({"status": 'processing', "task_id": task_id}), 202
    @Multipart
    @POST("/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

//    @app.route('/result/<task_id>', methods=['GET'])
//    return jsonify({"error": "There is no such id!"}), 404
//    return jsonify({"status": "done", "xml_file": task_info["xml_file"]}), 200
//    return jsonify({"status": "error", "message": task_info.get("error_message", "Processing failed")}), 500
//    return jsonify({"status": task_info["status"], "message": "File is not ready yet."}), 400
    @GET("/result/{task_id}")
    suspend fun getResult(
        @Path("task_id") taskId: String
    ): Response<ResultResponse>

}
