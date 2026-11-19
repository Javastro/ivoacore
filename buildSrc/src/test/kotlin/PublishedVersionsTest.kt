import com.sun.net.httpserver.HttpServer
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path

class PublishedVersionsTest {
    @TempDir
    lateinit var repository: Path

    private fun isNewer(version: String) = PublishedVersions.isNewer(
        repository.toUri(), "org.example", "artifact", version, null, null
    )

    private fun metadata(vararg versions: String) {
        val path = repository.resolve("org/example/artifact/maven-metadata.xml")
        Files.createDirectories(path.parent)
        Files.writeString(
            path,
            "<metadata><versioning><versions>" +
                versions.joinToString("") { "<version>$it</version>" } +
                "</versions></versioning></metadata>"
        )
    }

    @Test
    fun `missing metadata is treated as unpublished`() {
        assertTrue(isNewer("1.0.0"))
    }

    @Test
    fun `compares every version using semantic version ordering`() {
        metadata("1.2.0", "1.10.0", "1.3.0-SNAPSHOT")
        assertTrue(isNewer("1.11.0-SNAPSHOT"))
        assertFalse(isNewer("1.10.0"))
        assertFalse(isNewer("1.9.0"))
    }

    @Test
    fun `equal snapshots are skipped and releases follow snapshots`() {
        metadata("1.0.0-SNAPSHOT")
        assertFalse(isNewer("1.0.0-SNAPSHOT"))
        assertTrue(isNewer("1.0.0"))
    }

    @Test
    fun `semver precedence handles prerelease and build metadata`() {
        metadata("1.0.0-alpha.2", "1.0.0-alpha.10")
        assertTrue(isNewer("1.0.0-beta.1"))
        assertFalse(isNewer("1.0.0-alpha.9"))
        metadata("1.0.0+build.1")
        assertFalse(isNewer("1.0.0+build.2"))
    }

    @Test
    fun `invalid semantic versions fail even if another version prevents publishing`() {
        assertThrows(GradleException::class.java) { isNewer("1.0") }
        metadata("2.0.0", "not-a-version")
        val error = assertThrows(GradleException::class.java) { isNewer("1.0.0") }
        assertTrue(error.message!!.contains("not-a-version"))
    }

    @Test
    fun `invalid and unsafe metadata fail`() {
        metadata()
        assertThrows(GradleException::class.java) { isNewer("1.0.0") }
        val path = repository.resolve("org/example/artifact/maven-metadata.xml")
        Files.writeString(path, "<!DOCTYPE metadata [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><metadata>&x;</metadata>")
        assertThrows(GradleException::class.java) { isNewer("1.0.0") }
    }

    @Test
    fun `http errors other than missing metadata fail`() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            exchange.sendResponseHeaders(403, -1)
            exchange.close()
        }
        server.start()
        try {
            val url = java.net.URI("http://127.0.0.1:${server.address.port}/")
            assertThrows(GradleException::class.java) {
                PublishedVersions.isNewer(url, "org.example", "artifact", "1.0.0", null, null)
            }
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `http lookup authenticates and treats only 404 as missing`() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/org/example/artifact/maven-metadata.xml") { exchange ->
            assertEquals("Basic dXNlcjpwYXNz", exchange.requestHeaders.getFirst("Authorization"))
            exchange.sendResponseHeaders(404, -1)
            exchange.close()
        }
        server.start()
        try {
            val url = java.net.URI("http://127.0.0.1:${server.address.port}/")
            assertTrue(PublishedVersions.isNewer(url, "org.example", "artifact", "1.0.0", "user", "pass"))
        } finally {
            server.stop(0)
        }
    }
}
