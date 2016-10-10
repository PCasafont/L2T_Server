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

package ai.group_template;

import l2server.gameserver.Announcements;
import l2server.gameserver.GmListTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.util.Rnd;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LasTravel
 */

public class EventBosses extends L2AttackableAIScript
{
    private static boolean _isBossActive = false;
    private static int _bossStatus = 0;
    private static final L2Skill _knightFrenzy = SkillTable.getInstance().getInfo(10025, 4);
    private static final L2Skill _finalUltimateDefense = SkillTable.getInstance().getInfo(10017, 4);
    private static Map<L2PcInstance, String> _attackerIps = new HashMap<L2PcInstance, String>();

    private static final String[][] _individualDrop = {
            //Boss id, itemId, chance, min, max;
            {"80106", "10314,5,1,1;4357,100,2000,8000;"},
            //Beleth:	Ring of Beleth, Seal of Shilen

            {"80107", "21714,10,1,1;21894,10,1,1;16026,5,1,1;16025,5,1,1;4357,100,2000,8000;"},
            //Freya:	Sealed Cloak of Freya, Ice Queen's Tiara, Blessed Necklace of Freya, Necklace of Freya, Seal of Shilen

            {"80108", "19455,5,1,1;19456,5,1,1;19454,5,1,1;19453,5,1,1;19452,5,1,1;19451,5,1,1;4357,100,2000,8000;"},
            //Istina: 	Istina's Bracelet, Istina's Shirt, Istina's Belt, Istina's Necklace, Istina's Earring, Istina's Ring, Seal of Shilen

            {"80109", "19462,5,1,1;19460,5,1,1;19461,5,1,1;19457,5,1,1;19458,5,1,1;19459,5,1,1;4357,100,2000,8000;"},
            //Octavis:	Octavis' Shirt, Octavis' Belt, Octavis' Bracelet, Octavis' Ring, Octavis' Earring, Octavis' Necklace, Seal of Shilen

            {"80110", "4357,100,2000,8000;"},
            //Spezion:	Seal of Shilen

            {"80111", "4357,100,2000,8000;"},
            //Kimerian:	Seal of Shilen

            {"80112", "4357,100,2000,8000;"},
            //Balok:	Seal of Shilen

            {"80113", "4357,100,2000,8000;"},
            //Harnak":	Seal of Shilen

            {"80114", "35570,3,1,1;35294,3,1,1;35293,3,1,1;4357,100,2000,8000;"},
            //Tauti: 	Tauti's Ring, Refined Tauti's Bracelet, Tauti's Bracelet, Seal of Shilen

            {"80115", "21713,10,1,1;21892,20,1,1;6659,5,1,1;21712,5,1,1;4357,100,2000,8000;"},
            //Zaken: 	Sealed Cloak of Zaken, Pirate King Hat, Earring of Zaken, Blessed Earring of Zaken, Seal of Shilen

            {"80116", "22356,5,1,1;12784,20,1,1;6660,5,1,1;4357,100,2000,8000;"},
            //Queen Ant":	Mount - Tame Princess Ant, Ant Hat, Queen Ant's Ring, Seal of Shilen

            {"80117", "6658,5,1,1;4357,100,2000,8000;"},
            //Baium":	Baium's Ring, Seal of Shilen

            {"80118", "6661,5,1,1;4357,100,2000,8000;"},
            //Orfen":	Orfen's Earring, Seal of Shilen

            {"80119", "21718,15,1,1;8191,5,1,1;21893,20,1,1;4357,100,2000,8000;"},
            //Scarlet":	Frintezza's Cloak, Frintezza's Necklace, Halisha's Helmet, Seal of Shilen
    };

    public EventBosses(int questId, String name, String descr)
    {
        super(questId, name, descr);

        for (int i = 80106; i <= 80119; i++)
        {
            addKillId(i);
            addSpawnId(i);
            addAttackId(i);
        }
    }

    @Override
    public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
    {
        if (!_attackerIps.containsValue(player.getExternalIP()))
        {
            _attackerIps.put(player, player.getExternalIP());
        }

        if (_bossStatus == 0 && npc.getCurrentHp() < npc.getMaxHp() * 0.15)
        {
            _bossStatus = 1;

            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "WooooooooaHHH!"));

            _knightFrenzy.getEffects(npc, npc);
        }
        else if (_bossStatus == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.05)
        {
            _bossStatus = 2;

            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "NOO! NOOO!!"));

            _finalUltimateDefense.getEffects(npc, npc);
        }

        return super.onAttack(npc, player, damage, isPet, skill);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (npc != null && npc.getInstanceId() == 0)
        {
            for (Map.Entry<L2PcInstance, String> playerInfo : _attackerIps.entrySet())
            {
                if (playerInfo == null)
                {
                    continue;
                }

                L2PcInstance playerToReward = playerInfo.getKey();

                if (playerToReward == null || !playerToReward.isOnline() ||
                        !playerToReward.isInsideRadius(npc, 4000, false, false))
                {
                    continue;
                }

                boolean rewarded = false;

                for (String[] i : _individualDrop)
                {
                    if (Integer.valueOf(i[0]) == npc.getNpcId()) //Id found
                    {
                        String[] a = i[1].split(";");

                        if (!a[0].isEmpty())
                        {
                            for (String b : a)
                            {
                                if (rewarded)
                                {
                                    continue;
                                }

                                String[] c = b.split(",");

                                if (!c[0].isEmpty())
                                {
                                    if (Integer.valueOf(c[1]) > Rnd.get(100))
                                    {
                                        rewarded = true;

                                        int itemId = Integer.valueOf(c[0]);

                                        int itemCount = Rnd.get(Integer.valueOf(c[2]), Integer.valueOf(c[3]));

                                        playerToReward.addItem(getName(), itemId, itemCount, npc, true);

                                        npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(),
                                                " Player:  " + playerToReward.getName() + ", rewarded with: " +
                                                        ItemTable.getInstance().getTemplate(itemId).getName() + "(" +
                                                        itemCount + ")"));

                                        GmListTable.broadcastMessageToGMs(
                                                getName() + ": Player: " + playerToReward.getName() +
                                                        ", rewarded with: " +
                                                        ItemTable.getInstance().getTemplate(itemId).getName() + "(" +
                                                        itemCount + ")");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //End event
            _isBossActive = false;

            _bossStatus = 0;

            _attackerIps.clear();
        }

        return super.onKill(npc, player, isPet);
    }

    @Override
    public final String onSpawn(L2Npc npc)
    {
        if (npc != null && npc.getInstanceId() == 0)
        {
            L2Spawn spawn = npc.getSpawn();

            spawn.stopRespawn();

            if (_isBossActive) //Already active
            {
                npc.deleteMe();

                GmListTable.broadcastMessageToGMs("EventBosses: You can't spawn another boss at this moment");

                return "";
            }

            Announcements.getInstance().announceToAll("Event Raid: " + npc.getName() + " has been spawned!");

            GmListTable.broadcastMessageToGMs("EventBosses: " + npc.getName() + " has been spawned!");

            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 1, npc.getName(), "Is the time to die noobs!"));

            _isBossActive = true;

            _bossStatus = 0;
        }

        return super.onSpawn(npc);
    }

    @Override
    public int getOnKillDelay(int npcId)
    {
        return 0;
    }

    public static void main(String[] args)
    {
        new EventBosses(-1, "EventBosses", "ai");
    }
}
