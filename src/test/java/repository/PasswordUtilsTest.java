package repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    // SHA-256("test") — well-known reference value
    private static final String SHA256_OF_TEST =
            "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";

    @Test
    void hash_knownValue_matchesExpected() {
        assertEquals(SHA256_OF_TEST, PasswordUtils.hash("test"));
    }

    @Test
    void hash_deterministic_sameInputProducesSameOutput() {
        String h1 = PasswordUtils.hash("geheim123");
        String h2 = PasswordUtils.hash("geheim123");
        assertEquals(h1, h2);
    }

    @Test
    void hash_differentInputs_produceDifferentHashes() {
        assertNotEquals(PasswordUtils.hash("abc"), PasswordUtils.hash("ABC"));
        assertNotEquals(PasswordUtils.hash("pass1"), PasswordUtils.hash("pass2"));
    }

    @Test
    void hash_emptyString_returnsValidHash() {
        String hash = PasswordUtils.hash("");
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 produces 64 hex chars
    }

    @Test
    void hash_outputIsLowercaseHex() {
        String hash = PasswordUtils.hash("EasyRide");
        assertTrue(hash.matches("[0-9a-f]{64}"),
                "SHA-256 Hash muss 64 Hex-Zeichen (Kleinbuchstaben) sein");
    }

    @Test
    void verify_correctPassword_returnsTrue() {
        String hash = PasswordUtils.hash("meinPasswort");
        assertTrue(PasswordUtils.verify("meinPasswort", hash));
    }

    @Test
    void verify_wrongPassword_returnsFalse() {
        String hash = PasswordUtils.hash("richtig");
        assertFalse(PasswordUtils.verify("falsch", hash));
    }

    @Test
    void verify_emptyPasswordAgainstNonEmptyHash_returnsFalse() {
        String hash = PasswordUtils.hash("nichtLeer");
        assertFalse(PasswordUtils.verify("", hash));
    }

    @Test
    void verify_caseSensitive() {
        String hash = PasswordUtils.hash("Password");
        assertFalse(PasswordUtils.verify("password", hash));
        assertFalse(PasswordUtils.verify("PASSWORD", hash));
    }
}
