package l2server.gameserver.network;

public class GameCrypt
{
	private final byte[] inKey = new byte[16];
	private final byte[] outKey = new byte[16];
	private boolean isEnabled;

	public void setKey(byte[] key)
	{
		System.arraycopy(key, 0, inKey, 0, 16);
		System.arraycopy(key, 0, outKey, 0, 16);
	}

	public void decrypt(byte[] raw, final int offset, final int size)
	{
		if (!isEnabled)
		{
			return;
		}

		int temp = 0;
		for (int i = 0; i < size; i++)
		{
			int temp2 = raw[offset + i] & 0xFF;
			raw[offset + i] = (byte) (temp2 ^ inKey[i & 15] ^ temp);
			temp = temp2;
		}

		int old = inKey[8] & 0xff;
		old |= inKey[9] << 8 & 0xff00;
		old |= inKey[10] << 0x10 & 0xff0000;
		old |= inKey[11] << 0x18 & 0xff000000;

		old += size;

		inKey[8] = (byte) (old & 0xff);
		inKey[9] = (byte) (old >> 0x08 & 0xff);
		inKey[10] = (byte) (old >> 0x10 & 0xff);
		inKey[11] = (byte) (old >> 0x18 & 0xff);
	}

	public void encrypt(byte[] raw, final int offset, final int size)
	{
		if (!isEnabled)
		{
			isEnabled = true;
			return;
		}

		int temp = 0;
		for (int i = 0; i < size; i++)
		{
			int temp2 = raw[offset + i] & 0xFF;
			temp = temp2 ^ outKey[i & 15] ^ temp;
			raw[offset + i] = (byte) temp;
		}

		int old = outKey[8] & 0xff;
		old |= outKey[9] << 8 & 0xff00;
		old |= outKey[10] << 0x10 & 0xff0000;
		old |= outKey[11] << 0x18 & 0xff000000;

		old += size;

		outKey[8] = (byte) (old & 0xff);
		outKey[9] = (byte) (old >> 0x08 & 0xff);
		outKey[10] = (byte) (old >> 0x10 & 0xff);
		outKey[11] = (byte) (old >> 0x18 & 0xff);
	}
}
