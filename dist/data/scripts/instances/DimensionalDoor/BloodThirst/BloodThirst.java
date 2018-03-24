package instances.DimensionalDoor.BloodThirst;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2NpcBufferInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

/**
 * @author LasTravel
 *         <p>
 *         Blood Thirst
 *         <p>
 *         Source:
 *         - https://www.youtube.com/watch?v=t-rLTz-_ACE
 *         - https://www.youtube.com/watch?v=ElaX6oM5l1g
 */

public class BloodThirst extends L2AttackableAIScript
{
    //Quest
    private static final boolean _debug = false;
    private static final String _qn = "BloodThirst";

    //Ids
    private static final int _instanceTemplateId = 505;
    private static final int _bloodThirstId = 27481;
    private static final int _reuseMinutes = 1440;

    public BloodThirst(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());

        addKillId(_bloodThirstId);
    }

    private class BloodThirstWorld extends InstanceWorld
    {
        private L2Npc _bloodThirst;

        private BloodThirstWorld()
        {
        }
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAdvEvent: " + event);
        }

        InstanceWorld wrld = null;
        if (npc != null)
        {
            wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        }
        else if (player != null)
        {
            wrld = InstanceManager.getInstance().getPlayerWorld(player);
        }
        else
        {
            Log.warning(getName() + ": onAdvEvent: Unable to get world.");
            return null;
        }

        if (wrld != null && wrld instanceof BloodThirstWorld)
        {
            BloodThirstWorld world = (BloodThirstWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_start"))
            {
                InstanceManager.getInstance().stopWholeInstance(world.instanceId);
                InstanceManager.getInstance().showVidToInstance(109, world.instanceId);

                world._bloodThirst =
                        addSpawn(_bloodThirstId, 56167, -186938, -7944, 16383, false, 0, true, world.instanceId);

                for (L2Spawn iSpawn : SpawnTable.getInstance().getSpecificSpawns("blood_thirst"))
                {
                    if (iSpawn == null)
                    {
                        continue;
                    }

                    L2Npc iNpc = addSpawn(iSpawn.getNpcId(), iSpawn.getX(), iSpawn.getY(), iSpawn.getZ(),
                            iSpawn.getHeading(), false, 0, true, world.instanceId);

                    L2Spawn spawn = iNpc.getSpawn();
                    spawn.setRespawnDelay(20);
                    spawn.startRespawn();
                }

                final int instanceId = world.instanceId;
                ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        InstanceManager.getInstance().startWholeInstance(instanceId);
                    }
                }, 13000);
            }
        }
        else if (event.equalsIgnoreCase("enterToInstance"))
        {
            try
            {
                enterInstance(player);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof BloodThirstWorld)
        {
            BloodThirstWorld world = (BloodThirstWorld) tmpworld;
            if (npc == world._bloodThirst)
            {
                player.addItem(_qn, DimensionalDoor.getDimensionalDoorRewardId(),
                       10, player, true);

                InstanceManager.getInstance().setInstanceReuse(world.instanceId, _instanceTemplateId, _reuseMinutes);
                InstanceManager.getInstance().finishInstance(world.instanceId, true);
            }
        }
        return "";
    }

    @Override
    public final String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        if (npc.getNpcId() == DimensionalDoor.getNpcManagerId())
        {
            return _qn + ".html";
        }

        return super.onTalk(npc, player);
    }

    private final synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof BloodThirstWorld))
            {
                player.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
                return;
            }

            Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
            if (inst != null)
            {
                if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId()))
                {
                    player.setInstanceId(world.instanceId);
                    player.teleToLocation(56164, -185809, -7944);
                }
            }

            return;
        }
        else
        {
            if (!_debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, _instanceTemplateId, 1, 1, 99, Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");
            world = new BloodThirstWorld();
            world.instanceId = instanceId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            world.allowed.add(player.getObjectId());

            player.stopAllEffectsExceptThoseThatLastThroughDeath();
            player.setInstanceId(instanceId);
            player.teleToLocation(56164, -185809, -7944, true);

            L2NpcBufferInstance.giveBasicBuffs(player);

            startQuestTimer("stage_1_start", 20000, null, player);

            Log.fine(getName() + ":  instance started: " + instanceId + " created by player: " + player.getName());

            return;
        }
    }

    public static void main(String[] args)
    {
        new BloodThirst(-1, _qn, "instances/DimensionalDoor");
    }
}
