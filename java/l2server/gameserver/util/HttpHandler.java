/*
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

import l2server.log.Log;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Maxime
 */

public class HttpHandler
{
	private static final String USER_AGENT =
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.124 Safari/537.36";

	private static final boolean DEV_MODE = false;

	public HttpHandler()
	{
		CookieHandler.setDefault(new CookieManager());
	}

	public final String getWebResponse(final String url)
	{
		String result = null;

		try
		{
			URL obj = new URL(url);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);

			con.connect();

			int responseCode = con.getResponseCode();

			if (DEV_MODE)
			{
				Log.info("\nSending 'GET' request to URL : " + url);
				Log.info("Response Code : " + responseCode);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}

			in.close();

			result = response.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return result;
	}

	public final String doPost(final String url, final String post)
	{
		String result = null;

		try
		{
			HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			connection.setDoOutput(true);

			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

			outputStream.writeBytes(post);
			outputStream.flush();
			outputStream.close();

			int responseCode = connection.getResponseCode();

			if (DEV_MODE)
			{
				Log.info("Sending 'POST' request to...: " + url + ".");
				Log.info("Post...: " + post + ".");
				Log.info("Response Code...: " + responseCode + ".");
			}

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = bufferedReader.readLine()) != null)
			{
				response.append(inputLine);
			}

			bufferedReader.close();

			result = response.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return result;
	}
}
