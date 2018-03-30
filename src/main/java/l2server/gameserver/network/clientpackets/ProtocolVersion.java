package l2server.gameserver.network.clientpackets;

import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.serverpackets.KeyPacket;

public class ProtocolVersion extends L2GameClientPacket {
	private int version;
	private byte[] data;
	private byte[] check;

	public ProtocolVersion() {
	}

	@Override
	protected void readImpl() {
		if (buf.remaining() >= 0x04) {
			version = readD();
		}

		if (buf.remaining() == 0x104) {
			data = new byte[0x100];
			check = new byte[4];
			readB(data);
			readB(check);
		}
	}

	@Override
	protected void runImpl() {
		L2GameClient client = getClient();
		client.setProtocolVersion(version);
		if (version == -2L) {
			client.closeNow();
			return;
		} else if (version == -3L) {
			client.closeNow();
			return;
		} else if (version == 140) {
			client.setProtocolOk(true);
			KeyPacket pk = new KeyPacket(client.enableCrypt(), 1);
			client.sendPacket(pk);
			return;
		}

		client.setProtocolOk(false);
		client.close(new KeyPacket(null));
	}
}
