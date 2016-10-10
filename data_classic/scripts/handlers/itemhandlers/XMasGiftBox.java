package handlers.itemhandlers;

import l2server.Config;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;

public class XMasGiftBox implements IItemHandler
{
    private final int[] SANTA_HATS = {7836, 20095};
    private final int AGA_RUDOLPH = 10606;
    //private final int	ANC_ADENA	= 5575;
    //private final int	QH_POTION	= 1540;
    private final int K_EPAULETTE = 9912;
    private final int COIN_O_LUCK = 4037;

	/*private final int[]	A_GRD_RECS	=
	{
			5370,	// Tallum Boots
			5428,	// Tallum Helmet
			5368,	// Dark Crystal Boots
			5426,	// Dark Crystal Helmet
			5432,	// Majestic Circlet
			5430	// Helm of Nightmare
	};*/

    @Override
    public void useItem(L2Playable playable, L2ItemInstance giftBox, boolean forceUse)
    {
        if (!(playable instanceof L2PcInstance))
        {
            return;
        }

        L2PcInstance player = (L2PcInstance) playable;

        boolean rewarded = false;

        player.destroyItemByItemId("openGift", giftBox.getItemId(), 1, giftBox, true);

        if (Config.isServer(Config.TENKAI))    // Ceriel reward
        {
            player.addItem("xmas", SANTA_HATS[Rnd.get(2)], 1, giftBox, true);    // Always give santa hat

            if (Rnd.get(100) < 75)    // 75% for Rudolph Agathion
            {
                rewarded = true;
                player.addItem("xmas", AGA_RUDOLPH, 1, giftBox, true);
            }
			
			/*	Deactivated because there was a mistake in it which caused that no one got QHP and it would
			 *	be unfair to let people get QHP who take the gift later, so we let that for next year
			if (Rnd.get(100) < 50)	// 50% for 200-500 QHP
			{
				rewarded = true;
				player.addItem("xmas", QH_POTION, Rnd.get(200, 500), giftBox, true);
			}
			*/

            if (Rnd.get(100) < 50)    // 50% for 5-20k KE
            {
                rewarded = true;
                player.addItem("xmas", K_EPAULETTE, Rnd.get(5000, 20000), giftBox, true);
            }

            if (!rewarded)    // Those guys who had bad luck for all the other rewards, get a coin at least
            {
                player.addItem("xmas", COIN_O_LUCK, 1, giftBox, true);
            }
        }
    }
}
