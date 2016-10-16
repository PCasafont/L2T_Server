package l2server.gameserver.network.clientpackets;

import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.serverpackets.KeyPacket;

public class ProtocolVersion extends L2GameClientPacket
{
	private int version;
	private byte[] data;
	private byte[] check;

	public ProtocolVersion()
	{
	}

	@Override
	protected void readImpl()
	{
		if (this.buf.remaining() >= 0x04)
		{
			this.version = readD();
		}

		if (this.buf.remaining() == 0x104)
		{
			this.data = new byte[0x100];
			this.check = new byte[4];
			readB(this.data);
			readB(this.check);
		}
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		client.setProtocolVersion(this.version);
		if (this.version == -2L)
		{
			client.closeNow();
			return;
		}
		else if (this.version == -3L)
		{
			client.closeNow();
			return;
		}
		else if (this.version == 64)
		{
			client.setProtocolOk(true);
			KeyPacket pk = new KeyPacket(client.enableCrypt(), 1);
			client.sendPacket(pk);
			return;
		}

		client.setProtocolOk(false);
		client.close(new KeyPacket(null));
	}
}
