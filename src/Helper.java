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

	public static String getIdFromByteArrayNew(byte[] id) {
		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < id.length; i++) {
			hex.append(Integer.toHexString((int) id[i]));
		}
		return hex.toString();
	}

	public static int timeToInt(String time) {
		String[] h1 = time.split(":");
		int hour = Integer.parseInt(h1[0]);
		int minute = Integer.parseInt(h1[1]);
		int second = Integer.parseInt(h1[2]);
		return second + (60 * minute) + (3600 * hour);
	}

	public static String intToTime(int seconds) {
		int hours = seconds / 3600;
		int remainder = seconds - hours * 3600;
		int mins = remainder / 60;
		remainder = remainder - mins * 60;
		int secs = remainder;
		return String.format("%02d", hours) + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs);
	}
}
