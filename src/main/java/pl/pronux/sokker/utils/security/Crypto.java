package pl.pronux.sokker.utils.security;

import pl.pronux.sokker.exceptions.BadArgumentException;

public class Crypto {

	public static String encodeBase64(byte[] buf) {
		return new String(Base64Coder.encode(buf));
	}

	public static byte[] decodeBase64(String key) throws BadArgumentException {
		return Base64Coder.decode(key);
	}
}
