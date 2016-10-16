package l2server.gameserver.network;

public class GameCrypt
{
	private final byte[] inKey = new byte[16];
	private final byte[] outKey = new byte[16];
	private boolean isEnabled;

	public void setKey(byte[] key)
	{
		System.arraycopy(key, 0, this.inKey, 0, 16);
		System.arraycopy(key, 0, this.outKey, 0, 16);
	}

	public void decrypt(byte[] raw, final int offset, final int size)
	{
		if (!this.isEnabled)
		{
			return;
		}

		int temp = 0;
		for (int i = 0; i < size; i++)
		{
			int temp2 = raw[offset + i] & 0xFF;
			raw[offset + i] = (byte) (temp2 ^ this.inKey[i & 15] ^ temp);
			temp = temp2;
		}

		int old = this.inKey[8] & 0xff;
		old |= this.inKey[9] << 8 & 0xff00;
		old |= this.inKey[10] << 0x10 & 0xff0000;
		old |= this.inKey[11] << 0x18 & 0xff000000;

		old += size;

		this.inKey[8] = (byte) (old & 0xff);
		this.inKey[9] = (byte) (old >> 0x08 & 0xff);
		this.inKey[10] = (byte) (old >> 0x10 & 0xff);
		this.inKey[11] = (byte) (old >> 0x18 & 0xff);
	}

	public void encrypt(byte[] raw, final int offset, final int size)
	{
		if (!this.isEnabled)
		{
			this.isEnabled = true;
			return;
		}

		int temp = 0;
		for (int i = 0; i < size; i++)
		{
			int temp2 = raw[offset + i] & 0xFF;
			temp = temp2 ^ this.outKey[i & 15] ^ temp;
			raw[offset + i] = (byte) temp;
		}

		int old = this.outKey[8] & 0xff;
		old |= this.outKey[9] << 8 & 0xff00;
		old |= this.outKey[10] << 0x10 & 0xff0000;
		old |= this.outKey[11] << 0x18 & 0xff000000;

		old += size;

		this.outKey[8] = (byte) (old & 0xff);
		this.outKey[9] = (byte) (old >> 0x08 & 0xff);
		this.outKey[10] = (byte) (old >> 0x10 & 0xff);
		this.outKey[11] = (byte) (old >> 0x18 & 0xff);
	}
}
