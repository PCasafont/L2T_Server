package l2server.gameserver.network;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pere
 */
public class PacketOpcodes
{
	public static class PacketFamily
	{
		public int switchLength;
		public final Map<Integer, Object> children = new HashMap<>();
	}

	private static String PROTOCOL_FILE;
	private static boolean GENERATE_MISSING_PACKETS = true;

	public static final PacketFamily ClientPacketsFamily = new PacketFamily();
	public static final Map<Class<?>, byte[]> ClientPackets = new HashMap<>();
	public static final Map<Class<?>, byte[]> ServerPackets = new HashMap<>();

	private static long _lastModified = 0;

	static
	{
		PROTOCOL_FILE = "data_" + Config.SERVER_NAME + "/protocol.xml";
		if (!new File(Config.DATAPACK_ROOT, PROTOCOL_FILE).exists())
		{
			PROTOCOL_FILE = Config.DATA_FOLDER + "/protocol.xml";
		}

		load();

		// Auto reloader
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				File f = new File(Config.DATAPACK_ROOT, PROTOCOL_FILE);
				if (!f.isDirectory() && f.lastModified() > _lastModified)
				{
					load();
					//Log.info("Updated the protocols from the file " + f.getName());
				}
			}
		}, 10000, 10000);
	}

	public static void init()
	{
		// Dummy method to trigger the static block
	}

	private static void load()
	{
		File file = new File(Config.DATAPACK_ROOT, PROTOCOL_FILE);
		if (!file.exists())
		{
			Log.warning("File " + file.getAbsolutePath() + " doesn't exist");
			return;
		}

		if (!file.getName().endsWith(".xml"))
		{
			return;
		}

		ClientPacketsFamily.children.clear();
		ServerPackets.clear();

		XmlDocument doc = new XmlDocument(file);

		if (doc.getFirstChild() == null)
		{
			Log.warning("An error occured while loading PacketOpcodes.");
			return;
		}

		for (XmlNode n : doc.getChildren())
		{
			if (!n.getName().equals("protocol"))
			{
				continue;
			}

			for (XmlNode d : n.getChildren())
			{
				if (!d.getName().equals("packetfamilly"))
				{
					continue;
				}

				boolean isClientPacket = d.getString("way").equalsIgnoreCase("ClientPackets");
				parsePacketFamily(d, isClientPacket, new byte[0], isClientPacket ? ClientPacketsFamily : null);
			}
		}

		Log.info("PacketOpcodes: Loaded " + ClientPackets.size() + " Client Packets and " + ServerPackets.size() +
				" Server Packets.");

		_lastModified = file.lastModified();

		/*File dir = new File(Config.DATAPACK_ROOT, "java/l2server/gameserver/network/clientpackets");
		File[] files = dir.listFiles();
		for (File f : files)
		{
			if (!f.isDirectory() && f.getName().endsWith("java"))
			{
				String content = Util.readFile(f.getAbsolutePath());
				boolean parserGenerated = content.contains("MegaParzor!");

				try
				{
					Class<?> cl = Class.forName("l2server.gameserver.network.clientpackets." + f.getName().substring(0, f.getName().length() - 5));

					Log.info("This client" + (parserGenerated ? " (parser generated)" : "") + " packet doesn't figure in the protocol: " + cl.getSimpleName());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		dir = new File(Config.DATAPACK_ROOT, "java/l2server/gameserver/network/serverpackets");
		files = dir.listFiles();
		for (File f : files)
		{
			if (!f.isDirectory() && f.getName().endsWith("java"))
			{
				String content = Util.readFile(f.getAbsolutePath());
				boolean parserGenerated = content.contains("MegaParzor!");

				try
				{
					Class<?> cl = Class.forName("l2server.gameserver.network.serverpackets." + f.getName().substring(0, f.getName().length() - 5));
					if (ServerPackets.containsKey(cl))
						continue;

					Log.info("This server" + (parserGenerated ? " (parser generated)" : "") + " packet doesn't figure in the protocol: " + cl.getSimpleName());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}*/
	}

	private static void parsePacketFamily(XmlNode d, boolean isClientPacket, byte[] parentOpcode, PacketFamily family)
	{
		int length;
		switch (d.getString("switchtype"))
		{
			case "c":
				length = 1;
				break;
			case "h":
				length = 2;
				break;
			case "d":
				length = 4;
				break;
			default:
				Log.warning("'" + d.getString("switchtype") + "' switch type is not supported.");
				return;
		}

		if (isClientPacket)
		{
			family.switchLength = length;
		}

		for (XmlNode y : d.getChildren())
		{
			int subOpcode = Integer.decode(y.getString("id"));
			byte[] opcode = new byte[parentOpcode.length + length];
			System.arraycopy(parentOpcode, 0, opcode, 0, parentOpcode.length);
			for (int i = 0; i < length; i++)
			{
				opcode[parentOpcode.length + i] = (byte) (subOpcode >> i * 8 & 0xff);
			}

			if (y.getName().equals("packetfamilly"))
			{
				PacketFamily newFamily = null;
				if (isClientPacket)
				{
					newFamily = new PacketFamily();
					family.children.put(subOpcode, newFamily);
				}

				parsePacketFamily(y, isClientPacket, opcode, newFamily);
				continue;
			}

			if (!y.getName().equals("packet"))
			{
				continue;
			}

			String name = y.getString("name").replace("?", "");

			try
			{
				if (isClientPacket)
				{
					Class<?> packetClass = Class.forName("l2server.gameserver.network.clientpackets." + name);
					family.children.put(subOpcode, packetClass);
					ClientPackets.put(packetClass, opcode);
				}
				else
				{
					ServerPackets.put(Class.forName("l2server.gameserver.network.serverpackets." + name), opcode);
				}
			}
			catch (ClassNotFoundException e)
			{
				Map<String, String> parts = new HashMap<>();
				int rep = 1;
				for (XmlNode z : y.getChildren())
				{
					if (!z.getName().equals("part"))
					{
						continue;
					}

					String partName = z.getString("name").replace("?", "").replace(" ", "");
					String partType = z.getString("type");

					if (partName.length() == 0)
					{
						partName = "unk";
					}

					partName = partName.substring(0, 1).toLowerCase() + partName.substring(1, partName.length());

					if (parts.containsKey(partName) || parts.containsKey(partName + (rep - 1)))
					{
						if (rep == 1)
						{
							parts.put(partName + rep++, parts.get(partName) + "?");
							parts.remove(partName);
						}

						parts.put(partName + rep++, partType + "?");
					}
					else
					{
						parts.put(partName, partType);
					}
				}

				if (isClientPacket)
				{
					Log.warning("Client packet not implemented: " + name);

					if (!GENERATE_MISSING_PACKETS)
					{
						continue;
					}

					String content = "package l2server.gameserver.network.clientpackets;\n" + "\n" +
							"import l2server.log.Log;\n" + "\n" + "/**\n" + " * @author MegaParzor!\n" + " */\n" +
							"public class " + name + " extends L2GameClientPacket\n" + "{\n";

					boolean partDeclared = false;
					for (String partName : parts.keySet())
					{
						String partType = parts.get(partName);
						if (partType.contains("?") || partType.equals("b"))
						{
							continue;
						}

						String type = "int";
						switch (partType)
						{
							case "c":
								type = "byte";
								break;
							case "f":
								type = "float";
								break;
							case "Q":
								type = "long";
								break;
							case "S":
								type = "String";
								break;
						}

						content += "\t@SuppressWarnings(\"unused\")\n" + "\tprivate " + type + " _" + partName + ";\n";
						partDeclared = true;
					}

					if (partDeclared)
					{
						content += "\t\n";
					}

					content += "\t@Override\n" + "\tpublic void readImpl()\n" + "\t{\n";

					for (String partName : parts.keySet())
					{
						String partType = parts.get(partName);

						if (partType.contains("?"))
						{
							content +=
									"\t\tread" + partType.substring(0, 1).toUpperCase() + "(); // " + partName + "\n";
						}
						else if (partType.equals("b"))
						{
							content += "\t\treadB(new byte[1]); // " + partName + " (TODO: check size)\n";
						}
						else
						{
							content += "\t\t_" + partName + " = read" + partType.toUpperCase() + "();\n";
						}
					}

					content += "\t}\n" + "\t\n" + "\t@Override\n" + "\tpublic void runImpl()\n" + "\t{\n" +
							"\t\t// TODO\n" +
							"\t\tLog.info(getType() + \" was received from \" + getClient() + \".\");\n" + "\t}\n" +
							"}\n" + "\n";
					Util.writeFile("./java/l2server/gameserver/network/clientpackets/" + name + ".java", content);
				}
				else
				{
					Log.warning("Server packet not implemented: " + name);

					if (!GENERATE_MISSING_PACKETS)
					{
						continue;
					}

					String content = "package l2server.gameserver.network.serverpackets;\n" + "\n" + "/**\n" +
							" * @author MegaParzor!\n" + " */\n" + "public class " + name +
							" extends L2GameServerPacket\n" + "{\n";

					boolean partDeclared = false;
					for (String partName : parts.keySet())
					{
						String partType = parts.get(partName);
						if (partType.contains("?") || partType.equals("b"))
						{
							continue;
						}

						String type = "int";
						switch (partType)
						{
							case "c":
								type = "byte";
								break;
							case "f":
								type = "float";
								break;
							case "Q":
								type = "long";
								break;
							case "S":
								type = "String";
								break;
						}

						content += "\tprivate " + type + " _" + partName + ";\n";
						partDeclared = true;
					}

					if (partDeclared)
					{
						content += "\t\n" + "\tpublic " + name + "(";

						for (String partName : parts.keySet())
						{
							String partType = parts.get(partName);
							if (partType.contains("?") || partType.equals("b"))
							{
								continue;
							}

							String type = "int";
							switch (partType)
							{
								case "c":
									type = "byte";
									break;
								case "f":
									type = "float";
									break;
								case "Q":
									type = "long";
									break;
								case "S":
									type = "String";
									break;
							}

							content += type + " " + partName + ", ";
						}

						content = content.substring(0, content.length() - 2);

						content += ")\n" + "\t{\n";

						for (String partName : parts.keySet())
						{
							String partType = parts.get(partName);
							if (partType.contains("?") || partType.equals("b"))
							{
								continue;
							}

							content += "\t\t_" + partName + " = " + partName + ";\n";
						}

						content += "\t}\n" + "\t\n";
					}

					content += "\t@Override\n" + "\tpublic void writeImpl()\n" + "\t{\n";

					for (String partName : parts.keySet())
					{
						String partType = parts.get(partName);

						String neutral = "0x00";
						if (partType.contains("S"))
						{
							neutral = "\"\"";
						}
						else if (partType.contains("f"))
						{
							neutral = "0.0f";
						}

						if (partType.contains("?"))
						{
							content += "\t\twrite" + partType.substring(0, 1).toUpperCase() + "(" + neutral + "); // " +
									partName + "\n";
						}
						else if (partType.equals("b"))
						{
							content += "\t\twriteB(new byte[1]); // " + partName + " (TODO: check size)\n";
						}
						else
						{
							content += "\t\twrite" + partType.toUpperCase() + "(_" + partName + ");\n";
						}
					}

					content += "\t}\n" + "}\n" + "\n";
					Util.writeFile("./java/l2server/gameserver/network/serverpackets/" + name + ".java", content);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static byte[] getClientPacketOpcode(Class<?> packetClass)
	{
		byte[] opcode = ClientPackets.get(packetClass);
		if (opcode == null)
		{
			Log.warning("There's no opcode for the client packet " + packetClass.getSimpleName());
		}

		return opcode;
	}

	public static byte[] getServerPacketOpcode(Class<?> packetClass)
	{
		byte[] opcode = ServerPackets.get(packetClass);
		if (opcode == null)
		{
			Log.warning("There's no opcode for the server packet " + packetClass.getSimpleName());
		}

		return opcode;
	}
}
