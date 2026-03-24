package CEOS.concurrency.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CharacterType {

    CAT("🐱"),
    DOG("🐶"),
    RABBIT("🐰"),
    DEER("🫎"),
    LION("🦁"),
    FOX("🦊"),
    BEAR("🐻"),
    PENGUIN("🐧"),
    HAMSTER("🐹"),
    FROG("🐸");

    private final String emoji;
}