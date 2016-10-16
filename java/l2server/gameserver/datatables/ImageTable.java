package l2server.gameserver.datatables;

import gov.nasa.worldwind.formats.dds.DDSConverter;
import l2server.Config;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.PledgeCrest;
import l2server.log.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pere
 */
public class ImageTable
{
	private static ImageTable instance;

	private static Map<Integer, PledgeCrest> images = new HashMap<>();

	public static ImageTable getInstance()
	{
		if (instance == null)
		{
			instance = new ImageTable();
		}

		return instance;
	}

	private ImageTable()
	{
		readImages();
	}

	private void readImages()
	{
		File folder = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "images/");
		folder.mkdirs();
		int id = 0;
		try
		{
			for (File image : folder.listFiles())
			{
				if (!image.getName().substring(image.getName().length() - 3).equalsIgnoreCase("png"))
				{
					continue;
				}

				String imgName = image.getName();
				if (imgName.contains("_"))
				{
					imgName = imgName.substring(imgName.indexOf("_") + 1);
				}

				id = Integer.valueOf(imgName.substring(0, imgName.length() - 4));
				byte[] array = DDSConverter.convertToDDS(image).array();
				this.images.put(id, new PledgeCrest(id, array));
			}
		}
		catch (Exception e)
		{
			Log.warning("Image table: error loading image id " + id + ": " + e);
			e.printStackTrace();
		}

		//Load the specific server images
		folder = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/images/");
		folder.mkdirs();

		try
		{
			for (File image : folder.listFiles())
			{
				if (!image.getName().substring(image.getName().length() - 3).equalsIgnoreCase("png"))
				{
					continue;
				}
				id = Integer.valueOf(image.getName().substring(0, image.getName().length() - 4));
				byte[] array = DDSConverter.convertToDDS(image).array();
				this.images.put(id, new PledgeCrest(id, array));
			}
		}
		catch (Exception e)
		{
			Log.warning("Image table: error loading image id " + id + ": " + e);
			e.printStackTrace();
		}

		Log.info("ImageTable: Loaded " + this.images.size() + " custom images.");
	}

	public void sendImages(L2PcInstance player)
	{
		for (int imageId : this.images.keySet())
		{
			if (this.images.get(imageId) != null)
			{
				player.sendPacket(this.images.get(imageId));
			}
		}
	}
}
