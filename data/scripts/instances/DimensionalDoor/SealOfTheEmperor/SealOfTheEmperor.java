package instances.DimensionalDoor.SealOfTheEmperor;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2NpcBufferInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 *         <p>
 *         Source:
 *         - http://l2.eogamer.com/wiki/Seven_Signs_Part_5,_Seal_of_the_Emperor
 *         - http://www.youtube.com/watch?v=UNoLqqBgUyc
 *         - http://www.youtube.com/watch?v=wK9BgirjitI
 */

public class SealOfTheEmperor extends L2AttackableAIScript
{
    private static final String _qn = "SealOfTheEmperor";

    //Config
    private static final boolean _debug = false;
    private static final int _reuseMinutes = 1440;

    //Ids
    private static final int _npcManagerId = 80200;
    private static final int _disciplesGK = 32657;
    private static final int _instanceTemplateId = 112;
    private static final int _sacredSword = 15310;
    private static final int _courtWizardMagicStaff = 13809;
    private static final int _elmoredenHolyWater = 13808;
    private static final int _countMagican = 32598;
    private static final int _sealDevice = 80321;
    private static final int _anakim = 80325;
    private static final int _lilith = 80322;
    //private static final int _sealOfBinding 		= 13846;
    private static final int _lilimButcher = 80316;
    private static final int _lilimMagnus = 80317;
    private static final int _lilimKnight = 80318;
    private static final int[] _shilensEvils = {80319, 80320};
    private static final int[] _roomMobs =
            {_lilimButcher, _lilimMagnus, _lilimKnight, _shilensEvils[0], _shilensEvils[1]};
    private static final int[] _anakimMinions = {80326, 80327, 80328};
    private static final int[] _lilithMinions = {80323, 80324};
    private static final int[] _anakimChats = {19606, 19607, 19608, 19609, 19610, 19611, 19612, 19613, 19614};
    private static final int[] _lilitChats = {19615, 19616, 19617, 19618};
    private static final L2Skill _deviceSkill = SkillTable.getInstance().getInfo(5980, 3);

    public SealOfTheEmperor(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(_npcManagerId);
        addStartNpc(_npcManagerId);
        addTalkId(_countMagican);
        addStartNpc(_countMagican);
        addTalkId(_disciplesGK);
        addStartNpc(_disciplesGK);
        addAttackId(_sealDevice);
        addKillId(_sealDevice);

        for (int i : _roomMobs)
        {
            addAttackId(i);
            addKillId(i);
        }
    }

    private class SealOfTheEmperorWorld extends InstanceWorld
    {
        private List<L2Npc> toKill;
        private List<L2Npc> doneDevices;
        private L2PcInstance owner;
        private L2Npc bossLilith;
        private L2Npc bossAnakim;

        private SealOfTheEmperorWorld()
        {
            toKill = new ArrayList<L2Npc>();
            doneDevices = new ArrayList<L2Npc>();
        }
    }

