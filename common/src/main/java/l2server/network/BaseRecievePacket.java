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

package l2server.network;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:30:12 $
 */
public abstract class BaseRecievePacket {
	private byte[] decrypt;
	private int off;
	
	public BaseRecievePacket(byte[] decrypt) {
		this.decrypt = decrypt;
		off = 1; // skip packet type id
	}
	
	public int readD() {
		int result = decrypt[off++] & 0xff;
		result |= decrypt[off++] << 8 & 0xff00;
		result |= decrypt[off++] << 0x10 & 0xff0000;
		result |= decrypt[off++] << 0x18 & 0xff000000;
		return result;
	}
	
	public int readC() {
		return decrypt[off++] & 0xff;
	}
	
	public int readH() {
		int result = decrypt[off++] & 0xff;
		result |= decrypt[off++] << 8 & 0xff00;
		return result;
	}
	
	public double readF() {
		long result = decrypt[off++] & 0xff;
		result |= (decrypt[off++] & 0xffL) << 8L;
		result |= (decrypt[off++] & 0xffL) << 16L;
		result |= (decrypt[off++] & 0xffL) << 24L;
		result |= (decrypt[off++] & 0xffL) << 32L;
		result |= (decrypt[off++] & 0xffL) << 40L;
		result |= (decrypt[off++] & 0xffL) << 48L;
		result |= (decrypt[off++] & 0xffL) << 56L;
		return Double.longBitsToDouble(result);
	}
	
	public String readS() {
		String result = null;
		try {
			result = new String(decrypt, off, decrypt.length - off, "UTF-16LE");
			result = result.substring(0, result.indexOf(0x00));
		} catch (Exception e) {
			e.printStackTrace();
		}
		off += result.length() * 2 + 2;
		return result;
	}
	
	public final byte[] readB(int length) {
		byte[] result = new byte[length];
		System.arraycopy(decrypt, off + 0, result, 0, length);
		off += length;
		return result;
	}
	
	public long readQ() {
		long result = decrypt[off++] & 0xff;
		result |= (decrypt[off++] & 0xffL) << 8L;
		result |= (decrypt[off++] & 0xffL) << 16L;
		result |= (decrypt[off++] & 0xffL) << 24L;
		result |= (decrypt[off++] & 0xffL) << 32L;
		result |= (decrypt[off++] & 0xffL) << 40L;
		result |= (decrypt[off++] & 0xffL) << 48L;
		result |= (decrypt[off++] & 0xffL) << 56L;
		return result;
	}
}
