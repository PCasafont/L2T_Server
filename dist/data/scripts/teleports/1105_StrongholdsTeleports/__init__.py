# Made by Kerberos
# this script is part of the Official L2J Datapack Project.
# Visit http://www.l2jdp.com/forum/ for more details.
from l2server.gameserver.model.quest.jython import QuestJython as JQuest

qn = "1105_StrongholdsTeleports"

class Quest(JQuest):
    def __init__(self, id, name, descr):
        JQuest.__init__(self, id, name, descr)

    def onFirstTalk(self, npc, player):
        htmltext = None
        st = player.getQuestState(qn)
        if not st:
            st = self.newQuestState(player)
        if st.getPlayer().getLevel() < 20:
            htmltext = str(npc.getNpcId()) + ".htm"
        else:
            htmltext = str(npc.getNpcId()) + "-no.htm"
        if htmltext == None:
            npc.showChatWindow(player)
        return htmltext

QUEST = Quest(-1, qn, "Teleports")

for i in [32181, 32184, 32186]:  # 32163 removed because of fusion GK system
    QUEST.addFirstTalkId(i)
