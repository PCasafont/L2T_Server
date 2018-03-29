/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l2server.network;

/**
 * @param <E>
 * @author Forsaiken
 */
public final class NioNetStackList<E>
{
	private final NioNetStackNode start = new NioNetStackNode();

	private final NioNetStackNodeBuf buf = new NioNetStackNodeBuf();

	private NioNetStackNode end = new NioNetStackNode();

	public NioNetStackList()
	{
		clear();
	}

	public final void addLast(final E elem)
	{
		final NioNetStackNode newEndNode = buf.removeFirst();
		end.value = elem;
		end.next = newEndNode;
		end = newEndNode;
	}

	public final E removeFirst()
	{
		final NioNetStackNode old = start.next;
		final E value = old.value;
		start.next = old.next;
		buf.addLast(old);
		return value;
	}

	public final boolean isEmpty()
	{
		return start.next == end;
	}

	public final void clear()
	{
		start.next = end;
	}

	protected final class NioNetStackNode
	{
		protected NioNetStackNode next;

		protected E value;
	}

	private final class NioNetStackNodeBuf
	{
		private final NioNetStackNode start = new NioNetStackNode();

		private NioNetStackNode end = new NioNetStackNode();

		NioNetStackNodeBuf()
		{
			start.next = end;
		}

		final void addLast(final NioNetStackNode node)
		{
			node.next = null;
			node.value = null;
			end.next = node;
			end = node;
		}

		final NioNetStackNode removeFirst()
		{
			if (start.next == end)
			{
				return new NioNetStackNode();
			}

			final NioNetStackNode old = start.next;
			start.next = old.next;
			return old;
		}
	}
}
