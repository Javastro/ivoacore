import org.gradle.api.GradleException
import org.semver4j.Semver
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URI
import java.util.Base64
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

object PublishedVersions {
    fun isNewer(
        repository: URI,
        group: String,
        artifact: String,
        version: String,
        username: String?,
        password: String?
    ): Boolean {
        val metadata = URI(repository.toString().trimEnd('/') + "/")
            .resolve("${group.replace('.', '/')}/$artifact/maven-metadata.xml")
        fun parseVersion(value: String, description: String): Semver =
            try {
                Semver(value)
            } catch (e: IllegalArgumentException) {
                throw GradleException("Invalid semantic version '$value' for $description", e)
            }

        val current = parseVersion(version, "$group:$artifact")
        try {
            val connection = metadata.toURL().openConnection()
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            if (connection is HttpURLConnection) {
                connection.instanceFollowRedirects = false
                if (username != null && password != null) {
                    val token = Base64.getEncoder()
                        .encodeToString("$username:$password".toByteArray(Charsets.UTF_8))
                    connection.setRequestProperty("Authorization", "Basic $token")
                }
                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_NOT_FOUND) return true
                if (status !in 200..299) {
                    throw GradleException("Repository returned HTTP $status for $metadata")
                }
            }
            val factory = DocumentBuilderFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
            val root = connection.getInputStream().use {
                factory.newDocumentBuilder().parse(it).documentElement
            }
            fun Element.child(name: String): Element? =
                (0 until childNodes.length).mapNotNull { childNodes.item(it) as? Element }
                    .firstOrNull { it.tagName == name }

            val versions = root.takeIf { it.tagName == "metadata" }
                ?.child("versioning")?.child("versions")
                ?.let { element ->
                    (0 until element.childNodes.length)
                        .mapNotNull { element.childNodes.item(it) as? Element }
                        .filter { it.tagName == "version" }
                }.orEmpty()
            if (versions.isEmpty()) {
                throw GradleException("No versions found in repository metadata at $metadata")
            }
            val publishedVersions = versions.map {
                val published = it.textContent.trim()
                if (published.isEmpty()) {
                    throw GradleException("Empty version in repository metadata at $metadata")
                }
                parseVersion(published, "repository metadata at $metadata")
            }
            return publishedVersions.all { it < current }
        } catch (e: FileNotFoundException) {
            if (metadata.scheme == "file") return true
            throw GradleException("Could not read repository metadata at $metadata", e)
        } catch (e: GradleException) {
            throw e
        } catch (e: Exception) {
            throw GradleException("Could not read repository metadata at $metadata", e)
        }
    }
}
