package stasiek.wojcik.wordletrainingproject.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Scanner;

@Service
class EncryptionKeyLoader {

    private final String encryptionKeyFileName;

    EncryptionKeyLoader(@Value("${encryptionKeyFileName}") final String encryptionKeyFileName) {
        this.encryptionKeyFileName = encryptionKeyFileName;
    }

    String loadClientSecret() {
        try {
            final var resourceAsStream = EncryptionKeyLoader.class
                    .getClassLoader()
                    .getResourceAsStream(encryptionKeyFileName);
            final var scanner = new Scanner(Objects.requireNonNull(resourceAsStream));
            return scanner.nextLine();
        } catch (final NullPointerException e) {
            throw new IllegalStateException("No property for encryptionKeyFileName found.");
        }
    }
}
