package com.linkx.client;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static String accessToken;
    private static String refreshToken;
    private static Long userId;

    public static class ApiResponse<T> {
        public int code;
        public String message;
        public T data;
    }

    public static class AuthData {
        public String accessToken;
        public String refreshToken;
        public Long userId;
        public String username;
        public String nickname;
        public String avatar;
    }

    public static class UserData {
        public Long id;
        public String username;
        public String nickname;
        public String avatar;
        public String signature;
        public Integer gender;
        public String region;
        public String createTime;
    }

    public static void setTokens(String access, String refresh, Long uid) {
        accessToken = access;
        refreshToken = refresh;
        userId = uid;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static Long getUserId() {
        return userId;
    }

    public static AuthData register(String username, String nickname, String password) throws IOException {
        String json = gson.toJson(new RegisterReq(username, nickname, password));
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/auth/register")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body().string();
            ApiResponse<AuthData> resp = gson.fromJson(body, new com.google.gson.reflect.TypeToken<ApiResponse<AuthData>>(){}.getType());
            if (resp.code == 200) {
                setTokens(resp.data.accessToken, resp.data.refreshToken, resp.data.userId);
                return resp.data;
            }
            throw new IOException(resp.message);
        }
    }

    public static AuthData login(String username, String password) throws IOException {
        String json = gson.toJson(new LoginReq(username, password));
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/auth/login")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body().string();
            ApiResponse<AuthData> resp = gson.fromJson(body, new com.google.gson.reflect.TypeToken<ApiResponse<AuthData>>(){}.getType());
            if (resp.code == 200) {
                setTokens(resp.data.accessToken, resp.data.refreshToken, resp.data.userId);
                return resp.data;
            }
            throw new IOException(resp.message);
        }
    }

    public static UserData getMyProfile() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/user/me")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body().string();
            ApiResponse<UserData> resp = gson.fromJson(body, new com.google.gson.reflect.TypeToken<ApiResponse<UserData>>(){}.getType());
            if (resp.code == 200) {
                return resp.data;
            }
            throw new IOException(resp.message);
        }
    }

    public static UserData updateProfile(String nickname, Integer gender) throws IOException {
        String json = gson.toJson(new UpdateProfileReq(nickname, gender));
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/user/me")
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body().string();
            ApiResponse<UserData> resp = gson.fromJson(body, new com.google.gson.reflect.TypeToken<ApiResponse<UserData>>(){}.getType());
            if (resp.code == 200) {
                return resp.data;
            }
            throw new IOException(resp.message);
        }
    }

    // Request classes
    private static class RegisterReq {
        String username, nickname, password;
        RegisterReq(String u, String n, String p) { username = u; nickname = n; password = p; }
    }

    private static class LoginReq {
        String username, password;
        LoginReq(String u, String p) { username = u; password = p; }
    }

    private static class UpdateProfileReq {
        String nickname;
        Integer gender;
        UpdateProfileReq(String n, Integer g) {
            nickname = n; gender = g;
        }
    }
}
