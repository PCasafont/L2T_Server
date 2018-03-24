package ai.individual.AltarOfFantasyIsle;

import l2server.gameserver.Announcements;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yomi
 */

public class AltarOfFantasyIsle extends Quest
{
    private static final int _jisooId = 91003;
    private static final String _qn = "AltarOfFantasyIsle";
    private static Map<Integer, Boolean> _spawnInfo = new HashMap<Integer, Boolean>(3);
    private static final int[] _raidIds = {91004, 91005, 80246};
    private static final int[] _stoneIds = {9743, 1261, 20772};
    private static final int _altarofFantasyIsleId = 91003;

    public AltarOfFantasyIsle(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addSpawn(_jisooId, -44247, 75489, -3654, 843, false, 0);


        addFirstTalkId(_altarofFantasyIsleId);
        addStartNpc(_altarofFantasyIsleId);
        addTalkId(_altarofFantasyIsleId);

        for (int i : _raidIds)
        {
            addKillId(i);
            _spawnInfo.put(i, false);
        }
    }
	
    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        return "AltarOfFantasyIsle.html";
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (event.startsWith("trySpawnBoss"))
        {
	        int bossId = Integer.valueOf(event.split(" ")[1]);
	        int stoneId = 0;
	        if (bossId == _raidIds[0])
	        {
		        stoneId = _stoneIds[0];
	        }
	        else if (bossId == _raidIds[1])
	        {
	        	stoneId = _stoneIds[1];
            }
	        else
	        {
		        stoneId = _stoneIds[2];
	        }
	        
            if (stoneId == 0) //Cheating?
            {
                return null;
            }

            synchronized (_spawnInfo)
            {
            	if (!_spawnInfo.get(bossId))
                    if (!player.destroyItemByItemId(_qn, stoneId, 1, player, true))
                    {
                        return stoneId + "-no.html";
                    }
                    Announcements.getInstance().announceToAll("Jisoo: One of the Descendants has been summoned... Death is coming...");
                    _spawnInfo.put(bossId, true); //Boss is spawned

                    L2Attackable boss =
                            (L2Attackable) addSpawn(bossId, npc.getX(), npc.getY() + 200, npc.getZ(), 0, false, 0,
                                    true);
                    boss.setTarget(player);
                    boss.addDamageHate(player, 9999, 9999);
                    boss.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
	            
            }
        }

        return super.onAdvEvent(event, npc, player);
    }



    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        synchronized (_spawnInfo)
        {
            _spawnInfo.put(npc.getNpcId(), false);

        }
        Announcements.getInstance().announceToAll("Descendant: Ughnnn... This... can't be... happening! Nooooo!");
        return super.onKill(npc, player, isPet);
    }

	public String onTalkNpc(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	public static void main(String[] args)
    {
        new AltarOfFantasyIsle(-1, _qn, "ai/individual");
    }
}