    @Override
    public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof SealOfTheEmperorWorld)
        {
            SealOfTheEmperorWorld world = (SealOfTheEmperorWorld) tmpworld;
            if (npc.getNpcId() == _sealDevice)
            {
                if (npc.getCurrentHp() == 0)
                {
                    synchronized (world.doneDevices)
                    {
                        if (!world.doneDevices.contains(npc))
                        {
                            world.doneDevices.add(npc);

                            attacker.sendPacket(
                                    SystemMessage.getSystemMessage(SystemMessageId.SEALING_DEVICE_GLITTERS_AND_MOVES));

                            //attacker.addItem(_qn, _sealOfBinding, 1, npc, true);

                            //Check the end
                            if (world.doneDevices.size() == 4)
                            {
                                world.status++;

                                attacker.addItem(_qn, DimensionalDoor.getDimensionalDoorRewardId(),
                                       3, attacker, true);

                                InstanceManager.getInstance().showVidToInstance(13, world.instanceId);

                                final int worldInstid = world.instanceId;
                                ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        InstanceManager.getInstance()
                                                .setInstanceReuse(worldInstid, _instanceTemplateId, _reuseMinutes);
                                        InstanceManager.getInstance().finishInstance(worldInstid, true);
                                    }
                                }, ScenePlayerDataTable.getInstance().getVideoDuration(13));
                            }
                        }
                    }
                }
                else
                {
                    npc.setTarget(attacker);
                    npc.doCast(_deviceSkill);
                }
            }
        }
        return super.onAttack(npc, attacker, damage, isPet);
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

        if (wrld != null && wrld instanceof SealOfTheEmperorWorld)
        {
            SealOfTheEmperorWorld world = (SealOfTheEmperorWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_start"))
            {
                world.owner = player;

                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(17240101).openMe();

                //First Room
                for (int a = _roomMobs[0]; a <= _roomMobs[3]; a++)
                {
                    L2Npc minion = addSpawn(a, -89339, 217938, -7494, 26451, true, 0, false, world.instanceId);
                    synchronized (world.toKill)
                    {
                        world.toKill.add(minion);
                    }
                }
            }
            else if (event.equalsIgnoreCase("stage_2_start"))
            {
                //Start Room 2
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(17240102).openMe();

                //Spawn minions
                for (int b = 0; b <= 1; b++)
                {
                    for (int a = _roomMobs[0]; a <= _roomMobs[3]; a++)
                    {
                        L2Npc minion = addSpawn(a, -88588, 220082, -7494, 43572, true, 0, false, world.instanceId);
                        synchronized (world.toKill)
                        {
                            world.toKill.add(minion);
                        }
                    }
                }
            }
            else if (event.equalsIgnoreCase("readTheMessage"))
            {
                player.addItem(_qn, _courtWizardMagicStaff, 1, null, true);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BY_USING_COURT_WIZARD_STAFF));

                return "32598-2.html";
            }
            else if (event.equalsIgnoreCase("stage_3_start"))
            {
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(17240104).openMe();

                //Spawn minions
                for (int b = 0; b <= 1; b++)
                {
                    for (int a = _roomMobs[0]; a <= _roomMobs[3]; a++)
                    {
                        L2Npc minion = addSpawn(a, -87255, 220653, -7494, 15086, true, 0, false, world.instanceId);
                        synchronized (world.toKill)
                        {
                            world.toKill.add(minion);
                        }
                    }
                }
            }
            else if (event.equalsIgnoreCase("stage_4_start"))
            {
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(17240106).openMe();

                //MINIONZ
                for (int b = 0; b <= 1; b++)
                {
                    for (int a = _roomMobs[0]; a <= _roomMobs[3]; a++)
                    {
                        L2Npc minion = addSpawn(a, -85191, 219237, -7494, 42242, true, 0, false, world.instanceId);
                        synchronized (world.toKill)
                        {
                            world.toKill.add(minion);
                        }
                    }
                }
            }
            else if (event.equalsIgnoreCase("stage_5_start"))
            {
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(17240108).openMe();

                //MINIONZ
                for (int b = 0; b <= 1; b++)
                {
                    for (int a = _roomMobs[0]; a <= _roomMobs[4]; a++)
                    {
                        L2Npc minion = addSpawn(a, -87497, 217674, -7494, 25977, true, 0, false, world.instanceId);
                        synchronized (world.toKill)
                        {
                            world.toKill.add(minion);
                        }
                    }
                }
            }
            else if (event.equalsIgnoreCase("stage_6_start"))
            {
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(17240110).openMe();
            }
            else if (event.equalsIgnoreCase("stage_last_open_door"))
            {
                if (world.status == 5 || _debug)
                {
                    world.status++;

                    InstanceManager.getInstance().getInstance(world.instanceId).getDoor(17240111).openMe();

                    world.owner.showQuestMovie(12);
                    world.owner.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.IN_ORDER_HELP_ANAKIM_ACTIVATE_DEVICES));

                    startQuestTimer("stage_last_spawns", ScenePlayerDataTable.getInstance().getVideoDuration(12), npc,
                            null);
                }
            }
            else if (event.equalsIgnoreCase("stage_last_spawns"))
            {
                world.bossAnakim = addSpawn(_anakim, -83179, 216479, -7504, 0, true, 0, false, world.instanceId);
                world.bossAnakim.setIsInvul(true);

                for (int b = 0; b <= 1; b++)
                {
                    for (int a : _anakimMinions)
                    {
                        L2Npc minion = addSpawn(a, -83179, 216479, -7504, 0, true, 0, false, world.instanceId);
                        minion.setIsInvul(true);
                    }
                }
                world.bossLilith = addSpawn(_lilith, -83175, 217021, -7504, 0, true, 0, false, world.instanceId);
                world.bossLilith.setIsInvul(true);

                for (int b = 0; b <= 1; b++)
                {
                    for (int a : _lilithMinions)
                    {
                        L2Npc minion = addSpawn(a, -83175, 217021, -7504, 0, true, 0, false, world.instanceId);
                        minion.setIsInvul(true);
                    }
                }

                //Aggro both bosses
                ((L2Attackable) world.bossLilith).addDamageHate(world.bossAnakim, 99999, 99999);
                world.bossLilith.setTarget(world.bossAnakim);

                ((L2Attackable) world.bossAnakim).addDamageHate(world.bossLilith, 99999, 99999);
                world.bossAnakim.setTarget(world.bossLilith);

                //Make them invul
                for (L2Npc monster : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
                {
                    if (monster == null)
                    {
                        continue;
                    }

                    if (monster.getNpcId() == _sealDevice)
                    {
                        monster.setIsMortal(false); //device can't die
                        monster.setIsImmobilized(true);
                        monster.getStatus().stopHpMpRegeneration();
                    }
                }
                //Chats
                startQuestTimer("stage_last_bosses_talks", 1, npc, null);
            }
            else if (event.equalsIgnoreCase("stage_last_bosses_talks"))
            {
                if (world.status == 6)
                {
                    //Lilith
                    world.bossLilith.broadcastPacket(
                            new NpcSay(world.bossLilith.getObjectId(), 1, world.bossLilith.getTemplate().TemplateId,
                                    _lilitChats[Rnd.get(_lilitChats.length)]));

                    int chatAnakim = _anakimChats[Rnd.get(_anakimChats.length)];
                    //The are some chats of anakim that should have the char name inside, bah
                    world.bossAnakim.broadcastPacket(
                            new NpcSay(world.bossAnakim.getObjectId(), chatAnakim > 19612 ? 2 : 1,
                                    world.bossAnakim.getTemplate().TemplateId, chatAnakim));

                    //Chats
                    startQuestTimer("stage_last_bosses_talks", 15000, npc, null);
                }
            }
        }

        if (event.equalsIgnoreCase("enterToInstance"))
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

        return "";
    }

    @Override
    public final String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        switch (npc.getNpcId())
        {
            case _npcManagerId:
                return _qn + ".html";

            case _countMagican:
                if (player.getInventory().getItemByItemId(_courtWizardMagicStaff) == null)
                {
                    return "32598-1.html";
                }

                return "32598-3.html";

            case _disciplesGK:
                startQuestTimer("stage_last_open_door", 1, npc, null);
                break;
        }
        return super.onTalk(npc, player);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof SealOfTheEmperorWorld)
        {
            SealOfTheEmperorWorld world = (SealOfTheEmperorWorld) tmpworld;
            switch (world.status)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    synchronized (world.toKill)
                    {
                        if (world.toKill.contains(npc))
                        {
                            world.toKill.remove(npc);
                        }
                    }

                    if (world.toKill.isEmpty())
                    {
                        world.status++;
                        notifyEvent("stage_" + (world.status + 1) + "_start", npc, null);
                    }

                    switch (npc.getNpcId())
                    {
                        case _lilimMagnus:
                            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 1, npc.getTemplate().TemplateId,
                                    1000247)); //Lord Shilen... some day... you will accomplish... this mission...
                            break;

                        case _lilimKnight:
                            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 1, npc.getTemplate().TemplateId,
                                    1000270)); //Why are you getting in our way?
                            break;
                    }
                    break;
            }
        }
        return "";
    }

    private final synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof SealOfTheEmperorWorld))
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
                    player.teleToLocation(-89559, 216030, -7488, true);

                    L2NpcBufferInstance.giveBasicBuffs(player);
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
            world = new SealOfTheEmperorWorld();
            world.instanceId = instanceId;
            world.templateId = _instanceTemplateId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            world.allowed.add(player.getObjectId());

            player.stopAllEffectsExceptThoseThatLastThroughDeath();

            //Remove Items
            player.deleteAllItemsById(_sacredSword);
            player.deleteAllItemsById(_courtWizardMagicStaff);
            player.deleteAllItemsById(_elmoredenHolyWater);

            //Give Items
            player.addItem(_qn, _sacredSword, 1, null, true);
            player.addItem(_qn, _elmoredenHolyWater, 1, null, true);

            //Messages
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BY_USING_EINHASAD_SWORD));
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BY_USING_HOLY_WATER_EINHASAD));
            player.setInstanceId(instanceId);
            player.teleToLocation(-89559, 216030, -7488, true);

            L2NpcBufferInstance.giveBasicBuffs(player);

            startQuestTimer("stage_1_start", 4000, null, player);

            Log.fine(getName() + ": instance started: " + instanceId + " created by player: " + player.getName());
            return;
        }
    }

    public static void main(String[] args)
    {
        new SealOfTheEmperor(-1, _qn, "instances/DimensionalDoor");
    }
}
