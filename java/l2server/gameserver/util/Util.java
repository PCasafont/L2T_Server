/*
 * $Header: Util.java, 21/10/2005 23:17:40 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 21/10/2005 23:17:40 $
 * $Revision: 1 $
 * $Log: Util.java,v $
 * Revision 1  21/10/2005 23:17:40  luisantonioa
 * Added copyright notice
 *
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.util;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2NpcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * General Utility functions related to Gameserver
 */
public final class Util
{
	public static Node getFirstNodeFromXML(File base, String fileName)
	{
		Node result = null;

		File file = new File(base, fileName);

		if (!file.exists())
		{
			Log.warning("The following XML could not be loaded:");
			Log.warning("- " + file.getAbsolutePath());
			return null;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);

		Document doc = null;
		try
		{
			doc = factory.newDocumentBuilder().parse(file);

			result = doc.getFirstChild();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static void handleIllegalPlayerAction(L2PcInstance actor, String message, int punishment)
	{
		Broadcast.toGameMasters(message);
		ThreadPoolManager.getInstance().scheduleGeneral(new IllegalPlayerAction(actor, message, punishment), 5000);
	}

	public static String getRelativePath(File base, File file)
	{
		return file.toURI().getPath().substring(base.toURI().getPath().length());
	}

	/**
	 * Return degree value of object 2 to the horizontal line with object 1
	 * being the origin
	 */
	public static double calculateAngleFrom(L2Object obj1, L2Object obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}

	/**
	 * Return degree value of object 2 to the horizontal line with object 1
	 * being the origin
	 */
	public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return angleTarget;
	}

	public static double convertHeadingToDegree(int clientHeading)
	{
		return clientHeading / 182.044444444;
	}

	public static int convertDegreeToClientHeading(double degree)
	{
		if (degree < 0)
		{
			degree = 360 + degree;
		}
		return (int) (degree * 182.044444444);
	}

	public static int calculateHeadingFrom(L2Object obj1, L2Object obj2)
	{
		return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}

	public static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}

	public static int calculateHeadingFrom(double dx, double dy)
	{
		double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}

	/**
	 * @return the distance between the two coordinates in 2D plane
	 */
	public static double calculateDistance(int x1, int y1, int x2, int y2)
	{
		return calculateDistance(x1, y1, 0, x2, y2, 0, false);
	}

	/**
	 * @param includeZAxis - if true, includes also the Z axis in the calculation
	 * @return the distance between the two coordinates
	 */
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		double dx = (double) x1 - x2;
		double dy = (double) y1 - y2;

