package repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilsTest {

    @Test
    void hashIstKeinKlartext() {
        assertNotEquals("passwort123", PasswordUtils.hash("passwort123"));
    }

    @Test
    void hashIstDeterministisch() {
        assertEquals(PasswordUtils.hash("test"), PasswordUtils.hash("test"));
    }

    @Test
    void unterschiedlichePasswoerterVerschiedeneHashes() {
        assertNotEquals(PasswordUtils.hash("abc"), PasswordUtils.hash("xyz"));
    }

    @Test
    void hashLaengeIst64Zeichen() {
        assertEquals(64, PasswordUtils.hash("beliebig").length());
    }

    @Test
    void verifyMitKorrektemPasswort() {
        String hash = PasswordUtils.hash("geheim");
        assertTrue(PasswordUtils.verify("geheim", hash));
    }

    @Test
    void verifyMitFalschemPasswort() {
        String hash = PasswordUtils.hash("geheim");
        assertFalse(PasswordUtils.verify("falsch", hash));
    }

    @Test
    void leersesPasswortHashbar() {
        String hash = PasswordUtils.hash("");
        assertNotNull(hash);
        assertTrue(PasswordUtils.verify("", hash));
    }
}
