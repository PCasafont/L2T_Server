package l2tserver.gameserver.datatables;

import gov.nasa.worldwind.formats.dds.DDSConverter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import l2tserver.Config;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.PledgeCrest;
import l2tserver.log.Log;

/**
 * @author Pere
 */
public class ImageTable
{
	
	private static ImageTable _instance;
	
	private static Map<Integer, PledgeCrest> _images = new HashMap<Integer, PledgeCrest>();
	
	public static ImageTable getInstance()
	{
		if (_instance == null)
			_instance = new ImageTable();
		
		return _instance;
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
					continue;
				id = Integer.valueOf(image.getName().substring(0, image.getName().length() - 4));
				byte[] array = DDSConverter.convertToDDS(image).array();
				_images.put(id, new PledgeCrest(id, array));
			}
		}
		catch(Exception e)
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
					continue;
				id = Integer.valueOf(image.getName().substring(0, image.getName().length() - 4));
				byte[] array = DDSConverter.convertToDDS(image).array();
				_images.put(id, new PledgeCrest(id, array));
			}
		}
		catch(Exception e)
		{
			Log.warning("Image table: error loading image id " + id + ": " + e);
			e.printStackTrace();
		}
		
		Log.info("ImageTable: Loaded " + _images.size() + " custom images.");
	}
	
	public void sendImages(L2PcInstance player)
	{
		for (int imageId : _images.keySet())
		{
			if (_images.get(imageId) != null)
				player.sendPacket(_images.get(imageId));
		}
	}
}
