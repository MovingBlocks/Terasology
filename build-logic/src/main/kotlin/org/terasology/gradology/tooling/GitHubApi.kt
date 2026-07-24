// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology.tooling

import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Minimal GitHub REST API client - just enough to check a URL and list an org/user's repos with
 * pagination, matching what common.groovy's isUrlValid()/retrieveAvailableItems() did.
 */
object GitHubApi {
    private val client: HttpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
    private val gson = Gson()
    private val linkRel = Regex("""<([^>]+)>;\s*rel="next"""")

    private data class RepoSummary(val name: String? = null, val description: String? = null)

    /** A HEAD request to see if a URL is reachable (returns HTTP 200). */
    fun isUrlValid(url: String): Boolean = try {
        val request = HttpRequest.newBuilder(URI(url)).method("HEAD", HttpRequest.BodyPublishers.noBody()).build()
        client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200
    } catch (e: Exception) {
        false
    }

    fun getText(url: String): String {
        val request = HttpRequest.newBuilder(URI(url)).GET().build()
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body()
    }

    /** repo name -> description, for every repo owned by [githubHome], following Link-header pagination. */
    fun listRepos(githubHome: String): Map<String, String?> {
        val result = LinkedHashMap<String, String?>()
        var url: String? = "https://api.github.com/users/$githubHome/repos?per_page=99"
        while (url != null) {
            val request = HttpRequest.newBuilder(URI(url)).GET().build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val repos = gson.fromJson(response.body(), Array<RepoSummary>::class.java) ?: emptyArray()
            for (repo in repos) {
                if (repo.name != null) {
                    result[repo.name] = repo.description
                }
            }
            url = response.headers().firstValue("Link").orElse(null)?.let { header -> linkRel.find(header)?.groupValues?.get(1) }
            // Defends against pagination wrapping back around, same guard common.groovy had.
            if (url?.contains("page=1") == true) {
                url = null
            }
        }
        return result
    }
}
