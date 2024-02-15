package ru.infotecs.cicd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum SwitchMode {
	OFF("0"),
	BOIL("1"),
	HEAT("2");

	private final String key;

	public static Optional<SwitchMode> valueOfKey(String key) {
		return Arrays.stream(values())
				.filter(it -> it.key.equals(key)).findAny();
	}
}
