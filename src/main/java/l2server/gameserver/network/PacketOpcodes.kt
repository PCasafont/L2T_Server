package l2server.gameserver.network

import l2server.Config
import l2server.gameserver.ThreadPoolManager
import l2server.gameserver.util.Util
import l2server.log.Log
import l2server.util.loader.annotations.Load
import l2server.util.xml.XmlDocument
import l2server.util.xml.XmlNode

import java.io.File
import java.util.HashMap

/**
 * @author Pere
 */
object PacketOpcodes {

    private var PROTOCOL_FILE: String? = null
    private val GENERATE_MISSING_PACKETS = true

    val ClientPacketsFamily = PacketFamily()
    val ClientPackets: MutableMap<Class<*>, ByteArray> = HashMap()
    val ServerPackets: MutableMap<Class<*>, ByteArray> = HashMap()

    private var lastModified: Long = 0

    class PacketFamily {
        var switchLength: Int = 0
        val children: MutableMap<Int, Any> = HashMap()
    }

    @Load
    fun initialize() {
        PROTOCOL_FILE = "data_" + Config.SERVER_NAME + "/protocol.xml"
        if (!File(Config.DATAPACK_ROOT, PROTOCOL_FILE!!).exists()) {
            PROTOCOL_FILE = Config.DATA_FOLDER + "/protocol.xml"
        }

        load()

        // Auto reloader
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate({
            val f = File(Config.DATAPACK_ROOT, PROTOCOL_FILE!!)
            if (!f.isDirectory && f.lastModified() > lastModified) {
                load()
                //Log.info("Updated the protocols from the file " + f.getName());
            }
        }, 10000, 10000)
    }

