package l2server.gameserver.network.clientpackets;

import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.serverpackets.KeyPacket;

public class ProtocolVersion extends L2GameClientPacket
{
	private int _version;
	private byte[] _data;
	private byte[] _check;

	public ProtocolVersion()
	{
	}

	@Override
	protected void readImpl()
	{
		if (_buf.remaining() >= 0x04)
		{
			_version = readD();
		}

		if (_buf.remaining() == 0x104)
		{
			_data = new byte[0x100];
			_check = new byte[4];
			readB(_data);
			readB(_check);
		}
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		client.setProtocolVersion(_version);
		if (_version == -2L)
		{
			client.closeNow();
			return;
		}
		else if (_version == -3L)
		{
			client.closeNow();
			return;
		}
		else if (_version == 64)
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