		if (includeZAxis)
		{
			double dz = z1 - z2;
			return Math.sqrt(dx * dx + dy * dy + dz * dz);
		}
		else
		{
			return Math.sqrt(dx * dx + dy * dy);
		}
	}

	/**
	 * @param includeZAxis - if true, includes also the Z axis in the calculation
	 * @return the distance between the two objects
	 */
	public static double calculateDistance(L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
		{
			return 1000000;
		}

		return calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(),
				includeZAxis);
	}

	/**
	 * (Based on ucfirst() function of PHP)
	 *
	 * @param str - the string whose first letter to capitalize
	 * @return a string with the first letter of the {@code str} capitalized
	 */
	public static String capitalizeFirst(String str)
	{
		str = str.trim();

		if (str.length() > 0 && Character.isLetter(str.charAt(0)))
		{
			return str.substring(0, 1).toUpperCase() + str.substring(1);
		}

		return str;
	}

	/**
	 * (Based on ucwords() function of PHP)
	 *
	 * @param str - the string to capitalize
	 * @return a string with the first letter of every word in {@code str} capitalized
	 */
	public static String capitalizeWords(String str)
	{
		char[] charArray = str.toCharArray();
		String result = "";

		// Capitalize the first letter in the given string!
		charArray[0] = Character.toUpperCase(charArray[0]);

		for (int i = 0; i < charArray.length; i++)
		{
			if (Character.isWhitespace(charArray[i]))
			{
				charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);
			}

			result += Character.toString(charArray[i]);
		}

		return result;
	}

	/**
	 * @return {@code true} if the two objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInRange(int range, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
		{
			return false;
		}
		if (obj1.getInstanceId() != obj2.getInstanceId())
		{
			return false;
		}
		if (range == -1)
		{
			return true; // not limited
		}

		int rad = 0;
		if (obj1 instanceof L2Character)
		{
			rad += ((L2Character) obj1).getTemplate().collisionRadius;
		}
		if (obj2 instanceof L2Character)
		{
			rad += ((L2Character) obj2).getTemplate().collisionRadius;
		}

		double dx = obj1.getX() - obj2.getX();
		double dy = obj1.getY() - obj2.getY();

		if (includeZAxis)
		{
			double dz = obj1.getZ() - obj2.getZ();
			double d = dx * dx + dy * dy + dz * dz;

			return d <= range * range + 2 * range * rad + rad * rad;
		}
		else
		{
			double d = dx * dx + dy * dy;

			return d <= range * range + 2 * range * rad + rad * rad;
		}
	}

	public static boolean checkIfInRange(final int range, final int x1, final int y1, final int z1, final int x2, final int y2, final int z2, final boolean includeZAxis)
	{
		final double dx = (double) x1 - x2;
		final double dy = (double) y1 - y2;

		final double d;

		if (includeZAxis)
		{
			final double dz = (double) z1 - z2;
			d = dx * dx + dy * dy + dz * dz;
		}
		else
		{
			d = dx * dx + dy * dy;
		}

		return d <= (double) range * range;
	}

	/**
	 * Checks if object is within short (sqrt(int.max_value)) radius, not using collisionRadius.
	 * Faster calculation than checkIfInRange if distance is short and collisionRadius isn't needed.
	 * Not for long distance checks (potential teleports, far away castles etc).
	 *
	 * @param includeZAxis - if true, check also Z axis (3-dimensional check), otherwise only 2D
	 * @return {@code true} if objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInShortRadius(int radius, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
		{
			return false;
		}
		if (radius == -1)
		{
			return true; // not limited
		}

		int dx = obj1.getX() - obj2.getX();
		int dy = obj1.getY() - obj2.getY();

		if (includeZAxis)
		{
			int dz = obj1.getZ() - obj2.getZ();
			return dx * dx + dy * dy + dz * dz <= radius * radius;
		}
		else
		{
			return dx * dx + dy * dy <= radius * radius;
		}
	}

	/**
	 * @param str - the String to count
	 * @return the number of "words" in a given string.
	 */
	public static int countWords(String str)
	{
		return str.trim().split("\\s+").length;
	}

	/**
	 * (Based on implode() in PHP)
	 *
	 * @param strArray - an array of strings to concatenate
	 * @param strDelim - the delimiter to put between the strings
	 * @return a delimited string for a given array of string elements.
	 */
	public static String implodeString(String[] strArray, String strDelim)
	{
		String result = "";

		for (String strValue : strArray)
		{
			result += strValue + strDelim;
		}

		return result;
	}

	/**
	 * @param strCollection - a collection of strings to concatenate
	 * @param strDelim      - the delimiter to put between the strings
	 * @see #implodeString(String[] strArray, String strDelim)
	 */
	public static String implodeString(Collection<String> strCollection, String strDelim)
	{
		return implodeString(strCollection.toArray(new String[strCollection.size()]), strDelim);
	}

	/**
	 * (Based on round() in PHP)
	 *
	 * @param number    - the number to round
	 * @param numPlaces - how many digits after decimal point to leave intact
	 * @return the value of {@code number} rounded to specified number of digits after the decimal point.
	 */
	public static float roundTo(float number, int numPlaces)
	{
		if (numPlaces <= 1)
		{
			return Math.round(number);
		}

		float exponent = (float) Math.pow(10, numPlaces);

		return Math.round(number * exponent) / exponent;
	}

	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} contains only numbers, {@code false} otherwise
	 */
	public static boolean isDigit(String text)
	{
		if (text == null)
		{
			return false;
		}

		return text.matches("[0-9]+");
	}

	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} contains only letters and/or numbers, {@code false} otherwise
	 */
	public static boolean isAlphaNumeric(String text)
	{
		if (text == null || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isLetterOrDigit(c))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Format the specified digit using the digit grouping symbol "," (comma).
	 * For example, 123456789 becomes 123,456,789.
	 *
	 * @param amount - the amount of adena
	 * @return the formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		String s = "";
		long rem = amount % 1000;
		s = Long.toString(rem);
		amount = (amount - rem) / 1000;
		while (amount > 0)
		{
			if (rem < 99)
			{
				s = '0' + s;
			}
			if (rem < 9)
			{
				s = '0' + s;
			}
			rem = amount % 1000;
			s = Long.toString(rem) + "," + s;
			amount = (amount - rem) / 1000;
		}
		return s;
	}

	/**
	 * @param array - the array to look into
	 * @param obj   - the object to search for
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise
	 */
	public static <T> boolean contains(T[] array, T obj)
	{
		for (T element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param array - the array to look into
	 * @param obj   - the integer to search for
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise
	 */
	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}

	public static String getDecimalString(double d)
	{
		final long base = 100000L; // decimal places
		final int minDigitsAfterPoint = 1; // decimal places
		final int maxdigits = 6; // number
		String nString = "";
		long number = Math.round(d * base);
		int digitsShown = -1;
		boolean stringBegan = false;
		for (long divider = base * 100; divider > 0; divider /= 10)
		{
			if (number / divider % 10 > 0 || stringBegan)
			{
				nString += number / divider % 10;
				stringBegan = true;
				if (number / divider % 10 > 0 && digitsShown == -1)
				{
					digitsShown = 0;
				}
			}

			if (number % divider == 0 && divider * minDigitsAfterPoint <= base || digitsShown >= maxdigits - 1)
			{
				break;
			}

			if (divider == base)
			{
				if (digitsShown == -1)
				{
					nString += "0";
				}
				nString += ".";
				stringBegan = true;
			}

			if (digitsShown > -1)
			{
				digitsShown++;
			}
		}
		return nString;
	}

	public static String readFile(String file)
	{
		File log = new File(file);
		FileInputStream fis;
		BufferedInputStream bis;
		String content = "";
		try
		{
			fis = new FileInputStream(log);
			bis = new BufferedInputStream(fis);
			int bytes = bis.available();
			byte[] raw = new byte[bytes];

			bis.read(raw);

			fis.close();
			bis.close();

			content = new String(raw, "UTF-8");
			//content = content.replaceAll("\r\n", "\n");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return content;
	}

	public static String writeFile(String file, String content)
	{
		File log = new File(file);
		FileOutputStream fos;
		BufferedOutputStream bos;
		try
		{
			fos = new FileOutputStream(log);
			bos = new BufferedOutputStream(fos);

			bos.write(content.getBytes("UTF-8"));

			bos.close();
			fos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * Return the number of playable characters in a defined radius around the specified object.
	 *
	 * @param range     : the radius in which to look for players
	 * @param npc       : the object whose knownlist to check
	 * @param playable  : if {@code true}, count summons and pets aswell
	 * @param invisible : if {@code true}, count invisible characters aswell
	 * @return the number of targets found
	 */
	public static int getPlayersCountInRadius(int range, L2Object npc, boolean playable, boolean invisible)
	{
		int count = 0;
		final Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
		for (L2Object obj : objs)
		{
			if (obj != null && (obj instanceof L2Playable && playable || obj instanceof L2PetInstance ||
					obj instanceof L2SummonInstance))
			{
				if (obj instanceof L2PcInstance && !invisible && obj.getActingPlayer().getAppearance().getInvisible())
				{
					continue;
				}

				final L2Character cha = (L2Character) obj;
				if (cha.getZ() < npc.getZ() - 100 && cha.getZ() > npc.getZ() + 100 || !GeoData.getInstance()
						.canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), npc.getX(), npc.getY(), npc.getZ()))
				{
					continue;
				}

				if (Util.checkIfInRange(range, npc, obj, true) && !cha.isDead())
				{
					count++;
				}
			}
		}
		return count;
	}

	private static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("[EEEE d MMMMMMM yyyy] @ k:m:s: ");

	public static void logToFile(String string, String filename, boolean append)
	{
		logToFile(string, filename, "txt", append, true);
	}

	public static void logToFile(String string, String filename, String extension, boolean append)
	{
		logToFile(string, filename, extension, append, true);
	}

	public static void logToFile(String string, String filename, String extension, boolean append, boolean date)
	{
		if (Config.isServer(Config.TENKAI))
		{
			return;
		}

		try
		{
			FileWriter fstream = new FileWriter(filename + "." + extension, append);
			BufferedWriter file = new BufferedWriter(fstream);
			file.write(date ? getCurrentDate() + string : string);
			file.newLine();
			file.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Util: Error while logging to File - " + filename, e);
		}
	}

	public static String getCurrentDate()
	{
		return DATE_FORMATER.format(System.currentTimeMillis());
	}

	public static void hashFiles(final File dir, List<File> hash)
	{
		if (!dir.isDirectory())
		{
			return;
		}

		File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.getName().endsWith(".xml"))
			{
				hash.add(f);
			}
		}
	}

	public static void hashFiles(final File dir, Map<Integer, File> hash)
	{
		if (!dir.isDirectory())
		{
			return;
		}

		File[] files = dir.listFiles();
		for (File f : files)
		{
			try
			{
				if (f.getName().endsWith(".xml"))
				{
					hash.put(Integer.parseInt(f.getName().replaceAll(".xml", "")), f);
				}
			}
			catch (Exception ignored)
			{
			}
		}
	}

	public static L2NpcInstance getNpcCloseTo(final int npcId, final L2PcInstance activeChar)
	{
		Collection<L2Character> knownCharacters = activeChar.getKnownList().getKnownCharactersInRadius(900);
		for (L2Character character : knownCharacters)
		{
			if (!(character instanceof L2NpcInstance))
			{
				continue;
			}

			final L2NpcInstance npc = (L2NpcInstance) character;

			if (npcId != npc.getNpcId())
			{
				continue;
			}

			return npc;
		}

		return null;
	}
}
