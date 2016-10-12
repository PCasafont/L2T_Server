/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.loginserver;

import l2server.loginserver.network.L2LoginClient;
import l2server.loginserver.network.serverpackets.Init;
import l2server.network.*;
import l2server.util.IPv4Filter;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author KenM
 */
public class SelectorHelper implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter
{
	private ThreadPoolExecutor _generalPacketsThreadPool;
	private IPv4Filter _ipv4filter;

	public SelectorHelper()
	{
		_generalPacketsThreadPool = new ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
		_ipv4filter = new IPv4Filter();
	}

	/**
	 */
	@Override
	public void execute(ReceivablePacket<L2LoginClient> packet)
	{
		_generalPacketsThreadPool.execute(packet);
	}

	/**
	 */
	@Override
	public L2LoginClient create(MMOConnection<L2LoginClient> con)
	{
		L2LoginClient client = new L2LoginClient(con);
		client.sendPacket(new Init(client));
		return client;
	}

	/**
	 */
	@Override
	public boolean accept(SocketChannel sc)
	{
		return _ipv4filter.accept(sc) && !LoginController.getInstance().isBannedAddress(sc.socket().getInetAddress());
	}
}