    private fun load() {
        val file = File(Config.DATAPACK_ROOT, PROTOCOL_FILE!!)
        if (!file.exists()) {
            Log.warning("File " + file.absolutePath + " doesn't exist")
            return
        }

        if (!file.name.endsWith(".xml")) {
            return
        }

        ClientPacketsFamily.children.clear()
        ServerPackets.clear()

        val doc = XmlDocument(file)
        val n = doc.root
        if (n.name == "protocol") {
            for (d in n.getChildren()) {
                if (d.name != "packetfamilly") {
                    continue
                }

                val isClientPacket = d.getString("way").equals("ClientPackets", ignoreCase = true)
                parsePacketFamily(d, isClientPacket, ByteArray(0), if (isClientPacket) ClientPacketsFamily else null)
            }
        }

        Log.info("PacketOpcodes: Loaded " + ClientPackets.size + " Client Packets and " + ServerPackets.size + " Server Packets.")

        lastModified = file.lastModified()

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

    private fun parsePacketFamily(d: XmlNode, isClientPacket: Boolean, parentOpcode: ByteArray, family: PacketFamily?) {
        val length: Int
        when (d.getString("switchtype")) {
            "c" -> length = 1
            "h" -> length = 2
            "d" -> length = 4
            else -> {
                Log.warning("'" + d.getString("switchtype") + "' switch type is not supported.")
                return
            }
        }

        if (isClientPacket) {
            family!!.switchLength = length
        }

        for (y in d.getChildren()) {
            val subOpcode = Integer.decode(y.getString("id"))!!
            val opcode = ByteArray(parentOpcode.size + length)
            System.arraycopy(parentOpcode, 0, opcode, 0, parentOpcode.size)
            for (i in 0 until length) {
                opcode[parentOpcode.size + i] = (subOpcode shr i * 8 and 0xff).toByte()
            }

            if (y.name == "packetfamilly") {
                var newFamily: PacketFamily? = null
                if (isClientPacket) {
                    newFamily = PacketFamily()
                    family!!.children[subOpcode] = newFamily
                }

                parsePacketFamily(y, isClientPacket, opcode, newFamily)
                continue
            }

            if (y.name != "packet") {
                continue
            }

            val name = y.getString("name").replace("?", "")

            try {
                if (isClientPacket) {
                    val packetClass = Class.forName("l2server.gameserver.network.clientpackets.$name")
                    family!!.children[subOpcode] = packetClass
                    ClientPackets[packetClass] = opcode
                } else {
                    ServerPackets[Class.forName("l2server.gameserver.network.serverpackets.$name")] = opcode
                }
            } catch (e: ClassNotFoundException) {
                val parts = HashMap<String, String>()
                var rep = 1
                for (z in y.getChildren()) {
                    if (z.name != "part") {
                        continue
                    }

                    var partName = z.getString("name").replace("?", "").replace(" ", "")
                    val partType = z.getString("type")

                    if (partName.length == 0) {
                        partName = "unk"
                    }

                    partName = partName.substring(0, 1).toLowerCase() + partName.substring(1, partName.length)

                    if (parts.containsKey(partName) || parts.containsKey(partName + (rep - 1))) {
                        if (rep == 1) {
                            parts[partName + rep++] = parts[partName] + "?"
                            parts.remove(partName)
                        }

                        parts[partName + rep++] = "$partType?"
                    } else {
                        parts[partName] = partType
                    }
                }

                if (isClientPacket) {
                    Log.warning("Client packet not implemented: $name")

                    if (!GENERATE_MISSING_PACKETS) {
                        continue
                    }

                    var content = "package l2server.gameserver.network.clientpackets;\n" + "\n" + "import l2server.log.Log;\n" + "\n" + "/**\n" +
                            " * @author MegaParzor!\n" + " */\n" + "public class " + name + " extends L2GameClientPacket\n" + "{\n"

                    var partDeclared = false
                    for (partName in parts.keys) {
                        val partType = parts[partName]!!
                        if (partType.contains("?") || partType == "b") {
                            continue
                        }

                        var type = "int"
                        when (partType) {
                            "c" -> type = "byte"
                            "f" -> type = "float"
                            "Q" -> type = "long"
                            "S" -> type = "String"
                        }

                        content += "\t@SuppressWarnings(\"unused\")\n\tprivate $type _$partName;\n"
                        partDeclared = true
                    }

                    if (partDeclared) {
                        content += "\t\n"
                    }

                    content += "\t@Override\n" + "\tpublic void readImpl()\n" + "\t{\n"

                    for (partName in parts.keys) {
                        val partType = parts[partName]!!

                        if (partType.contains("?")) {
                            content += "\t\tread" + partType.substring(0, 1).toUpperCase() + "(); // " + partName + "\n"
                        } else if (partType == "b") {
                            content += "\t\treadB(new byte[1]); // $partName (TODO: check size)\n"
                        } else {
                            content += "\t\t_" + partName + " = read" + partType.toUpperCase() + "();\n"
                        }
                    }

                    content += "\t}\n" + "\t\n" + "\t@Override\n" + "\tpublic void runImpl()\n" + "\t{\n" + "\t\t// TODO\n" +
                            "\t\tLog.info(getType() + \" was received from \" + getClient() + \".\");\n" + "\t}\n" + "}\n" + "\n"
                    Util.writeFile("./java/l2server/gameserver/network/clientpackets/$name.java", content)
                } else {
                    Log.warning("Server packet not implemented: $name")

                    if (!GENERATE_MISSING_PACKETS) {
                        continue
                    }

                    var content = "package l2server.gameserver.network.serverpackets;\n" + "\n" + "/**\n" + " * @author MegaParzor!\n" + " */\n" +
                            "public class " + name + " extends L2GameServerPacket\n" + "{\n"

                    var partDeclared = false
                    for (partName in parts.keys) {
                        val partType = parts[partName]!!
                        if (partType.contains("?") || partType == "b") {
                            continue
                        }

                        var type = "int"
                        when (partType) {
                            "c" -> type = "byte"
                            "f" -> type = "float"
                            "Q" -> type = "long"
                            "S" -> type = "String"
                        }

                        content += "\tprivate $type _$partName;\n"
                        partDeclared = true
                    }

                    if (partDeclared) {
                        content += "\t\n\tpublic $name("

                        for (partName in parts.keys) {
                            val partType = parts[partName]!!
                            if (partType.contains("?") || partType == "b") {
                                continue
                            }

                            var type = "int"
                            when (partType) {
                                "c" -> type = "byte"
                                "f" -> type = "float"
                                "Q" -> type = "long"
                                "S" -> type = "String"
                            }

                            content += "$type $partName, "
                        }

                        content = content.substring(0, content.length - 2)

                        content += ")\n" + "\t{\n"

                        for (partName in parts.keys) {
                            val partType = parts[partName]!!
                            if (partType.contains("?") || partType == "b") {
                                continue
                            }

                            content += "\t\t_$partName = $partName;\n"
                        }

                        content += "\t}\n" + "\t\n"
                    }

                    content += "\t@Override\n" + "\tpublic void writeImpl()\n" + "\t{\n"

                    for (partName in parts.keys) {
                        val partType = parts[partName]!!

                        var neutral = "0x00"
                        if (partType.contains("S")) {
                            neutral = "\"\""
                        } else if (partType.contains("f")) {
                            neutral = "0.0f"
                        }

                        if (partType.contains("?")) {
                            content += "\t\twrite" + partType.substring(0, 1).toUpperCase() + "(" + neutral + "); // " + partName + "\n"
                        } else if (partType == "b") {
                            content += "\t\twriteB(new byte[1]); // $partName (TODO: check size)\n"
                        } else {
                            content += "\t\twrite" + partType.toUpperCase() + "(_" + partName + ");\n"
                        }
                    }

                    content += "\t}\n" + "}\n" + "\n"
                    Util.writeFile("./java/l2server/gameserver/network/serverpackets/$name.java", content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun getClientPacketOpcode(packetClass: Class<*>): ByteArray? {
        val opcode = ClientPackets[packetClass]
        if (opcode == null) {
            Log.warning("There's no opcode for the client packet " + packetClass.simpleName)
        }

        return opcode
    }

    fun getServerPacketOpcode(packetClass: Class<*>): ByteArray? {
        val opcode = ServerPackets[packetClass]
        if (opcode == null) {
            Log.warning("There's no opcode for the server packet " + packetClass.simpleName)
        }

        return opcode
    }
}
