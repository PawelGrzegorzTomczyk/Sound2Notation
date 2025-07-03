package com.example.sound2notation.data.network

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class SharedPreferencesCookieJar(context: Context) : CookieJar {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)

    private val cookies: ConcurrentHashMap<String, MutableList<Cookie>> = ConcurrentHashMap()

    init {
        loadAllCookies()
    }

    override fun saveFromResponse(url: HttpUrl, cookiesToSave: List<Cookie>) {
        val host = url.host
        val currentCookies = cookies.getOrPut(host) { mutableListOf() }

        val newCookies = cookiesToSave.filter { newCookie ->
            !currentCookies.any { existingCookie ->
                existingCookie.name == newCookie.name && existingCookie.domain == newCookie.domain
            }
        }
        currentCookies.addAll(newCookies)

        saveCookiesForHost(host, currentCookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val currentCookies = cookies[host] ?: mutableListOf()

        val validCookies = currentCookies.filter { cookie ->
            cookie.expiresAt > System.currentTimeMillis()
        }.toMutableList()

        if (validCookies.size != currentCookies.size) {
            cookies[host] = validCookies
            saveCookiesForHost(host, validCookies)
        }

        return validCookies
    }

    private fun saveCookiesForHost(host: String, hostCookies: List<Cookie>) {
        val editor = preferences.edit()
        val cookieStrings = hostCookies.map { cookie ->
            "${cookie.name}#${cookie.value}#${cookie.domain}#${cookie.path}#${cookie.expiresAt}#${cookie.secure}#${cookie.httpOnly}#${cookie.persistent}#${cookie.hostOnly}"
        }.toSet()

        editor.putStringSet(host, cookieStrings)
        editor.apply()
    }

    private fun loadAllCookies() {
        preferences.all.forEach { (host, cookieSet) ->
            if (cookieSet is Set<*>) {
                val hostCookies = mutableListOf<Cookie>()
                cookieSet.forEach { cookieString ->
                    if (cookieString is String) {
                        val parts = cookieString.split("#")
                        if (parts.size == 9) {
                            try {
                                val name = parts[0]
                                val value = parts[1]
                                val domain = parts[2]
                                val path = parts[3]
                                val expiresAt = parts[4].toLong()
                                val secure = parts[5].toBoolean()
                                val httpOnly = parts[6].toBoolean()
                                val persistent = parts[7].toBoolean()
                                val hostOnly = parts[8].toBoolean()

                                val cookieBuilder = Cookie.Builder()
                                    .name(name)
                                    .value(value)
                                    .expiresAt(expiresAt)
                                    .apply {
                                        if (hostOnly) hostOnlyDomain(domain) else domain(domain)
                                        path(path)
                                        if (secure) secure()
                                        if (httpOnly) httpOnly()
                                    }

                                hostCookies.add(cookieBuilder.build())
                            } catch (e: Exception) {
                                println("Error deserializing cookie: $e")
                            }
                        }
                    }
                }
                cookies[host] = hostCookies
            }
        }
    }

    fun clearAllCookies() {
        preferences.edit().clear().apply()
        cookies.clear()
    }
}