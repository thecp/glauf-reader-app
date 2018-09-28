public class Helper {
	public static int getIdFromByteArray(byte[] id) {
		int i = 0;
		try {
			for (int j = 0; j < id.length; ++j) {
				String hex = Integer.toHexString(id[j]);
				if (hex.length() > 2) {
					hex = hex.substring(hex.length() - 2);
				}
				i += (Math.pow(100, id.length - j - 1)) * Integer.valueOf(hex);
			}
		} catch (Exception e) {
			e.printStackTrace();
			i = 0;
		}
		return i;
	}
}
