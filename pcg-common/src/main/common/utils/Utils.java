package common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class Utils {

  private static final String base64Key = "kQ5u6YmYc5mjNLBgHh2IHvEbkUE17RUbgcB7DqF8t0w=";

  /**
   * Decripta una stringa utilizzando AES con una chiave segreta hardcoded.
   *
   * @param encryptedText Il testo criptato da decriptare.
   * @return La stringa decriptata.
   * @throws Exception Se si verifica un errore durante la decriptazione.
   */
  public static String decryptAES(String encryptedText) throws Exception {

    log.info("Inizio del processo di decriptazione...");

    try {

      Cipher cipher = getCipher(Cipher.DECRYPT_MODE);

      // Usa URL-safe decoder per gestire correttamente i caratteri speciali negli URL
      byte[] decryptedBytes =
              cipher.doFinal(Base64.getUrlDecoder().decode(encryptedText));
      return new String(decryptedBytes, StandardCharsets.UTF_8);

    } catch (Exception e) {

      log.error("Errore durante la decriptazione: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante la decriptazione", e);

    }
  }

  /**
   * Cripta una stringa utilizzando AES con una chiave segreta hardcoded.
   *
   * @param plainText Il testo da criptare.
   * @return La stringa criptata in Base64.
   * @throws Exception Se si verifica un errore durante la criptazione.
   */
  public static String encryptAES(String plainText) throws Exception {

    log.info("Inizio del processo di criptazione...");

    try {

      Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
      byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

      // Usa URL-safe encoder per evitare problemi con +, / e = negli URL
      return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);

    } catch (Exception e) {

      log.error("Errore durante la criptazione: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante la criptazione", e);

    }
  }

  private static Cipher getCipher(int mode) throws Exception {

    byte[] decodedKey = Base64.getDecoder().decode(base64Key);
    SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(mode, secretKey);

    return cipher;
  }

}
