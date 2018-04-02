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

package l2server.gameserver.instancemanager

import l2server.Config
import l2server.gameserver.idfactory.IdFactory
import l2server.gameserver.model.World
import l2server.gameserver.model.VehiclePathPoint
import l2server.gameserver.model.actor.instance.BoatInstance
import l2server.gameserver.network.serverpackets.L2GameServerPacket
import l2server.gameserver.templates.StatsSet
import l2server.gameserver.templates.chars.CreatureTemplate
import java.util.HashMap

object BoatManager {

    const val TALKING_ISLAND = 1
    const val GLUDIN_HARBOR = 2
    const val RUNE_HARBOR = 3

    private val boats = HashMap<Int, BoatInstance>()
    private val docksBusy = BooleanArray(3, { false })

    fun getNewBoat(boatId: Int, x: Int, y: Int, z: Int, heading: Int): BoatInstance? {
        if (!Config.ALLOW_BOAT) {
            return null
        }

        val npcDat = StatsSet()
        npcDat.set("npcId", boatId)
        npcDat.set("level", 0)
        npcDat.set("jClass", "boat")

        npcDat.set("STR", 0)
        npcDat.set("CON", 0)
        npcDat.set("DEX", 0)
        npcDat.set("INT", 0)
        npcDat.set("WIT", 0)
        npcDat.set("MEN", 0)

        // npcDat.set("name", "");
        npcDat.set("collisionRadius", 0)
        npcDat.set("collisionHeight", 0)
        npcDat.set("sex", "male")
        npcDat.set("type", "")
        npcDat.set("atkRange", 0)
        npcDat.set("mpMax", 0)
        npcDat.set("cpMax", 0)
        npcDat.set("rewardExp", 0)
        npcDat.set("rewardSp", 0)
        npcDat.set("pAtk", 0)
        npcDat.set("mAtk", 0)
        npcDat.set("pAtkSpd", 0)
        npcDat.set("aggroRange", 0)
        npcDat.set("mAtkSpd", 0)
        npcDat.set("rhand", 0)
        npcDat.set("lhand", 0)
        npcDat.set("armor", 0)
        npcDat.set("walkSpd", 0)
        npcDat.set("runSpd", 0)
        npcDat.set("hpMax", 50000)
        npcDat.set("hpReg", 3e-3)
        npcDat.set("mpReg", 3e-3)
        npcDat.set("pDef", 100)
        npcDat.set("mDef", 100)
        val template = CreatureTemplate(npcDat)
        val boat = BoatInstance(IdFactory.getInstance().nextId, template)
        boats[boat.objectId] = boat
        boat.heading = heading
        boat.setXYZInvisible(x, y, z)
        boat.spawnMe()
        return boat
    }

    /**
     * @param boatId
     * @return
     */
    fun getBoat(boatId: Int): BoatInstance? {
        return boats[boatId]
    }

    /**
     * Lock/unlock dock so only one ship can be docked
     *
     * @param h     Dock Id
     * @param value True if dock is locked
     */
    fun dockShip(h: Int, value: Boolean) {
        try {
            docksBusy[h] = value
        } catch (ignored: ArrayIndexOutOfBoundsException) {
        }

    }

    /**
     * Check if dock is busy
     *
     * @param h Dock Id
     * @return Trye if dock is locked
     */
    fun dockBusy(h: Int): Boolean {
        try {
            return docksBusy[h]
        } catch (e: ArrayIndexOutOfBoundsException) {
            return false
        }

    }

    /**
     * Broadcast one packet in both path points
     */
    fun broadcastPacket(point1: VehiclePathPoint, point2: VehiclePathPoint, packet: L2GameServerPacket) {
        var dx: Double
        var dy: Double
        val players = World.getInstance().allPlayers.values
        for (player in players) {
            if (player == null) {
                continue
            }

            dx = player.x.toDouble() - point1.x
            dy = player.y.toDouble() - point1.y
            if (Math.sqrt(dx * dx + dy * dy) < Config.BOAT_BROADCAST_RADIUS) {
                player.sendPacket(packet)
            } else {
                dx = player.x.toDouble() - point2.x
                dy = player.y.toDouble() - point2.y
                if (Math.sqrt(dx * dx + dy * dy) < Config.BOAT_BROADCAST_RADIUS) {
                    player.sendPacket(packet)
                }
            }
        }
    }

    /**
     * Broadcast several packets in both path points
     */
    fun broadcastPackets(point1: VehiclePathPoint, point2: VehiclePathPoint, vararg packets: L2GameServerPacket) {
        var dx: Double
        var dy: Double
        val players = World.getInstance().allPlayers.values
        for (player in players) {
            if (player == null) {
                continue
            }
            dx = player.x.toDouble() - point1.x
            dy = player.y.toDouble() - point1.y
            if (Math.sqrt(dx * dx + dy * dy) < Config.BOAT_BROADCAST_RADIUS) {
                for (p in packets) {
                    player.sendPacket(p)
                }
            } else {
                dx = player.x.toDouble() - point2.x
                dy = player.y.toDouble() - point2.y
                if (Math.sqrt(dx * dx + dy * dy) < Config.BOAT_BROADCAST_RADIUS) {
                    for (p in packets) {
                        player.sendPacket(p)
                    }
                }
            }
        }
    }
}
